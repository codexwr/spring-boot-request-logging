package com.github.codexwr.springbootrequestlogging.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("servlet")
public class ServletLoggingTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    private final HttpHeaders headers = new HttpHeaders() {{
        put("x-mask-item", List.of("맴버 생성에서 마스크 됨"));
        put("X-DEFAULT-ITEM", List.of("기본 마스크 됨"));
    }};


    @Test
    @DisplayName("create member")
    public void testCreateMember() throws Exception {
        // given
        var member = new WebTestApplication.Member("Alice", 20);

        // when
        var req = post("/test/member")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(member));
        mvc.perform(req)
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("get all member")
    public void testGetAllMember() throws Exception {
        // when
        var req = get("/test/member")
                .headers(headers);
        mvc.perform(req)
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("get member by id")
    public void testGetMemberById() throws Exception {
        // given
        var memberId = 1;

        // when
        var req = get("/test/member/{id}", memberId)
                .queryParam("email", "test@example.org")
                .headers(headers);
        mvc.perform(req)
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("create member avatar")
    public void testCreateMemberAvatar() throws Exception {
        // given
        var memberId = 1;
        var fileResource = new ClassPathResource("static/unsplash.jpg");
        var multipartFile = new MockMultipartFile("avatar", "unsplash.jpg", "image/jpeg", fileResource.getInputStream());


        var filePart = new MockPart("avatar", "unsplash.jpg", fileResource.getContentAsByteArray(), MediaType.IMAGE_JPEG);
        var colorPart = new MockPart("color", "red".getBytes());
        var nickPart = new MockPart("nick", "engine".getBytes());


        // when
        var req = multipart("/test/member/{id}/avatar", memberId)
                .part(filePart, colorPart, nickPart)
//                .file(avatarFile)
//                .param("color", "red")
//                .param("nick", "engine")
                .headers(headers);


        mvc.perform(req)
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }
}
