plugins {
    id("java-platform")
    id("com.konfigyr.deploy")
}

description = "Bill of Materials (BOM) for the Konfigyr Mail library"

dependencies {
    constraints {
        api(project(":konfigyr-mail-api"))
        api(project(":konfigyr-mail-smtp"))
        api(project(":konfigyr-mail-thymeleaf"))
    }
}
