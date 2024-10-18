package com.github.codexwr.springbootrequestlogging.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.codexwr.springbootrequestlogging.component.RequestBodyMask;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DefaultRequestBodyMaskTest {
    public static class RequestBodyMaskSteps {
        public static final String maskOverlay = "{{***}}";

        public static final Set<String> jsonPattern = Set.of(
                "$.path.not.found.exception",
                "$..password",
                "$.email",
                "$.user..name"
        );
        public static final List<LoggingFilterProperties.PathJsonMask> jsonMasks = List.of(
                new LoggingFilterProperties.PathJsonMask(HttpMethod.POST.name(), "/auth/sign-up/**", jsonPattern)
        );

        public static final Set<String> keyMask = Set.of("password", "email");
        public static final List<LoggingFilterProperties.PathKeyMask> keyMasks = List.of(
                new LoggingFilterProperties.PathKeyMask(HttpMethod.POST.name(), "/auth/login/**", keyMask)
        );

        public static final RequestBodyMask defaultRequestBodyMask = new DefaultRequestBodyMask(maskOverlay, jsonMasks, keyMasks);
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("JSON 경로 불일치")
    void jsonNoPath() {
        // given
        var method = HttpMethod.POST;
        var path = "/auth/sign-in/email";
        var jsonBody = """
                {
                "password": "password",
                "email": "email",
                }
                """;

        // when
        var maskBody = RequestBodyMaskSteps.defaultRequestBodyMask.getMaskBody(method, path, MediaType.APPLICATION_JSON, jsonBody);

        // then
        assertEquals(jsonBody, maskBody);
    }

    @Test
    @DisplayName("JSON 패턴 불일치")
    void jsonNoPatternMatch() throws JsonProcessingException {
        // given
        var method = HttpMethod.POST;
        var path = "/auth/sign-up/email";
        var jsonBody = """
                {
                    "no-password": "password",
                    "no-email": "email"
                }
                """;
        // json string minify
        jsonBody = objectMapper.readValue(jsonBody, JsonNode.class).toString();

        // when
        var maskBody = RequestBodyMaskSteps.defaultRequestBodyMask.getMaskBody(method, path, MediaType.APPLICATION_JSON, jsonBody);

        // then
        assertEquals(jsonBody, maskBody);
    }

    @Test
    @DisplayName("JSON 패턴 일치")
    void jsonPatternMatch() {
        // given
        var method = HttpMethod.POST;
        var path = "/auth/sign-up/email";
        var jsonBody = """
                {
                    "user": {
                        "email": "email",
                        "profile": {
                            "name": "Hong",
                            "password": "password",
                            "private": {
                                "name": {
                                    "firstName": "John",
                                    "lastName": "Doe",
                                    "password": "password"
                                }
                            }
                        }
                    }
                }
                """;

        // when
        var maskBody = RequestBodyMaskSteps.defaultRequestBodyMask.getMaskBody(method, path, MediaType.APPLICATION_JSON, jsonBody);

        // then
        var doc = JsonPath.parse(maskBody);
        assertNotEquals(doc.<String>read("$.user.email"), RequestBodyMaskSteps.maskOverlay);
        assertEquals(doc.<String>read("$.user.profile.name"), RequestBodyMaskSteps.maskOverlay);
        assertEquals(doc.<String>read("$.user.profile.password"), RequestBodyMaskSteps.maskOverlay);
        assertEquals(doc.<String>read("$.user.profile.private.name"), RequestBodyMaskSteps.maskOverlay);
        assertThrowsExactly(PathNotFoundException.class, () -> doc.<String>read("$.user.profile.private.name.password"));
    }

    @Test
    @DisplayName("FORM 경로 불일치")
    void formNoPath() throws JsonProcessingException {
        // given
        var method = HttpMethod.GET;
        var path = "/auth/login/email";
        var formData = Map.of("email", List.of("email"), "password", List.of("password"));
        var formString = objectMapper.writeValueAsString(formData);

        // when
        var maskBody = RequestBodyMaskSteps.defaultRequestBodyMask.getMaskBody(method, path, MediaType.APPLICATION_FORM_URLENCODED, formString);

        // then
        var maskFormData = objectMapper.readValue(maskBody, new TypeReference<Map<String, List<String>>>() {
        });
        assertThat(maskFormData).isEqualTo(formData);
    }

    @Test
    @DisplayName("FORM 패턴 불일치")
    void formNoPatternMatch() throws JsonProcessingException {
        // given
        var method = HttpMethod.POST;
        var path = "/auth/login/email";
        var formData = Map.of("no-email", List.of("email"), "no-password", List.of("password"), "key", List.of("key"));
        var formString = objectMapper.writeValueAsString(formData);

        // when
        var maskBody = RequestBodyMaskSteps.defaultRequestBodyMask.getMaskBody(method, path, MediaType.APPLICATION_FORM_URLENCODED, formString);

        // then
        var maskFormData = objectMapper.readValue(maskBody, new TypeReference<Map<String, List<String>>>() {
        });
        assertThat(maskFormData).isEqualTo(formData);
    }

    @Test
    @DisplayName("FORM 패턴 일치")
    void formPatternMatch() throws JsonProcessingException {
        // given
        var method = HttpMethod.POST;
        var path = "/auth/login/email";
        var formData = Map.of("email", List.of("email"), "password", List.of("password"), "key", List.of("key"));
        var formString = objectMapper.writeValueAsString(formData);

        // when
        var maskBody = RequestBodyMaskSteps.defaultRequestBodyMask.getMaskBody(method, path, MediaType.APPLICATION_FORM_URLENCODED, formString);

        // then
        var maskFormData = objectMapper.readValue(maskBody, new TypeReference<Map<String, Object>>() {
        });
        assertThat(maskFormData).isNotEqualTo(formData)
                .hasEntrySatisfying("email", v -> assertThat(v).isEqualTo(RequestBodyMaskSteps.maskOverlay))
                .hasEntrySatisfying("password", v -> assertThat(v).isEqualTo(RequestBodyMaskSteps.maskOverlay))
                .hasEntrySatisfying("key", v -> assertThat(v).isEqualTo(formData.get("key")));
    }
}