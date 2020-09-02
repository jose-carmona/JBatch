package es.jose.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.jboss.weld.junit5.WeldInitiator;

import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.EntityManagerFactory;

import javax.enterprise.context.ApplicationScoped;

@ExtendWith(WeldJunit5Extension.class)
public class EntityTest {

    private static final Logger logger = Logger.getLogger("EntityTest");

    private EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("test");

    @WeldSetup
    public WeldInitiator weld = WeldInitiator
                .fromTestPackage()
                .activate(ApplicationScoped.class)
                .setPersistenceContextFactory(ip -> entityManagerFactory.createEntityManager())
                .inject(this)
                .build();

    @PersistenceContext
    private EntityManager em;

    private EntityTransaction tx;

    @BeforeEach
    public void before_each_test_init_transaction() {
        tx = em.getTransaction();
        tx.begin();
    }

    @AfterEach
    public void after_each_test_rollback_transaction() {
        tx.rollback();
    }


	@Test
	public void test_EntityManager_is_working() {

        // GIVEN
        // ... we have 3 entities stored in the EntityManager (with 100 max amount and 5 max details)
        EntityFactory factory = new EntityFactory(100, 5);
		em.persist(factory.randomEntity());
		em.persist(factory.randomEntity());
		em.persist(factory.randomEntity());

        // WHEN
        // ... we retrieve all entities from EntityManager
        List<MyEntity> r = em.createQuery("SELECT e FROM MyEntity e", MyEntity.class).getResultList();

        // THEN 
        // ... we must have 3 entities
        assertEquals(3, r.size());

        // and all entities must be correct
        for (MyEntity e : r) {
            EntityTestHelper.entity_must_be_correct(e);
            assertEquals(0,e.getSum()); // all entities must have sum = 0
        }
	}

	@Test
	public void test_we_can_use_java_streams_with_details_entity() {
        logger.log(Level.INFO, "EntityTest:test_I_can_use_java_streams_with_details_entity");

        // GIVEN
        // ... we have a Entity with 3 Details with amount = 100, 101 and 102
        MyEntity e1 = new MyEntity("987654321019");

        Set<Detail>  details = new HashSet<>();
		details.add(new Detail(e1,100));
		details.add(new Detail(e1,101));
		details.add(new Detail(e1,102));
        e1.setDetails(details);

		em.persist(e1);

        // WHEN 
        // ... we find the Entity
        MyEntity r = em.find(MyEntity.class, "987654321019");

        // THEN
        // ... we must find the Entity in EM
        assertNotNull(r);

        // ... and the entity must be correct
        EntityTestHelper.entity_must_be_correct(r);

        // ... and the sum of amount must be 303 
        assertEquals(303, r.getDetails().stream().mapToDouble(d -> d.getAmount()).sum());
	}

}
