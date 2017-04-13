package am.ik.blog;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@SpringBootApplication
@EnableBinding(Source.class)
public class BlogWebhookApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlogWebhookApplication.class, args);
	}

	@Bean
	RouterFunction<ServerResponse> route(GitHubWebHook webHook) {
		return RouterFunctions.route(POST("/"), webHook::hook);
	}
}
