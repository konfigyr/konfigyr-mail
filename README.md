# konfigyr-mail

A modular Spring Boot library for sending templated emails. It provides a clean abstraction over mail transport
and template rendering, with autoconfigured implementations for SMTP (via Spring Mail) and Thymeleaf.

## Requirements

- Java 21+
- Spring Boot 4.1.0+

## Modules

| Artifact | Description |
|---|---|
| `com.konfigyr:konfigyr-mail-api` | Core abstractions: `Mail`, `Mailer`, `TemplateEngine`, `Transport`, `Preparator` |
| `com.konfigyr:konfigyr-mail-smtp` | SMTP `Transport` implementation backed by Spring's `JavaMailSender` |
| `com.konfigyr:konfigyr-mail-thymeleaf` | Thymeleaf `TemplateEngine` implementation |
| `com.konfigyr:konfigyr-mail-test` | AssertJ assertions for `Mail` and `Template` — for use in tests |
| `com.konfigyr:konfigyr-mail-dependencies` | BOM for consistent dependency management |

## Architecture

`Mailer` is the application-facing API. Under the hood it composes two independent SPIs:

```
Mailer.send(Mail)
  ├─ TemplateEngine.render(Mail)  →  Template
  └─ Transport.send(Mail, Template)
```

**`TemplateEngine`** resolves the template name carried by the `Mail`, evaluates its context attributes, and returns a rendered `Template` with a content type (e.g. `text/html`).

**`Transport`** receives the fully-rendered `Template` together with the original `Mail` envelope (recipients, subject, encoding) and dispatches the message to the delivery infrastructure. It intentionally knows nothing about template resolution — that concern belongs to `TemplateEngine`.

The two SPIs vary independently. You can swap Thymeleaf for another renderer without touching the SMTP transport, or route mail through SendGrid or AWS SES without re-implementing rendering. `Mailer.of(TemplateEngine, Transport)` is the canonical composition point and is exactly what `MailerAutoConfiguration` uses when assembling the default bean.

`MailerAutoConfiguration` requires both a `TemplateEngine` bean and a `Transport` bean to be present. If either is missing the application context will fail to start; `MailingFailureAnalyzer` surfaces a targeted error message with instructions on which module to add.

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

### Sending mail

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

To add custom preparators, register any number of `Preparator<MimeMessageHelper>` beans; they are picked up automatically
and appended to the end of the preparation chain, after addresses, subject, and template body have already been applied.

> **Note:** If neither `Mail.Builder.from()` is called on a message nor `spring.mail.sender.email` is configured,
> no `From` header is added to the outgoing message. Most SMTP servers will reject such a message.

### Custom transport

To deliver mail through a channel other than SMTP — such as a transactional HTTP API (SendGrid, Mailgun, AWS SES) or a
test stub — implement `Transport` and register it as a Spring bean. `MailerAutoConfiguration` detects any `Transport`
bean and composes it with the available `TemplateEngine` automatically. The autoconfigured SMTP transport from
`konfigyr-mail-smtp` is skipped whenever a `Transport` bean is already present in the application context.

```java
@Component
class HttpApiTransport implements Transport {

    private final MailApiClient client;

    HttpApiTransport(MailApiClient client) {
        this.client = client;
    }

    @Override
    public void send(Mail mail, Template template) {
        try {
            client.submit(
                mail.recipients(),
                mail.from(),
                template.contents(),
                template.contentType()
            );
        } catch (ApiAuthException ex) {
            throw new MailingException(MailingException.ErrorCode.AUTHENTICATION_FAILED,
                "API authentication failed", ex);
        } catch (ApiRateLimitException ex) {
            throw new MailingException(MailingException.ErrorCode.QUOTA_EXCEEDED,
                "Sending quota exceeded", ex);
        } catch (Exception ex) {
            throw new MailingException(MailingException.ErrorCode.SEND_FAILED,
                "Mail delivery failed", ex);
        }
    }
}
```

Implementations must be thread-safe and must wrap every failure in `MailingException` with an appropriate `ErrorCode`
(see [Error handling](#error-handling)). This ensures callers of `Mailer` only ever need to handle a single exception
type regardless of which transport is in use.

If you are not using Spring Boot autoconfiguration, compose a `Mailer` manually:

```java
Mailer mailer = Mailer.of(templateEngine, new HttpApiTransport(client));
```

### Error handling

Every failure in the pipeline — template rendering, message preparation, or delivery — is reported as a
`MailingException`. Its `ErrorCode` identifies the stage so callers can decide whether to retry without
inspecting the exception cause:

| Error code | Stage | Retryable? |
|---|---|---|
| `TEMPLATE_RENDERING_FAILED` | `TemplateEngine` could not render the template | No — fix template or context |
| `MESSAGE_PREPARATION_FAILED` | Invalid address, unsupported content type, or envelope construction error | No — fix message data |
| `AUTHENTICATION_FAILED` | Transport credentials were rejected | No — operator intervention required |
| `CONNECTION_FAILED` | Server unreachable, TLS failure, or timeout | Yes — retry with back-off |
| `QUOTA_EXCEEDED` | Rate limit or sending quota exceeded | Yes — retry after a longer delay |
| `SEND_FAILED` | Delivery infrastructure rejected the message for any other reason | Inspect cause |

```java
try {
    mailer.send(mail);
} catch (MailingException ex) {
    switch (ex.getErrorCode()) {
        case CONNECTION_FAILED, QUOTA_EXCEEDED -> scheduleRetry(mail);
        case AUTHENTICATION_FAILED         -> alertOperations(ex);
        default -> log.error("Mail delivery failed: {}", ex.getMessage(), ex);
    }
}
```

### Test support

`konfigyr-mail-test` provides custom AssertJ assertions for verifying `Mail` and `Template` instances in your own tests. Add it with test scope:

```kotlin
// Gradle
testImplementation("com.konfigyr:konfigyr-mail-test")
```

```xml
<!-- Maven -->
<dependency>
  <groupId>com.konfigyr</groupId>
  <artifactId>konfigyr-mail-test</artifactId>
  <scope>test</scope>
</dependency>
```

Use `MailAssert` to verify the envelope of a captured `Mail`:

```java
MailAssert.assertThat(mail)
    .hasSubject("welcome.subject")
    .hasTemplate("emails/welcome")
    .containsRecipient(Recipient.to("user@example.com", "User"))
    .sentBy(new Address("no-reply@example.com"))
    .hasAttribute("name", "User");
```

Use `TemplateAssert` to verify a rendered `Template`, for example after capturing the value passed to a mocked `Transport`:

```java
TemplateAssert.assertThat(template)
    .isHtml()
    .contentsContain("<h1>Welcome, User</h1>");
```

Both assert types integrate with the standard AssertJ factory pattern via `MailAssert.factory()` and `TemplateAssert.factory()`, so they can be used inline with `assertThat(...).asInstanceOf(MailAssert.factory())`.

## Links

- [Issue tracker](https://github.com/konfigyr/konfigyr-mail/issues)
- [Contributing](CONTRIBUTING.md) — build, test, and pull-request workflow
- [License](LICENSE) — Apache 2.0
