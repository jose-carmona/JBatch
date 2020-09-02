package es.jose.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

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

import org.junit.jupiter.api.Tag;

@Tag("wip")
@ExtendWith(WeldJunit5Extension.class)
public class DetailTest {
    private static final Logger logger = Logger.getLogger("DetailTest");

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
        // ... we have any Entity with 3 details persisting in the EntityManager
        MyEntity e1 = new MyEntity("987654321019");
		em.persist(e1);
		em.persist(new Detail(e1,100));
		em.persist(new Detail(e1,100));
		em.persist(new Detail(e1,100));

        // WHEN
        // ... we find all details 
        List<Detail> r = em.createQuery("SELECT d FROM Detail d", Detail.class).getResultList();

        // THEN
        // .... we must have 3 details
        assertEquals(3, r.size());

        // ... and all details must have de same Id and amount
        for (Detail d : r) {
            assertEquals(100, d.getAmount());
            assertEquals("987654321019", d.getEntity().getId());
        }

	}

}
