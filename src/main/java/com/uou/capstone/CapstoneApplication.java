package com.uou.capstone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CapstoneApplication {
    // EC2(Ubuntu 20.04) 도커 패키지 설치 후 재배포
    public static void main(String[] args) {
        SpringApplication.run(CapstoneApplication.class, args);
    }

}
