package es.jose;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;

import org.jboss.weld.junit5.auto.EnableAutoWeld;

import es.jose.Foo;

@EnableAutoWeld
public class WeldTest {
	
	@Inject
	public Foo foo;
	
    @Test
    public void test_given_CDI_is_working() {
		assertEquals("Hello World!", foo.getBar());
	}
}