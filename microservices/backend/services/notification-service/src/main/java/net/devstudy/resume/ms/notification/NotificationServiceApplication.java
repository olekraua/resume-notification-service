package net.devstudy.resume.ms.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "net.devstudy.resume")
@ComponentScan(basePackages = {
        "net.devstudy.resume.notification",
        "net.devstudy.resume.shared",
        "net.devstudy.resume.ms.notification"
})
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
