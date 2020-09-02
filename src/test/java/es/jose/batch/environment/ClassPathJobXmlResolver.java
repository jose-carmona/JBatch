package es.jose.batch.environment;

import org.jberet.spi.JobXmlResolver;
import org.jberet.tools.AbstractJobXmlResolver;

/**
 * An implementation of {@link JobXmlResolver} that resolves job xml
 * by searching the class path.
 * <p>
 * Implementation notes: as of version 1.3.0.Beta5, this class extends
 * {@link AbstractJobXmlResolver}, and {@code resolveJobXml} method is
 * moved to the parent class.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class ClassPathJobXmlResolver extends AbstractJobXmlResolver {
}