plugins {
    java
    application
}

group = "dev.claudefoundations"
version = "0.1.0"

application {
    mainClass = "dev.claudefoundations.lab.Main"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.anthropic:anthropic-java:3.12.0")
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.test {
    useJUnitPlatform()
}
