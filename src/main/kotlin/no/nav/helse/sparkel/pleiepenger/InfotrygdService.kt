package no.nav.helse.sparkel.pleiepenger

import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.sparkel.pleiepenger.pleiepenger.InfotrygdClient
import no.nav.helse.sparkel.pleiepenger.pleiepenger.Stønadsperiode
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.time.LocalDate

internal class InfotrygdService(private val infotrygdClient: InfotrygdClient) {

    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    private val log = LoggerFactory.getLogger(this::class.java)

    fun løsningForBehov(
        stønadstype: Stønadsperiode.Stønadstype,
        behovId: String,
        vedtaksperiodeId: String,
        fødselsnummer: String,
        fom: LocalDate,
        tom: LocalDate
    ): List<Stønadsperiode> = withMDC("id" to behovId, "vedtaksperiodeId" to vedtaksperiodeId) {
        try {
            val pleiepenger = infotrygdClient.hent(
                stønadstype = stønadstype,
                fnr = fødselsnummer,
                fom = fom,
                tom = tom
            )
                .get("vedtak")
                .map { Stønadsperiode(it) }
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
            throw err
        }
    }
}

private fun <T> withMDC(vararg values: Pair<String, String>, block: () -> T): T = try {
    values.forEach { (key, value) -> MDC.put(key, value) }
    block()
} finally {
    values.forEach { (key, _) -> MDC.remove(key) }
}
