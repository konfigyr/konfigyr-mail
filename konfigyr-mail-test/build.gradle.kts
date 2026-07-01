plugins {
    `java-library`
}

description = "Konfigyr Mail Spring Test support"

dependencies {
    api(project(":konfigyr-mail-api"))

    compileOnly(libs.spring.starter)
    compileOnly("org.assertj:assertj-core")
}
