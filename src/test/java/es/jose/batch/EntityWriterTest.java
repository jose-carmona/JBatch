package es.jose.batch;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.ArrayList;
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
public class EntityWriterTest {
    private static final Logger logger = Logger.getLogger("EntityWriterTest");

    private EntityManager entityManagerToInject = Persistence.createEntityManagerFactory("test").createEntityManager();

	private static final int MAX_TRIES = 40;
    private static final int THREAD_SLEEP = 1000;

    @WeldSetup
    public WeldInitiator weld = WeldInitiator
                .from( 
                    BatchCDIExtension.class,
                    EntityWriter.class
                )
                .activate(ApplicationScoped.class)
                .setPersistenceContextFactory(ip -> entityManagerToInject)
                .inject(this)
                .build();

    @PersistenceContext
    private EntityManager em;
    private EntityTransaction tx;

    @Inject
    private EntityWriter writer;

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
        // ... we have a list of 5 entities 
        List<Object> list = new ArrayList<Object>();
        EntityFactory factory = new EntityFactory(100, 5);
        for(int i=0; i<5; i++) {
            list.add(factory.randomEntity());
        }

        // WHEN 
        // ... we write all entities
        writer.writeItems(list);

        // THEN 
        // ... we must read 5 entites from PersistenceContext
        List<MyEntity> r = em.createQuery("SELECT e FROM MyEntity e", MyEntity.class).getResultList();
        assertEquals(5, r.size());

        // ... and all entities mut be correct
        for (MyEntity e : r) {
            EntityTestHelper.entity_must_be_correct(e);
        }

    }
}