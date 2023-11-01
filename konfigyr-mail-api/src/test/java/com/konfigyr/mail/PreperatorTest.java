package com.konfigyr.mail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author : Vladimir Spasic
 * @since : 31.10.23, Tue
 **/
@ExtendWith(MockitoExtension.class)
class PreperatorTest {

	@Mock
	Mail mail;

	@Test
	@DisplayName("should chain preparators")
	void shouldChainPreparators() throws Exception {
		final var preparator = preperatorAppend(";1").and(preperatorAppend(";2"))
			.and(Preperator.noop())
			.and(Preperator.noop())
			.and(preperatorAppend(";3"))
			.and(preperatorAppend(";4"))
			.and(Preperator.noop())
			.and(preperatorAppend(";5"));

		assertThat(preparator.prepare(mail, "0")).isEqualTo("0;1;2;3;4;5");

	}

	@Test
	@DisplayName("should aggregate preparators")
	void shouldAggregatePreparators() throws Exception {
		final var preparator = Preperator
			.aggregate(List.of(preperatorAppend(";1"), preperatorAppend(";2"), Preperator.noop(),
					preperatorAppend(";3"), preperatorAppend(";4"), preperatorAppend(";5"), Preperator.noop()));

		assertThat(preparator.prepare(mail, "0")).isEqualTo("0;1;2;3;4;5");

		assertThat(Preperator.aggregate((Iterable<Preperator<String>>) null).prepare(mail, "target"))
			.isEqualTo("target");

		assertThat(Preperator.aggregate((Stream<Preperator<String>>) null).prepare(mail, "target")).isEqualTo("target");
	}

	static Preperator<String> preperatorAppend(String value) {
		return (mail, target) -> target + value;
	}

}