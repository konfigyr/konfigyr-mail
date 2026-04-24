description = "Konfigyr Mail Spring SMTP support"

dependencies {
    api(project(":konfigyr-mail-api"))
    api(libs.jmail)

    compileOnly(libs.spring.starter.mail)

    testImplementation(libs.spring.starter.mail)
    testImplementation(libs.greenmail)
}
