package com.konfigyr.mail;

import org.jspecify.annotations.NullMarked;
import org.springframework.util.Assert;
import org.springframework.util.MimeType;

/**
 * Immutable Mail template object that is returned by the {@link TemplateEngine}.
 *
 * @param contents the actual template content to be added the mail message, can't be {@literal blank}
 * @param contentType defines which content type should be used for this template
 * @author Vladimir Spasic
 * @since 1.0.0
 **/
@NullMarked
public record Template(String contents, MimeType contentType) {

	/**
	 * Canonical HTML content type ({@code text/html}).
	 */
	public static final MimeType HTML = MimeType.valueOf("text/html");

	/**
	 * Canonical plain-text content type ({@code text/plain}).
	 */
	public static final MimeType TEXT = MimeType.valueOf("text/plain");

	/**
	 * Creates a new Mail template instance with contents and content type.
	 *
	 * @param contents the actual template content to be added the mail message, can't be {@literal blank}
	 * @param contentType defines which content type should be used for this template
	 * @throws IllegalArgumentException when template contents is blank.
	 */
	public Template {
		Assert.hasText(contents, "Template contents can not be blank");
	}

	/**
	 * Constructs a new HTML {@link Template} instance with given contents.
	 *
	 * @param contents the fully-rendered HTML body string for the message; must not be blank
	 * @return a new {@link Template} with content type {@link #HTML} and the given contents; never {@literal null}
	 * @throws IllegalArgumentException when template contents is blank.
	 */
	public static Template html(String contents) {
		return new Template(contents, HTML);
	}

	/**
	 * Constructs a new plain text {@link Template} instance with given contents.
	 *
	 * @param contents the fully-rendered plain-text body string for the message; must not be blank
	 * @return a new {@link Template} with content type {@link #TEXT} and the given contents; never {@literal null}
	 * @throws IllegalArgumentException when contents is blank.
	 */
	public static Template text(String contents) {
		return new Template(contents, TEXT);
	}

}
