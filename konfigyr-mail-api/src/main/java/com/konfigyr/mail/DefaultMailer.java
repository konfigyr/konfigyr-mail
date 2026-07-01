package com.konfigyr.mail;

import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default {@link Mailer} implementation that composes a {@link TemplateEngine} and a {@link Transport}.
 * <p>
 * Instantiate via {@link Mailer#of(TemplateEngine, Transport)}.
 *
 * @author Vladimir Spasic
 * @since 1.0.0
 **/
@NullMarked
final class DefaultMailer implements Mailer {

	private static final Logger log = LoggerFactory.getLogger(DefaultMailer.class);

	private final TemplateEngine engine;
	private final Transport transport;

	DefaultMailer(TemplateEngine engine, Transport transport) {
		this.engine = engine;
		this.transport = transport;
	}

	@Override
	public void send(Mail mail) {
		log.debug("Sending mail with template '{}' to {} recipient(s)", mail.template(), mail.recipients().size());

		final Template template;

		try {
			template = engine.render(mail);
		} catch (MailingException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new MailingException(
				MailingException.ErrorCode.TEMPLATE_RENDERING_FAILED,
				"Failed to render mail template '" + mail.template() + "'",
				ex
			);
		}

		try {
			transport.send(mail, template);
		} catch (MailingException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new MailingException(
				MailingException.ErrorCode.SEND_FAILED,
				"Unexpected error while sending email with template '" + mail.template() + "'",
				ex
			);
		}
	}

}
