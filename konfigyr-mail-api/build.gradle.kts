description = "Konfigyr Mail Spring core API"

dependencies {
    compileOnly(libs.spring.starter)

    testImplementation(project(":konfigyr-mail-test"))
}
