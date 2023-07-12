package univ.ulsan.capstone;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CapstoneApplication {

    public static void main(String[] args) {
//        // .env 파일 로드
//        Dotenv dotenv = Dotenv.configure().load();
//
//        // 비밀 변수 사용
//        String prodDbUrl = dotenv.get("PROD_DB");
//        String rdsUsername = dotenv.get("RDS_USER_NAME");
//        String rdsPassword = dotenv.get("RDS_PW");
//        String jwtToken = dotenv.get("JWT_TOKEN");
//
//        // application.yml 파일에 적용
//        System.setProperty("spring.datasource.url", prodDbUrl);
//        System.setProperty("spring.datasource.username", rdsUsername);
//        System.setProperty("spring.datasource.password", rdsPassword);
//        System.setProperty("jwt.secret", jwtToken);

        SpringApplication.run(CapstoneApplication.class, args);
    }

}
