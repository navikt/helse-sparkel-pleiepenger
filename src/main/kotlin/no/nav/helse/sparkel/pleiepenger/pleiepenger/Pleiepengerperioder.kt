package no.nav.helse.sparkel.pleiepenger.pleiepenger

import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDate

class Pleiepengerperioder(jsonNode: JsonNode) {
    val perioder = jsonNode["vedtak"].map { Pleiepengerperiode(it) }
}

data class Pleiepengerperiode(private val jsonNode: JsonNode) {
    val fom: LocalDate? = jsonNode["fom"]?.takeUnless { it.isNull }?.textValue()?.let { LocalDate.parse(it) }
    val tom: LocalDate? = jsonNode["tom"]?.takeUnless { it.isNull }?.textValue()?.let { LocalDate.parse(it) }
    val grad: Int = jsonNode["grad"].intValue()
}
