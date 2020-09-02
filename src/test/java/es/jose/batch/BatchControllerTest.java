package es.jose.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.jose.batch.beans.BatchController;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.enterprise.context.SessionScoped;

import org.junit.jupiter.api.Test;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;


@EnableAutoWeld
public class BatchControllerTest {
	
    @WeldSetup
    public WeldInitiator weld = WeldInitiator.from(BatchController.class).activate(SessionScoped.class).build();

	@Inject
	private BatchController batchController;
    
    private static final Logger logger = Logger.getLogger("BatchControllerTest");
	
    @Test
    public void test_given_a_new_job_then_the_job_is_not_started() {
        assertEquals("notCreated", batchController.getJobStatus());
	}
}