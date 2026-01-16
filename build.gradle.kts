import groovy.util.Node
import groovy.util.NodeList

plugins {
    java
    id("org.springframework.boot") version "3.3.13"
    id("io.spring.dependency-management") version "1.1.6"
    `maven-publish`
}

group = "com.github.codexwr"
version = "2.1.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
    withSourcesJar()
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.springframework.boot:spring-boot-starter-webflux")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.springframework.boot:spring-boot-autoconfigure-processor")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")

    implementation("com.jayway.jsonpath:json-path:2.9.0")
    testImplementation("com.jayway.jsonpath:json-path-assert:2.9.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
    archiveClassifier.set("")

    manifest.attributes.also {
        it["Implementation-Title"] = project.name
        it["Implementation-Version"] = project.version
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name.lowercase()
            version = project.version.toString()

            from(components["java"])

            pom {
                withXml {
                    val nodes = asNode().get("dependencyManagement") as NodeList
                    nodes.forEach { asNode().remove(it as Node) }
                }

                name = project.name
                description = "A library for logging client requests in Spring Boot 3.x.x Web Servlet."
                url = "https://github.com/codexwr/spring-boot-request-logging"
                licenses {
                    license {
                        name = "MIT License"
                        url = "https://github.com/codexwr/spring-boot-request-logging/blob/main/LICENSE"
                    }
                }
                developers {
                    developer {
                        id = "codexwr"
                        name = "codexwr"
                    }
                }
            }
        }
    }
}