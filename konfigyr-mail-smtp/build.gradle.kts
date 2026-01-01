description = "Konfigyr Mail Spring SMTP support"

dependencies {
    api(project(":konfigyr-mail-api"))
    api("com.sanctionco.jmail:jmail:2.1.0")

    compileOnly("org.springframework.boot:spring-boot-starter-mail")

    testImplementation("org.springframework.boot:spring-boot-starter-mail")
    testImplementation("com.icegreen:greenmail:2.1.8")
}
