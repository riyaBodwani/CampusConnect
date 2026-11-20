package com.riyabodwani.campusconnect.controller;

import com.riyabodwani.campusconnect.dto.LoginRequestDto;
import com.riyabodwani.campusconnect.dto.LoginResponseDto;
import com.riyabodwani.campusconnect.dto.StudentRequestDto;
import com.riyabodwani.campusconnect.dto.StudentResponseDto;
import com.riyabodwani.campusconnect.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public StudentResponseDto registerStudent(@Valid @RequestBody StudentRequestDto studentRequestDto) {
        return studentService.saveStudent(studentRequestDto);
    }

    @PostMapping("/login")
    public LoginResponseDto login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        return studentService.login(loginRequestDto);
    }

    @GetMapping
    public List<StudentResponseDto> getAllStudents() {
        return studentService.getAllStudents();
    }

    @GetMapping("/{id}")
    public StudentResponseDto getStudentById(@PathVariable Long id) {
        return studentService.getStudentById(id);
    }

    @PutMapping("/{id}")
    public StudentResponseDto updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentRequestDto studentRequestDto) {
        return studentService.updateStudent(id, studentRequestDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
    }
}
