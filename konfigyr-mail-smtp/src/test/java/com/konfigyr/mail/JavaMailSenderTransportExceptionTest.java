package com.konfigyr.mail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class JavaMailSenderTransportExceptionTest {

	@Mock
	JavaMailSender sender;

	@Mock
	MessageSource messageSource;

	Mail mail;
	Template template;
	JavaMailSenderTransport transport;

	@BeforeEach
	void setup() {
		mail = Mail.builder()
			.subject("test-subject")
			.template("test-template")
			.to("test@konfigyr.com")
			.build();

		template = Template.html("<p>Hello</p>");
		transport = new JavaMailSenderTransport(sender, messageSource, List.of());
	}

	@Test
	@DisplayName("should map MailAuthenticationException to AUTHENTICATION_FAILED")
	void shouldMapAuthenticationException() {
		doThrow(new MailAuthenticationException("bad credentials"))
			.when(sender).send(any(MimeMessagePreparator.class));

		assertThatExceptionOfType(MailingException.class)
			.isThrownBy(() -> transport.send(mail, template))
			.returns(MailingException.ErrorCode.AUTHENTICATION_FAILED, MailingException::getErrorCode)
			.withCauseInstanceOf(MailAuthenticationException.class);
	}

	@Test
	@DisplayName("should map MailSendException with ConnectException cause to CONNECTION_FAILED")
	void shouldMapConnectionRefused() {
		doThrow(new MailSendException("connection refused", new ConnectException("connection refused")))
			.when(sender).send(any(MimeMessagePreparator.class));

		assertThatExceptionOfType(MailingException.class)
			.isThrownBy(() -> transport.send(mail, template))
			.returns(MailingException.ErrorCode.CONNECTION_FAILED, MailingException::getErrorCode)
			.withCauseInstanceOf(MailSendException.class);
	}

	@Test
	@DisplayName("should map MailSendException with SocketTimeoutException cause to CONNECTION_FAILED")
	void shouldMapReadTimeout() {
		doThrow(new MailSendException("read timeout", new SocketTimeoutException("read timed out")))
			.when(sender).send(any(MimeMessagePreparator.class));

		assertThatExceptionOfType(MailingException.class)
			.isThrownBy(() -> transport.send(mail, template))
			.returns(MailingException.ErrorCode.CONNECTION_FAILED, MailingException::getErrorCode)
			.withCauseInstanceOf(MailSendException.class);
	}

	@Test
	@DisplayName("should map MailSendException without connection cause to SEND_FAILED")
	void shouldMapSmtpRejection() {
		doThrow(new MailSendException("550 user unknown"))
			.when(sender).send(any(MimeMessagePreparator.class));

		assertThatExceptionOfType(MailingException.class)
			.isThrownBy(() -> transport.send(mail, template))
			.returns(MailingException.ErrorCode.SEND_FAILED, MailingException::getErrorCode)
			.withCauseInstanceOf(MailSendException.class);
	}

	@Test
	@DisplayName("should map MailException without connection cause to SEND_FAILED")
	void shouldMapCustomMailException() {
		final var cause = new MailException("Custom mail exception") { /*noop*/ };
		doThrow(cause)
			.when(sender).send(any(MimeMessagePreparator.class));

		assertThatExceptionOfType(MailingException.class)
			.isThrownBy(() -> transport.send(mail, template))
			.returns(MailingException.ErrorCode.SEND_FAILED, MailingException::getErrorCode)
			.withCause(cause);
	}

}
