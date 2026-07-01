package com.konfigyr.mail;

import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
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
 * Autoconfigures a {@link Transport} bean backed by Spring's {@link JavaMailSender}.
 * <p>
 * When a {@link JavaMailSender} bean is present this configuration registers a
 * {@link JavaMailSenderTransport} as the {@link Transport}. The {@link Mailer} itself is
 * then assembled by {@link MailerAutoConfiguration} from this {@link Transport} and
 * whatever {@link TemplateEngine} is available in the application context.
 * <p>
 * If the {@code spring.mail.sender.email} property is set, a default-sender
 * {@link Preparator} is also registered and applies the {@code From} header to messages
 * that do not specify one via {@link Mail.Builder} from methods. The optional
 * {@code spring.mail.sender.name} property sets the corresponding display name.
 * <p>
 * Any {@code Preparator<MimeMessageHelper>} beans present in the application context are
 * automatically appended to the preparator chain, after the built-in address and subject
 * steps.
 *
 * @author Vladimir Spasic
 * @since 1.0.0
 **/
@NullMarked
@AutoConfiguration
@ConditionalOnBean(JavaMailSender.class)
@ConditionalOnMissingBean(Transport.class)
@AutoConfigureBefore(MailerAutoConfiguration.class)
@AutoConfigureAfter(MailSenderAutoConfiguration.class)
public class JavaMailSenderTransportAutoConfiguration {

	/** Creates a new {@link JavaMailSenderTransportAutoConfiguration} instance. */
	public JavaMailSenderTransportAutoConfiguration() {
	}

	static final String SENDER_PROPERTY = "spring.mail.sender";

	@Bean
	Transport javaMailSenderTransport(
		JavaMailSender sender,
		MessageSource messageSource,
		ObjectProvider<Preparator<MimeMessageHelper>> preparators
	) {
		return new JavaMailSenderTransport(sender, messageSource, preparators);
	}

	@Bean
	@ConditionalOnProperty(prefix = SENDER_PROPERTY, name = "email")
	Preparator<MimeMessageHelper> defaultSenderPreparator(Environment environment) {
		return JavaMailSenderTransport.sender(
			environment.getProperty(SENDER_PROPERTY + ".email"),
			environment.getProperty(SENDER_PROPERTY + ".name")
		);
	}

}
