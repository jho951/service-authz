package com.example.enroll.service.seeder;

import com.example.enroll.domain.course.Course;
import com.example.enroll.domain.course.CourseSchedule;
import com.example.enroll.domain.professor.Professor;
import com.example.enroll.domain.student.Student;
import com.example.enroll.repository.CourseRepository;
import com.example.enroll.repository.ProfessorRepository;
import com.example.enroll.repository.StudentRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class DataSeeder implements ApplicationRunner {
    private static final int DEPARTMENT_COUNT = 12;
    private static final int PROFESSOR_COUNT = 100;
    private static final int STUDENT_COUNT = 10000;
    private static final int COURSE_COUNT = 500;
    private final ProfessorRepository professorRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    private volatile SeedingStatus status = SeedingStatus.SEEDING;

    public DataSeeder(ProfessorRepository professorRepository,
                      StudentRepository studentRepository,
                      CourseRepository courseRepository) {
        this.professorRepository = professorRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (professorRepository.count() > 0
                || studentRepository.count() > 0
                || courseRepository.count() > 0) {
            status = SeedingStatus.DONE;
            return;
        }
        List<DepartmentSeed> departments = buildDepartments();

        List<Professor> professors = new ArrayList<>(PROFESSOR_COUNT);
        for (int i = 1; i <= PROFESSOR_COUNT; i++) {
            DepartmentSeed dept = departments.get((i - 1) % departments.size());
            professors.add(new Professor("Prof-" + i, dept.id(), dept.name()));
        }
        professorRepository.saveAll(professors);

        List<Student> students = new ArrayList<>(STUDENT_COUNT);
        for (int i = 1; i <= STUDENT_COUNT; i++) {
            DepartmentSeed dept = departments.get((i - 1) % departments.size());
            students.add(new Student("Student-" + i, dept.id(), dept.name()));
        }
        studentRepository.saveAll(students);

        List<Course> courses = new ArrayList<>(COURSE_COUNT);
        for (int i = 1; i <= COURSE_COUNT; i++) {
            DepartmentSeed dept = departments.get((i - 1) % departments.size());
            long professorId = professors.get((i - 1) % professors.size()).getId();
            int credits = 1 + (i % 3);
            int capacity = 20 + (i % 41);
            Course course = new Course("Course-" + i, dept.id(), dept.name(), professorId, credits, capacity);
            course.getSchedules().addAll(randomSchedules());
            courses.add(course);
        }
        courseRepository.saveAll(courses);

        status = SeedingStatus.DONE;
    }

    public SeedingStatus getStatus() {
        return status;
    }

    public int getProfessorCount() {
        return (int) professorRepository.count();
    }

    public int getStudentCount() {
        return (int) studentRepository.count();
    }

    public int getCourseCount() {
        return (int) courseRepository.count();
    }

    private List<CourseSchedule> randomSchedules() {
        List<CourseSchedule> schedules = new ArrayList<>();
        List<ScheduleSlot> slotPool = buildSchedulePool();
        int slotCount = ThreadLocalRandom.current().nextInt(1, 3);
        for (int i = 0; i < slotCount; i++) {
            ScheduleSlot slot = slotPool.get(ThreadLocalRandom.current().nextInt(slotPool.size()));
            schedules.add(new CourseSchedule(slot.day(), slot.start().toString(), slot.end().toString()));
        }
        return schedules;
    }

    private List<DepartmentSeed> buildDepartments() {
        List<DepartmentSeed> departments = new ArrayList<>(DEPARTMENT_COUNT);
        for (int i = 1; i <= DEPARTMENT_COUNT; i++) {
            departments.add(new DepartmentSeed((long) i, "Department-" + i));
        }
        return departments;
    }

    private List<ScheduleSlot> buildSchedulePool() {
        List<ScheduleSlot> pool = new ArrayList<>();
        String[] days = {"MON", "TUE", "WED", "THU", "FRI"};
        LocalTime[] starts = {
                LocalTime.of(9, 0),
                LocalTime.of(10, 30),
                LocalTime.of(12, 0),
                LocalTime.of(13, 30),
                LocalTime.of(15, 0),
                LocalTime.of(16, 30)
        };
        for (String day : days) {
            for (LocalTime start : starts) {
                pool.add(new ScheduleSlot(day, start, start.plusMinutes(90)));
            }
        }
        return pool;
    }

    private record DepartmentSeed(Long id, String name) {
    }

    private record ScheduleSlot(String day, LocalTime start, LocalTime end) {
    }
}
