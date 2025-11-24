package com.konfigyr.mail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.thymeleaf.autoconfigure.ThymeleafAutoConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author : Vladimir Spasic
 * @since : 31.10.23, Tue
 **/
class ThymeleafTemplateEngineAutoConfigurationTest {

	@Test
	@DisplayName("should not register engine when thymeleaf bean is missing")
	void shouldNotAutoconfigureDueToMissingThymeleaf() {
		final var runner = new ApplicationContextRunner().withConfiguration(
			AutoConfigurations.of(ThymeleafTemplateEngineAutoConfiguration.class)
		);

		runner.run(context -> assertThat(context).hasNotFailed()
			.doesNotHaveBean(ThymeleafTemplateEngineAutoConfiguration.class)
			.doesNotHaveBean(TemplateEngine.class));
	}

	@Test
	@DisplayName("should not register engine when already defined")
	void shouldNotAutoconfigureWhenEngineIsPresent() {
		final var engine = mock(TemplateEngine.class);

		final var runner = new ApplicationContextRunner()
			.withBean("customTemplateEngine", TemplateEngine.class, () -> engine)
			.withConfiguration(
				AutoConfigurations.of(
					ThymeleafAutoConfiguration.class,
					ThymeleafTemplateEngineAutoConfiguration.class
				)
			);

		runner.run(context -> assertThat(context).hasNotFailed()
			.doesNotHaveBean(ThymeleafTemplateEngineAutoConfiguration.class)
			.hasSingleBean(TemplateEngine.class)
			.getBean(TemplateEngine.class)
			.isEqualTo(engine));
	}

	@Test
	@DisplayName("should register thymeleaf mail template engine")
	void shouldAutoconfigure() {
		final var runner = new ApplicationContextRunner().withConfiguration(
			AutoConfigurations.of(
				ThymeleafAutoConfiguration.class,
				ThymeleafTemplateEngineAutoConfiguration.class
			)
		);

		runner.run(context -> assertThat(context).hasNotFailed()
			.hasSingleBean(ThymeleafTemplateEngineAutoConfiguration.class)
			.hasSingleBean(TemplateEngine.class)
			.hasBean("thymeleafMailTemplateEngine"));
	}

}
