package am.ik.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;

@SpringBootApplication
@EnableBinding(Source.class)
public class BlogWebhookApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlogWebhookApplication.class, args);
	}
}
