import java.util.Locale

plugins {
    `java-library`
    id("java")
    id("maven-publish")
}

group = "cn.encmys.ykdz.forest"
version = "0.1.0-Beta"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    annotationProcessor("org.jetbrains:annotations:26.0.2-1")
    compileOnly("net.kyori:adventure-api:4.22.0")
    compileOnly("net.kyori:adventure-text-minimessage:4.22.0")
    testImplementation("net.kyori:adventure-api:4.22.0")
    testImplementation("net.kyori:adventure-text-minimessage:4.22.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
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