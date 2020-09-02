package es.jose.batch;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.Serializable;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import javax.enterprise.context.Dependent;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import javax.batch.api.BatchProperty;
import javax.batch.api.chunk.AbstractItemReader;

import es.jose.entities.MyEntity;

@Dependent
@Named
public class EntityReader extends AbstractItemReader {
    private static final Logger logger = Logger.getLogger("EntityReader");

    @PersistenceContext
    private EntityManager em;

    @Inject
    @BatchProperty
    private String partition;

    List<MyEntity> resultList;

    protected int readPosition;

    public EntityReader() {
        logger.log(Level.INFO, "creating EntityReader");
    }

    @Override
    public void open(final Serializable checkpoint) throws Exception {
        logger.log(Level.INFO, "partition = " + partition);
        logger.log(Level.INFO, "creating Query");

        String qry = "SELECT e FROM MyEntity e";

        if(partition != null) {
            qry = qry + " WHERE e.partition = :partition";
        }

        TypedQuery<MyEntity> query = em.createQuery(qry, MyEntity.class);

        if(partition != null) {
            query = query.setParameter("partition", Integer.valueOf(partition));
        }

        resultList = query.getResultList();
        if (checkpoint == null) {
            logger.log(Level.INFO, "checkpoint null --> position = 0");
            readPosition = 0;
        } else {
            logger.log(Level.INFO, "checkpoint = " + Integer.toString((Integer) checkpoint));
            readPosition = (Integer) checkpoint;
        }
    }

    @Override
    public Object readItem() throws Exception {
        logger.log(Level.INFO, "reading Item # " + Integer.toString(readPosition));
        if (readPosition >= resultList.size()) {
            return null;
        }
        return resultList.get(readPosition++);
    }

    @Override
    public Serializable checkpointInfo() throws Exception {
        return readPosition;
    }

}
