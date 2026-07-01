package com.konfigyr.mail;

import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.mail.autoconfigure.MailSenderAutoConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * Autoconfiguration class that would register the {@link Mailer} that is using Spring
 * {@link org.springframework.mail.javamail.JavaMailSender} to send {@link Mail mails}.
 *
 * @author Vladimir Spasic
 * @since 1.0.0
 **/
@NullMarked
@AutoConfiguration
@ConditionalOnBean(JavaMailSender.class)
@ConditionalOnMissingBean(Mailer.class)
@AutoConfigureAfter(MailSenderAutoConfiguration.class)
public class JavaMailerAutoConfiguration {

	/** Creates a new {@link JavaMailerAutoConfiguration} instance. */
	public JavaMailerAutoConfiguration() {
	}

	static final String SENDER_PROPERTY = "spring.mail.sender";

	@Bean
	Mailer javaMailer(JavaMailSender sender, MessageSource messageSource, TemplateEngine templateEngine,
			ObjectProvider<Preparator<MimeMessageHelper>> preparators) {
		return new JavaMailer(sender, messageSource, templateEngine, preparators);
	}

	@Bean
	@ConditionalOnProperty(prefix = SENDER_PROPERTY, name = "email")
	Preparator<MimeMessageHelper> defaultSenderPreparator(Environment environment) {
		return JavaMailer.sender(environment.getProperty(SENDER_PROPERTY + ".email"),
				environment.getProperty(SENDER_PROPERTY + ".name"));
	}

}
