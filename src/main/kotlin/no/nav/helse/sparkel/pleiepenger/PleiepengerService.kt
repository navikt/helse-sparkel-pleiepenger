package no.nav.helse.sparkel.pleiepenger

import com.fasterxml.jackson.databind.JsonNode
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.sparkel.pleiepenger.pleiepenger.PleiepengeClient
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.time.LocalDate

internal class PleiepengerService(private val pleiepengeClient: PleiepengeClient) {

    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    private val log = LoggerFactory.getLogger(this::class.java)

    fun løsningForBehov(
            behovId: String,
            vedtaksperiodeId: String,
            fødselsnummer: String,
            fom: LocalDate,
            tom: LocalDate
    ): JsonNode? = withMDC("id" to behovId, "vedtaksperiodeId" to vedtaksperiodeId) {
        try {
            val pleiepenger = pleiepengeClient.hentPleiepenger(
                    fnr = fødselsnummer,
                    fom = fom,
                    tom = tom
            )
            log.info(
                    "løser behov: {} for {}",
                    keyValue("id", behovId),
                    keyValue("vedtaksperiodeId", vedtaksperiodeId)
            )
            sikkerlogg.info(
                    "løser behov: {} for {}",
                    keyValue("id", behovId),
                    keyValue("vedtaksperiodeId", vedtaksperiodeId)
            )
            pleiepenger
        } catch (err: Exception) {
            log.warn(
                    "feil ved henting av pleiepenger-data: ${err.message} for {}",
                    keyValue("vedtaksperiodeId", vedtaksperiodeId),
                    err
            )
            sikkerlogg.warn(
                    "feil ved henting av pleiepenger-data: ${err.message} for {}",
                    keyValue("vedtaksperiodeId", vedtaksperiodeId),
                    err
            )
            null
        }
    }
}

private fun <T> withMDC(vararg values: Pair<String, String>, block: () -> T): T = try {
    values.forEach { (key, value) -> MDC.put(key, value) }
    block()
} finally {
    values.forEach { (key, _) -> MDC.remove(key) }
}
