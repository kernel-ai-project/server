package org.example.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaAuditing
@EntityScan(basePackages = "org.example.server.juwon.entity")  // ✅ 엔티티 스캔
@EnableJpaRepositories(basePackages = "org.example.server.juwon.repository") // ✅ 리포지토리 스캔
@ComponentScan(basePackages = "org.example.server.juwon") // ✅ 서비스, 컨트롤러 스캔
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

}
