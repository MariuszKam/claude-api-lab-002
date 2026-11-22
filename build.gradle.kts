plugins {
    java
}

group = "dev.claudefoundations"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.anthropic:anthropic-java:3.12.0")
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
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
