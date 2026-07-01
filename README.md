# konfigyr-mail

A modular Spring Boot library for sending templated emails. It provides a clean abstraction over mail transport
and template rendering, with autoconfigured implementations for SMTP (via Spring Mail) and Thymeleaf.

## Requirements

- Java 21+
- Spring Boot 4.1.0+

## Modules

| Artifact | Description |
|---|---|
| `com.konfigyr:konfigyr-mail-api` | Core abstractions: `Mail`, `Mailer`, `TemplateEngine`, `Preparator` |
| `com.konfigyr:konfigyr-mail-smtp` | SMTP implementation backed by Spring's `JavaMailSender` |
| `com.konfigyr:konfigyr-mail-thymeleaf` | Thymeleaf template engine implementation |
| `com.konfigyr:konfigyr-mail-dependencies` | BOM for consistent dependency management |

## Installation

Import the BOM to align all module versions, then declare only the modules you need.

### Maven

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.konfigyr</groupId>
      <artifactId>konfigyr-mail-dependencies</artifactId>
      <version>1.0.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependencies>
  <dependency>
    <groupId>com.konfigyr</groupId>
    <artifactId>konfigyr-mail-smtp</artifactId>
  </dependency>
  <dependency>
    <groupId>com.konfigyr</groupId>
    <artifactId>konfigyr-mail-thymeleaf</artifactId>
  </dependency>
</dependencies>
```

### Gradle

```kotlin
implementation(platform("com.konfigyr:konfigyr-mail-dependencies:1.0.0"))
implementation("com.konfigyr:konfigyr-mail-smtp")
implementation("com.konfigyr:konfigyr-mail-thymeleaf")
```

## Usage

Configure your SMTP connection via standard Spring Boot properties:

```properties
spring.mail.host=smtp.example.com
spring.mail.port=587
spring.mail.sender.email=no-reply@example.com
spring.mail.sender.name=Example
```

Build a `Mail` message and inject the auto-configured `Mailer` bean:

```java
@Service
class NotificationService {

    private final Mailer mailer;

    NotificationService(Mailer mailer) {
        this.mailer = mailer;
    }

    void sendWelcome(String email, String name) {
        Mail mail = Mail.builder()
            .subject("welcome.subject")
            .template("emails/welcome")
            .to(email, name)
            .attribute("name", name)
            .build();

        mailer.send(mail);
    }
}
```

The `subject` value is resolved against a `MessageSource` — if no message is found the raw value is used as a fallback.
The `template` name is passed to the `TemplateEngine`; with Thymeleaf it maps to a template on the classpath (e.g. `templates/emails/welcome.html`).

To add custom preparators, register any number of `Preparator<MimeMessageHelper>` beans, they are picked up automatically
and appended to the end of the preparation chain, after addresses, subject, and template have already been applied.

> **Note:** If neither `Mail.Builder.from()` is called on a message nor `spring.mail.sender.email` is configured,
> no `From` header is added to the outgoing message. Most SMTP servers will reject such a message.

## Links

- [Issue tracker](https://github.com/konfigyr/konfigyr-mail/issues)
- [Contributing](CONTRIBUTING.md) — build, test, and pull-request workflow
- [License](LICENSE) — Apache 2.0
