import java.util.*

plugins {
    `java-library`
    id("java")
    id("maven-publish")
}

group = "cn.encmys.ykdz.forest"
version = "0.1.3-Beta"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    annotationProcessor("org.jetbrains:annotations:26.0.2-1")
    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    compileOnly("net.kyori:adventure-api:4.25.0")
    compileOnly("net.kyori:adventure-text-minimessage:4.25.0")
    compileOnly("net.kyori:adventure-text-serializer-plain:4.25.0")
    compileOnly("me.clip:placeholderapi:2.11.7")

    testImplementation("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    testImplementation("net.kyori:adventure-api:4.25.0")
    testImplementation("net.kyori:adventure-text-minimessage:4.25.0")
    testImplementation("net.kyori:adventure-text-serializer-plain:4.25.0")
    testImplementation("org.junit.jupiter:junit-jupiter:6.0.1")
    testImplementation("org.junit.platform:junit-platform-launcher")
    testImplementation("me.clip:placeholderapi:2.11.7")
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