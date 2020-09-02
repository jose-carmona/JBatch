package es.jose.batch;

import java.util.ArrayList;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.batch.api.chunk.AbstractItemWriter;
import javax.inject.Named;

import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;

import es.jose.entities.MyEntity;

@Named
public class EntityWriter extends AbstractItemWriter {
    private static final Logger logger = Logger.getLogger("EntityWriter");

    @PersistenceContext
    private EntityManager em;

    @Override
    public void writeItems(List<Object> items) throws Exception {
        logger.log(Level.INFO, "writing " + Integer.toString(items.size()) + " items");
        for (Object e : items) {
            em.merge((MyEntity)e);
        }

    }
}