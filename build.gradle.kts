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
    implementation("org.jetbrains:annotations:26.0.1")
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
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "cn.encmys"
            artifactId = rootProject.name
            version = rootProject.version.toString()

            pom {
                name.set(rootProject.name)
                description.set("Script Engine for Forest MC plugin series.")
                url.set("https://github.com/YKDZ")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("ykdz")
                        name.set("YKDZ")
                        email.set("3070799584@qq.com")
                    }
                }
            }
        }
    }

    repositories {
        mavenLocal()
    }
}