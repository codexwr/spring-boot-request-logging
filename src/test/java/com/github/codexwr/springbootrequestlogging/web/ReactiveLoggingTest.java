package com.github.codexwr.springbootrequestlogging.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

import java.util.List;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("reactive")
public class ReactiveLoggingTest {
    @Autowired
    private WebTestClient webClient;

    private final HttpHeaders headers = new HttpHeaders() {{
        put("x-mask-item", List.of("맴버 생성에서 마스크 됨"));
        put("X-DEFAULT-ITEM", List.of("기본 마스크 됨"));
    }};

    private final ExchangeFilterFunction logRequest = ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
        System.out.println("Request: " + clientRequest.method() + " " + clientRequest.url());
        System.out.println("Headers: ");
        clientRequest.headers().forEach((name, values) -> values.forEach(value -> System.out.println("\t" + name + ": " + value)));
        System.out.println("Content-Type: " + clientRequest.headers().getContentType());
        System.out.println();
        return Mono.just(clientRequest);
    });

    private final ExchangeFilterFunction logResponse = ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
        System.out.println();
        System.out.println("Response: " + clientResponse.statusCode());
        clientResponse.headers().asHttpHeaders().forEach((name, values) -> values.forEach(value -> System.out.println(name + ": " + value)));
        System.out.println();
        return Mono.just(clientResponse);
    });

    @BeforeEach
    public void setup() {
        webClient = webClient.mutate()
                .filter(logRequest)
                .filter(logResponse)
                .build();
    }

    @Test
    @DisplayName("create member")
    public void testCreateMember() {
        // given
        var member = new WebTestApplication.Member("Alice", 20);

        // when
        webClient.post()
                .uri("/test/member")
                .headers(it -> it.addAll(headers))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(member)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("get all member")
    public void testGetAllMember() {
        // when
        webClient.get()
                .uri("/test/member")
                .headers(it -> it.addAll(headers))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("get member by id")
    public void testGetMemberById() {
        // given
        var memberId = 1;

        // when
        webClient.get()
                .uri(builder -> builder.path("/test/member/{id}")
                        .queryParam("email", "test@example.org")
                        .build(memberId)
                )
                .headers(it -> it.addAll(headers))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("create member avatar")
    public void testCreateMemberAvatar() {
        // given
        var memberId = 1;
        var fileResource = new ClassPathResource("static/unsplash.jpg");

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("avatar", fileResource, MediaType.IMAGE_JPEG).filename("unsplash.jpg");
        builder.part("color", "red");
        builder.part("color", "blue");
        builder.part("nick", "engine");
        builder.part("nick", "engine2");

        // when
        webClient.post()
                .uri("/test/member/{id}/avatar/async", memberId)
                .headers(it -> it.addAll(headers))
//                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("No URL PATH")
    public void noUrl() {
        // given
        var member = new WebTestApplication.Member("Alice", 20);

        // when
        webClient.post()
                .uri("/no-url-path")
                .headers(it -> it.addAll(headers))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(member)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    @DisplayName("Server RuntimeError")
    public void runtimeError() {
        // when
        webClient.get()
                .uri("/test/error")
                .headers(it -> it.addAll(headers))
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("Server RuntimeError(Async)")
    public void runtimeErrorAsync() {
        // when
        webClient.get()
                .uri("/test/error/async")
                .headers(it -> it.addAll(headers))
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
