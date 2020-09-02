package es.jose.entities;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EntityTestHelper {
    private static final Logger logger = Logger.getLogger("EntityTestHelper");

    public static void detail_must_be_correct(Detail d) {
        assertNotNull(d.getEntity());
        assertTrue(d.getAmount() > 0);
    }

    public static void entity_must_be_correct(MyEntity e) {
        assertNotNull(e.getId());
        assertTrue(e.getId().matches("[0-9]{12}"), "Id = " + e.getId() + " must match with [0-9]{12}");
        String cdDeuda = e.getId();
        for (Detail d : e.getDetails()) { 
            detail_must_be_correct(d);
        }
    }
}