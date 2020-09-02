package es.jose.batch;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.batch.api.chunk.listener.AbstractChunkListener;

import javax.enterprise.context.Dependent;
import javax.inject.Named;

@Dependent
@Named
public class OnErrorChunkListener extends AbstractChunkListener {

    private static final Logger logger = Logger.getLogger("OnErrorChunkListener");
    
    public OnErrorChunkListener() { }
    
    @Override
    public void onError(Exception ex) throws Exception {
        logger.log(Level.INFO, "ERROR: " + ex.toString()    );
    }
    
}