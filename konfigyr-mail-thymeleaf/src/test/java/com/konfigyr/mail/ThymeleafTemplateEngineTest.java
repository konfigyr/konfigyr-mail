package com.konfigyr.mail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author : Vladimir Spasic
 * @since : 31.10.23, Tue
 **/
@SpringBootTest(classes = IntegrationTestConfiguration.class)
class ThymeleafTemplateEngineTest {

	@Autowired
	TemplateEngine engine;

	@Test
	@DisplayName("should render template")
	void shouldRenderEngine() throws Exception {
		final var template = engine.render(Mail.builder()
			.subject("Test subject")
			.template("test-template")
			.attribute("user", "John Doe")
			.to("to@konfigyr.com")
			.build());

		assertThat(template).isNotNull()
			.returns(Template.HTML, Template::contentType)
			.extracting(Template::contents)
			.asString()
			.contains("<h1>Greetings John Doe,</h1>")
			.contains("<p>This is your Thymeleaf test template.</p>");
	}

}