package com.example.enroll.service.registration;

import com.example.enroll.domain.course.Course;
import com.example.enroll.domain.student.Student;
import com.example.enroll.repository.CourseRepository;
import com.example.enroll.repository.RegistrationRepository;
import com.example.enroll.repository.StudentRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest
class RegistrationConcurrencyTest {

	@Autowired private RegistrationService registrationService;
	@Autowired private StudentRepository studentRepository;
	@Autowired private CourseRepository courseRepository;
	@Autowired private RegistrationRepository registrationRepository;

	@Test
	void capacityOne_allowsOnlyOneSuccess_underConcurrentRequests() throws Exception {
		registrationRepository.deleteAll();
		courseRepository.deleteAll();
		studentRepository.deleteAll();

		// 1) 학생 100명 생성
		List<Student> students = new ArrayList<>();
		for (int i = 1; i <= 100; i++) {
			students.add(new Student("Student-T" + i, 1L, "Department-1"));
		}
		students = studentRepository.saveAll(students);

		// 2) capacity=1 강좌 생성 (save 1번만!)
		Course course = new Course("Course-1", 1L, "Department-1", 1L, 3, 1);
		course = courseRepository.save(course);

		int threadCount = 100;

		ExecutorService executor = Executors.newFixedThreadPool(20);
		CountDownLatch startGate = new CountDownLatch(1);
		CountDownLatch doneGate = new CountDownLatch(threadCount);
		AtomicInteger success = new AtomicInteger();
		AtomicInteger failed = new AtomicInteger();

		Long courseId = course.getId(); // 저장된 ID

		for (int i = 0; i < threadCount; i++) {
			final Long studentId = students.get(i).getId();
			executor.submit(() -> {
				try {
					startGate.await();
					registrationService.register(studentId, courseId);
					success.incrementAndGet();
				} catch (Exception ex) {
					failed.incrementAndGet();
				} finally {
					doneGate.countDown();
				}
			});
		}

		startGate.countDown();
		boolean finished = doneGate.await(30, TimeUnit.SECONDS);
		executor.shutdownNow();

		assertThat(finished).isTrue(); // 타임아웃 방지(안 끝났으면 실패)

		Course reloaded = courseRepository.findById(courseId).orElseThrow();
		assertThat(success.get()).isEqualTo(1);
		assertThat(failed.get()).isEqualTo(99);
		assertThat(reloaded.getEnrolled()).isEqualTo(1);
	}
}
