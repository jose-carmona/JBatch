package es.jose.batch.environment;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.transaction.TransactionManager;

import org.jberet.repository.JobRepository;
import org.jberet.spi.ArtifactFactory;
import org.jberet.spi.BatchEnvironment;
import org.jberet.spi.JobExecutor;
import org.jberet.spi.JobTask;
import org.jberet.spi.JobXmlResolver;
import org.jberet.tools.ChainedJobXmlResolver;
import org.jberet.tools.MetaInfBatchJobsJobXmlResolver;
import org.jberet.tx.LocalTransactionManager;

import javax.batch.operations.BatchRuntimeException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.weld.environment.se.WeldContainer;


/**
 * Represents the Java SE batch runtime environment and its services.
 */
public final class BatchTestEnvironment implements BatchEnvironment {
    private static final Logger logger = Logger.getLogger("BatchTestEnvironment");

    public static final String CONFIG_FILE_NAME = "jberet.properties";
    public static final String JOB_REPOSITORY_TYPE_KEY = "job-repository-type";
    public static final String REPOSITORY_TYPE_IN_MEMORY = "in-memory";
    public static final String REPOSITORY_TYPE_JDBC = "jdbc";
    public static final String REPOSITORY_TYPE_MONGODB = "mongodb";
    public static final String REPOSITORY_TYPE_INFINISPAN = "infinispan";

    private static final JobXmlResolver[] DEFAULT_JOB_XML_RESOLVERS = {
            new ClassPathJobXmlResolver(),
            new MetaInfBatchJobsJobXmlResolver(),
    };

    private final Properties configProperties;
    private final TransactionManager tm;
    private final JobXmlResolver jobXmlResolver;
    private final JobExecutor executor;

    static final String THREAD_POOL_TYPE = "thread-pool-type";
    static final String THREAD_POOL_TYPE_CACHED = "Cached";
    static final String THREAD_POOL_TYPE_FIXED = "Fixed";
    static final String THREAD_POOL_TYPE_CONFIGURED = "Configured";

    static final String THREAD_POOL_CORE_SIZE = "thread-pool-core-size";
    static final String THREAD_POOL_MAX_SIZE = "thread-pool-max-size";
    static final String THREAD_POOL_KEEP_ALIVE_TIME = "thread-pool-keep-alive-time";
    static final String THREAD_POOL_QUEUE_CAPACITY = "thread-pool-queue-capacity";
    static final String THREAD_POOL_ALLOW_CORE_THREAD_TIMEOUT = "thread-pool-allow-core-thread-timeout";
    static final String THREAD_POOL_PRESTART_ALL_CORE_THREADS = "thread-pool-prestart-all-core-threads";
    static final String THREAD_POOL_REJECTION_POLICY = "thread-pool-rejection-policy";
    static final String THREAD_FACTORY = "thread-factory";

    private WeldContainer container;

    public BatchTestEnvironment(WeldContainer container) {
        logger.log(Level.INFO,"creating BatchTestEnvironment");
        configProperties = new Properties();
        final InputStream configStream = getClassLoader().getResourceAsStream(CONFIG_FILE_NAME);
        if (configStream != null) {
            try {
                configProperties.load(configStream);
            } catch (final IOException e) {
                throw new BatchRuntimeException("SEBatchMessages.MESSAGES.failToLoadConfig(e, CONFIG_FILE_NAME)");
            } finally {
                try {
                    configStream.close();
                } catch (final IOException ioe) {
                    //ignore
                }
            }
        } else {
            logger.log(Level.INFO,"SEBatchLogger.LOGGER.useDefaultJBeretConfig(CONFIG_FILE_NAME)");
        }
        this.tm = LocalTransactionManager.getInstance();

        final ThreadPoolExecutor threadPoolExecutor = createThreadPoolExecutor();
        executor = new JobExecutor(threadPoolExecutor) {
            @Override
            protected int getMaximumPoolSize() {
                return threadPoolExecutor.getMaximumPoolSize();
            }
        };
        final ServiceLoader<JobXmlResolver> userJobXmlResolvers = ServiceLoader.load(JobXmlResolver.class, getClassLoader());
        this.jobXmlResolver = new ChainedJobXmlResolver(userJobXmlResolvers, DEFAULT_JOB_XML_RESOLVERS);
        this.container = container;
    }

