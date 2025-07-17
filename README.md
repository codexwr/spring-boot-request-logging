[![Release](https://jitpack.io/v/codexwr/spring-boot-request-logging.svg)](https://jitpack.io/#codexwr/spring-boot-request-logging)

# Request Logging Library

This is a library for logging client requests of Restful APIs in Spring Boot 3.x.x Web Servlet/Reactive(Webflux).

## Installation

Add the Jitpack to your build.gradle.kts:

```kotlin
repositories {
    // Jitpack
    maven {
        url = uri("https://jitpack.io")
    }
}
```

Add the dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.codexwr:spring-boot-request-logging:2.0.2")
}
```

## Properties

You can adjust settings in `application.yml`.

```yaml
codexwr:
  springboot:
    request-logging:
      enabled: true
      filter-order: -101
      exclude-logging-paths:
        - method: post
          path-patterns:
            - /api/url/1
            - /api/url/2
        - method: get
          path-patterns:
            - /api-docs/**
            - /swagger/*
      include-query-string: true
      include-client-info: true
      include-headers: true
      path-header-mask:
        - method: post
          path-pattern: /api/member
          mask-key:
            - X-USER-KEY
            - Postman-Token
        - method: get
          path-pattern: /api/member/*
          mask-key:
            - Postman-Token
      default-header-masks:
        - Authorization
      include-request-body: true
      request-json-body-masks:
        - method: post
          path-pattern: /api/auth/**
          mask-json:
            - $.password
            - $.name.*
        - method: get
            - $.access-token
      include-response-body: true
      response-json-body-masks:
        - method: post
          path-pattern: /api/auth/**
          mask-json:
            - $.password
            - $.name.*
        - method: get
            - $.access-token
      mask-string: '{{MASKED}}'
      enter-prefix-decor: '[+] '
      exit-prefix-decor: '[-] '


```

- Refer to [JsonPath](https://github.com/json-path/JsonPath) for `request-json-body-masks.mask-json` and
  `response-json-body-masks.mask-json` pattern.

## Log Sample

```yaml
codexwr:
  springboot:
    request-logging:
      enabled: true
      exclude-logging-paths:
        - method: get
          path-patterns:
            - /test/member
      include-query-string: true
      include-client-info: true
      include-headers: true
      path-header-mask:
        - method: post
          path-pattern: /test/member
          mask-key:
            - x-mask-item
      default-header-masks:
        - x-default-item
      include-request-body: true
      request-json-body-masks:
        - method: post
          path-pattern: /test/member
          mask-json:
            - $.age
      request-form-data-masks:
        - method: post
          path-pattern: /test/member/*/**
          mask-key:
            - email
            - nick
      include-response-body: true
      response-json-body-masks:
        - method: post
          path-pattern: /**
          mask-json:
            - code
```

```http request
MockHttpServletRequest:
        HTTP Method = POST
        Request URI = /test/member
        Parameters = {}
        Headers = [Content-Type:"application/json;charset=UTF-8", x-mask-item:"맴버 생성에서 마스크 됨", X-DEFAULT-ITEM:"기본 마스크 됨", Content-Length:"25"]
        Body = {"name":"Alice","age":20}
        Session Attrs = {}

MockHttpServletResponse:
           Status = 200
    Error message = null
          Headers = [Content-Type:"application/json"]
     Content type = application/json
             Body = {"code":200,"message":"OK"}
    Forwarded URL = null
   Redirected URL = null
          Cookies = []
```

> 2024-10-25T11:20:55.596+09:00  INFO 7262 --- [    Test worker] LogPrinter                               : [+] POST /test/member, ip=127.0.0.1, headers=[Content-Type:"application/json;charset=UTF-8", x-mask-item:"{{MASKED}}", X-DEFAULT-ITEM:"기본 마스크 됨", Content-Length:"25"]  
> 2024-10-25T11:20:55.658+09:00  INFO 7262 --- [    Test worker] LogPrinter                               : [-] <200 OK:50ms> POST /test/member, requestBody={"name":"Alice","age":"{{MASKED}}"}, responseBody={"code":"{{MASKED}}","message":"OK"}
