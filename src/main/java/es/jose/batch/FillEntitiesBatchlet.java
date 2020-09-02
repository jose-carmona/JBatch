package es.jose.batch;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.enterprise.context.Dependent;

import javax.batch.api.BatchProperty;
import javax.batch.api.Batchlet;

import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import es.jose.entities.EntityFactory;

@Named
@Dependent
@Transactional
public class FillEntitiesBatchlet implements Batchlet {
    private static final Logger logger = Logger.getLogger("FillEntitiesBatchlet");

    @Inject
    @BatchProperty
    private String maxAmount;

    @Inject
    @BatchProperty
    private String maxDetails;

    @Inject
    @BatchProperty
    private String nmEntities;

    @Inject
    @BatchProperty
    private String partitions;

    @PersistenceContext
    private EntityManager em;
    
    @Override
    public String process() throws Exception {
        logger.log(Level.INFO, "maxAmount = " + maxAmount);
        logger.log(Level.INFO, "maxDetails = " + maxDetails);
        logger.log(Level.INFO, "partitions = " + partitions);
        logger.log(Level.INFO, "nmEntities = " + nmEntities);
        int limit = Integer.valueOf(nmEntities);
        int parts = Integer.valueOf(partitions);
        EntityFactory factory = new EntityFactory(Double.valueOf(maxAmount), Integer.valueOf(maxDetails), parts);

        // we need a entity at least in each partition
        for(int i=0; i<parts; i++) {
            em.persist(factory.randomEntity(i));
        }

        // the rest of entities
        for(int i=parts; i<limit; i++) {
            em.persist(factory.randomEntity());
        }
        
        em.flush();
        
        logger.log(Level.INFO, "SUCCESS");
        return "SUCCESS";
    }

    @Override
    public void stop() throws Exception {
        throw new UnsupportedOperationException("Not Implemented");
    }
}
