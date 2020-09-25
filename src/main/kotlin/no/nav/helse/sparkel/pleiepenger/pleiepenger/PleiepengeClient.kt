package no.nav.helse.sparkel.pleiepenger.pleiepenger

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate

class PleiepengeClient(
    private val baseUrl: String,
    private val accesstokenScope: String,
    private val azureClient: AzureClient
) {

    companion object {
        private val objectMapper = ObjectMapper()
        private val tjenestekallLog = LoggerFactory.getLogger("tjenestekall")
    }

    //TODO: fix
    internal fun hentPleiepenger(
        behovId: String,
        vedtaksperiodeId: String,
        fnr: String,
        fom: LocalDate,
        tom: LocalDate
    ): ArrayNode {
        val url =
            "${baseUrl}/"
        val (responseCode, responseBody) = with(URL(url).openConnection() as HttpURLConnection) {
            requestMethod = "GET"
            connectTimeout = 10000
            readTimeout = 10000
            setRequestProperty("Authorization", "Bearer ${azureClient.getToken(accesstokenScope).accessToken}")
            setRequestProperty("Accept", "application/json")

            val stream: InputStream? = if (responseCode < 300) this.inputStream else this.errorStream
            responseCode to stream?.bufferedReader()?.readText()
        }

        if (responseCode >= 300 || responseBody == null) {
            throw RuntimeException("unknown error (responseCode=$responseCode) from pleiepenger")
        }

        val jsonNode = objectMapper.readTree(responseBody)

        try {
            MDC.put("id", behovId)
            MDC.put("vedtaksperiodeId", vedtaksperiodeId)
            return jsonNode["vedtak"] as ArrayNode
        } finally {
            MDC.remove("id")
            MDC.remove("vedtaksperiodeID")
        }
    }
}