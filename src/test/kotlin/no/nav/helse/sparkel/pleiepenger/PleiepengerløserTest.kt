package no.nav.helse.sparkel.pleiepenger

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.pleiepenger.pleiepenger.AzureClient
import no.nav.helse.sparkel.pleiepenger.pleiepenger.PleiepengeClient
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import java.time.LocalDate

@TestInstance(Lifecycle.PER_CLASS)
internal class PleiepengerløserTest {

    private companion object {
        private const val orgnummer = "80000000"
    }

    private val wireMockServer: WireMockServer = WireMockServer(WireMockConfiguration.options().dynamicPort())
    private val objectMapper = jacksonObjectMapper()
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .registerModule(JavaTimeModule())

    private lateinit var sendtMelding: JsonNode
    private lateinit var service: PleiepengerService

    private val context = object : RapidsConnection.MessageContext {
        override fun send(message: String) {
            sendtMelding = objectMapper.readTree(message)
        }

        override fun send(key: String, message: String) {}
    }

    private val rapid = object : RapidsConnection() {

        fun sendTestMessage(message: String) {
            listeners.forEach { it.onMessage(message, context) }
        }

        override fun publish(message: String) {}

        override fun publish(key: String, message: String) {}

        override fun start() {}

        override fun stop() {}
    }

    @BeforeAll
    fun setup() {
        wireMockServer.start()
        configureFor(create().port(wireMockServer.port()).build())
        stubEksterneEndepunkt()
        service = PleiepengerService(
            PleiepengeClient(
                baseUrl = wireMockServer.baseUrl(),
                accesstokenScope = "a_scope",
                azureClient = AzureClient(
                    tenantUrl = "${wireMockServer.baseUrl()}/AZURE_TENANT_ID",
                    clientId = "client_id",
                    clientSecret = "client_secret"
                )
            )
        )
    }

    @AfterAll
    internal fun teardown() {
        wireMockServer.stop()
    }

    @Test
    fun `løser behov`() {

    }

    private fun testBehov(behov: String) {
        Pleiepengerløser(rapid, service)
        rapid.sendTestMessage(behov)
    }


    private fun stubEksterneEndepunkt() {
        stubFor(
            post(urlMatching("/AZURE_TENANT_ID/oauth2/v2.0/token"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """{
                        "token_type": "Bearer",
                        "expires_in": 3599,
                        "access_token": "1234abc"
                    }"""
                        )
                )
        )
        stubFor(
            get(urlPathEqualTo("/vedtak"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """{
                                      "vedtak": [
                                        {
                                          "fom": 2018-01-01,
                                          "tom": "2018-01-31",
                                          "grad": 100,
                                        }
                                      ]
                                    }"""
                        )
                )
        )
    }
}
