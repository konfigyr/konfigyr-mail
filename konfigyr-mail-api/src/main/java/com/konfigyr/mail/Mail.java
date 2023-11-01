package com.konfigyr.mail;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Immutable mail object with all necessary data for an effective mailing action.
 * <p>
 * You can send this message using the {@link Mailer} interface.
 * <p>
 * The template for the message is rendered using the {@link TemplateEngine} where the
 * configured message template is loaded and message attributes are used as render
 * context.
 * <p>
 * You can specify a {@link Locale} which would be used to translate the message subject
 * and messages inside the rendered {@link Template}. If no language is specified, the one
 * from Spring {@link LocaleContextHolder} would be used.
 *
 * @param subject the mail subject
 * @param template name of the template to be rendered
 * @param attributes template rendering context attributes
 * @param recipients the mail recipients
 * @param from specifies the sender of the mail
 * @param replyTo reply-to address
 * @param encoding mail contents character encoding
 * @param locale language to be used by the subject and template
 * @author : Vladimir Spasic
 * @since : 31.10.23, Tue
 **/
public record Mail(@NonNull Subject subject, @NonNull String template, @NonNull Map<String, Object> attributes,
		@NonNull Set<Recipient> recipients, @Nullable Address from, @NonNull Set<Address> replyTo,
		@NonNull Charset encoding, @NonNull Locale locale) implements Serializable {

	@Serial
	private static final long serialVersionUID = -2629706208245513082L;

	/**
	 * Creates a new {@link Builder Mail Builder} instance where you can easily create new
	 * {@link Mail} instances.
	 * @return mail builder
	 */
	@NonNull
	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {

		private Subject subject;

		private String template;

		private Address from;

		private Charset encoding;

		private Locale locale;

		private final Map<String, Object> attributes = new LinkedHashMap<>();

		private final Set<Address> replyTo = new LinkedHashSet<>();

		private final Set<Recipient> recipients = new LinkedHashSet<>();

		private Builder() {
		}

		/**
		 * Sets the subject of this mail.
		 * @param subject subject message source code or a default value
		 * @param arguments subject message source arguments
		 * @return builder instance
		 */
		@NonNull
		public Builder subject(String subject, Object... arguments) {
			return subject(new Subject(subject, arguments));
		}

		/**
		 * Sets the subject of this mail.
		 * @param subject subject to be used
		 * @return builder instance
		 */
		@NonNull
		public Builder subject(Subject subject) {
			this.subject = subject;
			return this;
		}

		/**
		 * Sets the name of the template that would be rendered by the
		 * {@link TemplateEngine} when this mail is sent.
		 * @param template template name for the mail
		 * @return builder instance
		 */
		@NonNull
		public Builder template(String template) {
			this.template = template;
			return this;
		}

		/**
		 * Specify the context attribute for the {@link TemplateEngine} when it evaluates
		 * the mail template.
		 * @param key attribute key
		 * @param value attribute value
		 * @return builder instance
		 */
		@NonNull
		public Builder attribute(String key, Object value) {
			this.attributes.put(key, value);
			return this;
		}

		/**
		 * Specify the context attributes for the {@link TemplateEngine} when it evaluates
		 * the mail template.
		 * @param attributes additional attributes map
		 * @return builder instance
		 */
		@NonNull
		public Builder attributes(Map<String, Object> attributes) {
			if (attributes != null) {
				attributes.forEach(this::attribute);
			}
			return this;
		}

		/**
		 * Defines the character encoding of the mail.
		 * @param encoding encoding name
		 * @return builder instance
		 */
		@NonNull
		public Builder encoding(String encoding) {
			return encoding(Charset.forName(encoding));
		}

		/**
		 * Defines the character encoding of the mail.
		 * @param encoding character set
		 * @return builder instance
		 */
		@NonNull
		public Builder encoding(Charset encoding) {
			this.encoding = encoding;
			return this;
		}

		/**
		 * Sets the sender email address for this mail.
		 * @param email sender email address
		 * @return builder instance
		 */
		@NonNull
		public Builder from(String email) {
			return from(email, null);
		}

		/**
		 * Sets the sender email address and display name for this mail.
		 * @param email sender email address
		 * @param name sender name
		 * @return builder instance
		 */
		@NonNull
		public Builder from(String email, String name) {
			return from(new Address(email, name));
		}

		/**
		 * Sets the sender address for this mail.
		 * @param from sender address
		 * @return builder instance
		 */
		@NonNull
		public Builder from(Address from) {
			this.from = from;
			return this;
		}

		/**
		 * Adds the email address of the primary recipient for this mail.
		 * @param email recipient email address
		 * @return builder instance
		 */
		@NonNull
		public Builder to(String email) {
			return recipient(Recipient.to(email));
		}

		/**
		 * Adds the email address and name of the primary recipient for this mail.
		 * @param email recipient email address
		 * @param name recipient name
		 * @return builder instance
		 */
		@NonNull
		public Builder to(String email, String name) {
			return recipient(Recipient.to(email, name));
		}

		/**
		 * Adds the email address of the carbon-copy recipient for this mail.
		 * @param email recipient email address
		 * @return builder instance
		 */
		@NonNull
		public Builder cc(String email) {
			return recipient(Recipient.cc(email));
		}

		/**
		 * Adds the email address and name of the carbon-copy recipient for this mail.
		 * @param email recipient email address
		 * @param name recipient name
		 * @return builder instance
		 */
		@NonNull
		public Builder cc(String email, String name) {
			return recipient(Recipient.cc(email, name));
		}

		/**
		 * Adds the email address of the blind carbon-copy recipient for this mail.
		 * @param email recipient email address
		 * @return builder instance
		 */
		@NonNull
		public Builder bcc(String email) {
			return recipient(Recipient.bcc(email));
		}

		/**
		 * Adds the email address and name of the blind carbon-copy recipient for this
		 * mail.
		 * @param email recipient email address
		 * @param name recipient name
		 * @return builder instance
		 */
		@NonNull
		public Builder bcc(String email, String name) {
			return recipient(Recipient.bcc(email, name));
		}

		/**
		 * Adds the recipient for this mail.
		 * @param recipient recipient address and type
		 * @return builder instance
		 */
		@NonNull
		public Builder recipient(Recipient recipient) {
			if (recipient != null) {
				this.recipients.add(recipient);
			}

			return this;
		}

		/**
		 * Adds additional recipients for this mail.
		 * @param recipients recipient to be added
		 * @return builder instance
		 */
		@NonNull
		public Builder recipients(Recipient... recipients) {
			return recipients(Arrays.stream(recipients)::iterator);
		}

		/**
		 * Adds additional recipients for this mail.
		 * @param recipients recipient to be added
		 * @return builder instance
		 */
		@NonNull
		public Builder recipients(Iterable<Recipient> recipients) {
			for (var recipient : recipients) {
				recipient(recipient);
			}

			return this;
		}

		/**
		 * Adds the email address of the reply-to for this mail.
		 * <p>
		 * If provided, email clients should prioritize the <em>replyTo</em> recipient
		 * over the <em>from</em> recipient when replying to this email.
		 * @param email recipient email address
		 * @return builder instance
		 */
		@NonNull
		public Builder replyTo(String email) {
			return replyTo(email, null);
		}

		/**
		 * Adds the email address and display name of the reply-to for this mail.
		 * <p>
		 * If provided, email clients should prioritize the <em>replyTo</em> recipient
		 * over the <em>from</em> recipient when replying to this email.
		 * @param email reply-to email address
		 * @param name reply-to display name
		 * @return builder instance
		 */
		@NonNull
		public Builder replyTo(String email, String name) {
			return replyTo(new Address(email, name));
		}

		/**
		 * Adds the addresses that should be used as the reply-to for this mail.
		 * <p>
		 * If provided, email clients should prioritize the <em>replyTo</em> recipient
		 * over the <em>from</em> recipient when replying to this email.
		 * @param addresses reply-to addresses
		 * @return builder instance
		 */
		@NonNull
		public Builder replyTo(Address... addresses) {
			return replyTo(Arrays.stream(addresses)::iterator);
		}

		/**
		 * Adds the addresses that should be used as the reply-to for this mail.
		 * <p>
		 * If provided, email clients should prioritize the <em>replyTo</em> recipient
		 * over the <em>from</em> recipient when replying to this email.
		 * @param addresses reply-to addresses
		 * @return builder instance
		 */
		@NonNull
		public Builder replyTo(Iterable<Address> addresses) {
			for (var address : addresses) {
				if (address != null) {
					replyTo.add(address);
				}
			}
			return this;
		}

		/**
		 * Sets the {@link Locale language} for this mail. If not specified the language
		 * from the {@link LocaleContextHolder} would be used instead.
		 * @param locale locale to be used
		 * @return builder instance
		 */
		@NonNull
		public Builder locale(Locale locale) {
			this.locale = locale;
			return this;
		}

		/**
		 * Creates a new immutable {@link Mail} instance from this builder configuration.
		 * @throws IllegalArgumentException when required configuration is missing
		 * @return resulting mail instance, never {@literal null}
		 */
		@NonNull
		public Mail build() {
			Assert.notNull(subject, "Mail needs to have a subject set");
			Assert.hasText(template, "Mail needs to have a template set");
			Assert.notEmpty(recipients, "Mail needs to have at least one recipient");

			if (locale == null) {
				locale = LocaleContextHolder.getLocale();
			}

			if (encoding == null) {
				encoding = StandardCharsets.UTF_8;
			}

			return new Mail(subject, template, Collections.unmodifiableMap(attributes),
					Collections.unmodifiableSet(recipients), from, Collections.unmodifiableSet(replyTo), encoding,
					locale);
		}

	}
}
