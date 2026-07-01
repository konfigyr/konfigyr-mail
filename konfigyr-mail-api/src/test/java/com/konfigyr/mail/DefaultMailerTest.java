package com.konfigyr.mail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultMailerTest {

	@Mock
	TemplateEngine engine;

	@Mock
	Transport transport;

	Mail mail;
	Mailer mailer;

	@BeforeEach
	void setup() {
		mail = Mail.builder()
			.subject("test-subject")
			.template("test-template")
			.to("test@konfigyr.com")
			.build();

		mailer = Mailer.of(engine, transport);
	}

	@Test
	@DisplayName("should render template and dispatch via transport")
	void shouldSendMail() throws IOException {
		final var template = Template.html("<p>Hello</p>");
		doReturn(template).when(engine).render(mail);

		assertThatNoException().isThrownBy(() -> mailer.send(mail));

		final var order = inOrder(engine, transport);
		order.verify(engine).render(mail);
		order.verify(transport).send(mail, template);
	}

	@Test
	@DisplayName("should wrap IOException from template engine as TEMPLATE_RENDERING_FAILED")
	void shouldWrapIOExceptionFromEngine() throws IOException {
		final var cause = new IOException("template not found");
		doThrow(cause).when(engine).render(mail);

		assertThatExceptionOfType(MailingException.class)
			.isThrownBy(() -> mailer.send(mail))
			.returns(MailingException.ErrorCode.TEMPLATE_RENDERING_FAILED, MailingException::getErrorCode)
			.withCause(cause);

		verifyNoInteractions(transport);
	}

	@Test
	@DisplayName("should wrap unexpected exception from template engine as TEMPLATE_RENDERING_FAILED")
	void shouldWrapRuntimeExceptionFromEngine() throws IOException {
		final var cause = new RuntimeException("unexpected failure");
		doThrow(cause).when(engine).render(mail);

		assertThatExceptionOfType(MailingException.class)
			.isThrownBy(() -> mailer.send(mail))
			.returns(MailingException.ErrorCode.TEMPLATE_RENDERING_FAILED, MailingException::getErrorCode)
			.withCause(cause);

		verifyNoInteractions(transport);
	}

	@Test
	@DisplayName("should re-throw MailingException from template engine without wrapping")
	void shouldRethrowMailingExceptionFromEngine() throws IOException {
		final var cause = new MailingException(MailingException.ErrorCode.TEMPLATE_RENDERING_FAILED, "already wrapped");
		doThrow(cause).when(engine).render(mail);

		assertThatThrownBy(() -> mailer.send(mail)).isSameAs(cause);

		verifyNoInteractions(transport);
	}

	@Test
	@DisplayName("should wrap unexpected exception from transport as SEND_FAILED")
	void shouldWrapRuntimeExceptionFromTransport() throws IOException {
		final var template = Template.html("<p>Hello</p>");
		final var cause = new RuntimeException("connection refused");
		doReturn(template).when(engine).render(mail);
		doThrow(cause).when(transport).send(mail, template);

		assertThatExceptionOfType(MailingException.class)
			.isThrownBy(() -> mailer.send(mail))
			.returns(MailingException.ErrorCode.SEND_FAILED, MailingException::getErrorCode)
			.withCause(cause);
	}

	@Test
	@DisplayName("should re-throw MailingException from transport without wrapping")
	void shouldRethrowMailingExceptionFromTransport() throws IOException {
		final var template = Template.html("<p>Hello</p>");
		final var cause = new MailingException(MailingException.ErrorCode.CONNECTION_FAILED, "unreachable");
		doReturn(template).when(engine).render(mail);
		doThrow(cause).when(transport).send(mail, template);

		assertThatThrownBy(() -> mailer.send(mail)).isSameAs(cause);
	}

}
