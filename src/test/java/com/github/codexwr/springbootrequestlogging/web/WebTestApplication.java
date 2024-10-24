package com.github.codexwr.springbootrequestlogging.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@SpringBootApplication
@RestController
@RequestMapping(value = "/test", produces = MediaType.APPLICATION_JSON_VALUE)
public class WebTestApplication {
    public record Member(String name, int age) {
    }

    public record ResponseDto(int code, String message) {
    }

    public record MultipartMemberAvatar(String color, String nick, MultipartFile avatar) {
    }

    public record MultipartMemberAvatarAsync(String color, String nick, FilePart avatar) {
    }

    private final Logger log = LoggerFactory.getLogger(WebTestApplication.class);

    @PostMapping("/member")
    public ResponseEntity<ResponseDto> createMember(@RequestBody Member member) {
        log.info("create member: {}", member);

        return ResponseEntity.ok(new ResponseDto(200, "OK"));
    }

    @GetMapping("/member")
    public ResponseEntity<ResponseDto> getMember() {
        log.info("get all member");
        return ResponseEntity.ok(new ResponseDto(200, "OK"));
    }

    @GetMapping("/member/{id}")
    public ResponseEntity<ResponseDto> getMember(@PathVariable int id, @RequestParam(required = false) String email) {
        log.info("get member: {} / email: {}", id, email);
        return ResponseEntity.ok(new ResponseDto(200, "OK"));
    }

    @PostMapping(value = "/member/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto> createAvatar(@PathVariable int id, MultipartMemberAvatar member) {
        log.info("create avatar: {}", member);

        return ResponseEntity.ok(new ResponseDto(200, "OK"));
    }

    @PostMapping(value = "/member/{id}/avatar/async", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto> createAvatarAsync(@PathVariable int id, MultipartMemberAvatarAsync member) {
        log.info("create avatar async: {}", member);

        return ResponseEntity.ok(new ResponseDto(200, "OK"));
    }
}
