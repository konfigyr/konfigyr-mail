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
 * Autoconfigures a {@link Mailer} bean backed by Spring's {@link org.springframework.mail.javamail.JavaMailSender}.
 * <p>
 * If the {@code spring.mail.sender.email} property is set, a default-sender
 * {@link Preparator} is also registered and applies the {@code From} header to messages
 * that do not specify one via {@link Mail.Builder} from methods. The optional
 * {@code spring.mail.sender.name} property sets the corresponding display name.
 * <p>
 * Any {@code Preparator<MimeMessageHelper>} beans present in the application context are
 * automatically appended to the preparator chain, after the built-in address, subject, and
 * template steps.
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
