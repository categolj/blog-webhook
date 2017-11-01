package am.ik.blog;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BlogWebhookApplicationTests {
	@Autowired
	TestRestTemplate restTemplate;
	@Autowired
	MessageCollector messageCollector;
	@Autowired
	Source source;
	@Autowired
	ObjectMapper objectMapper;

	@Test
	@SuppressWarnings("unchecked")
	public void addedRequest() throws Exception {
		Fixtures.WebHook webhook = Fixtures.added();
		RequestEntity<JsonNode> requestEntity = new RequestEntity<>(webhook.payload(),
				webhook.headers(), HttpMethod.POST, URI.create("/webhook"));
		ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity,
				String.class);
		assertThat(responseEntity.getBody())
				.isEqualTo("{\"added\":1,\"removed\":0,\"modified\":0}");
		Message<?> poll = messageCollector.forChannel(source.output()).poll(3,
				TimeUnit.SECONDS);
		assertThat(this.decodePayload(poll)).isEqualTo(
				"{\"paths\":[\"content/00412.md\"],\"repository\":\"making/blog.ik.am\"}");
		assertThat(poll.getHeaders().get("type")).isEqualTo("added");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void removedRequest() throws Exception {
		Fixtures.WebHook webhook = Fixtures.removed();
		RequestEntity<JsonNode> requestEntity = new RequestEntity<>(webhook.payload(),
				webhook.headers(), HttpMethod.POST, URI.create("/webhook"));
		ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity,
				String.class);
		assertThat(responseEntity.getBody())
				.isEqualTo("{\"added\":0,\"removed\":1,\"modified\":0}");
		Message<?> poll = messageCollector.forChannel(source.output()).poll(3,
				TimeUnit.SECONDS);
		assertThat(this.decodePayload(poll)).isEqualTo(
				"{\"paths\":[\"content/00012.md\"],\"repository\":\"making/blog.ik.am\"}");
		assertThat(poll.getHeaders().get("type")).isEqualTo("removed");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void modifiedRequest() throws Exception {
		Fixtures.WebHook webhook = Fixtures.modified();
		RequestEntity<JsonNode> requestEntity = new RequestEntity<>(webhook.payload(),
				webhook.headers(), HttpMethod.POST, URI.create("/webhook"));
		ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity,
				String.class);
		assertThat(responseEntity.getBody())
				.isEqualTo("{\"added\":0,\"removed\":0,\"modified\":1}");
		Message<?> poll = messageCollector.forChannel(source.output()).poll(3,
				TimeUnit.SECONDS);
		assertThat(this.decodePayload(poll)).isEqualTo(
				"{\"paths\":[\"content/00379.md\"],\"repository\":\"making/blog.ik.am\"}");
		assertThat(poll.getHeaders().get("type")).isEqualTo("modified");
	}

//	@Test
//	@SuppressWarnings("unchecked")
//	public void formatChanged() throws Exception {
//		Fixtures.WebHook webhook = Fixtures.added();
//		RequestEntity<?> requestEntity = new RequestEntity<>(Collections.emptyMap(),
//				webhook.headers(), HttpMethod.POST, URI.create("/"));
//		ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity,
//				String.class);
//		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
//		BlockingQueue<Message<?>> queue = messageCollector.forChannel(source.output());
//		assertThat(queue.isEmpty()).isTrue();
//	}

	String decodePayload(Message<?> message) {
		return (String) message.getPayload();
	}

}
