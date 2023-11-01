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
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author : Vladimir Spasic
 * @since : 31.10.23, Tue
 **/
@ExtendWith(MockitoExtension.class)
class JavaMailerAutoConfigurationTest {

	@Mock
	JavaMailSender sender;

	@Mock
	TemplateEngine engine;

	ApplicationContextRunner runner;

	@BeforeEach
	void setup() {
		runner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(JavaMailerAutoConfiguration.class));
	}

	@Test
	@DisplayName("should not register mailer when java mail sender bean is missing")
	void shouldNotAutoconfigureDueToMissingSender() {
		runner.run(context -> assertThat(context).hasNotFailed()
			.doesNotHaveBean(JavaMailerAutoConfiguration.class)
			.doesNotHaveBean(Mailer.class));
	}

	@Test
	@DisplayName("should not register mailer when already defined")
	void shouldNotAutoconfigureWhenMailerIsPresent() {
		final var mailer = mock(Mailer.class);

		runner.withBean(JavaMailSender.class, () -> sender)
			.withBean(Mailer.class, () -> mailer)
			.run(context -> assertThat(context).hasNotFailed()
				.doesNotHaveBean(JavaMailerAutoConfiguration.class)
				.hasSingleBean(Mailer.class)
				.getBean(Mailer.class)
				.isEqualTo(mailer));
	}

	@Test
	@DisplayName("should fail to register mailer when template engine is missing")
	void shouldFailToAutoconfigureWhenTemplateEngineIsPresent() {
		runner.withBean(JavaMailSender.class, () -> sender)
			.run(context -> assertThat(context).hasFailed()
				.getFailure()
				.isInstanceOf(UnsatisfiedDependencyException.class));
	}

	@Test
	@DisplayName("should register mailer without default sender preperator")
	void shouldAutoconfigureWithoutPreparator() {
		runner.withBean(JavaMailSender.class, () -> sender)
			.withBean(TemplateEngine.class, () -> engine)
			.run(context -> assertThat(context).hasNotFailed()
				.hasSingleBean(JavaMailerAutoConfiguration.class)
				.hasSingleBean(Mailer.class)
				.hasBean("javaMailer")
				.doesNotHaveBean(Preperator.class));
	}

	@Test
	@DisplayName("should register mailer with default sender preperator")
	void shouldAutoconfigureWithPreparator() {
		runner.withBean(JavaMailSender.class, () -> sender)
			.withBean(TemplateEngine.class, () -> engine)
			.withPropertyValues("spring.mail.sender.email=john.doe@konfigyr.com", "spring.mail.sender.name=John Doe")
			.run(context -> assertThat(context).hasNotFailed()
				.hasSingleBean(JavaMailerAutoConfiguration.class)
				.hasSingleBean(Mailer.class)
				.hasBean("javaMailer")
				.hasBean("defaultSenderPreperator"));
	}

}