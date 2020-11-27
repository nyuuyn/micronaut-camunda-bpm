package info.novatec.micronaut.camunda.bpm.example;

import info.novatec.micronaut.camunda.bpm.example.webapps.*;
import info.novatec.micronaut.camunda.bpm.example.rest.RestApp;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import org.camunda.bpm.admin.impl.web.bootstrap.AdminContainerBootstrap;
import org.camunda.bpm.cockpit.Cockpit;
import org.camunda.bpm.cockpit.impl.web.bootstrap.CockpitContainerBootstrap;
import org.camunda.bpm.engine.rest.filter.CacheControlFilter;
import org.camunda.bpm.engine.rest.filter.EmptyBodyFilter;
import org.camunda.bpm.tasklist.impl.web.bootstrap.TasklistContainerBootstrap;
import org.camunda.bpm.webapp.impl.security.auth.AuthenticationFilter;
import org.camunda.bpm.webapp.impl.security.filter.CsrfPreventionFilter;
import org.camunda.bpm.webapp.impl.security.filter.SecurityFilter;
import org.camunda.bpm.webapp.impl.security.filter.util.HttpSessionMutexListener;
import org.camunda.bpm.welcome.impl.web.bootstrap.WelcomeContainerBootstrap;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.servlet.*;
import java.util.EnumSet;

/**
 * Using Micronaut Servlet with Jetty to run the REST API as a servlet.
 * https://micronaut-projects.github.io/micronaut-servlet/1.0.x/guide/#jetty
 *
 * @author Martin Sawilla
 */
@Singleton
public class JettyServerCustomizer implements BeanCreatedEventListener<Server> {

    private static final Logger log = LoggerFactory.getLogger(JettyServerCustomizer.class);



    @Override
    public Server onCreated(BeanCreatedEvent<Server> event) {


        log.info("HALLO WELT ON CREATED");
        Server jettyServer = event.getBean();

        ServletContextHandler contextHandler = (ServletContextHandler) jettyServer.getHandler();
        ServletContext context = contextHandler.getServletContext();

        //REST
        ServletContainer servletContainer = new ServletContainer(new RestApp());
        ServletHolder servletHolder = new ServletHolder(servletContainer);
        contextHandler.addServlet(servletHolder, "/rest/*");


        contextHandler.addEventListener(new CockpitContainerBootstrap());
        contextHandler.addEventListener(new AdminContainerBootstrap());
        contextHandler.addEventListener(new TasklistContainerBootstrap());
        contextHandler.addEventListener(new WelcomeContainerBootstrap());
        contextHandler.addEventListener(new HttpSessionMutexListener());

        contextHandler.addEventListener(new InitListener());

        log.info("REST API initialized with Micronaut Servlet - try accessing it on http://localhost:8080/rest/engine");

        EnumSet<DispatcherType> DISPATCHER_TYPES = EnumSet.of(DispatcherType.REQUEST, DispatcherType.INCLUDE, DispatcherType.FORWARD, DispatcherType.ERROR);
        // Filter to replace the $APP_ROOT stuff
        contextHandler.addFilter(ProcessEnginesFilter.class, "/app/*", DISPATCHER_TYPES);
        //contextHandler.addFilter(TestFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));

        return jettyServer;
    }

    // I need to configure the Camunda Webapps here because in the onCreated method
    // I do not have access to e.g. Cockpit.getRuntimeDelegate() (results in null)
    // But here I the getRuntimeDelegate is available and so I do not get errors with my servlet
    public static class InitListener implements ServletContextListener
    {
        private static final Logger log = LoggerFactory.getLogger(InitListener.class);

        private static final EnumSet<DispatcherType> DISPATCHER_TYPES = EnumSet.of(DispatcherType.REQUEST, DispatcherType.INCLUDE, DispatcherType.FORWARD, DispatcherType.ERROR);

        @Override
        public void contextInitialized(ServletContextEvent sce)
        {
            log.info(Cockpit.getRuntimeDelegate().getAppPluginRegistry().getPlugins().get(0).getId());
            ServletContext servletContext = sce.getServletContext();

            servletContext.addServlet("CockpitApp", new ServletContainer(new CockpitApp())).addMapping("/api/cockpit/*");
            servletContext.addServlet("AdminApp", new ServletContainer(new AdminApp())).addMapping("/api/admin/*");
            servletContext.addServlet("TasklistApp", new ServletContainer(new TasklistApp())).addMapping("/api/tasklist/*");
            servletContext.addServlet("EngineRestApp", new ServletContainer(new EngineRestApp())).addMapping("/api/engine/*");
            servletContext.addServlet("WelcomeApp", new ServletContainer(new WelcomeApp())).addMapping("/api/welcome/*");
            log.info("In theory: Servlets are initialized");
        }

        @Override
        public void contextDestroyed(ServletContextEvent sce)
        {
        }
    }

    /*public static class TestFilter implements Filter {
        private static final Logger log = LoggerFactory.getLogger(TestFilter.class);
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            log.info(request.toString());
            chain.doFilter(request, response);
        }

    }*/
}

