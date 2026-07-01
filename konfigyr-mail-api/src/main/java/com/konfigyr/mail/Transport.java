package com.konfigyr.mail;

import org.jspecify.annotations.NullMarked;

/**
 * Interface that defines how a fully-rendered {@link Mail} should be dispatched to its
 * recipients.
 * <p>
 * {@code Transport} occupies a single, narrow responsibility in the mail pipeline: given
 * an already-rendered {@link Template}, construct the protocol-level message envelope
 * and hand it off to the underlying mail infrastructure. It intentionally knows nothing
 * about template resolution or rendering, those concerns belong to {@link TemplateEngine}.
 * <p>
 * The separation exists so that the rendering pipeline and the delivery backend can
 * vary independently. A new delivery channel (SMTP, SendGrid HTTP API, AWS SES, Mailgun,
 * etc.) only needs to implement this interface; it automatically gets template rendering
 * for free when composed with a {@link TemplateEngine} through a {@link Mailer}.
 *
 * <h2>Relationship to {@link Mailer}</h2>
 * <p>
 * {@link Mailer} is the application-facing API. Use it to send mail. {@code Transport}
 * is the SPI for delivery backends. Implement it when you need to add a new channel
 * without re-implementing the rendering pipeline. The two are composed by
 * {@link Mailer#of(TemplateEngine, Transport)}.
 *
 * <h2>Subject localization</h2>
 * <p>
 * Resolving the {@link Mail#subject()} code via a
 * {@link org.springframework.context.MessageSource} is transport-specific and should be
 * handled by the implementation when needed. When no message source is available,
 * {@link Subject#value()} provides the raw subject string as a safe fallback.
 *
 * <h2>Thread safety</h2>
 * <p>
 * Implementations must be thread-safe; they are typically registered as singleton Spring
 * beans and may be invoked concurrently.
 *
 * @author Vladimir Spasic
 * @since 1.0.0
 * @see Mailer
 * @see TemplateEngine
 **/
@NullMarked
public interface Transport {

	/**
	 * Dispatches the given {@link Mail} using the fully-rendered {@link Template}.
	 * <p>
	 * At the point this method is called the template has already been rendered by a {@link TemplateEngine}.
	 * The implementation is responsible for mapping the {@link Mail} envelope fields like recipients, sender,
	 * subject, encoding, etc. and the rendered template body to the underlying transport protocol.
	 * <p>
	 * Implementations must wrap all internal failures in a {@link MailingException} with
	 * an appropriate {@link MailingException.ErrorCode}:
	 * <ul>
	 *   <li>{@link MailingException.ErrorCode#MESSAGE_PREPARATION_FAILED}: invalid
	 *       address, unsupported content type, or any other envelope construction error</li>
	 *   <li>{@link MailingException.ErrorCode#AUTHENTICATION_FAILED}: invalid or expired credentials</li>
	 *   <li>{@link MailingException.ErrorCode#CONNECTION_FAILED}: server unreachable, TLS failure, or timeout</li>
	 *   <li>{@link MailingException.ErrorCode#QUOTA_EXCEEDED}: rate-limit or sending quota exceeded</li>
	 *   <li>{@link MailingException.ErrorCode#SEND_FAILED}: all other transport rejections</li>
	 * </ul>
	 * This ensures callers of {@link Mailer} only ever need to handle a single exception
	 * type regardless of which transport is in use.
	 *
	 * @param mail mail to be dispatched, including envelope metadata; never {@literal null}
	 * @param template the fully-rendered template body and its content type; never {@literal null}
	 * @throws MailingException if message preparation or dispatch fails
	 */
	void send(Mail mail, Template template);

}
