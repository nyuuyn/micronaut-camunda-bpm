/*
 * Copyright 2021 original authors
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
package info.novatec.micronaut.camunda.externaltask.process;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Factory;
import org.camunda.bpm.engine.rest.impl.FetchAndLockContextListener;

/**
 * Initialize default FetchAndLockContextListener so that ExternalWorker can fetch Tasks and lock them to be processed.
 */
@Factory
class FetchAndLockContextFactory {

    @Context
    @Bean(preDestroy = "contextDestroyed")
    FetchAndLockContextListener fetchAndLockContextListener() {
        FetchAndLockContextListener fetchAndLockContextListener = new FetchAndLockContextListener();
        // passing null value is fine as long as no unique workers are needed
        // for details check org.camunda.bpm.engine.rest.impl.FetchAndLockHandlerImpl.contextInitialized
        fetchAndLockContextListener.contextInitialized(null); // starts the listener
        return fetchAndLockContextListener;
    }
}