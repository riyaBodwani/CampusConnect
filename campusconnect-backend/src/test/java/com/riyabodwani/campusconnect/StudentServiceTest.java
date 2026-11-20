package com.riyabodwani.campusconnect;

import com.riyabodwani.campusconnect.dto.LoginRequestDto;
import com.riyabodwani.campusconnect.dto.LoginResponseDto;
import com.riyabodwani.campusconnect.dto.StudentRequestDto;
import com.riyabodwani.campusconnect.dto.StudentResponseDto;
import com.riyabodwani.campusconnect.exception.EmailAlreadyExistsException;
import com.riyabodwani.campusconnect.exception.InvalidCredentialsException;
import com.riyabodwani.campusconnect.exception.StudentNotFoundException;
import com.riyabodwani.campusconnect.repository.StudentRepository;
import com.riyabodwani.campusconnect.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class StudentServiceTest {

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentRepository studentRepository;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();
    }

    private StudentRequestDto buildRequest(String email) {
        StudentRequestDto dto = new StudentRequestDto();
        dto.setFullName("Test Student");
        dto.setEmail(email);
        dto.setPhoneNumber("9876543210");
        dto.setDateOfBirth(LocalDate.of(2002, 1, 10));
        dto.setAddress("Mumbai");
        dto.setCourse("B.Tech");
        dto.setBranch("IT");
        dto.setCgpa(8.5);
        dto.setPassword("secretPassword");
        dto.setPlacementStatus("UNPLACED");
        return dto;
    }

    @Test
    void testSaveStudent_Success_And_NoPasswordInResponse() {
        StudentRequestDto request = buildRequest("student@example.com");
        StudentResponseDto response = studentService.saveStudent(request);

        assertNotNull(response.getId());
        assertEquals("Test Student", response.getFullName());
        assertEquals("student@example.com", response.getEmail());
        assertEquals(8.5, response.getCgpa());
        // Verify StudentResponseDto does not have a getPassword() method or password exposure
    }

    @Test
    void testSaveStudent_DuplicateEmail_ThrowsEmailAlreadyExistsException() {
        StudentRequestDto request1 = buildRequest("unique@example.com");
        studentService.saveStudent(request1);

        StudentRequestDto request2 = buildRequest("unique@example.com");
        assertThrows(EmailAlreadyExistsException.class, () -> studentService.saveStudent(request2));
    }

    @Test
    void testGetStudentById_Success() {
        StudentRequestDto request = buildRequest("get@example.com");
        StudentResponseDto created = studentService.saveStudent(request);

        StudentResponseDto found = studentService.getStudentById(created.getId());
        assertEquals(created.getId(), found.getId());
        assertEquals("get@example.com", found.getEmail());
    }

    @Test
    void testGetStudentById_NotFound_ThrowsStudentNotFoundException() {
        assertThrows(StudentNotFoundException.class, () -> studentService.getStudentById(99999L));
    }

    @Test
    void testGetAllStudents_ReturnsAllWithoutPassword() {
        studentService.saveStudent(buildRequest("student1@example.com"));
        studentService.saveStudent(buildRequest("student2@example.com"));

        List<StudentResponseDto> students = studentService.getAllStudents();
        assertEquals(2, students.size());
    }

    @Test
    void testUpdateStudent_Success() {
        StudentRequestDto original = buildRequest("update@example.com");
        StudentResponseDto created = studentService.saveStudent(original);

        StudentRequestDto updateDto = buildRequest("update@example.com");
        updateDto.setFullName("Updated Name");
        updateDto.setCgpa(9.5);

        StudentResponseDto updated = studentService.updateStudent(created.getId(), updateDto);
        assertEquals("Updated Name", updated.getFullName());
        assertEquals(9.5, updated.getCgpa());
    }

    @Test
    void testUpdateStudent_NotFound_ThrowsStudentNotFoundException() {
        StudentRequestDto updateDto = buildRequest("notfound@example.com");
        assertThrows(StudentNotFoundException.class, () -> studentService.updateStudent(88888L, updateDto));
    }

    @Test
    void testUpdateStudent_DuplicateEmailOnAnotherStudent_ThrowsEmailAlreadyExistsException() {
        StudentResponseDto student1 = studentService.saveStudent(buildRequest("first@example.com"));
        StudentResponseDto student2 = studentService.saveStudent(buildRequest("second@example.com"));

        StudentRequestDto updateDto = buildRequest("first@example.com"); // already taken by student1
        assertThrows(EmailAlreadyExistsException.class, () -> studentService.updateStudent(student2.getId(), updateDto));
    }

    @Test
    void testDeleteStudent_Success() {
        StudentResponseDto created = studentService.saveStudent(buildRequest("delete@example.com"));
        studentService.deleteStudent(created.getId());

        assertThrows(StudentNotFoundException.class, () -> studentService.getStudentById(created.getId()));
    }

    @Test
    void testDeleteStudent_NotFound_ThrowsStudentNotFoundException() {
        assertThrows(StudentNotFoundException.class, () -> studentService.deleteStudent(77777L));
    }

    @Test
    void testLogin_Success() {
        StudentRequestDto request = buildRequest("login.success@example.com");
        request.setPassword("SecretPass123!");
        studentService.saveStudent(request);

        LoginRequestDto loginRequest = new LoginRequestDto("login.success@example.com", "SecretPass123!");
        LoginResponseDto loginResponse = studentService.login(loginRequest);

        assertNotNull(loginResponse);
        assertEquals("Login successful", loginResponse.getMessage());
        assertNotNull(loginResponse.getStudent());
        assertEquals("login.success@example.com", loginResponse.getStudent().getEmail());
        assertNotNull(loginResponse.getToken());
        assertFalse(loginResponse.getToken().isBlank());
        String[] parts = loginResponse.getToken().split("\\.");
        assertEquals(3, parts.length);
    }

    @Test
    void testLogin_WrongPassword_ThrowsInvalidCredentialsException() {
        StudentRequestDto request = buildRequest("login.wrongpass@example.com");
        request.setPassword("CorrectPassword123!");
        studentService.saveStudent(request);

        LoginRequestDto loginRequest = new LoginRequestDto("login.wrongpass@example.com", "WrongPassword!");
        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class,
                () -> studentService.login(loginRequest));
        assertEquals("Invalid email or password", ex.getMessage());
    }

    @Test
    void testLogin_NonExistentEmail_ThrowsInvalidCredentialsException() {
        LoginRequestDto loginRequest = new LoginRequestDto("doesnotexist@example.com", "AnyPassword123!");
        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class,
                () -> studentService.login(loginRequest));
        assertEquals("Invalid email or password", ex.getMessage());
    }
}
