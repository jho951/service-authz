package com.example.enroll.service.registration;

import com.example.enroll.api.BusinessException;
import com.example.enroll.api.ErrorCode;
import com.example.enroll.domain.course.Course;
import com.example.enroll.domain.course.CourseSchedule;
import com.example.enroll.domain.registration.Registration;
import com.example.enroll.domain.student.Student;
import com.example.enroll.repository.CourseRepository;
import com.example.enroll.repository.RegistrationRepository;
import com.example.enroll.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class RegistrationService {
    private static final String STATUS_ENROLLED = "ENROLLED";
    private static final String STATUS_CANCELED = "CANCELED";
    private static final int MAX_CREDITS = 18;

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final RegistrationRepository registrationRepository;
    private final CourseLockManager courseLockManager;

    public RegistrationService(StudentRepository studentRepository,
                               CourseRepository courseRepository,
                               RegistrationRepository registrationRepository,
                               CourseLockManager courseLockManager) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.registrationRepository = registrationRepository;
        this.courseLockManager = courseLockManager;
    }

    @Transactional
    public Registration register(Long studentId, Long courseId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REG_NOT_FOUND));
        courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REG_NOT_FOUND));

        ReentrantLock lock = courseLockManager.getLock(courseId);
        lock.lock();
        try {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.REG_NOT_FOUND));

            if (course.getEnrolled() >= course.getCapacity()) {
                throw new BusinessException(ErrorCode.REG_CAPACITY_FULL);
            }

            Optional<Registration> existing = registrationRepository.findByStudentIdAndCourseId(studentId, courseId);
            if (existing.isPresent() && STATUS_ENROLLED.equals(existing.get().getStatus())) {
                throw new BusinessException(ErrorCode.REG_DUPLICATE_COURSE);
            }

            List<Registration> enrolledRegistrations = getEnrolledRegistrations(studentId);
            int currentCredits = sumCredits(enrolledRegistrations);
            if (currentCredits + course.getCredits() > MAX_CREDITS) {
                throw new BusinessException(ErrorCode.REG_MAX_CREDITS);
            }

            if (hasTimeConflict(enrolledRegistrations, course)) {
                throw new BusinessException(ErrorCode.REG_TIME_CONFLICT);
            }

            Registration registration = new Registration(student.getId(), course.getId(), STATUS_ENROLLED);
            Registration saved = registrationRepository.save(registration);
            course.increaseEnrolled();
            courseRepository.save(course);
            return saved;
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public Registration cancel(Long registrationId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REG_NOT_FOUND));
        if (STATUS_CANCELED.equals(registration.getStatus())) {
            throw new BusinessException(ErrorCode.REG_ALREADY_CANCELED);
        }

        Long courseId = registration.getCourseId();
        ReentrantLock lock = courseLockManager.getLock(courseId);
        lock.lock();
        try {
            registration.cancel();
            Registration saved = registrationRepository.save(registration);

            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.REG_NOT_FOUND));
            course.decreaseEnrolled();
            courseRepository.save(course);
            return saved;
        } finally {
            lock.unlock();
        }
    }

    private List<Registration> getEnrolledRegistrations(Long studentId) {
        List<Registration> registrations = registrationRepository.findByStudentId(studentId);
        List<Registration> enrolled = new ArrayList<>();
        for (Registration registration : registrations) {
            if (STATUS_ENROLLED.equals(registration.getStatus())) {
                enrolled.add(registration);
            }
        }
        return enrolled;
    }

    private int sumCredits(List<Registration> registrations) {
        int total = 0;
        for (Registration registration : registrations) {
            Course course = courseRepository.findById(registration.getCourseId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.REG_NOT_FOUND));
            total += course.getCredits();
        }
        return total;
    }

    private boolean hasTimeConflict(List<Registration> registrations, Course newCourse) {
        List<ScheduleSlot> newSlots = toSlots(newCourse.getSchedules());
        for (Registration registration : registrations) {
            Course course = courseRepository.findById(registration.getCourseId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.REG_NOT_FOUND));
            List<ScheduleSlot> existingSlots = toSlots(course.getSchedules());
            if (conflict(newSlots, existingSlots)) {
                return true;
            }
        }
        return false;
    }

    private boolean conflict(List<ScheduleSlot> a, List<ScheduleSlot> b) {
        for (ScheduleSlot s1 : a) {
            for (ScheduleSlot s2 : b) {
                if (s1.day == s2.day && s1.start < s2.end && s2.start < s1.end) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<ScheduleSlot> toSlots(List<CourseSchedule> schedules) {
        List<ScheduleSlot> slots = new ArrayList<>();
        for (CourseSchedule schedule : schedules) {
            int day = parseDay(schedule.getDayOfWeek());
            int start = parseTime(schedule.getStartTime());
            int end = parseTime(schedule.getEndTime());
            slots.add(new ScheduleSlot(day, start, end));
        }
        return slots;
    }

    private int parseDay(String dayText) {
        Map<String, Integer> map = new HashMap<>();
        map.put("월", 1);
        map.put("화", 2);
        map.put("수", 3);
        map.put("목", 4);
        map.put("금", 5);
        map.put("토", 6);
        map.put("일", 7);
        map.put("MON", 1);
        map.put("TUE", 2);
        map.put("WED", 3);
        map.put("THU", 4);
        map.put("FRI", 5);
        map.put("SAT", 6);
        map.put("SUN", 7);
        return map.getOrDefault(dayText, 0);
    }

    private int parseTime(String timeText) {
        String[] parts = timeText.split(\":\");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        return hour * 60 + minute;
    }

    private static class ScheduleSlot {
        private final int day;
        private final int start;
        private final int end;

        private ScheduleSlot(int day, int start, int end) {
            this.day = day;
            this.start = start;
            this.end = end;
        }
    }
}
