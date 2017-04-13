package am.ik.blog;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Fixtures {
	private static final ObjectMapper objectMapper = new ObjectMapper();

	public static WebHook added() {
		try {
			return new WebHook(objectMapper.readValue(
					new ClassPathResource("added-payload.json").getInputStream(),
					JsonNode.class));
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static WebHook modified() {
		try {
			return new WebHook(objectMapper.readValue(
					new ClassPathResource("modified-payload.json").getInputStream(),
					JsonNode.class));
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static WebHook removed() {
		try {
			return new WebHook(objectMapper.readValue(
					new ClassPathResource("removed-payload.json").getInputStream(),
					JsonNode.class));
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static class WebHook {
		private final JsonNode payload;
		private final HttpHeaders headers;

		public WebHook(JsonNode payload) {
			this.payload = payload;
			this.headers = new HttpHeaders();
			this.headers.add("User-Agent", "GitHub-Hookshot/2ce10f4");
			this.headers.add("X-GitHub-Delivery", "3e6ed480-1294-11e7-9720-3f07597c331f");
			this.headers.add("X-GitHub-Event", "push");
		}

		public JsonNode payload() {
			return payload;
		}

		public HttpHeaders headers() {
			return headers;
		}
	}
}
