package com.uou.capstone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CapstoneApplication {
    // 로컬 docker-compose 실행 테스트 후 재배포
    public static void main(String[] args) {
        SpringApplication.run(CapstoneApplication.class, args);
    }

}
