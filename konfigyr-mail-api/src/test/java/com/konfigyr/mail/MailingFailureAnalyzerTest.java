package com.konfigyr.mail;

import org.assertj.core.api.ThrowableAssertAlternative;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;

/**
 * @author Vladimir Spasic
 * @since : 31.10.23, Tue
 **/
@ExtendWith(OutputCaptureExtension.class)
class MailingFailureAnalyzerTest {

	@Test
	@DisplayName("should perform analysis when mailer bean is not defined")
	void analysisIsPerformedForMailerBean(CapturedOutput output) {
		analyze(TestConfiguration.class, MissingMailerConfiguration.class)
			.isInstanceOf(UnsatisfiedDependencyException.class);

		assertThat(output).contains("APPLICATION FAILED TO START")
			.contains("It seems you attempted to use the Konfigyr Mail library")
			.contains(Mailer.class.getTypeName())
			.contains("konfigyr-mail-smtp");
	}

	@Test
	@DisplayName("should perform analysis when template engine bean is not defined")
	void analysisIsPerformedForEngineBean(CapturedOutput output) {
		analyze(TestConfiguration.class, MissingEngineConfiguration.class)
			.isInstanceOf(UnsatisfiedDependencyException.class);

		assertThat(output).contains("APPLICATION FAILED TO START")
			.contains("It seems you attempted to use the Konfigyr Mail library")
			.contains(TemplateEngine.class.getTypeName())
			.contains("konfigyr-mail-thymeleaf");
	}

	@Test
	@DisplayName("should not perform analysis when other beans are not defined")
	void analysisIsNotPerformedForOtherBeans(CapturedOutput output) {
		analyze(TestConfiguration.class, OtherMissingBeanConfiguration.class)
			.isInstanceOf(UnsatisfiedDependencyException.class);

		assertThat(output).contains("APPLICATION FAILED TO START")
			.doesNotContain("It seems you attempted to use the Konfigyr Mail library")
			.contains(Preparator.class.getTypeName());
	}

	static ThrowableAssertAlternative<Exception> analyze(Class<?>... classes) {
		return assertThatException()
			.isThrownBy(() -> new SpringApplicationBuilder(classes).web(WebApplicationType.NONE).run());
	}

	@EnableAutoConfiguration
	@Configuration(proxyBeanMethods = false)
	static class TestConfiguration {

	}

	@Configuration(proxyBeanMethods = false)
	static class MissingMailerConfiguration {

		MissingMailerConfiguration(Mailer mailer) {
		}

	}

	@Configuration(proxyBeanMethods = false)
	static class MissingEngineConfiguration {

		MissingEngineConfiguration(TemplateEngine engine) {
		}

	}

	@Configuration(proxyBeanMethods = false)
	static class OtherMissingBeanConfiguration {

		OtherMissingBeanConfiguration(Preparator<String> preparator) {
		}

	}

}
