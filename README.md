# Request Logging Library
This is a library for logging client requests of Restful APIs in Spring Boot 3.x.x Web Servlet.

## Installation
Add the GitHub repository or Jitpack to your build.gradle.kts:
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
    implementation("com.github.codexwr:spring-boot-request-logging:1.0.0")
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
      exclude-url-patterns: /set/the/paths/you/want/to/exclude, /exclude/**, /error
      enter-prefix: '[+] '
      exit-prefix: '[-] '
      include-query-string: true
      include-client-info: true
      include-headers: false
      masking-headers: Authorization, postman-token
      include-request-payload: false
      max-request-payload-size: 64
      masking-request-payload-pattern:
        - $.set_the_jsonpath_you_want_to_mask
        - $.name
        - $..password
      include-response-payload: false
      max-response-payload-size: 64
      masking-response-payload-pattern:
        - $.set_the_jsonpath_you_want_to_mask
        - $.*.birthday
      mask-string: '{{***}}'
```
- Refer to [JsonPath](https://github.com/json-path/JsonPath) for `masking-request-payload-pattern` and `masking-response-payload-pattern` configurations.

## Log Sample
```yaml
include-headers: true
masking-headers: Authorization, postman-token
include-request-payload: true
max-request-payload-size: 512
masking-request-payload-pattern: $..password
include-response-payload: true
max-response-payload-size: 512
masking-response-payload-pattern: $.userInfo.password
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
 

