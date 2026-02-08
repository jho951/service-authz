package com.example.enroll.controller;

import com.example.enroll.api.ApiResponse;
import com.example.enroll.api.BusinessException;
import com.example.enroll.api.ErrorCode;
import com.example.enroll.domain.course.Course;
import com.example.enroll.domain.course.CourseSchedule;
import com.example.enroll.repository.CourseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CourseController {
    private final CourseRepository courseRepository;

    public CourseController(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @GetMapping("/courses")
    public ApiResponse<List<CourseResponse>> list(@RequestParam(required = false) Long departmentId,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Course> result = departmentId == null
                ? courseRepository.findAll(pageable)
                : courseRepository.findByDepartmentId(departmentId, pageable);
        List<CourseResponse> data = result.map(CourseResponse::from).toList();
        return ApiResponse.of(data);
    }

    @GetMapping("/courses/{courseId}")
    public ApiResponse<CourseResponse> get(@PathVariable Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REG_NOT_FOUND));
        return ApiResponse.of(CourseResponse.from(course));
    }

    public record CourseResponse(Long id, String name, Long departmentId, String departmentName,
                                 Long professorId, int credits, int capacity, int enrolled,
                                 List<String> schedule) {
        public static CourseResponse from(Course course) {
            return new CourseResponse(
                    course.getId(),
                    course.getName(),
                    course.getDepartmentId(),
                    course.getDepartmentName(),
                    course.getProfessorId(),
                    course.getCredits(),
                    course.getCapacity(),
                    course.getEnrolled(),
                    course.getSchedules().stream().map(CourseController::formatSchedule).toList()
            );
        }
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
