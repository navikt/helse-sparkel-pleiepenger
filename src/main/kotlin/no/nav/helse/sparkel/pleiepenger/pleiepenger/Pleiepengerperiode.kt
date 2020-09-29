package no.nav.helse.sparkel.pleiepenger.pleiepenger

import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDate

class Pleiepengerperiode(jsonNode: JsonNode) {
    val fom: LocalDate = jsonNode["fom"].textValue().let { LocalDate.parse(it) }
    val tom: LocalDate = jsonNode["tom"].textValue().let { LocalDate.parse(it) }
    val grad: Int = jsonNode["grad"].intValue()
}
