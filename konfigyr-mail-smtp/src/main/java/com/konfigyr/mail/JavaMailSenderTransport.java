package com.konfigyr.mail;

import com.sanctionco.jmail.EmailValidationResult;
import com.sanctionco.jmail.JMail;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.Assert;

import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

/**
 * {@link Transport} implementation that dispatches mail via SMTP using Spring's {@link JavaMailSender}.
 *
 * @author Vladimir Spasic
 * @since 1.0.0
 **/
@NullMarked
class JavaMailSenderTransport implements Transport {

	private static final Logger log = LoggerFactory.getLogger(JavaMailSenderTransport.class);

	private final JavaMailSender sender;
	private final Preparator<MimeMessageHelper> preparator;

	/**
	 * Creates a {@link JavaMailSenderTransport} that chains address setup, subject
	 * resolution, and any additional preparators in that order.
	 *
	 * @param sender the Spring {@link JavaMailSender} used to dispatch messages
	 * @param messageSource used to resolve and translate the mail subject
	 * @param additionalPreparators optional extra preparators appended at the end of the chain
	 */
	JavaMailSenderTransport(
		JavaMailSender sender,
		MessageSource messageSource,
		Iterable<Preparator<MimeMessageHelper>> additionalPreparators
	) {
		this.sender = sender;
		this.preparator = addresses()
			.and(subject(messageSource))
			.and(Preparator.aggregate(additionalPreparators));
	}

	@Override
	public void send(Mail mail, Template template) {
		log.debug("Dispatching mail with template '{}' via SMTP", mail.template());

		try {
			sender.send(mime -> {
				final MimeMessageHelper helper = new MimeMessageHelper(mime, mail.encoding().name());

				try {
					preparator.and(body(template)).prepare(mail, helper);
				} catch (MailException ex) {
					throw ex;
				} catch (Exception ex) {
					throw new MailPreparationException("Unexpected error while preparing mail message", ex);
				}
			});
		} catch (MailAuthenticationException ex) {
			throw new MailingException(
				MailingException.ErrorCode.AUTHENTICATION_FAILED,
				"SMTP authentication failed",
				ex
			);
		} catch (MailPreparationException | MailParseException ex) {
			throw new MailingException(
				MailingException.ErrorCode.MESSAGE_PREPARATION_FAILED,
				"Failed to prepare mail message for template '" + mail.template() + "'",
				ex
			);
		} catch (MailSendException ex) {
			throw new MailingException(
				isConnectionError(ex)
					? MailingException.ErrorCode.CONNECTION_FAILED
					: MailingException.ErrorCode.SEND_FAILED,
				"Failed to send mail via SMTP",
				ex
			);
		} catch (MailException ex) {
			throw new MailingException(
				MailingException.ErrorCode.SEND_FAILED,
				"Failed to send mail via SMTP",
				ex
			);
		}
	}

	static Preparator<MimeMessageHelper> addresses() {
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
	}

	static Preparator<MimeMessageHelper> subject(MessageSource messageSource) {
		Assert.notNull(messageSource, "Mail Message Source can not be null");

		return (mail, helper) -> {
			final Subject subject = mail.subject();
			String message;

			try {
				message = messageSource.getMessage(subject.toResolvable(), mail.locale());
			} catch (Exception ex) {
				log.warn("Failed to lookup Mail message subject for code '{}', using default subject value instead.",
						subject.value(), ex);
				message = null;
			}

			if (message == null) {
				message = subject.value();
			}

			helper.setSubject(message);

			return helper;
		};
	}

	static Preparator<MimeMessageHelper> body(Template template) {
		if (Template.HTML.isCompatibleWith(template.contentType())) {
			return (mail, helper) -> {
				helper.setText(template.contents(), true);
				return helper;
			};
		}
		if (Template.TEXT.isCompatibleWith(template.contentType())) {
			return (mail, helper) -> {
				helper.setText(template.contents(), false);
				return helper;
			};
		}
		throw new MailPreparationException("Unsupported template content type: " + template.contentType());
	}

	static Preparator<MimeMessageHelper> sender(String email, String name) {
		Assert.hasText(email, "Default mail sender email address can not be null");

		return sender(new Address(email, name));
	}

	static Preparator<MimeMessageHelper> sender(Address defaultSender) {
		Assert.notNull(defaultSender, "Default mail sender address can not be null");

		return (mail, helper) -> {
			if (mail.from() == null) {
				helper.setFrom(toInternetAddress(defaultSender));
			}

			return helper;
		};
	}

	static InternetAddress toInternetAddress(Address address) {
		final EmailValidationResult result = JMail.validate(address.email());

		if (result.isFailure()) {
			throw new MailPreparationException("Address validation failed with reason: " + result.getFailureReason());
		}

		try {
			return new InternetAddress(address.email(), address.name());
		} catch (UnsupportedEncodingException ignore) {
			try {
				return new InternetAddress(address.email(), true);
			} catch (AddressException ex) {
				throw new MailPreparationException("Failed to validate internet address", ex);
			}
		}
	}

	private static boolean isConnectionError(MailSendException ex) {
		final Throwable cause = ex.getMostSpecificCause();
		return cause instanceof ConnectException || cause instanceof SocketTimeoutException;
	}

}
