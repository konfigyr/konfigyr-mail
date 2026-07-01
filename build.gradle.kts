plugins {
    id("idea")
    id("checkstyle")
    id("java-library")
    id("com.konfigyr.sonatype") apply false
    id("com.konfigyr.deploy") apply false
}

apply(plugin = "com.konfigyr.sonatype")

allprojects {
	group = "com.konfigyr"
	version = "1.0.0"

    repositories {
        mavenCentral()
        mavenLocal()
    }
}

subprojects {
    if (name == "konfigyr-mail-dependencies") {
        return@subprojects
    }

    apply(plugin = "checkstyle")
    apply(plugin = "java-library")
    apply(plugin = "com.konfigyr.deploy")

    java {
        withJavadocJar()
        withSourcesJar()

        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    checkstyle {
        toolVersion = "12.1.1"
    }

    dependencies {
        implementation(platform(rootProject.libs.spring.dependencies))

        annotationProcessor(rootProject.libs.spring.processor.autoconfigure)
        annotationProcessor(rootProject.libs.spring.processor.configuration)

        testImplementation(rootProject.libs.spring.starter.test)
        testImplementation(rootProject.libs.spring.starter.validation)
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    apply(plugin = "jacoco")

    tasks.withType<JavaCompile>().configureEach {
        options.release = 21
    }

    tasks.test {
        useJUnitPlatform()
        finalizedBy(tasks.named("jacocoTestReport"))
    }

    tasks.named("jacocoTestReport", JacocoReport::class) {
        dependsOn(tasks.test)
        reports {
            xml.required = true
            html.required = true
        }
    }

}
