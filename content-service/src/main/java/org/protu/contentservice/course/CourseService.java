package org.protu.contentservice.course;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.protu.contentservice.common.exception.custom.EntityNotFoundException;
import org.protu.contentservice.common.properties.AppProperties;
import org.protu.contentservice.lesson.LessonService;
import org.protu.contentservice.lesson.dto.LessonWithoutContent;
import org.protu.contentservice.lesson.dto.LessonsWithCompletion;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class CourseService {

  private static final String CACHE_ALL_COURSES_LIST = "all-courses-list";
  private static final String CACHE_COURSE_DETAILS = "course-details";
  private static final String CACHE_COURSE_LESSONS = "course-lessons";
  private static final String CACHE_COURSE_LESSONS_WITH_COMPLETION = "course-lessons-with-completion";
  private final CourseRepository courses;
  private final LessonService lessonService;
  private final AppProperties props;

  public CourseService(CourseRepository courses, LessonService lessonService, AppProperties props) {
    this.courses = courses;
    this.lessonService = lessonService;
    this.props = props;
  }

  @Transactional
  @CacheEvict(value = CACHE_ALL_COURSES_LIST, allEntries = true)
  public void createCourse(CourseRequest courseRequest) {
    courses.add(courseRequest);
  }

  @Transactional(readOnly = true)
  @Cacheable(value = CACHE_COURSE_DETAILS, key = "#courseName", unless = "#result == null")
  public CourseWithLessons getCourseWithLessonsByName(String courseName) {
    return courses.findByName(courseName).orElseThrow(() -> new EntityNotFoundException("Course", courseName));
  }

  @Transactional(readOnly = true)
  @Cacheable(value = CACHE_COURSE_DETAILS, key = "#courseName", unless = "#result == null")
  public CourseDto getCourseByNameOrThrow(String courseName) {
    return courses.findByNameOrThrow(courseName);
  }

  @Transactional
  @Caching(evict = {
      @CacheEvict(value = CACHE_ALL_COURSES_LIST, allEntries = true),
      @CacheEvict(value = CACHE_COURSE_DETAILS, key = "#courseName"),
      @CacheEvict(value = CACHE_COURSE_LESSONS, key = "#courseName"),
  })
  public void updateCourse(String courseName, CourseRequest courseRequest) {
    courses.update(courseName, courseRequest);
  }

  @Transactional(readOnly = true)
  @Cacheable(value = CACHE_ALL_COURSES_LIST, unless = "#result == null || #result.isEmpty()")
  public List<CourseWithLessons> getAllCourses() {
    return courses.findAll();
  }

  @SuppressWarnings("rawtypes")
  private String uploadToCloudinary(MultipartFile file) {
    try {
      File file1 = File.createTempFile("coursePic-", file.getOriginalFilename());
      file.transferTo(file1);
      Map uploadMap = ObjectUtils.asMap("asset_folder", "courses-pic");
      Map uploadResults = new Cloudinary(ObjectUtils.asMap(
          "cloud_name", props.cloudinary().cloudName(),
          "api_key", props.cloudinary().apiKey(),
          "api_secret", props.cloudinary().apiSecret()
      )).uploader().upload(file1, uploadMap);

      return uploadResults.get("secure_url").toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Transactional
  @Caching(evict = {
      @CacheEvict(value = CACHE_ALL_COURSES_LIST, allEntries = true),
      @CacheEvict(value = CACHE_COURSE_DETAILS, key = "#courseName"),
      @CacheEvict(value = CACHE_COURSE_LESSONS, key = "#courseName"),
  })
  private void saveCoursePic(String courseName, String picUrl) {
    courses.updateCoursePicture(courseName, picUrl);
  }

  public void uploadCoursePic(String courseName, MultipartFile file) {
    courses.findByNameOrThrow(courseName);
    String secureAssetUrl = uploadToCloudinary(file);
    saveCoursePic(courseName, secureAssetUrl);
  }

  @Transactional(readOnly = true)
  @Cacheable(value = CACHE_COURSE_LESSONS, key = "#courseName", unless = "#result == null || #result.isEmpty()")
  public List<LessonWithoutContent> getAllLessonsForCourse(String courseName) {
    CourseDto course = courses.findByNameOrThrow(courseName);
    return courses.findLessonsByCourseId(course.id());
  }

  @Transactional
  @Caching(evict = {
      @CacheEvict(value = CACHE_ALL_COURSES_LIST, allEntries = true),
      @CacheEvict(value = CACHE_COURSE_DETAILS, key = "#courseName"),
      @CacheEvict(value = CACHE_COURSE_LESSONS, key = "#courseName"),
  })
  public void addExistingLessonToCourse(String courseName, String lessonName) {
    CourseDto course = courses.findByNameOrThrow(courseName);
    LessonWithoutContent lesson = lessonService.findByNameWithoutContent(lessonName);
    courses.addLessonToCourse(course.id(), lesson.id());
  }

  @Transactional
  @Caching(evict = {
      @CacheEvict(value = CACHE_ALL_COURSES_LIST, allEntries = true),
      @CacheEvict(value = CACHE_COURSE_DETAILS, key = "#courseName"),
      @CacheEvict(value = CACHE_COURSE_LESSONS, key = "#courseName"),
  })
  public void deleteLessonFromCourse(String courseName, String lessonName) {
    CourseDto course = courses.findByNameOrThrow(courseName);
    LessonWithoutContent lesson = lessonService.findByNameWithoutContent(lessonName);
    courses.deleteLessonFromCourse(course.id(), lesson.id());
  }

  @Transactional
  @Caching(evict = {
      @CacheEvict(value = CACHE_ALL_COURSES_LIST, allEntries = true),
      @CacheEvict(value = CACHE_COURSE_DETAILS, key = "#courseName"),
      @CacheEvict(value = CACHE_COURSE_LESSONS, key = "#courseName"),
  })
  public void deleteCourse(String courseName) {
    courses.delete(courseName);
  }

  @Transactional(readOnly = true)
  @Cacheable(value = CACHE_COURSE_LESSONS_WITH_COMPLETION, key = "{#userId, #courseName}", unless = "#result == null || #result.isEmpty()")
  public List<LessonsWithCompletion> getAllLessonsWithCompletionStatusForCourse(Long userId, String courseName) {
    CourseDto course = courses.findByNameOrThrow(courseName);
    return courses.findLessonsWithCompletionStatus(userId, course.id());
  }
}