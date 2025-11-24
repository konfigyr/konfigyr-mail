description = "Konfigyr Mail Thymeleaf templating support"

dependencies {
    api(project(":konfigyr-mail-api"))

    compileOnly("org.springframework.boot:spring-boot-starter-thymeleaf")
    compileOnly("org.thymeleaf:thymeleaf-spring6")

    testImplementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    testImplementation("org.thymeleaf:thymeleaf-spring6")
}
