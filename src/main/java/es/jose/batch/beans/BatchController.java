package es.jose.batch.beans;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.List;
import java.util.Properties;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;


import javax.batch.runtime.BatchRuntime;
import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.JobExecution;
import javax.batch.runtime.StepExecution;
import javax.batch.runtime.Metric;

/* Managed bean for the JSF front end pages
 */
@Named
@SessionScoped
public class BatchController implements Serializable {
    private static final Logger logger = Logger.getLogger("BatchController");
    private static final long serialVersionUID = 6775054787257816151L;
    final private String jobName = "fillAndComputeJob";
    private long executionId = 0;
    private JobOperator jobOperator;
    private Properties props;

    public BatchController() {
        jobOperator = BatchRuntime.getJobOperator();
        props = new Properties();
    }

    /** Constructor for testing purposes injecting the JobOperator + Properties
    */
    public BatchController(JobOperator jobOperator, Properties props) {
        this.jobOperator = jobOperator;
        this.props = props;
    }

    public String startBatchJob() {
        executionId = jobOperator.start(jobName, props);
        return "index";
    }

    public Long getExecutionId() {
        return executionId;
    }
    
    public boolean isCompleted() {
        return(BatchStatus.COMPLETED == jobOperator.getJobExecution(executionId).getBatchStatus());
    }
    
    public String getJobName() {
        return(jobName);
    }

    public String getJobStatus() {
        String rt = "notCreated";
        if(executionId != 0) {
            rt = jobOperator.getJobExecution(executionId).getBatchStatus().toString();
        }
        return(rt);
    }

    public String getJobLog() {
        String st = "notCreated";
        if( executionId != 0) {
            final JobExecution jobExecution = jobOperator.getJobExecution(executionId);

            st = String.format("jobExecution id=%s, batchStatus=%s, exitStatus=%s, jobParameters=%s, " +
                        "createTime=%s, startTime=%s, lastUpdateTime=%s, endTime=%s%n",
                        executionId, jobExecution.getBatchStatus(), jobExecution.getExitStatus(), jobExecution.getJobParameters(),
                        jobExecution.getCreateTime(), jobExecution.getStartTime(),
                        jobExecution.getLastUpdatedTime(), jobExecution.getEndTime());

            List<StepExecution> stepExecutions = jobOperator.getStepExecutions(executionId);

            for(StepExecution step : stepExecutions) {
                st = st + String.format(" stepExecution id=%s, name=%s, batchStatus=%s, exitStatus=%s, persistentUserData=%s%n",
                                step.getStepExecutionId(), step.getStepName(), step.getBatchStatus(), step.getExitStatus(), step.getPersistentUserData());
                Metric[] metrics = step.getMetrics();
                    for (final Metric m : metrics) {
                        st = st + String.format(" metric: %s = %s%n ", m.getType(), m.getValue());
                    }
            }
        }
        return st;
    }
}