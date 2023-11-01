package com.konfigyr.mail;

import org.assertj.core.api.InstanceOfAssertFactories;
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

		assertThat(mail).returns("test-template", Mail::template)
			.returns(StandardCharsets.UTF_8, Mail::encoding)
			.returns(Locale.US, Mail::locale)
			.returns(new Address("sender@konfigyr.com"), Mail::from)
			.satisfies(it -> assertThat(it.subject()).returns("test-subject", Subject::value)
				.extracting(Subject::arguments)
				.asInstanceOf(InstanceOfAssertFactories.array(Object[].class))
				.containsExactlyInAnyOrder("args", "used", "by message source"))
			.satisfies(it -> assertThat(it.subject()).extracting(Subject::toResolvable)
				.returns(new String[] { it.subject().value() }, MessageSourceResolvable::getCodes)
				.returns(it.subject().value(), MessageSourceResolvable::getDefaultMessage)
				.returns(it.subject().arguments(), MessageSourceResolvable::getArguments))
			.satisfies(it -> assertThat(it.recipients()).hasSize(4)
				.containsExactlyInAnyOrder(Recipient.to("to@konfigyr.com"), Recipient.cc("cc@konfigyr.com", "CC"),
						Recipient.bcc("bcc@konfigyr.com"), Recipient.bcc("bcc2@konfigyr.com")))
			.satisfies(it -> assertThat(it.replyTo()).hasSize(1)
				.containsExactlyInAnyOrder(new Address("reply-to@konfigyr.com")))
			.satisfies(it -> assertThat(it.attributes()).hasSize(3)
				.containsEntry("key", "value")
				.containsEntry("number", 1)
				.containsEntry("bool", true));
	}

}