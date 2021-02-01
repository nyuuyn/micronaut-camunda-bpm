package info.novatec.micronaut.camunda.bpm.feature.test

import info.novatec.micronaut.camunda.bpm.feature.MnTelemetryRegistry
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import javax.inject.Inject


/**
 * Tomcat Tests for [MnTelemetryRegistry]
 *
 * @author Tobias Schäfer
 */
@MicronautTest
class MnTomcatTelemetryRegistryTest : MnTelemetryRegistryTest() {

    @Test
    fun `application server info is set to jetty`() {
        val applicationServer = telemetryRegistry.applicationServer
        assertNotNull(applicationServer)
        assertEquals("jetty", applicationServer.vendor)
        assertTrue(applicationServer.version.matches(Regex("""jetty/\d+\.\d+\.\d+.*""")))
    }
}