package es.jose.batch;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.JobExecution;
import javax.batch.runtime.Metric;
import javax.batch.runtime.StepExecution;

public class BatchTestHelper {
    
    private static final Logger logger = Logger.getLogger("BatchTestHelper");

    private static final int MAX_TRIES = 40;
    private static final int THREAD_SLEEP = 1000;

    private BatchTestHelper() {
        throw new UnsupportedOperationException();
    }

    public static void keepTestAlive(Long executionId) throws InterruptedException {
        int t = 0;
        while(t++ < MAX_TRIES) {
            if(BatchRuntime.getJobOperator().getJobExecution(executionId).getExitStatus() != null){
                break;
            }
            logger.log(Level.INFO, "Try #" + Integer.toString(t) + " of " + Integer.toString(MAX_TRIES) + " --> sleep");
            Thread.sleep(THREAD_SLEEP);
        }
    }

    public static void logJob(JobOperator jobImp, long executionId) {
        final JobExecution jobExecution = jobImp.getJobExecution(executionId);

        logger.log(Level.INFO, 
                    String.format("jobExecution id=%s, batchStatus=%s, exitStatus=%s, jobParameters=%s, " +
                        "createTime=%s, startTime=%s, lastUpdateTime=%s, endTime=%s%n",
                        executionId, jobExecution.getBatchStatus(), jobExecution.getExitStatus(), jobExecution.getJobParameters(),
                        jobExecution.getCreateTime(), jobExecution.getStartTime(),
                        jobExecution.getLastUpdatedTime(), jobExecution.getEndTime()));

        List<StepExecution> stepExecutions = jobImp.getStepExecutions(executionId);

        for(StepExecution step : stepExecutions) {
            logger.log(Level.INFO, 
                        String.format("stepExecution id=%s, name=%s, batchStatus=%s, exitStatus=%s, PersistentUserData=%s%n",
                        step.getStepExecutionId(), step.getStepName(), step.getBatchStatus(), step.getExitStatus(), step.getPersistentUserData()));
            Metric[] metrics = step.getMetrics();
            for (final Metric m : metrics) {
                logger.log(Level.INFO, m.toString());
            }
        }

    }
}