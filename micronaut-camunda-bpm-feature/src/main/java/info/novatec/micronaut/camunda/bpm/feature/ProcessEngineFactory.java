/*
 * Copyright 2020-2021 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.novatec.micronaut.camunda.bpm.feature;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Factory;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author Tobias Schäfer
 */
@Factory
public class ProcessEngineFactory {

    public static final String MICRONAUT_AUTO_DEPLOYMENT_NAME = "MicronautAutoDeployment";

    private static final Logger log = LoggerFactory.getLogger(ProcessEngineFactory.class);

    /**
     * The {@link ProcessEngine} is started with the application start so that the task scheduler is started immediately.
     * @param processEngineConfiguration the {@link ProcessEngineConfiguration} to build the {@link ProcessEngine}.
     * @param camundaVersion the @{@link CamundaVersion} to log on application start.
     * @param processApplication the @{@link MnProcessApplication}
     * @return the initialized {@link ProcessEngine} in the application context.
     * @throws IOException if a resource, i.e. a model, cannot be loaded.
     */
    @Context
    @Bean(preDestroy = "close")
    public ProcessEngine processEngine(ProcessEngineConfiguration processEngineConfiguration, CamundaVersion camundaVersion, MnProcessApplication processApplication) throws IOException {

        if (camundaVersion.getVersion().isPresent()) {
            log.info("Camunda version: {}", camundaVersion.getVersion().get());
        } else {
            log.warn("The Camunda version cannot be determined. If you created a Fat/Uber/Shadow JAR then please consider using the Micronaut Application Plugin's 'dockerBuild' task to create a Docker image.");
        }

        ProcessEngine processEngine = processEngineConfiguration.buildProcessEngine();

        deployProcessModels(processEngine, processApplication);

        return processEngine;
    }

    /**
     * Deploys all process models found in root directory of the resources.
     * <p>
     * Note: Currently this is not recursive!
     *
     * @param processEngine the {@link ProcessEngine}
     * @param processApplication the @{@link MnProcessApplication}
     * @throws IOException if a resource, i.e. a model, cannot be loaded.
     */
    protected void deployProcessModels(ProcessEngine processEngine, MnProcessApplication processApplication) throws IOException {
        log.info("Searching non-recursively for models in the resources");
        PathMatchingResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();

        DeploymentBuilder builder = processEngine.getRepositoryService().createDeployment(processApplication.getReference())
                .name(MICRONAUT_AUTO_DEPLOYMENT_NAME)
                .enableDuplicateFiltering(true);

        boolean deploy = false;
        for (String extension : Arrays.asList("dmn", "bpmn")) {
            for (Resource resource : resourceLoader.getResources(PathMatchingResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "*." + extension)) {
                log.info("Deploying model: {}", resource.getFilename());
                builder.addInputStream(resource.getFilename(), resource.getInputStream());
                deploy = true;
            }
        }
        if (deploy) {
            builder.deploy();
        }
    }
}
