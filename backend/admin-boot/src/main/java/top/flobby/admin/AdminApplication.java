package top.flobby.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Admin Management System 启动类
 */
@SpringBootApplication
public class AdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
        System.out.println("""

                ====================================
                Admin Management System Started!
                API Docs: http://localhost:8080/doc.html
                ====================================
                """);
    }
}
