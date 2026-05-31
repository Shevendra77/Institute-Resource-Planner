package com.example.irp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InstituteResourcePlannerApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(InstituteResourcePlannerApplication.class, args);
        System.out.println("Hello Spring Boot Application Started Successfully!");
    }
}