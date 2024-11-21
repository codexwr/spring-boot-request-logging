package com.github.codexwr.springbootrequestlogging.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DefaultResponseBodyMaskTest {
    public static class ResponseBodyMaskSteps {
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

        public static final ResponseBodyMask defaultResponseBodyMask = new DefaultResponseBodyMask(maskOverlay, jsonMasks);
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
        var maskBody = ResponseBodyMaskSteps.defaultResponseBodyMask.getMaskBody(method, path, MediaType.APPLICATION_JSON, jsonBody);

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
        var maskBody = ResponseBodyMaskSteps.defaultResponseBodyMask.getMaskBody(method, path, MediaType.APPLICATION_JSON, jsonBody);

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
        var maskBody = ResponseBodyMaskSteps.defaultResponseBodyMask.getMaskBody(method, path, MediaType.APPLICATION_JSON, jsonBody);

        // then
        var doc = JsonPath.parse(maskBody);
        assertNotEquals(doc.<String>read("$.user.email"), DefaultRequestBodyMaskTest.RequestBodyMaskSteps.maskOverlay);
        assertEquals(doc.<String>read("$.user.profile.name"), DefaultRequestBodyMaskTest.RequestBodyMaskSteps.maskOverlay);
        assertEquals(doc.<String>read("$.user.profile.password"), DefaultRequestBodyMaskTest.RequestBodyMaskSteps.maskOverlay);
        assertEquals(doc.<String>read("$.user.profile.private.name"), DefaultRequestBodyMaskTest.RequestBodyMaskSteps.maskOverlay);
        assertThrowsExactly(PathNotFoundException.class, () -> doc.<String>read("$.user.profile.private.name.password"));
    }

    @Test
    @DisplayName("ContentType 미지원")
    void contentTypeException() throws JsonProcessingException {
        // given
        var method = HttpMethod.POST;
        var path = "/auth/sign-up/email";
        var formData = Map.of("email", List.of("email"), "password", List.of("password"));
        var formString = objectMapper.writeValueAsString(formData);

        // when
        Executable maskBody = () -> ResponseBodyMaskSteps.defaultResponseBodyMask.getMaskBody(method, path, MediaType.APPLICATION_FORM_URLENCODED, formString);

        // then
        assertThrows(IllegalStateException.class, maskBody);
    }
}