package info.novatec.micronaut.camunda.bpm.feature.webapp;

import org.camunda.bpm.admin.impl.web.AdminApplication;
import org.glassfish.jersey.server.ResourceConfig;

public class AdminApp extends ResourceConfig {

    static AdminApplication adminApplication = new AdminApplication();

    public AdminApp() {
        registerClasses(adminApplication.getClasses());
        // Disable WADL-Feature because we do not want to expose a XML description of our RESTful web application.
        property("jersey.config.server.wadl.disableWadl", "true");
    }
}
