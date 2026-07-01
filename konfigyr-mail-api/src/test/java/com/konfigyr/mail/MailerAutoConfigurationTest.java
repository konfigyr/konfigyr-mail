package com.konfigyr.mail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class MailerAutoConfigurationTest {

	@Mock
	TemplateEngine engine;

	@Mock
	Transport transport;

	ApplicationContextRunner runner;

	@BeforeEach
	void setup() {
		runner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(MailerAutoConfiguration.class));
	}

	@Test
	@DisplayName("should fail to start when both template engine and transport beans are missing")
	void shouldFailWhenBothBeansAreMissing() {
		runner.run(context -> assertThat(context).hasFailed()
			.getFailure()
			.isInstanceOf(UnsatisfiedDependencyException.class));
	}

	@Test
	@DisplayName("should fail to start when transport bean is missing")
	void shouldFailWhenTransportIsMissing() {
		runner.withBean(TemplateEngine.class, () -> engine)
			.run(context -> assertThat(context).hasFailed()
				.getFailure()
				.isInstanceOf(UnsatisfiedDependencyException.class));
	}

	@Test
	@DisplayName("should fail to start when template engine bean is missing")
	void shouldFailWhenTemplateEngineIsMissing() {
		runner.withBean(Transport.class, () -> transport)
			.run(context -> assertThat(context).hasFailed()
				.getFailure()
				.isInstanceOf(UnsatisfiedDependencyException.class));
	}

	@Test
	@DisplayName("should not register mailer when already defined")
	void shouldNotAutoconfigureWhenMailerIsAlreadyDefined() {
		final var mailer = mock(Mailer.class);

		runner.withBean(TemplateEngine.class, () -> engine)
			.withBean(Transport.class, () -> transport)
			.withBean(Mailer.class, () -> mailer)
			.run(context -> assertThat(context).hasNotFailed()
				.doesNotHaveBean(MailerAutoConfiguration.class)
				.hasSingleBean(Mailer.class)
				.getBean(Mailer.class)
				.isEqualTo(mailer));
	}

	@Test
	@DisplayName("should register default mailer when template engine and transport beans are present")
	void shouldAutoconfigure() {
		runner.withBean(TemplateEngine.class, () -> engine)
			.withBean(Transport.class, () -> transport)
			.run(context -> assertThat(context).hasNotFailed()
				.hasSingleBean(MailerAutoConfiguration.class)
				.hasSingleBean(Mailer.class)
				.hasBean("defaultMailer")
				.getBean(Mailer.class)
				.isInstanceOf(DefaultMailer.class));
	}

}
