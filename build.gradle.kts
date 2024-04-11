import groovy.util.Node
import groovy.util.NodeList

plugins {
    java
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.4"
    `maven-publish`
}

group = "com.github.codexwr"
version = "1.0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_17
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
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.springframework.boot:spring-boot-autoconfigure-processor")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    implementation("com.jayway.jsonpath:json-path:2.9.0")
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