package es.jose.batch;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.batch.api.chunk.ItemProcessor;
import javax.inject.Named;

import es.jose.entities.MyEntity;

@Named
public class ComputeSumProcessor implements ItemProcessor {
    private static final Logger logger = Logger.getLogger("ComputeSumProcessor");

    @Override
    public MyEntity processItem(Object t) {
        MyEntity e = (MyEntity)t;
        logger.log(Level.INFO, "Processing...");
        logger.log(Level.INFO, " > Item: " + e.getId());

        Double sum = e.getDetails().stream().mapToDouble(d -> d.getAmount()).sum();
        logger.log(Level.INFO, " > Sum: " + Double.toString(sum));
        e.setSum(sum);

        return e;
    }
}