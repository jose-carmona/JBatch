package es.jose;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class JUnit5Test {

	@Test
	public void test_given_junit5_is_working() {
		String result = "HelloWorld!";
		assertEquals("HelloWorld!", result);
	}

}