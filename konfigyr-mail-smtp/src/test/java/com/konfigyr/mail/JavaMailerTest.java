package com.konfigyr.mail;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.sanctionco.jmail.FailureReason;
import jakarta.mail.Message;
import jakarta.mail.internet.InternetAddress;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.MailPreparationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.MimeType;

import java.io.IOException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author : Vladimir Spasic
 * @since : 31.10.23, Tue
 **/
@SpringBootTest(classes = IntegrationTestConfiguration.class)
@ExtendWith(MockitoExtension.class)
class JavaMailerTest {

	static ServerSetup server = new ServerSetup(2500, null, "smtp");
	static GreenMail smtp = new GreenMail(server);

	@DynamicPropertySource
	static void smtpProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.mail.host", server::getBindAddress);
		registry.add("spring.mail.port", server::getPort);
		registry.add("spring.mail.sender.email", () -> "info@konfigyr.com");
		registry.add("spring.mail.sender.name", () -> "Default sender");
		registry.add("spring.messages.basename", () -> "messages/mail");
	}

	@MockBean
	TemplateEngine engine;

	@Autowired
	Mailer mailer;

	@BeforeAll
	static void start() {
		smtp.start();
	}

	@AfterEach
	void reset() {
		smtp.reset();
	}

	@AfterAll
	static void stop() {
		smtp.stop();
	}

	@Test
	@DisplayName("should send mail via SMTP")
	void shouldSendMail() throws IOException {
		final var mail = Mail.builder()
			.subject("test-email-subject")
			.from("sender@konfigyr.com")
			.template("template")
			.to("test@konfigyr.com")
			.cc("cc@konfigyr.com")
			.replyTo("reply@konfigyr.com")
			.locale(Locale.US)
			.build();

		doReturn(Template.text("Email template")).when(engine).render(mail);

		assertThatNoException().isThrownBy(() -> mailer.send(mail));

		final var messages = smtp.getReceivedMessages();
		assertThat(messages).isNotEmpty()
			.hasSize(2)
			.allSatisfy(message -> assertThat(message)
				.satisfies(it -> assertThat(it.getSubject()).isEqualTo("Test subject"))
				.satisfies(it -> assertThat(message.getContentType()).contains("text/plain"))
				.satisfies(it -> assertThat(it.getContent()).isEqualTo("Email template"))
				.satisfies(it -> assertThat(it.getFrom()).containsExactly(new InternetAddress("sender@konfigyr.com")))
				.satisfies(it -> assertThat(it.getRecipients(Message.RecipientType.TO))
					.containsExactly(new InternetAddress("test@konfigyr.com")))
				.satisfies(it -> assertThat(it.getRecipients(Message.RecipientType.CC))
					.containsExactly(new InternetAddress("cc@konfigyr.com")))
				.satisfies(it -> assertThat(it.getRecipients(Message.RecipientType.BCC)).isNullOrEmpty())
				.satisfies(
						it -> assertThat(it.getReplyTo()).containsExactly(new InternetAddress("reply@konfigyr.com"))));
	}

	@Test
	@DisplayName("should send mail via SMTP with default sender")
	void shouldSendMailWithDefaultSender() throws IOException {
		final var mail = Mail.builder()
			.subject("test-email-subject")
			.template("template")
			.to("test@konfigyr.com", "Test Konfigyr")
			.locale(Locale.GERMANY)
			.build();

		doReturn(Template.html("Email HTML template")).when(engine).render(mail);

		assertThatNoException().isThrownBy(() -> mailer.send(mail));

		final var messages = smtp.getReceivedMessages();
		assertThat(messages).isNotEmpty()
			.hasSize(1)
			.allSatisfy(
					message -> assertThat(message).satisfies(it -> assertThat(it.getSubject()).isEqualTo("Test Titel"))
						.satisfies(it -> assertThat(message.getContentType()).contains("text/html"))
						.satisfies(it -> assertThat(it.getContent()).isEqualTo("Email HTML template"))
						.satisfies(it -> assertThat(it.getFrom())
							.containsExactly(new InternetAddress("info@konfigyr.com", "Default sender")))
						.satisfies(it -> assertThat(it.getRecipients(Message.RecipientType.TO))
							.containsExactly(new InternetAddress("test@konfigyr.com", "Test Konfigyr")))
						.satisfies(it -> assertThat(it.getRecipients(Message.RecipientType.CC)).isNullOrEmpty())
						.satisfies(it -> assertThat(it.getRecipients(Message.RecipientType.BCC)).isNullOrEmpty())
						.satisfies(it -> assertThat(it.getReplyTo())
							.containsExactly(new InternetAddress("info@konfigyr.com", "Default sender"))));
	}

	@Test
	@DisplayName("should fail to send when template engines fails")
	void templateEngineShouldFail() throws IOException {
		final var mail = Mail.builder()
			.subject("test-email-subject")
			.template("template")
			.to("test@konfigyr.com", "Test Konfigyr")
			.locale(Locale.JAPAN)
			.build();

		doThrow(IOException.class).when(engine).render(mail);

		assertThatException().isThrownBy(() -> mailer.send(mail))
			.isInstanceOf(MailPreparationException.class)
			.withCauseInstanceOf(IOException.class);
	}

	@Test
	@DisplayName("should fail to send for invalid template content type")
	void templateContentTypeShouldFail() throws IOException {
		final var mail = Mail.builder()
			.subject("test-email-subject")
			.template("template")
			.to("test@konfigyr.com", "Test Konfigyr")
			.locale(Locale.JAPAN)
			.build();

		doReturn(new Template("template", MimeType.valueOf("image/gif"))).when(engine).render(mail);

		assertThatException().isThrownBy(() -> mailer.send(mail))
			.isInstanceOf(MailPreparationException.class)
			.withMessageContaining("image/gif")
			.withNoCause();
	}

	@Test
	@DisplayName("should fail to send for invalid email address")
	void emailAddressShouldFail() {
		final var mail = Mail.builder()
			.subject("test-email-subject")
			.template("template")
			.to("inval)&9q8705id test")
			.locale(Locale.JAPAN)
			.build();

		assertThatException().isThrownBy(() -> mailer.send(mail))
			.isInstanceOf(MailPreparationException.class)
			.withMessageContaining(FailureReason.DISALLOWED_UNQUOTED_CHARACTER.name())
			.withNoCause();

		verifyNoInteractions(engine);
	}

}