package no.nav.helse.sparkel.pleiepenger

import com.fasterxml.jackson.databind.JsonNode
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.rapids_rivers.*
import no.nav.helse.sparkel.pleiepenger.pleiepenger.Pleiepengerperiode
import org.slf4j.LoggerFactory

internal class Pleiepengerløser(
        rapidsConnection: RapidsConnection,
        private val pleiepengerService: PleiepengerService
) : River.PacketListener {

    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

    companion object {
        const val behov = "Pleiepenger"
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandAll("@behov", listOf(behov)) }
            validate { it.rejectKey("@løsning") }
            validate { it.requireKey("@id") }
            validate { it.requireKey("fødselsnummer") }
            validate { it.requireKey("vedtaksperiodeId") }
            validate { it.require("pleiepengerFom", JsonNode::asLocalDate) }
            validate { it.require("pleiepengerTom", JsonNode::asLocalDate) }
        }.register(this)
    }

    override fun onError(problems: MessageProblems, context: RapidsConnection.MessageContext) {
        sikkerlogg.error("forstod ikke $behov med melding\n${problems.toExtendedReport()}")
    }

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext) {
        sikkerlogg.info("mottok melding: ${packet.toJson()}")
        pleiepengerService.løsningForBehov(
                packet["@id"].asText(),
                packet["vedtaksperiodeId"].asText(),
                packet["fødselsnummer"].asText(),
                packet["pleiepengerFom"].asLocalDate(),
                packet["pleiepengerTom"].asLocalDate()
        ).let { løsning ->
            packet["@løsning"] = mapOf(
                    behov to (løsning?.get("vedtak")?.map { Pleiepengerperiode(it) } ?: emptyList())
            )
            context.send(packet.toJson().also { json ->
                sikkerlogg.info(
                        "sender svar {} for {}:\n\t{}",
                        keyValue("id", packet["@id"].asText()),
                        keyValue("vedtaksperiodeId", packet["vedtaksperiodeId"].asText()),
                        json
                )
            })
        }
    }
}
