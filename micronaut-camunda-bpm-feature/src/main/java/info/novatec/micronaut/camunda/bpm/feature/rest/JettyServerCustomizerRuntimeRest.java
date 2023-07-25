/*
 * Copyright 2022 original authors
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
package info.novatec.micronaut.camunda.bpm.feature.rest;

import info.novatec.micronaut.camunda.bpm.feature.Configuration;
import info.novatec.micronaut.camunda.bpm.feature.initialization.ParallelInitializationWithoutProcessEngine;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import org.camunda.bpm.engine.rest.impl.FetchAndLockContextListener;
import org.camunda.bpm.engine.rest.security.auth.ProcessEngineAuthenticationFilter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import java.util.EnumSet;

import static info.novatec.micronaut.camunda.bpm.feature.Configuration.Rest.DEFAULT_AUTHENTICATION_PROVIDER;

/**
 * Using Micronaut Servlet with Jetty to run the REST API/Webapps as a servlet.
 *
 * see https://micronaut-projects.github.io/micronaut-servlet/latest/guide/#jetty
 *
 * @author Martin Sawilla
 */
@Singleton
@Requires(beans = Server.class)
@Requires(property = "camunda.rest.enabled", value = "true")
//Implementation based on Spring-Boot-Starter: https://github.com/camunda/camunda-bpm-spring-boot-starter/tree/master/starter-webapp-core/src/main/java/org/camunda/bpm/spring/boot/starter/webapp
public class JettyServerCustomizerRuntimeRest implements ParallelInitializationWithoutProcessEngine {

    private static final Logger log = LoggerFactory.getLogger(JettyServerCustomizerRuntimeRest.class);

    protected final Server server;

    // Configuration must be resolved during construction - otherwise code might be blocked if a parallel thread constructs a bean during execution, e.g. the ProcessEngine
    protected final String contextPath;
    protected final boolean basicAuthEnabled;
    protected final String authenticationProvider;

    public JettyServerCustomizerRuntimeRest(Server server, Configuration configuration) {
        this.server = server;
        contextPath = configuration.getRest().getContextPath();
        basicAuthEnabled = configuration.getRest().isBasicAuthEnabled();
        authenticationProvider = configuration.getRest().getAuthenticationProvider();
    }

    @Override
    public void execute() throws Exception {
        ServletContextHandler restServletContextHandler = new ServletContextHandler();
        restServletContextHandler.setContextPath(contextPath);
        restServletContextHandler.addServlet(new ServletHolder(new ServletContainer(new RestApp())), "/*");
        restServletContextHandler.addEventListener(new ServletContextListener() {
            @Override
            public void contextInitialized(ServletContextEvent sce) {
                // Required for long polling
                new FetchAndLockContextListener().contextInitialized(sce);
            }

            @Override
            public void contextDestroyed(ServletContextEvent sce) {
            }
        });

        boolean customAuthenticationProvider = !DEFAULT_AUTHENTICATION_PROVIDER.equals(authenticationProvider);
        if (basicAuthEnabled || customAuthenticationProvider) {
            // see https://docs.camunda.org/manual/latest/reference/rest/overview/authentication/
            FilterHolder filterHolder = new FilterHolder(ProcessEngineAuthenticationFilter.class);
            filterHolder.setInitParameter("authentication-provider", authenticationProvider);
            restServletContextHandler.addFilter(filterHolder, "/*", EnumSet.of(DispatcherType.REQUEST));
            log.debug("REST API - Authentication enabled with authentication-provider {}", authenticationProvider);
        }

        restServletContextHandler.setServer(server);
        restServletContextHandler.start();
        ((HandlerCollection)server.getHandler()).addHandler(restServletContextHandler);

        log.info("REST API initialized on {}/*", contextPath);
    }
}

