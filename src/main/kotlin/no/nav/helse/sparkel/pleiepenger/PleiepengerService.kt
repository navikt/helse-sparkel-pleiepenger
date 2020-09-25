package no.nav.helse.sparkel.pleiepenger

import com.fasterxml.jackson.databind.JsonNode
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.sparkel.pleiepenger.pleiepenger.PleiepengeClient
import org.slf4j.LoggerFactory
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
    ): JsonNode? {
        try {
            val pleiepenger = pleiepengeClient.hentPleiepenger(
                behovId = behovId,
                vedtaksperiodeId = vedtaksperiodeId,
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
            return pleiepenger
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
            return null
        }
    }

}
