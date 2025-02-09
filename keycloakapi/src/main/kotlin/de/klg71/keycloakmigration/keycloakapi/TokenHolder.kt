package de.klg71.keycloakmigration.keycloakapi

import de.klg71.keycloakmigration.keycloakapi.model.AccessToken
import org.slf4j.LoggerFactory
import java.lang.System.currentTimeMillis
import java.util.concurrent.TimeUnit.SECONDS

/**
 * Manages the keycloak access tokens and refreshes if needed
 */
internal class TokenHolder(private val client: KeycloakLoginClient,
    private val adminUser: String, private val adminPassword: String,
    private val realm: String, private val clientId: String,
    private val totp: String) {

    companion object {
        val LOG = LoggerFactory.getLogger(TokenHolder::class.java)!!
    }

    private var token: AccessToken = client.login(realm, "password", clientId, adminUser, adminPassword, totp)
    private var tokenReceived: Long = currentTimeMillis()

    private fun tokenExpired() = currentTimeMillis() - tokenReceived > SECONDS.toMillis(token.expiresIn)
    private fun refreshExpired() = currentTimeMillis() - tokenReceived > SECONDS.toMillis(token.refreshExpiresIn)

    fun token(): AccessToken {
        if (tokenExpired()) {
            LOG.info("Token expired retrieving new one.")
            token = getNewToken()
            tokenReceived = currentTimeMillis()
        }
        return token
    }

    private fun getNewToken() = if (!refreshExpired()) {
        LOG.info("using refreshToken")
        client.login(realm, "refresh_token", token.refreshToken, clientId)
    } else {
        LOG.info("using password")
        client.login(realm, "password", clientId, adminUser, adminPassword, totp)
    }
}
