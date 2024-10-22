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
    implementation("com.github.codexwr:spring-boot-request-logging:2.0.0")
}
```
- In a Spring Reactive project, if you encounter the following error:
  ```text
  java.lang.IllegalStateException: Error processing condition on com.github.codexwr.springbootrequestlogging.configuration.LoggingFilterAutoConfiguration
  ...
  ...
  Caused by: java.lang.ClassNotFoundException: jakarta.servlet.Filter
  ```

  Please add the following library to your dependencies:
  ```kotlin
  dependencies {
    compileOnly ("jakarta.servlet:jakarta.servlet-api:6.1.0")
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
      mask-string: '{{***}}'
      enter-prefix-decor: '[+] '
      exit-prefix-decor: '[-] '
      

```
- Refer to [JsonPath](https://github.com/json-path/JsonPath) for `request-json-body-masks.mask-json` and `response-json-body-masks.mask-json` pattern.

## Log Sample
```yaml
include-headers: true
default-header-masks: Authorization, postman-token
include-request-body: true
request-json-body-masks:
  - method: post
    path-pattern: /sample
    mask-json:
      - $..password
include-response-body: true
response-json-body-masks:
  - method: post
    path-pattern: /sample
    mask-json:
      - $.userInfo.password
```

```http request
POST /sample?itemName=sample&itemValue=20 HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJteSI6Im5hbWUifQ.FJguyRm0oWUX8Vheem-CVsnZyW58kGJEHX41GXFwFEs
Content-Length: 73

{
    "username": "sample-user-name",
    "password": "sample-password"
}

===[Response]===
{
    "post": "post sample",
    "userInfo": {
        "username": "sample-user-name",
        "password": "sample-password"
    }
}
```

> `2024-04-06T16:05:46.726+09:00  INFO 36822 --- [nio-8080-exec-1] c.g.c.s.RequestLoggingFilter             : [+] POST /sample?itemName=sample&itemValue=20, client=0:0:0:0:0:0:0:1, headers=[authorization:"{{***}}", user-agent:"PostmanRuntime/7.37.0", accept:"*/*", postman-token:"{{***}}", host:"localhost:8080", accept-encoding:"gzip, deflate, br", connection:"keep-alive", content-length:"73", Content-Type:"application/json;charset=UTF-8"]`
> 
> `2024-04-06T16:05:47.213+09:00  INFO 36822 --- [nio-8080-exec-1] c.g.c.s.RequestLoggingFilter             : [-] <200 OK:452ms> POST /sample?itemName=sample&itemValue=20, [requestPayload]={"username":"sample-user-name","password":"{{***}}"}, [responsePayload]={"post":"post sample","userInfo":{"username":"sample-user-name","password":"{{***}}"}}`
 

