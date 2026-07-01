package com.konfigyr.mail;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.core.NestedRuntimeException;
import org.springframework.util.Assert;

/**
 * Unchecked exception thrown when the {@link Mailer} fails to render or dispatch a {@link Mail}.
 * <p>
 * This is the single exception type callers need to handle regardless of which {@link TemplateEngine}
 * or {@link Transport} implementation is in use. Every failure in the mailing pipeline, template rendering,
 * message preparation, or physical dispatch, is reported through this type, so switching delivery backends
 * does not change error handling code.
 * <p>
 * The {@link ErrorCode} identifies the stage at which the failure occurred and whether a retry is meaningful.
 * The original cause is always preserved and accessible via {@link #getCause()} or {@link #getMostSpecificCause()}.
 *
 * @author Vladimir Spasic
 * @since 1.0.0
 * @see Mailer
 * @see Transport
 **/
@NullMarked
public class MailingException extends NestedRuntimeException {

	/**
	 * Identifies the stage at which a mailing failure occurred, allowing callers to
	 * decide whether to retry, alert, or surface the error to the user without
	 * inspecting the exception message or cause type.
	 */
	public enum ErrorCode {

		/**
		 * The {@link TemplateEngine} could not render the requested template, for
		 * example because the template file was not found, contained an invalid
		 * expression, or was missing a required context variable.
		 * <p>
		 * Retrying without changing the template or its context will not succeed.
		 */
		TEMPLATE_RENDERING_FAILED,

		/**
		 * The message envelope could not be constructed, for example because a
		 * recipient or sender address failed validation, or the rendered content type
		 * is not supported by the transport.
		 * <p>
		 * Retrying without fixing the message data will not succeed.
		 */
		MESSAGE_PREPARATION_FAILED,

		/**
		 * The transport could not authenticate with the delivery infrastructure, for
		 * example due to invalid or expired SMTP credentials, or a rejected API key.
		 * <p>
		 * Retrying without correcting the credentials or token will not succeed.
		 * This typically requires operator intervention.
		 * <p>
		 * Maps to Spring's {@code MailAuthenticationException}.
		 */
		AUTHENTICATION_FAILED,

		/**
		 * The transport could not reach the delivery infrastructure, for example
		 * because the connection was refused, a TLS handshake failed, or the operation
		 * timed out before the server responded.
		 * <p>
		 * This failure is transient; callers may retry with exponential back-off.
		 * <p>
		 * Maps to {@code MailSendException} wrapping a {@code ConnectException} or
		 * {@code SocketTimeoutException}.
		 */
		CONNECTION_FAILED,

		/**
		 * The delivery infrastructure rejected the request because a sending limit was
		 * reached, for example an SMTP server returning 452 or 421, or an HTTP API
		 * returning a rate-limit response.
		 * <p>
		 * This failure is transient; callers should back off and retry after a longer
		 * delay than for {@link #CONNECTION_FAILED}.
		 */
		QUOTA_EXCEEDED,

		/**
		 * The delivery infrastructure refused or failed to transmit the message for a
		 * reason not covered by another code, for example a permanent SMTP 5xx
		 * rejection or an unexpected API error response.
		 * <p>
		 * Whether a retry is meaningful depends on the underlying cause; inspect
		 * {@link MailingException#getCause()} or {@link MailingException#getMostSpecificCause()}
		 * for more details.
		 * <p>
		 * Maps to {@code MailSendException} in cases not classified above.
		 */
		SEND_FAILED

	}

	private final ErrorCode errorCode;

	/**
	 * Creates a new {@link MailingException} with the given message and error code.
	 *
	 * @param errorCode the error code identifying the failure stage; never
	 * @param message the detail message
	 * {@literal null}
	 */
	public MailingException(ErrorCode errorCode, String message) {
		this(errorCode, message, null);
	}

	/**
	 * Creates a new {@link MailingException} with the given message, error code and
	 * cause.
	 *
	 * @param message the detail message
	 * @param errorCode the error code identifying the failure stage; never
	 * {@literal null}
	 * @param cause the underlying cause, may be {@literal null}
	 */
	public MailingException(ErrorCode errorCode, String message, @Nullable Throwable cause) {
		super(message, cause);
		Assert.notNull(errorCode, "MailingException ErrorCode must not be null");
		this.errorCode = errorCode;
	}

	/**
	 * Returns the error code that identifies the stage at which this failure occurred.
	 *
	 * @return the error code; never {@literal null}
	 */
	public ErrorCode getErrorCode() {
		return errorCode;
	}

}
