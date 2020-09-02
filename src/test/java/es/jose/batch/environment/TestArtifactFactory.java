package es.jose.batch.environment;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.jberet.creation.AbstractArtifactFactory;
import org.jboss.weld.bootstrap.api.helpers.RegistrySingletonProvider;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ArtifactFactory for Java SE runtime environment using Weld and CDI.
 */
public final class TestArtifactFactory extends AbstractArtifactFactory {
    private static final Logger logger = Logger.getLogger("TestArtifactFactory");
    private final BeanManager beanManager;

    public TestArtifactFactory(WeldContainer container) {
        WeldContainer weldContainer;
        
        weldContainer = container;

        /*
        synchronized (TestArtifactFactory.class) {
            weldContainer = WeldContainer.instance(RegistrySingletonProvider.STATIC_INSTANCE);
            if (weldContainer == null) {
                weldContainer = new Weld(RegistrySingletonProvider.STATIC_INSTANCE).initialize();
            }
        }
        */
        logger.log(Level.INFO,"getBeanManager");
        beanManager = weldContainer.getBeanManager();
    }

    @Override
    public Class<?> getArtifactClass(final String ref, final ClassLoader classLoader) {
        logger.log(Level.INFO,"getArtifactClass ref = " + ref);
        final Bean<?> bean = getBean(ref);
        return bean == null ? null : bean.getBeanClass();
    }

    @Override
    public Object create(final String ref, final Class<?> cls, final ClassLoader classLoader) throws Exception {
        logger.log(Level.INFO,"create ref = " + ref);
        final Bean<?> bean = getBean(ref);
        return bean == null ? null : beanManager.getReference(bean, bean.getBeanClass(), beanManager.createCreationalContext(bean));
    }

    private Bean<?> getBean(final String ref) {
        logger.log(Level.INFO,"getBean ref = " + ref);
        return beanManager.resolve(beanManager.getBeans(ref));
    }
}