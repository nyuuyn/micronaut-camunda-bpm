package info.novatec.micronaut.camunda.bpm.feature.webapp;

import org.camunda.bpm.welcome.impl.web.WelcomeApplication;
import org.glassfish.jersey.server.ResourceConfig;

public class WelcomeApp extends ResourceConfig {

    static WelcomeApplication welcomeApplication = new WelcomeApplication();

    public WelcomeApp() {
        registerClasses(welcomeApplication.getClasses());
        // Disable WADL-Feature because we do not want to expose a XML description of our RESTful web application.
        property("jersey.config.server.wadl.disableWadl", "true");
    }

}
