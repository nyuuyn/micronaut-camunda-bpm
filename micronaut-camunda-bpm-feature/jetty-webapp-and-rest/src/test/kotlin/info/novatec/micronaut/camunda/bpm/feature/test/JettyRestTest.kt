package info.novatec.micronaut.camunda.bpm.feature.test

import info.novatec.micronaut.camunda.bpm.feature.Configuration
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.eclipse.jetty.server.Server
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import javax.inject.Inject

/**
 * Simple Test to check if the REST API runs.
 *
 * @author Martin Sawilla
 */
@MicronautTest
@Requires(beans = [Server::class])
class JettyRestTest {

    @Inject
    lateinit var configuration: Configuration

    @Inject
    @field:Client("/")
    lateinit var client: RxHttpClient


    @Test
    fun engine() {
        val request: HttpRequest<String> = HttpRequest.GET(configuration.rest.contextPath + "/engine")
        val body = client.toBlocking().retrieve(request)

        Assertions.assertEquals("""[{"name":"default"}]""", body)
    }
}
