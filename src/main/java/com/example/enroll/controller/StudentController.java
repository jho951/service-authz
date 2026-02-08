package com.example.enroll.controller;

import com.example.enroll.api.ApiResponse;
import com.example.enroll.api.BusinessException;
import com.example.enroll.api.ErrorCode;
import com.example.enroll.domain.course.Course;
import com.example.enroll.domain.course.CourseSchedule;
import com.example.enroll.domain.professor.Professor;
import com.example.enroll.domain.student.Student;
import com.example.enroll.domain.registration.Registration;
import com.example.enroll.repository.CourseRepository;
import com.example.enroll.repository.ProfessorRepository;
import com.example.enroll.repository.RegistrationRepository;
import com.example.enroll.repository.StudentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class StudentController {
    private final StudentRepository studentRepository;
    private final RegistrationRepository registrationRepository;
    private final CourseRepository courseRepository;
    private final ProfessorRepository professorRepository;

    public StudentController(StudentRepository studentRepository,
                             RegistrationRepository registrationRepository,
                             CourseRepository courseRepository,
                             ProfessorRepository professorRepository) {
        this.studentRepository = studentRepository;
        this.registrationRepository = registrationRepository;
        this.courseRepository = courseRepository;
        this.professorRepository = professorRepository;
    }

    @GetMapping("/students")
    public ApiResponse<List<StudentResponse>> list(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<StudentResponse> data = studentRepository.findAll(pageable)
                .map(StudentResponse::from)
                .toList();
        return ApiResponse.of(data);
    }

    @GetMapping("/students/{studentId}")
    public ApiResponse<StudentResponse> get(@PathVariable Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REG_NOT_FOUND));
        return ApiResponse.of(StudentResponse.from(student));
    }

    @GetMapping("/students/{studentId}/schedule")
    public ApiResponse<List<ScheduleResponse>> schedule(@PathVariable Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REG_NOT_FOUND));
        List<Registration> registrations = registrationRepository.findByStudentId(studentId);
        List<ScheduleResponse> data = registrations.stream()
                .filter(reg -> "ENROLLED".equals(reg.getStatus()))
                .map(reg -> toSchedule(student, reg))
                .toList();
        return ApiResponse.of(data);
    }

    public record StudentResponse(Long id, String name, Long departmentId, String departmentName) {
        public static StudentResponse from(Student student) {
            return new StudentResponse(
                    student.getId(),
                    student.getName(),
                    student.getDepartmentId(),
                    student.getDepartmentName()
            );
        }
    }

    public record ScheduleResponse(Long courseId, String courseName, int credits,
                                   String departmentName, String professorName, List<String> schedule) {
    }

    private ScheduleResponse toSchedule(Student student, Registration registration) {
        Course course = courseRepository.findById(registration.getCourseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.REG_NOT_FOUND));
        Professor professor = professorRepository.findById(course.getProfessorId())
                .orElseThrow(() -> new BusinessException(ErrorCode.REG_NOT_FOUND));
        return new ScheduleResponse(
                course.getId(),
                course.getName(),
                course.getCredits(),
                student.getDepartmentName(),
                professor.getName(),
                course.getSchedules().stream().map(StudentController::formatSchedule).toList()
        );
    }

    private static String formatSchedule(CourseSchedule schedule) {
        return dayToKorean(schedule.getDayOfWeek()) + " " + schedule.getStartTime() + "-" + schedule.getEndTime();
    }

    private static String dayToKorean(String day) {
        return switch (day) {
            case "MON" -> "월";
            case "TUE" -> "화";
            case "WED" -> "수";
            case "THU" -> "목";
            case "FRI" -> "금";
            case "SAT" -> "토";
            case "SUN" -> "일";
            default -> day;
        };
    }
}
