package com.konfigyr.mail.test;

import com.konfigyr.mail.Template;
import org.assertj.core.api.*;
import org.assertj.core.error.BasicErrorMessageFactory;
import org.springframework.util.MimeType;

import java.util.Objects;

/**
 * Assert class that should be used to test {@link Template}.
 *
 * @author Vladimir Spasic
 * @since 1.0.0
 */
public final class TemplateAssert extends AbstractObjectAssert<TemplateAssert, Template> {

	/**
	 * Creates a new {@link TemplateAssert} with the given {@link Template} to check.
	 *
	 * @param template the actual value to verify
	 * @return template assert
	 */
	public static TemplateAssert assertThat(Template template) {
		return new TemplateAssert(template);
	}

	/**
	 * Creates an {@link InstanceOfAssertFactory} that can be used to create a {@link TemplateAssert}
	 * for an asserted object.
	 *
	 * @return template assert factory
	 */
	public static InstanceOfAssertFactory<Template, TemplateAssert> factory() {
		return new InstanceOfAssertFactory<>(Template.class, TemplateAssert::new);
	}

	TemplateAssert(Template template) {
		super(template, TemplateAssert.class);
	}

	/**
	 * Checks that the {@link Template} has an HTML content type ({@code text/html}).
	 *
	 * @return the template assert object, never {@literal null}
	 */
	public TemplateAssert isHtml() {
		isNotNull();

		if (!Template.HTML.isCompatibleWith(actual.contentType())) {
			throwAssertionError(new BasicErrorMessageFactory(
					"Expected template to have HTML content type but was %s",
					actual.contentType()
			));
		}

		return myself;
	}

	/**
	 * Checks that the {@link Template} has a plain text content type ({@code text/plain}).
	 *
	 * @return the template assert object, never {@literal null}
	 */
	public TemplateAssert isText() {
		isNotNull();

		if (!Template.TEXT.isCompatibleWith(actual.contentType())) {
			throwAssertionError(new BasicErrorMessageFactory(
					"Expected template to have plain text content type but was %s",
					actual.contentType()
			));
		}

		return myself;
	}

	/**
	 * Checks that the {@link Template} has the given content type.
	 *
	 * @param contentType expected content type
	 * @return the template assert object, never {@literal null}
	 */
	public TemplateAssert hasContentType(MimeType contentType) {
		isNotNull();

		if (!Objects.equals(contentType, actual.contentType())) {
			throwAssertionError(new BasicErrorMessageFactory(
					"Expected template to have content type %s but was %s",
					contentType, actual.contentType()
			));
		}

		return myself;
	}

	/**
	 * Checks that the {@link Template} has exactly the given contents.
	 *
	 * @param contents expected full contents
	 * @return the template assert object, never {@literal null}
	 */
	public TemplateAssert hasContents(String contents) {
		isNotNull();

		Assertions.assertThat(actual.contents())
				.as("Template contents")
				.isEqualTo(contents);

		return myself;
	}

	/**
	 * Checks that the {@link Template} contents contain the given substring.
	 *
	 * @param value expected substring
	 * @return the template assert object, never {@literal null}
	 */
	public TemplateAssert contentsContain(String value) {
		isNotNull();

		Assertions.assertThat(actual.contents())
				.as("Template contents")
				.contains(value);

		return myself;
	}

	/**
	 * Returns an {@link AbstractStringAssert} for the template contents, allowing the full
	 * AssertJ string assertion API to be applied.
	 *
	 * @return string assert for the template contents, never {@literal null}
	 */
	public AbstractStringAssert<?> extractingContents() {
		isNotNull();

		return Assertions.assertThat(actual.contents())
				.as("Template contents");
	}

}
