package com.exam.studentapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
 
import com.exam.studentapi.entity.Student; 

public interface StudentRepository extends JpaRepository<Student, String> {

}