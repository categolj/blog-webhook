package am.ik.blog;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.fasterxml.jackson.databind.JsonNode;

import reactor.core.publisher.Mono;

@Component
public class GitHubWebHook {
	private static final Logger log = LoggerFactory.getLogger(GitHubWebHook.class);

	private final Source source;

	public GitHubWebHook(Source source) {
		this.source = source;
	}

	Mono<ServerResponse> hook(ServerRequest request) {
		return request.bodyToMono(JsonNode.class).flatMap(x -> {
			JsonNode commits = x.get("commits").get(0);
			Mono<Map<String, Integer>> added = sendEvent(commits, "added");
			Mono<Map<String, Integer>> removed = sendEvent(commits, "removed");
			Mono<Map<String, Integer>> modified = sendEvent(commits, "modified");
			Mono<Map<String, Integer>> body = Mono.when(added, removed, modified)
					.map(t -> {
						Map<String, Integer> result = new LinkedHashMap<>();
						result.putAll(t.getT1());
						result.putAll(t.getT2());
						result.putAll(t.getT3());
						return result;
					});
			return ServerResponse.ok().body(fromPublisher(body, ResolvableType
					.forClassWithGenerics(Map.class, String.class, Integer.class)));
		}).switchOnError(e -> {
			log.error("Error!", e);
			return ServerResponse.status(HttpStatus.BAD_REQUEST)
					.body(Mono.justOrEmpty(e.getMessage()), String.class);
		});
	}

	private Mono<Map<String, Integer>> sendEvent(JsonNode commits, String type) {
		List<String> paths = stream(commits.get(type).spliterator(), false)
				.map(JsonNode::asText).collect(toList());
		if (paths.isEmpty()) {
			return Mono.just(Collections.singletonMap(type, 0));
		}
		else {
			Map<String, Object> payload = Collections.singletonMap("paths", paths);
			log.info("Send {} event = {}", type, payload);
			Message<?> message = MessageBuilder.withPayload(payload)
					.setHeader("type", type).build();
			source.output().send(message);
			return Mono.just(Collections.singletonMap(type, paths.size()));
		}
	}
}
