package com.riyabodwani.campusconnect.service;

import com.riyabodwani.campusconnect.dto.LoginRequestDto;
import com.riyabodwani.campusconnect.dto.LoginResponseDto;
import com.riyabodwani.campusconnect.dto.StudentRequestDto;
import com.riyabodwani.campusconnect.dto.StudentResponseDto;
import com.riyabodwani.campusconnect.entity.Student;
import com.riyabodwani.campusconnect.exception.EmailAlreadyExistsException;
import com.riyabodwani.campusconnect.exception.InvalidCredentialsException;
import com.riyabodwani.campusconnect.exception.StudentNotFoundException;
import com.riyabodwani.campusconnect.repository.StudentRepository;
import com.riyabodwani.campusconnect.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public StudentResponseDto saveStudent(StudentRequestDto request) {
        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email '" + request.getEmail() + "' is already registered");
        }

        Student student = new Student();
        student.setFullName(request.getFullName());
        student.setDateOfBirth(request.getDateOfBirth());
        student.setEmail(request.getEmail());
        student.setPhoneNumber(request.getPhoneNumber());
        student.setAddress(request.getAddress());
        student.setCourse(request.getCourse());
        student.setBranch(request.getBranch());
        student.setCgpa(request.getCgpa());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            student.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        student.setPlacementStatus(request.getPlacementStatus());

        Student savedStudent = studentRepository.save(student);
        return mapToResponseDto(savedStudent);
    }

    public List<StudentResponseDto> getAllStudents() {
        return studentRepository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    public StudentResponseDto getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException("Student with id " + id + " not found"));
        return mapToResponseDto(student);
    }

    public StudentResponseDto updateStudent(Long id, StudentRequestDto request) {
        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException("Student with id " + id + " not found"));

        Optional<Student> existingByEmail = studentRepository.findByEmail(request.getEmail());
        if (existingByEmail.isPresent() && !existingByEmail.get().getId().equals(id)) {
            throw new EmailAlreadyExistsException("Email '" + request.getEmail() + "' is already registered with another student");
        }

        existingStudent.setFullName(request.getFullName());
        existingStudent.setDateOfBirth(request.getDateOfBirth());
        existingStudent.setEmail(request.getEmail());
        existingStudent.setPhoneNumber(request.getPhoneNumber());
        existingStudent.setAddress(request.getAddress());
        existingStudent.setCourse(request.getCourse());
        existingStudent.setBranch(request.getBranch());
        existingStudent.setCgpa(request.getCgpa());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            existingStudent.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        existingStudent.setPlacementStatus(request.getPlacementStatus());

        Student updatedStudent = studentRepository.save(existingStudent);
        return mapToResponseDto(updatedStudent);
    }

    public void deleteStudent(Long id) {
        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException("Student with id " + id + " not found"));

        studentRepository.delete(existingStudent);
    }

    public LoginResponseDto login(LoginRequestDto loginRequest) {
        Student student = studentRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), student.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(student.getEmail(), student.getId());
        return new LoginResponseDto("Login successful", token, mapToResponseDto(student));
    }

    private StudentResponseDto mapToResponseDto(Student student) {
        return new StudentResponseDto(
                student.getId(),
                student.getFullName(),
                student.getDateOfBirth(),
                student.getEmail(),
                student.getPhoneNumber(),
                student.getAddress(),
                student.getCourse(),
                student.getBranch(),
                student.getCgpa(),
                student.getPlacementStatus()
        );
    }
}
