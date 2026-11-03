package com.faridchavez.afora

import android.content.Context
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import java.net.Socket

/** Platform HTTPS only: no Firebase SDK or Google Play Services requirement. */
object RestTransport {
    data class Reply(val status: Int, val body: String)
    fun request(url: String, body: String? = null, form: Boolean = false): Reply {
        require(url.startsWith("https://")) { "Se requiere HTTPS." }
        val c = URL(url).openConnection() as HttpsURLConnection
        try {
            c.sslSocketFactory = Tls12Factory(HttpsURLConnection.getDefaultSSLSocketFactory())
            c.connectTimeout = 10000
            c.readTimeout = 15000
            c.instanceFollowRedirects = false
            c.setRequestProperty("Accept", "application/json")
            if (body != null) {
                c.requestMethod = "POST"
                c.doOutput = true
                c.setRequestProperty("Content-Type", if (form) "application/x-www-form-urlencoded" else "application/json; charset=utf-8")
                c.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            }
            val status = c.responseCode
            val stream = if (status >= 400) c.errorStream else c.inputStream
            return Reply(status, stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: "")
        } finally { c.disconnect() }
    }
    private class Tls12Factory(private val delegate: SSLSocketFactory) : SSLSocketFactory() {
        override fun getDefaultCipherSuites(): Array<String> = delegate.defaultCipherSuites
        override fun getSupportedCipherSuites(): Array<String> = delegate.supportedCipherSuites
        private fun tls(socket: Socket): Socket {
            if (socket is SSLSocket) {
                check(socket.supportedProtocols.contains("TLSv1.2")) { "El dispositivo no admite TLS 1.2." }
                socket.enabledProtocols = arrayOf("TLSv1.2")
            }
            return socket
        }
        override fun createSocket(s: Socket, h: String, p: Int, close: Boolean): Socket = tls(delegate.createSocket(s,h,p,close))
        override fun createSocket(h: String, p: Int): Socket = tls(delegate.createSocket(h,p))
        override fun createSocket(h: String, p: Int, a: java.net.InetAddress, lp: Int): Socket = tls(delegate.createSocket(h,p,a,lp))
        override fun createSocket(a: java.net.InetAddress, p: Int): Socket = tls(delegate.createSocket(a,p))
        override fun createSocket(a: java.net.InetAddress, p: Int, l: java.net.InetAddress, lp: Int): Socket = tls(delegate.createSocket(a,p,l,lp))
    }
}

class FirebaseSession(context: Context) {
    private val apiKey = context.getString(R.string.firebase_web_api_key)
    val databaseUrl = context.getString(R.string.firebase_database_url).trimEnd('/')
    private var idToken = ""
    private var refreshToken = ""
    private var expiresAt = 0L
    private var displayName = ""
    var onRefresh: ((String) -> Unit)? = null
    val signedIn: Boolean @Synchronized get() = refreshToken.isNotEmpty()

    private fun auth(action: String, body: JSONObject): JSONObject {
        check(apiKey.isNotBlank() && !apiKey.startsWith("CONFIGURAR")) { "Configura firebase_web_api_key antes de conectar." }
        return checked(RestTransport.request("https://identitytoolkit.googleapis.com/v1/accounts:$action?key=${encode(apiKey)}", body.toString()))
    }
    private fun checked(reply: RestTransport.Reply): JSONObject {
        if (reply.status !in 200..299) {
            val code = try { JSONObject(reply.body).getJSONObject("error").getString("message") } catch (_: Exception) { "" }
            val message = when {
                code.startsWith("EMAIL_EXISTS") -> "Este correo ya está registrado. Inicia sesión."
                code.startsWith("WEAK_PASSWORD") || code.startsWith("PASSWORD_DOES_NOT_MEET_REQUIREMENTS") -> "La contraseña no cumple la política del servicio. Usa al menos 6 caracteres."
                code in listOf("INVALID_LOGIN_CREDENTIALS", "INVALID_PASSWORD", "EMAIL_NOT_FOUND") -> "Correo o contraseña incorrectos."
                code == "INVALID_EMAIL" -> "Ingresa un correo válido."
                code == "TOO_MANY_ATTEMPTS_TRY_LATER" -> "Demasiados intentos. Espera unos minutos."
                else -> "No se pudo autenticar (${reply.status}). Comprueba la conexión y la configuración de Firebase."
            }
            throw IllegalStateException(message)
        }
        return JSONObject(reply.body)
    }
    @Synchronized fun login(email: String, password: String): String {
        val json = auth("signInWithPassword", JSONObject().put("email", email.trim()).put("password", password).put("returnSecureToken", true))
        val profile = auth("lookup", JSONObject().put("idToken", json.getString("idToken"))).getJSONArray("users").getJSONObject(0)
        accept(json)
        displayName = profile.optString("displayName").ifBlank { email.substringBefore('@') }
        return displayName
    }
    @Synchronized fun register(name: String, email: String, password: String): String {
        // Persist the profile explicitly so the name is available at subsequent logins.
        val json = auth("signUp", JSONObject().put("email", email.trim()).put("password", password)
            .put("displayName", name.trim()).put("returnSecureToken", true))
        try {
            auth("update", JSONObject().put("idToken", json.getString("idToken")).put("displayName", name.trim()).put("returnSecureToken", true))
        } catch (e: Exception) {
            throw IllegalStateException("La cuenta fue creada, pero no se pudo guardar el nombre. Inicia sesión con tu correo y contraseña.", e)
        }
        accept(json)
        displayName = name.trim()
        return displayName
    }
    private fun accept(json: JSONObject) {
        idToken = json.getString("idToken")
        refreshToken = json.getString("refreshToken")
        expiresAt = System.currentTimeMillis() + json.getString("expiresIn").toLong() * 1000 - 60000
        onRefresh?.invoke(refreshToken)
    }
    @Synchronized fun restore(saved: String): String {
        refreshToken = saved
        expiresAt = 0
        val profile = auth("lookup", JSONObject().put("idToken", token())).getJSONArray("users").getJSONObject(0)
        displayName = profile.optString("displayName").ifBlank { profile.getString("email").substringBefore('@') }
        return displayName
    }
    @Synchronized fun token(): String {
        check(refreshToken.isNotEmpty()) { "Inicia sesión para continuar." }
        if (System.currentTimeMillis() >= expiresAt) {
            val json = checked(RestTransport.request("https://securetoken.googleapis.com/v1/token?key=${encode(apiKey)}",
                "grant_type=refresh_token&refresh_token=${encode(refreshToken)}", true))
            idToken = json.getString("id_token")
            refreshToken = json.getString("refresh_token")
            expiresAt = System.currentTimeMillis() + json.getString("expires_in").toLong() * 1000 - 60000
            onRefresh?.invoke(refreshToken)
        }
        return idToken
    }
    @Synchronized fun logout() { idToken = ""; refreshToken = ""; displayName = ""; expiresAt = 0 }
    companion object { fun encode(value: String): String = URLEncoder.encode(value, "UTF-8") }
}
