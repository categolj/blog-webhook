package am.ik.blog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;

import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BlogWebhookApplicationTests {
	@Autowired
	WebTestClient webClient;
	@Autowired
	MessageCollector messageCollector;
	@Autowired
	Source source;

	@Test
	@SuppressWarnings("unchecked")
	public void addedRequest() throws Exception {
		Fixtures.WebHook webhook = Fixtures.added();
		webClient.post().uri("/").body(fromObject(webhook.payload()))
				.headers(webhook.headers()).exchange().expectStatus().isOk()
				.expectBody(String.class)
				.isEqualTo("{\"added\":1,\"removed\":0,\"modified\":0}");
		Message<?> poll = messageCollector.forChannel(source.output()).poll(3,
				TimeUnit.SECONDS);
		assertThat(poll.getPayload()).isEqualTo("{\"paths\":[\"content/00412.md\"]}");
		assertThat(poll.getHeaders().get("type")).isEqualTo("added");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void removedRequest() throws Exception {
		Fixtures.WebHook webhook = Fixtures.removed();
		webClient.post().uri("/").body(fromObject(webhook.payload()))
				.headers(webhook.headers()).exchange().expectStatus().isOk()
				.expectBody(String.class)
				.isEqualTo("{\"added\":0,\"removed\":1,\"modified\":0}");
		Message<?> poll = messageCollector.forChannel(source.output()).poll(3,
				TimeUnit.SECONDS);
		assertThat(poll.getPayload()).isEqualTo("{\"paths\":[\"content/00012.md\"]}");
		assertThat(poll.getHeaders().get("type")).isEqualTo("removed");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void modifiedRequest() throws Exception {
		Fixtures.WebHook webhook = Fixtures.modified();
		webClient.post().uri("/").body(fromObject(webhook.payload()))
				.headers(webhook.headers()).exchange().expectStatus().isOk()
				.expectBody(String.class)
				.isEqualTo("{\"added\":0,\"removed\":0,\"modified\":1}");
		Message<?> poll = messageCollector.forChannel(source.output()).poll(3,
				TimeUnit.SECONDS);
		assertThat(poll.getPayload()).isEqualTo("{\"paths\":[\"content/00379.md\"]}");
		assertThat(poll.getHeaders().get("type")).isEqualTo("modified");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void formatChanged() throws Exception {
		Fixtures.WebHook webhook = Fixtures.added();
		webClient.post().uri("/").contentType(MediaType.APPLICATION_JSON_UTF8)
				.body(fromObject(Collections.emptyMap())).headers(webhook.headers())
				.exchange().expectStatus().isBadRequest();
		BlockingQueue<Message<?>> queue = messageCollector.forChannel(source.output());
		assertThat(queue.isEmpty()).isTrue();
	}

}
