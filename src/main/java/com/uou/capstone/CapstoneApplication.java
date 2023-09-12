package com.uou.capstone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CapstoneApplication {
    // 인스턴스 ssh 포트 Anywhere 허용
    public static void main(String[] args) {
        SpringApplication.run(CapstoneApplication.class, args);
    }

}
