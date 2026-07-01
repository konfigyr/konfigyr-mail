package com.konfigyr.mail.test;

import com.konfigyr.mail.Template;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.util.MimeType;

import static org.assertj.core.api.Assertions.*;

class TemplateAssertTest {

	final Template html = Template.html("<h1>Hello World</h1>");
	final Template text = Template.text("Hello World");

	@Test
	@DisplayName("should assert HTML template")
	void shouldAssertHtmlTemplate() {
		TemplateAssert.assertThat(html)
			.isHtml()
			.hasContentType(Template.HTML)
			.hasContents("<h1>Hello World</h1>")
			.contentsContain("Hello World")
			.contentsContain("<h1>");
	}

	@Test
	@DisplayName("should assert plain text template")
	void shouldAssertTextTemplate() {
		TemplateAssert.assertThat(text)
			.isText()
			.hasContentType(Template.TEXT)
			.hasContents("Hello World")
			.contentsContain("Hello");
	}

	@Test
	@DisplayName("should assert template with custom content type")
	void shouldAssertTemplateWithCustomContentType() {
		final var contentType = MimeType.valueOf("text/markdown");
		final var markdown = new Template("# Hello", contentType);

		TemplateAssert.assertThat(markdown)
			.hasContentType(contentType)
			.hasContents("# Hello")
			.contentsContain("Hello");
	}

	@Test
	@DisplayName("should extract template contents as string assert")
	void shouldExtractTemplateContents() {
		TemplateAssert.assertThat(html)
			.extractingContents()
			.startsWith("<h1>")
			.endsWith("</h1>")
			.contains("Hello World");
	}

	@Test
	@DisplayName("should use assert factory")
	void shouldUseAssertFactory() {
		assertThat(html)
			.asInstanceOf(TemplateAssert.factory())
			.isHtml()
			.contentsContain("<h1>Hello World</h1>");
	}

	@Test
	@DisplayName("should fail to assert template")
	void shouldFailToAssertTemplate() {
		final var htmlAssert = assertThat(html).asInstanceOf(TemplateAssert.factory());
		final var textAssert = assertThat(text).asInstanceOf(TemplateAssert.factory());

		assertThatExceptionOfType(AssertionError.class)
			.isThrownBy(textAssert::isHtml)
			.withMessageContaining("Expected template to have HTML content type but was text/plain");

		assertThatExceptionOfType(AssertionError.class)
			.isThrownBy(htmlAssert::isText)
			.withMessageContaining("Expected template to have plain text content type but was text/html");

		assertThatExceptionOfType(AssertionError.class)
			.isThrownBy(() -> htmlAssert.hasContentType(Template.TEXT))
			.withMessageContaining("Expected template to have content type text/plain but was text/html");

		assertThatExceptionOfType(AssertionError.class)
			.isThrownBy(() -> htmlAssert.hasContents("different content"))
			.withMessageContaining("Template contents");

		assertThatExceptionOfType(AssertionError.class)
			.isThrownBy(() -> htmlAssert.contentsContain("missing text"))
			.withMessageContaining("Template contents");
	}

	@Test
	@DisplayName("should fail to assert null template")
	void shouldFailToAssertNullTemplate() {
		assertThatExceptionOfType(AssertionError.class)
			.isThrownBy(() -> TemplateAssert.assertThat(null).isHtml());

		assertThatExceptionOfType(AssertionError.class)
			.isThrownBy(() -> TemplateAssert.assertThat(null).isText());

		assertThatExceptionOfType(AssertionError.class)
			.isThrownBy(() -> TemplateAssert.assertThat(null).hasContentType(Template.HTML));

		assertThatExceptionOfType(AssertionError.class)
			.isThrownBy(() -> TemplateAssert.assertThat(null).hasContents("content"));

		assertThatExceptionOfType(AssertionError.class)
			.isThrownBy(() -> TemplateAssert.assertThat(null).contentsContain("content"));

		assertThatExceptionOfType(AssertionError.class)
			.isThrownBy(() -> TemplateAssert.assertThat(null).extractingContents());
	}

}
