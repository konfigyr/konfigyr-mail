package com.konfigyr.mail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class JavaMailSenderTransportAutoConfigurationTest {

	@Mock
	JavaMailSender sender;

	ApplicationContextRunner runner;

	@BeforeEach
	void setup() {
		runner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(JavaMailSenderTransportAutoConfiguration.class));
	}

	@Test
	@DisplayName("should not register transport when java mail sender bean is missing")
	void shouldNotAutoconfigureDueToMissingSender() {
		runner.run(context -> assertThat(context).hasNotFailed()
			.doesNotHaveBean(JavaMailSenderTransportAutoConfiguration.class)
			.doesNotHaveBean(Transport.class));
	}

	@Test
	@DisplayName("should not register transport when already defined")
	void shouldNotAutoconfigureWhenTransportIsPresent() {
		final var transport = mock(Transport.class);

		runner.withBean(JavaMailSender.class, () -> sender)
			.withBean(Transport.class, () -> transport)
			.run(context -> assertThat(context).hasNotFailed()
				.doesNotHaveBean(JavaMailSenderTransportAutoConfiguration.class)
				.hasSingleBean(Transport.class)
				.getBean(Transport.class)
				.isEqualTo(transport));
	}

	@Test
	@DisplayName("should register transport without default sender preparator")
	void shouldAutoconfigureWithoutPreparator() {
		runner.withBean(JavaMailSender.class, () -> sender)
			.run(context -> assertThat(context).hasNotFailed()
				.hasSingleBean(JavaMailSenderTransportAutoConfiguration.class)
				.hasSingleBean(Transport.class)
				.hasBean("javaMailSenderTransport")
				.doesNotHaveBean(Preparator.class));
	}

	@Test
	@DisplayName("should register transport with default sender preparator")
	void shouldAutoconfigureWithPreparator() {
		runner.withBean(JavaMailSender.class, () -> sender)
			.withPropertyValues("spring.mail.sender.email=john.doe@konfigyr.com", "spring.mail.sender.name=John Doe")
			.run(context -> assertThat(context).hasNotFailed()
				.hasSingleBean(JavaMailSenderTransportAutoConfiguration.class)
				.hasSingleBean(Transport.class)
				.hasBean("javaMailSenderTransport")
				.hasBean("defaultSenderPreparator"));
	}

	@Test
	@DisplayName("should be registered before MailerAutoConfiguration so Mailer is wired with SMTP transport")
	void shouldAutoconfigureBeforeMailerAutoConfiguration() {
		final var runner = new ApplicationContextRunner()
			.withBean(JavaMailSender.class, () -> sender)
			.withBean(TemplateEngine.class, () -> mock(TemplateEngine.class))
			.withConfiguration(
				AutoConfigurations.of(
					JavaMailSenderTransportAutoConfiguration.class,
					MailerAutoConfiguration.class
				)
			);

		runner.run(context -> assertThat(context).hasNotFailed()
			.hasSingleBean(Mailer.class)
			.hasSingleBean(Transport.class)
			.getBean(Transport.class)
			.isInstanceOf(JavaMailSenderTransport.class));
	}

}
