package es.jose.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class EntityFactoryTest {

    private static final Logger logger = Logger.getLogger("EntityFactoryTest");

	@Test
	public void test_EntityFactory_generate_corrects_entities() {
        // GIVEN
        // ... we have a EntityFactory for 100 max amount and 5 max details
        EntityFactory factory = new EntityFactory(100, 5);

        // WHEN
        // ... we generate a high number of entities (100 entities)
        Set<MyEntity> entities = new HashSet<>();
        for(int i=0; i<100; i++) {
            entities.add(factory.randomEntity());
        }

        // THEN
        // ... all generated entities must be corrects
        for(MyEntity e : entities) {
            EntityTestHelper.entity_must_be_correct(e);
        }

	}

    @ParameterizedTest
    @CsvSource({"0,0", "1,1", "2,2", "3,0", "4,1", "5,2", 
        "10,1", "11,2", "12,0", "13,1", "14,2", 
        "123456789012,0", "123456789013,1", "123456789014,2", "123456789015,0"})
	public void test_EntityFactory_generate_corrects_partitions(String input, int expected) {
        // GIVEN
        // ... we have a EntityFactory for 100 max amount and 5 max details and 3 partitions
        EntityFactory factory = new EntityFactory(100, 5, 3);

        // THEN
        // ... all generated entities must be corrects
        assertEquals(expected, factory.computePartition(input));
        logger.log(Level.INFO, "OK:  " + input + " --> " + Integer.toString(expected));
	}

}
