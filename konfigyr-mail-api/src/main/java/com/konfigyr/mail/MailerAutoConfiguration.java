package com.konfigyr.mail;

import org.jspecify.annotations.NullMarked;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Autoconfigures a {@link Mailer} bean by composing a {@link TemplateEngine} and a
 * {@link Transport} using {@link Mailer#of(TemplateEngine, Transport)}.
 * <p>
 * This configuration is skipped only when a {@link Mailer} bean is already present —
 * either provided by the application or by another autoconfiguration — allowing custom
 * implementations to take precedence. In all other cases it is unconditional: if a
 * {@link TemplateEngine} or {@link Transport} bean is missing the application context
 * will fail to start, and the {@link MailingFailureAnalyzer} will surface a targeted
 * error message with instructions on which library module to add.
 *
 * @author Vladimir Spasic
 * @since 1.0.0
 **/
@NullMarked
@AutoConfiguration
@ConditionalOnMissingBean(Mailer.class)
public class MailerAutoConfiguration {

	private final TemplateEngine templateEngine;
	private final Transport transport;

	/**
	 * Creates a new {@link MailerAutoConfiguration} instance with the required
	 * template engine and transport dependencies.
	 *
	 * @param templateEngine the templating engine implementation
	 * @param transport the Mail transport implementation
	 */
	public MailerAutoConfiguration(TemplateEngine templateEngine, Transport transport) {
		this.templateEngine = templateEngine;
		this.transport = transport;
	}

	@Bean
	Mailer defaultMailer() {
		return Mailer.of(templateEngine, transport);
	}

}
