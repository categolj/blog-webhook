package am.ik.blog;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

@RestController
public class GitHubWebHookController {
	private static final Logger log = LoggerFactory
			.getLogger(GitHubWebHookController.class);

	private final Source source;

	public GitHubWebHookController(Source source) {
		this.source = source;
	}

	@PostMapping(headers = "X-GitHub-Event=push")
	public Map<String, Integer> webhook(@RequestBody JsonNode x) {
		String repository = x.get("repository").get("full_name").asText();
		log.info("Received a webhook from {}", repository);
		JsonNode commits = x.get("commits").get(0);
		Map<String, Integer> added = sendEvent(repository, commits, "added");
		Map<String, Integer> removed = sendEvent(repository, commits, "removed");
		Map<String, Integer> modified = sendEvent(repository, commits, "modified");
		Map<String, Integer> body = new LinkedHashMap<>();
		body.putAll(added);
		body.putAll(removed);
		body.putAll(modified);
		return body;
	}

	private Map<String, Integer> sendEvent(String repository, JsonNode commits,
			String type) {
		List<String> paths = stream(commits.get(type).spliterator(), false)
				.map(JsonNode::asText).collect(toList());
		if (paths.isEmpty()) {
			return Collections.singletonMap(type, 0);
		}
		else {
			Map<String, Object> payload = new LinkedHashMap<>();
			payload.put("paths", paths);
			payload.put("repository", repository);
			log.info("Send {} event = {}", type, payload);
			Message<?> message = MessageBuilder.withPayload(payload)
					.setHeader("type", type).build();
			source.output().send(message);
			return Collections.singletonMap(type, paths.size());
		}
	}
}
