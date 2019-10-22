package io.hyscale.ctl.dockerfile.gen.services.generator;

import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.models.DockerfileEntity;
import io.hyscale.ctl.dockerfile.gen.services.model.DockerfileGenContext;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;

/**
 * Interface to generate dockerfile from the service spec file
 * <p>Implementation Notes</p>
 * <p>
 * Any implementation to this class should generate a @see {@link DockerfileEntity}
 * from the service spec .
 * </p>
 */

public interface DockerfileGenerator {

    /**
     * @param serviceSpec Service Spec from which the dockerfile has to be generated
     * @param context     consists of attributes to control the dockerfile generation
     * @return DockerfileEntity @see {@link DockerfileEntity}
     * @throws HyscaleException
     */

    DockerfileEntity generateDockerfile(ServiceSpec serviceSpec, DockerfileGenContext context) throws HyscaleException;
}
