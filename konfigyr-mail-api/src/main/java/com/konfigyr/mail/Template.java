package com.konfigyr.mail;

import org.jspecify.annotations.NullMarked;
import org.springframework.util.Assert;
import org.springframework.util.MimeType;

/**
 * Immutable Mail template object that is returned by the {@link TemplateEngine}.
 *
 * @param contents the actual template content to be added the mail message
 * @param contentType defines which content type should be used for this template
 * @author : Vladimir Spasic
 * @since : 31.10.23, Tue
 **/
@NullMarked
public record Template(String contents, MimeType contentType) {

	/**
	 * HTML content type (text/html).
	 */
	public static MimeType HTML = MimeType.valueOf("text/html");

	/**
	 * Plain text content type (text/plain).
	 */
	public static MimeType TEXT = MimeType.valueOf("text/plain");

	public Template {
		Assert.hasText(contents, "Template contents can not be blank");
	}

	/**
	 * Constructs a new HTML {@link Template} instance with given contents.
	 * @param contents template contents
	 * @return HTML template
	 * @throws IllegalArgumentException when contents is blank.
	 */
	public static Template html(String contents) {
		return new Template(contents, HTML);
	}

	/**
	 * Constructs a new plain text {@link Template} instance with given contents.
	 * @param contents template contents
	 * @return plain text template
	 * @throws IllegalArgumentException when contents is blank.
	 */
	public static Template text(String contents) {
		return new Template(contents, TEXT);
	}

}
