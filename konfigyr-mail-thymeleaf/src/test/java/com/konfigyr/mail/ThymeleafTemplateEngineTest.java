package com.konfigyr.mail;

import com.konfigyr.mail.test.TemplateAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.thymeleaf.exceptions.TemplateInputException;

import java.util.Locale;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = IntegrationTestConfiguration.class)
class ThymeleafTemplateEngineTest {

	@Autowired
	Mailer mailer;

	@MockitoBean
	Transport transport;

	@Captor
	ArgumentCaptor<Template> captor;

	@Test
	@DisplayName("should send mail with a Thymeleaf rendered template")
	void shouldSendMailWithThymeleafRenderedTemplate() {
		final var mail = Mail.builder()
			.subject("Test subject")
			.template("test-template")
			.attribute("user", "John Doe")
			.to("to@konfigyr.com")
			.locale(Locale.GERMAN)
			.build();

		assertThatNoException().isThrownBy(() -> mailer.send(mail));

		verify(transport).send(eq(mail), captor.capture());

		TemplateAssert.assertThat(captor.getValue())
			.isHtml()
			.contentsContain("<h1>Greetings John Doe,</h1>")
			.contentsContain("<p>This is your Thymeleaf test template.</p>")
			.contentsContain("<p>de</p>");
	}

	@Test
	@DisplayName("should render template when mail has no attributes")
	void shouldRenderTemplateWithoutAttributes() {
		final var mail = Mail.builder()
			.subject("Test subject")
			.template("test-template")
			.to("to@konfigyr.com")
			.locale(Locale.FRENCH)
			.build();

		assertThatNoException().isThrownBy(() -> mailer.send(mail));

		verify(transport).send(eq(mail), captor.capture());

		TemplateAssert.assertThat(captor.getValue())
			.isHtml()
			.contentsContain("<h1>Greetings ,</h1>")
			.contentsContain("<p>fr</p>");
	}

	@Test
	@DisplayName("should fail to send mail for an unknown template")
	void shouldFailToSendMailForUnknownTemplate() {
		final var mail = Mail.builder()
			.subject("Test subject")
			.template("unknown-template")
			.to("to@konfigyr.com")
			.build();

		assertThatExceptionOfType(MailingException.class)
			.isThrownBy(() -> mailer.send(mail))
			.returns(MailingException.ErrorCode.TEMPLATE_RENDERING_FAILED, MailingException::getErrorCode)
			.havingRootCause()
			.isInstanceOf(TemplateInputException.class)
			.withMessageContaining("Error resolving template [unknown-template]");

		verifyNoInteractions(transport);
	}


}
