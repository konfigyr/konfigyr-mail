description = "Konfigyr Mail Thymeleaf templating support"

dependencies {
    api(project(":konfigyr-mail-api"))

    compileOnly(libs.spring.starter.thymeleaf)
    compileOnly(libs.thymeleaf)

    testImplementation(libs.spring.starter.thymeleaf)
    testImplementation(libs.thymeleaf)
}