    @Override
    public ClassLoader getClassLoader() {
        logger.log(Level.INFO,"looking for ClassLoader");
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            logger.log(Level.INFO,"> from BatchTestEnvironment.class");
            cl = BatchTestEnvironment.class.getClassLoader();
        }
        return cl;
    }

    @Override
    public ArtifactFactory getArtifactFactory() {
        logger.log(Level.INFO,"new TestArtifactFactory");
        return new TestArtifactFactory(container);
    }

    @Override
    public void submitTask(final JobTask task) {
        executor.execute(task);
    }

    @Override
    public TransactionManager getTransactionManager() {
        return tm;
    }

    @Override
    public JobRepository getJobRepository() {
        return JobRepositoryFactory.getJobRepository(configProperties);
    }

    @Override
    public JobXmlResolver getJobXmlResolver() {
        return jobXmlResolver;
    }

    @Override
    public Properties getBatchConfigurationProperties() {
        return this.configProperties;
    }

    ThreadPoolExecutor createThreadPoolExecutor() {
        String threadPoolType = configProperties.getProperty(THREAD_POOL_TYPE);
        final String threadFactoryProp = configProperties.getProperty(THREAD_FACTORY);
        final ThreadFactory threadFactory;
        if (threadFactoryProp != null && !threadFactoryProp.isEmpty()) {
            try {
                final Class<?> threadFactoryClass = getClassLoader().loadClass(threadFactoryProp.trim());
                threadFactory = (ThreadFactory) threadFactoryClass.getDeclaredConstructor().newInstance();
            } catch (final Exception e) {
                throw new BatchRuntimeException("SEBatchMessages.MESSAGES.failToGetConfigProperty(THREAD_FACTORY, threadFactoryProp, e)");
            }
        } else {
            threadFactory = new BatchThreadFactory();
        }

        if (threadPoolType == null || threadPoolType.isEmpty() || threadPoolType.trim().equalsIgnoreCase(THREAD_POOL_TYPE_CACHED)) {
            return (ThreadPoolExecutor) Executors.newCachedThreadPool(threadFactory);
        }

        final String coreSizeProp = configProperties.getProperty(THREAD_POOL_CORE_SIZE);
        final int coreSize;
        try {
            coreSize = Integer.parseInt(coreSizeProp.trim());
        } catch (final Exception e) {
            throw new BatchRuntimeException("SEBatchMessages.MESSAGES.failToGetConfigProperty(THREAD_POOL_CORE_SIZE, coreSizeProp, e)");
        }

        threadPoolType = threadPoolType.trim();
        if (threadPoolType.equalsIgnoreCase(THREAD_POOL_TYPE_FIXED)) {
            return (ThreadPoolExecutor) Executors.newFixedThreadPool(coreSize, threadFactory);
        }

        if (threadPoolType.equalsIgnoreCase(THREAD_POOL_TYPE_CONFIGURED)) {
            final String maxSizeProp = configProperties.getProperty(THREAD_POOL_MAX_SIZE);
            final int maxSize;
            try {
                maxSize = Integer.parseInt(maxSizeProp.trim());
            } catch (final Exception e) {
                throw new BatchRuntimeException("SEBatchMessages.MESSAGES.failToGetConfigProperty(THREAD_POOL_MAX_SIZE, maxSizeProp, e)");
            }

            final String keepAliveProp = configProperties.getProperty(THREAD_POOL_KEEP_ALIVE_TIME);
            final long keepAliveSeconds;
            try {
                keepAliveSeconds = Long.parseLong(keepAliveProp.trim());
            } catch (final Exception e) {
                throw new BatchRuntimeException("SEBatchMessages.MESSAGES.failToGetConfigProperty(THREAD_POOL_KEEP_ALIVE_TIME, keepAliveProp, e)");
            }

            final String queueCapacityProp = configProperties.getProperty(THREAD_POOL_QUEUE_CAPACITY);
            final int queueCapacity;
            try {
                queueCapacity = Integer.parseInt(queueCapacityProp.trim());
            } catch (final Exception e) {
                throw new BatchRuntimeException("SEBatchMessages.MESSAGES.failToGetConfigProperty(THREAD_POOL_QUEUE_CAPACITY, queueCapacityProp, e)");
            }

            final String allowCoreThreadTimeoutProp = configProperties.getProperty(THREAD_POOL_ALLOW_CORE_THREAD_TIMEOUT);
            final boolean allowCoreThreadTimeout = allowCoreThreadTimeoutProp == null || allowCoreThreadTimeoutProp.isEmpty() ? false :
                    Boolean.parseBoolean(allowCoreThreadTimeoutProp.trim());

            final String prestartAllCoreThreadsProp = configProperties.getProperty(THREAD_POOL_PRESTART_ALL_CORE_THREADS);
            final boolean prestartAllCoreThreads = prestartAllCoreThreadsProp == null || prestartAllCoreThreadsProp.isEmpty() ? false :
                    Boolean.parseBoolean(prestartAllCoreThreadsProp.trim());

            final BlockingQueue<Runnable> workQueue = queueCapacity > 0 ?
                    new LinkedBlockingQueue<Runnable>(queueCapacity) : new SynchronousQueue<Runnable>(true);

            final String rejectionPolicyProp = configProperties.getProperty(THREAD_POOL_REJECTION_POLICY);
            RejectedExecutionHandler rejectionHandler = null;

            if (rejectionPolicyProp != null && !rejectionPolicyProp.isEmpty()) {
                try {
                    final Class<?> aClass = getClassLoader().loadClass(rejectionPolicyProp.trim());
                    rejectionHandler = (RejectedExecutionHandler) aClass.getDeclaredConstructor().newInstance();
                } catch (final Exception e) {
                    throw new BatchRuntimeException("SEBatchMessages.MESSAGES.failToGetConfigProperty(THREAD_POOL_REJECTION_POLICY, rejectionPolicyProp, e)");
                }
            }

            final ThreadPoolExecutor threadPoolExecutor = rejectionHandler == null ?
                    new ThreadPoolExecutor(coreSize, maxSize, keepAliveSeconds, TimeUnit.SECONDS, workQueue, threadFactory) :
                    new ThreadPoolExecutor(coreSize, maxSize, keepAliveSeconds, TimeUnit.SECONDS, workQueue, threadFactory, rejectionHandler);

            if (allowCoreThreadTimeout) {
                threadPoolExecutor.allowCoreThreadTimeOut(true);
            }
            if (prestartAllCoreThreads) {
                threadPoolExecutor.prestartAllCoreThreads();
            }
            return threadPoolExecutor;
        }

        throw new BatchRuntimeException("SEBatchMessages.MESSAGES.failToGetConfigProperty(THREAD_POOL_TYPE, threadPoolType, null)");
    }
}