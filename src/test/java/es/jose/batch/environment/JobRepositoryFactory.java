package es.jose.batch.environment;

import java.util.Properties;

import org.jberet.repository.InMemoryRepository;
import org.jberet.repository.InfinispanRepository;
import org.jberet.repository.JdbcRepository;
import org.jberet.repository.JobRepository;
import org.jberet.repository.MongoRepository;


import javax.batch.operations.BatchRuntimeException;

/**
 * Determines the {@link org.jberet.repository.JobRepository job repistory} to use.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class JobRepositoryFactory {

    private static final JobRepositoryFactory INSTANCE = new JobRepositoryFactory();

    private JobRepository jobRepository;

    private JobRepositoryFactory() {
    }

    public static JobRepository getJobRepository(final Properties configProperties) {
        String repositoryType = null;
        if (configProperties != null) {
            repositoryType = configProperties.getProperty(BatchTestEnvironment.JOB_REPOSITORY_TYPE_KEY);
            if (repositoryType != null) {
                repositoryType = repositoryType.trim();
            }
        }
        JobRepository jobRepository;
        synchronized (INSTANCE) {
            jobRepository = INSTANCE.jobRepository;
            if (repositoryType == null || repositoryType.equalsIgnoreCase(BatchTestEnvironment.REPOSITORY_TYPE_IN_MEMORY)) {
                if (!(jobRepository instanceof InMemoryRepository)) {
                    jobRepository = INSTANCE.jobRepository = InMemoryRepository.getInstance();
                }
            } else if (repositoryType.isEmpty() || repositoryType.equalsIgnoreCase(BatchTestEnvironment.REPOSITORY_TYPE_JDBC)) {
                if (!(jobRepository instanceof JdbcRepository)) {
                    jobRepository = INSTANCE.jobRepository = JdbcRepository.create(configProperties);
                }
            } else if (repositoryType.equalsIgnoreCase(BatchTestEnvironment.REPOSITORY_TYPE_MONGODB)) {
                if (!(jobRepository instanceof MongoRepository)) {
                    jobRepository = INSTANCE.jobRepository = MongoRepository.create(configProperties);
                }
/*            } else if (repositoryType.equalsIgnoreCase(BatchTestEnvironment.REPOSITORY_TYPE_INFINISPAN)) {
                if (!(jobRepository instanceof InfinispanRepository)) {
                    jobRepository = INSTANCE.jobRepository = InfinispanRepository.create(configProperties);
                } */
            } else {
                throw new BatchRuntimeException("SEBatchMessages.MESSAGES.unrecognizedJobRepositoryType(repositoryType)");
            }
        }
        return jobRepository;
    }
}