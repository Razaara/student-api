package com.exam.studentapi.service;

import java.util.List;

import com.exam.studentapi.entity.Student;

public interface StudentService {
    Student save(Student student);
    List<Student> getAll();
    Student getById(String id);
    Student update(String id, Student student);
    void delete(String id);

}
