package com.konfigyr.mail;

import org.jspecify.annotations.NullMarked;

/**
 * Application-facing interface for sending a {@link Mail}.
 * <p>
 * This is the primary entry point for sending mail from application code. It accepts a
 * {@link Mail} that carries the template name, rendering context, and envelope metadata
 * (recipients, subject, encoding, etc.), and is responsible for the full pipeline:
 * rendering the template and dispatching the message.
 * <p>
 * Use {@link #of(TemplateEngine, Transport)} to compose a {@code Mailer} from a
 * {@link TemplateEngine} and a {@link Transport}. The engine renders the template body;
 * the transport dispatches the fully-prepared message. This is the canonical way to wire
 * up a {@code Mailer} when you are providing a custom delivery backend.
 *
 * @author Vladimir Spasic
 * @since 1.0.0
 * @see Transport
 * @see TemplateEngine
 **/
@NullMarked
public interface Mailer {

	/**
	 * Sends out a {@link Mail}.
	 *
	 * @param mail mail message to be sent, can't be {@literal null}
	 */
	void send(Mail mail);

	/**
	 * Creates a {@link Mailer} that renders the template with the given {@link TemplateEngine} and
	 * then dispatches the result via the given {@link Transport}.
	 * <p>
	 * This is the canonical composition point for adding a new delivery backend: supply your {@link Transport}
	 * implementation here and receive a fully functional {@link Mailer} without implementing the
	 * rendering pipeline yourself.
	 *
	 * @param engine    the template engine used to render the mail body; never {@literal null}
	 * @param transport the transport used to dispatch the rendered message; never {@literal null}
	 * @return a new {@link Mailer} instance; never {@literal null}
	 */
	static Mailer of(TemplateEngine engine, Transport transport) {
		return new DefaultMailer(engine, transport);
	}

}
