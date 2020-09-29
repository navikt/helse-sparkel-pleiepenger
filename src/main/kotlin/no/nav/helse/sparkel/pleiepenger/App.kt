package no.nav.helse.sparkel.pleiepenger

import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.pleiepenger.pleiepenger.AzureClient
import no.nav.helse.sparkel.pleiepenger.pleiepenger.PleiepengeClient
import java.io.File
import java.io.FileNotFoundException

fun main() {
    val app = createApp(System.getenv())
    app.start()
}

internal fun createApp(env: Map<String, String>): RapidsConnection {
    val azureClient = AzureClient(
        tenantUrl = "${env.getValue("AZURE_TENANT_BASEURL")}/${env.getValue("AZURE_TENANT_ID")}",
        clientId = env.getValue("AZURE_APP_CLIENT_ID"),
        clientSecret = env.getValue("AZURE_APP_CLIENT_SECRET")
    )
    val pleiepengeClient = PleiepengeClient(
        baseUrl = env.getValue("PLEIEPENGER_URL"),
        accesstokenScope = env.getValue("PLEIEPENGER_SCOPE"),
        azureClient = azureClient
    )
    val pleiepengerService = PleiepengerService(pleiepengeClient)

    return RapidApplication.create(env).apply {
        Pleiepengerløser(this, pleiepengerService)
    }
}
