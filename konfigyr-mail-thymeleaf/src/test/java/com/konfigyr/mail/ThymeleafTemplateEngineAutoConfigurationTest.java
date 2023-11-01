package com.konfigyr.mail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.thymeleaf.ITemplateEngine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author : Vladimir Spasic
 * @since : 31.10.23, Tue
 **/
class ThymeleafTemplateEngineAutoConfigurationTest {

	@Mock
	ITemplateEngine thymeleaf;

	ApplicationContextRunner runner;

	@BeforeEach
	void setup() {
		runner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(ThymeleafTemplateEngineAutoConfiguration.class));
	}

	@Test
	@DisplayName("should not register engine when thymeleaf bean is missing")
	void shouldNotAutoconfigureDueToMissingThymeleaf() {
		runner.run(context -> assertThat(context).hasNotFailed()
			.doesNotHaveBean(ThymeleafTemplateEngineAutoConfiguration.class)
			.doesNotHaveBean(TemplateEngine.class));
	}

	@Test
	@DisplayName("should not register engine when already defined")
	void shouldNotAutoconfigureWhenEngineIsPresent() {
		final var engine = mock(TemplateEngine.class);

		runner.withBean(ITemplateEngine.class, () -> thymeleaf)
			.withBean(TemplateEngine.class, () -> engine)
			.run(context -> assertThat(context).hasNotFailed()
				.doesNotHaveBean(ThymeleafTemplateEngineAutoConfiguration.class)
				.hasSingleBean(TemplateEngine.class)
				.getBean(TemplateEngine.class)
				.isEqualTo(engine));
	}

	@Test
	@DisplayName("should register thymeleaf mail template engine")
	void shouldAutoconfigure() {
		runner.withBean(ITemplateEngine.class, () -> thymeleaf)
			.run(context -> assertThat(context).hasNotFailed()
				.hasSingleBean(ThymeleafTemplateEngineAutoConfiguration.class)
				.hasSingleBean(TemplateEngine.class)
				.hasBean("thymeleafMailTemplateEngine"));
	}

}