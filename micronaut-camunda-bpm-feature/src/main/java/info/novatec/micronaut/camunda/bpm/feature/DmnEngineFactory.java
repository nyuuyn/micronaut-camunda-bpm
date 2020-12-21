package info.novatec.micronaut.camunda.bpm.feature;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tobias Schäfer
 */
@Factory
public class DmnEngineFactory {

    private static final Logger log = LoggerFactory.getLogger(DmnEngineFactory.class);

    //@Bean(preDestroy = "close")
    @Bean
    public DmnEngine dmnEngine() {
        // todo: Caused by: java.lang.ClassNotFoundException: javax.el.ELResolver
        log.info("Building DMN engine");
        return DmnEngineConfiguration.createDefaultDmnEngineConfiguration().buildEngine();
    }
}
