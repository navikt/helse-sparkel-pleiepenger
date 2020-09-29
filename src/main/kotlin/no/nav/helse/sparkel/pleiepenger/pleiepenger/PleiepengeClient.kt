package no.nav.helse.sparkel.pleiepenger.pleiepenger

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
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
    }

    internal fun hentPleiepenger(
        fnr: String,
        fom: LocalDate,
        tom: LocalDate
    ): JsonNode {
        val url = "${baseUrl}/vedtak"

        val requestBody = objectMapper.createObjectNode().apply {
            put("identitetsnummer", fnr)
            put("fom", fom.toString())
            put("tom", tom.toString())
        }

        val (responseCode, responseBody) = with(URL(url).openConnection() as HttpURLConnection) {
            requestMethod = "POST"
            connectTimeout = 10000
            readTimeout = 10000
            setRequestProperty("Authorization", "Bearer ${azureClient.getToken(accesstokenScope).accessToken}")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json")

            doOutput = true
            objectMapper.writeValue(outputStream, requestBody)

            val stream: InputStream? = if (responseCode < 300) this.inputStream else this.errorStream
            responseCode to stream?.bufferedReader()?.readText()
        }

        if (responseCode >= 300 || responseBody == null) {
            throw RuntimeException("unknown error (responseCode=$responseCode) from pleiepenger")
        }

        return objectMapper.readTree(responseBody)
    }
}
