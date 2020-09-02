package es.jose.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.Set;
import java.util.HashSet;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.WeldInitiator;

import es.jose.entities.MyEntity;
import es.jose.entities.Detail;
import es.jose.entities.EntityFactory;

@EnableAutoWeld
@AddBeanClasses(ComputeSumProcessor.class)
public class ComputeSumProcessorTest {

    private static final Logger logger = Logger.getLogger("ComputeSumProcessorTest");
	
	@Inject
	private ComputeSumProcessor processor;

    @Test
    public void test_ComputeSumProcessor_works() {
        // GIVEN
        // ... and we have an Entity
        EntityFactory factory = new EntityFactory(100, 5);
        MyEntity e = new MyEntity(factory.randomIdEntity());

        // ... and the Entity have 3 detail of 0.01
        Set<Detail> details = new HashSet<>();
        details.add(new Detail(e, 0.01));
        details.add(new Detail(e, 0.01));
        details.add(new Detail(e, 0.01));
        e.setDetails(details);

        // WHEN
        // ... the ComputeSumProcessorTest is executed
        MyEntity r = processor.processItem(e);

        // THEN
        // ... the sum must be 0.03
        assertEquals(0.03, r.getSum());
	}
}