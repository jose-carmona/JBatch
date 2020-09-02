package es.jose.batch;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.jboss.weld.junit5.WeldInitiator;

import javax.inject.Inject;
import javax.enterprise.context.ApplicationScoped;

import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.jberet.creation.BatchCDIExtension;

import es.jose.entities.MyEntity;
import es.jose.entities.Detail;
import es.jose.entities.EntityTestHelper;
import es.jose.entities.EntityFactory;

@ExtendWith(WeldJunit5Extension.class)
public class EntityReaderTest {
    private static final Logger logger = Logger.getLogger("EntityReaderTest");

    private EntityManager entityManagerToInject = Persistence.createEntityManagerFactory("test").createEntityManager();

	private static final int MAX_TRIES = 40;
    private static final int THREAD_SLEEP = 1000;

    @WeldSetup
    public WeldInitiator weld = WeldInitiator
                .from( 
                    BatchCDIExtension.class,
                    EntityReader.class
                )
                .activate(ApplicationScoped.class)
                .setPersistenceContextFactory(ip -> entityManagerToInject)
                .inject(this)
                .build();

    @PersistenceContext
    private EntityManager em;
    private EntityTransaction tx;

    @Inject
    private EntityReader reader;

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
    public void the_EntityReader_works() throws Exception {
        // GIVEN
        // ... we have 5 stored entities 
        EntityFactory factory = new EntityFactory(100, 5);
        for(int i=0; i<5; i++) {
            em.persist(factory.randomEntity());
        }

        // WHEN 
        // ... we read the all Items
        MyEntity e1;
        Set<MyEntity> r = new HashSet<>();
        reader.open(0);
        while((e1 = (MyEntity)reader.readItem())!=null) {
            r.add(e1);
        }

        // THEN 
        // ... we must have 5 entities as result
        assertEquals(5, r.size());

        // ... and all entities mut be correct
        for (MyEntity e : r) {
            EntityTestHelper.entity_must_be_correct(e);
        }

    }
}