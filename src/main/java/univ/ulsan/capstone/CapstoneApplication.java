package univ.ulsan.capstone;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;

@SpringBootApplication
public class CapstoneApplication {

    public static void main(String[] args) {
        // 프로파일에 따른 환경 변수 파일 이름 설정
        String profile = "dev"; // 로컬 개발 환경
        if (Arrays.asList(args).contains("--prod")) {
            profile = "prod"; // 배포 환경
        }

        // 환경 변수 파일 이름 설정
        String configName = "application-" + profile;

        // 프로파일에 따른 환경 변수 파일 적용
        System.setProperty("spring.config.name", configName);

        // .env 파일 로드 (로컬일때)
        if (profile.equals("dev")) {
            Dotenv dotenv = Dotenv.configure().load();
            // .env 파일에서 필요한 환경 변수 가져오기
            String prodDbUrl = dotenv.get("PROD_DB");
            String rdsUsername = dotenv.get("RDS_USER_NAME");
            String rdsPassword = dotenv.get("RDS_PW");
            String jwtToken = dotenv.get("JWT_TOKEN");
            // 환경 변수 적용
            System.setProperty("spring.datasource.url", prodDbUrl);
            System.setProperty("spring.datasource.username", rdsUsername);
            System.setProperty("spring.datasource.password", rdsPassword);
            System.setProperty("jwt.secret", jwtToken);
        }

        SpringApplication.run(CapstoneApplication.class, args);
    }

}
