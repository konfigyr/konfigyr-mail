package com.konfigyr.mail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PreparatorTest {

	@Mock
	Mail mail;

	@Test
	@DisplayName("should chain preparators")
	void shouldChainPreparators() throws Exception {
		final var preparator = preparatorAppend(";1").and(preparatorAppend(";2"))
			.and(Preparator.noop())
			.and(Preparator.noop())
			.and(preparatorAppend(";3"))
			.and(preparatorAppend(";4"))
			.and(Preparator.noop())
			.and(preparatorAppend(";5"));

		assertThat(preparator.prepare(mail, "0")).isEqualTo("0;1;2;3;4;5");

	}

	@Test
	@DisplayName("should aggregate preparators")
	void shouldAggregatePreparators() throws Exception {
		final var preparator = Preparator
			.aggregate(List.of(preparatorAppend(";1"), preparatorAppend(";2"), Preparator.noop(),
					preparatorAppend(";3"), preparatorAppend(";4"), preparatorAppend(";5"), Preparator.noop()));

		assertThat(preparator.prepare(mail, "0")).isEqualTo("0;1;2;3;4;5");

		assertThat(Preparator.aggregate((Iterable<Preparator<String>>) null).prepare(mail, "target"))
			.isEqualTo("target");

		assertThat(Preparator.aggregate((Stream<Preparator<String>>) null).prepare(mail, "target")).isEqualTo("target");
	}

	static Preparator<String> preparatorAppend(String value) {
		return (mail, target) -> target + value;
	}

}
