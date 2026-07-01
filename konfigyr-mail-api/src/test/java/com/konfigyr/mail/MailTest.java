package com.konfigyr.mail;

import com.konfigyr.mail.test.MailAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSourceResolvable;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MailTest {

	@Test
	@DisplayName("should create mail instance")
	void shouldCreateMailInstance() {
		final var mail = Mail.builder()
			.subject("test-subject", "args", "used", "by message source")
			.template("test-template")
			.attribute("key", "value")
			.attributes(Map.of("number", 1, "bool", true))
			.to("to@konfigyr.com")
			.cc("cc@konfigyr.com", "CC")
			.recipients(Recipient.bcc("bcc@konfigyr.com"), Recipient.bcc("bcc2@konfigyr.com"))
			.from("sender@konfigyr.com")
			.replyTo("reply-to@konfigyr.com")
			.encoding("UTF-8")
			.locale(Locale.US)
			.build();

		MailAssert.assertThat(mail)
			.hasTemplate("test-template")
			.hasEncoding(StandardCharsets.UTF_8)
			.hasLocale(Locale.US)
			.hasSubject("test-subject", "args", "used", "by message source")
			.sentBy(new Address("sender@konfigyr.com"))
			.hasReplyTo(new Address("reply-to@konfigyr.com"))
			.hasRecipients(
				Recipient.to("to@konfigyr.com"),
				Recipient.cc("cc@konfigyr.com", "CC"),
				Recipient.bcc("bcc@konfigyr.com"),
				Recipient.bcc("bcc2@konfigyr.com"))
			.hasAttribute("key", "value")
			.hasAttribute("number", 1)
			.hasAttribute("bool", true);

		assertThat(mail.subject()).extracting(Subject::toResolvable)
			.returns(new String[] { mail.subject().value() }, MessageSourceResolvable::getCodes)
			.returns(mail.subject().value(), MessageSourceResolvable::getDefaultMessage)
			.returns(mail.subject().arguments(), MessageSourceResolvable::getArguments);
	}

}
