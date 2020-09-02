package es.jose.batch;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.jboss.weld.junit5.WeldInitiator;

import javax.inject.Inject;
import javax.enterprise.context.ApplicationScoped;

import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.JobExecution;
import javax.batch.runtime.StepExecution;
import javax.batch.runtime.Metric;
import org.jberet.operations.JobOperatorImpl;
import org.jberet.creation.BatchCDIExtension;

import es.jose.batch.environment.BatchTestEnvironment;
import es.jose.entities.MyEntity;
import es.jose.entities.Detail;
import es.jose.entities.EntityTestHelper;

@ExtendWith(WeldJunit5Extension.class)
public class ReaderProcessorWriterTest {
    private static final Logger logger = Logger.getLogger("ReaderProcessorWriterTest");

    private EntityManager entityManagerToInject = Persistence.createEntityManagerFactory("test").createEntityManager();

	private static final int MAX_TRIES = 40;
    private static final int THREAD_SLEEP = 1000;

    @WeldSetup
    public WeldInitiator weld = WeldInitiator
                .from( 
                    BatchCDIExtension.class,
                    EntityReader.class,
                    ComputeSumProcessor.class,
                    EntityWriter.class,
                    FillEntitiesBatchlet.class
                )
                .activate(ApplicationScoped.class)
                .setPersistenceContextFactory(ip -> entityManagerToInject)
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

    JobOperatorImpl jobImp;

    /*
    We need a standard JEE Batch system. We use JBeret in order to test. JBeret-SE use Weld but we can't
    control it so we can create an JBeret batch + Weld environment to test.
    */ 
    public void PrepareJBeretEnvironment() {
        logger.log(Level.INFO, "new BatchTestEnvironment with existing Weld Container");
        BatchTestEnvironment env = new BatchTestEnvironment(weld.container());
        logger.log(Level.INFO, "new JobOperatorImp with our environment");
        jobImp = new JobOperatorImpl(env);
    }

    /*
    The standard java instruction to start a Job is something like:
        Long executionId = BatchRuntime.getJobOperator().start("job-name", prop);
    We start the job through the JobOperatorImp JBeret class in order to configure correctly the environment
    */
    public Long start(String job, Properties params){
        logger.log(Level.INFO, "starting the Job: " + job);
        return jobImp.start(job, params);
    }

    public void awaitTermination(Long executionId) throws InterruptedException {
        int t = 0;
        while(t++ < MAX_TRIES) {
            if(jobImp.getJobExecution(executionId).getExitStatus() != null){
                break;
            }
            logger.log(Level.INFO, "Try #" + Integer.toString(t) + " of " + Integer.toString(MAX_TRIES) + " --> sleep");
            Thread.sleep(THREAD_SLEEP);
        }
    }

    @Test
    public void the_FillEntitiesBatchlet_finished_successfully() throws Exception {
        // GIVEN
        // ... we have a Batch Environment 
        PrepareJBeretEnvironment();

        // ... and we need to create only 5 entities
        Properties prop = new Properties();
        prop.setProperty("nmEntities", "10");
        prop.setProperty("partitions", "4");

        // WHEN 
        // ... the fillEntities Job is executed
        Long executionId = start("fillEntities", prop);

        // ... and the Job has finished
        awaitTermination(executionId);

        // ... the Job BatchStatus must be COMPLETED
        assertEquals(BatchStatus.COMPLETED, jobImp.getJobExecution(executionId).getBatchStatus());

        // ... the computeSumJob Job is executed
        executionId = start("computeSumJob", prop);

        // ... and the Job has finished
        awaitTermination(executionId);

        // ... the Job BatchStatus must be COMPLETED
        assertEquals(BatchStatus.COMPLETED, jobImp.getJobExecution(executionId).getBatchStatus());

        // THEN 
        // ... and we must have 10 entities in the EntityManager
        List<MyEntity> r = em.createQuery("SELECT e FROM MyEntity e", MyEntity.class).getResultList();
        assertEquals(10, r.size());

        int p = 0;
        int pr[] = {0,0,0,0}; //... 4 partitions (max)
        // ... and all entities ...
        for (MyEntity e : r) {
            // ... mut be correct
            EntityTestHelper.entity_must_be_correct(e);
            p = e.getPartition();
            // ... and partition must be < 4
            assertTrue(p < 4, "Partition must be < 4");
    
            pr[p]++;
        }
        // ... and we must have an entity in any each partition
        assertTrue(pr[0] > 0, "Partition 0 must have an Entity");
        assertTrue(pr[1] > 0, "Partition 1 must have an Entity");
        assertTrue(pr[2] > 0, "Partition 2 must have an Entity");
        assertTrue(pr[3] > 0, "Partition 2 must have an Entity");

        BatchTestHelper.logJob(jobImp, executionId);    
    }
}