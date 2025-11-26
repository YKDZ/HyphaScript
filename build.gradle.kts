import java.util.*

plugins {
    `java-library`
    id("java")
    id("maven-publish")
}

group = "cn.encmys.ykdz.forest"
version = "0.1.2-Beta"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    annotationProcessor("org.jetbrains:annotations:26.0.2-1")
    compileOnly("net.kyori:adventure-api:4.25.0")
    compileOnly("net.kyori:adventure-text-minimessage:4.25.0")
    compileOnly("net.kyori:adventure-text-serializer-plain:4.25.0")
    testImplementation("net.kyori:adventure-api:4.25.0")
    testImplementation("net.kyori:adventure-text-minimessage:4.25.0")
    testImplementation("net.kyori:adventure-text-serializer-plain:4.25.0")
    testImplementation("org.junit.jupiter:junit-jupiter:6.0.1")
    testImplementation("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    withSourcesJar()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

publishing {
    repositories {
        mavenLocal()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/ykdz/HyphaScript")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GPR_USER")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GPR_KEY")
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])
            groupId = group as String
            artifactId = rootProject.name.lowercase(Locale.getDefault())
            version = version.lowercase(Locale.getDefault())
        }
    }
}