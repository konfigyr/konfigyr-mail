package com.konfigyr.mail;

import com.sanctionco.jmail.EmailValidationResult;
import com.sanctionco.jmail.JMail;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.jspecify.annotations.NonNull;
import org.springframework.mail.MailException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @author : Vladimir Spasic
 * @since : 31.10.23, Tue
 **/
@Slf4j
@RequiredArgsConstructor
class JavaMailer implements Mailer {

	private final JavaMailSender sender;

	private final Preperator<@NonNull MimeMessageHelper> preperator;

	JavaMailer(JavaMailSender sender, MessageSource messageSource, TemplateEngine templateEngine,
			Iterable<Preperator<@NonNull MimeMessageHelper>> additionalPreparators) {
		this.sender = sender;

		// create the MIME message preperator with following steps:
		//
		// 1) setup sender, recipients and reply to address
		// 2) translate mail subject and add to message
		// 3) render the template and add to message
		// 4) append any additional preparators
		this.preperator = addresses().and(subject(messageSource))
			.and(template(templateEngine))
			.and(Preperator.aggregate(additionalPreparators));
	}

	@Override
	public void send(@NonNull Mail mail) {
		if (log.isDebugEnabled()) {
			log.debug("Sending {} using SMTP Java Sender", mail);
		}

		sender.send(mime -> {
			final MimeMessageHelper helper = new MimeMessageHelper(mime, mail.encoding().name());

			try {
				preperator.prepare(mail, helper);
			}
			catch (MailException e) {
				throw e;
			}
			catch (Exception e) {
				throw new MailPreparationException("Unexpected exception occurred while preparing your"
						+ " SMTP Mime Message with subject: " + mail.subject().value(), e);
			}
		});
	}

	static Preperator<@NonNull MimeMessageHelper> addresses() {
		return (mail, helper) -> {
			for (var recipient : mail.recipients()) {
				if (Recipient.Type.TO == recipient.type()) {
					helper.addTo(toInternetAddress(recipient.address()));
				}
				if (Recipient.Type.CC == recipient.type()) {
					helper.addCc(toInternetAddress(recipient.address()));
				}
				if (Recipient.Type.BCC == recipient.type()) {
					helper.addBcc(toInternetAddress(recipient.address()));
				}
			}

			if (mail.from() != null) {
				helper.setFrom(toInternetAddress(mail.from()));
			}

			for (var replyTo : mail.replyTo()) {
				helper.setReplyTo(toInternetAddress(replyTo));
			}

			return helper;
		};
	};

	static Preperator<@NonNull MimeMessageHelper> subject(MessageSource messageSource) {
		Assert.notNull(messageSource, "Mail Message Source can not be null");

		return (mail, helper) -> {
			final Subject subject = mail.subject();
			String message;

			try {
				message = messageSource.getMessage(subject.toResolvable(), mail.locale());
			}
			catch (Exception e) {
				log.warn("Failed to lookup Mail message subject for code '{}', using default subject value instead.",
						subject.value(), e);
				message = null;
			}

			if (message == null) {
				message = subject.value();
			}

			helper.setSubject(message);

			return helper;
		};
	}

	static Preperator<@NonNull MimeMessageHelper> template(TemplateEngine engine) {
		Assert.notNull(engine, "Template engine can not be null");

		return (mail, helper) -> {
			final Template template;

			try {
				template = engine.render(mail);
			}
			catch (IOException e) {
				throw new MailPreparationException("Fail to render Mail template '" + mail.template() + "'.", e);
			}

			if (Template.HTML.isCompatibleWith(template.contentType())) {
				helper.setText(template.contents(), true);
			}
			else if (Template.TEXT.isCompatibleWith(template.contentType())) {
				helper.setText(template.contents(), false);
			}
			else {
				throw new MailPreparationException("Unsupported template content type: " + template.contentType());
			}

			return helper;
		};
	}

	static Preperator<@NonNull MimeMessageHelper> sender(String email, String name) {
		Assert.hasText(email, "Default mail sender email address can not be null");

		return sender(new Address(email, name));
	}

	static Preperator<@NonNull MimeMessageHelper> sender(Address defaultSender) {
		Assert.notNull(defaultSender, "Default mail sender address can not be null");

		return (mail, helper) -> {
			if (mail.from() == null) {
				helper.setFrom(toInternetAddress(defaultSender));
			}

			return helper;
		};
	}

	static InternetAddress toInternetAddress(@NonNull Address address) {
		final EmailValidationResult result = JMail.validate(address.email());

		if (result.isFailure()) {
			throw new MailPreparationException("Address validation failed with reason: " + result.getFailureReason());
		}

		try {
			return new InternetAddress(address.email(), address.name());
		}
		catch (UnsupportedEncodingException e) {
			try {
				return new InternetAddress(address.email(), true);
			}
			catch (AddressException ex) {
				throw new MailPreparationException("Failed to validate internet address", ex);
			}
		}
	}

}
