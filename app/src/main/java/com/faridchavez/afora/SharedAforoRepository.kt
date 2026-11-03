package com.faridchavez.afora

import org.json.JSONObject

data class SharedAforo(val people: Int, val capacity: Int, val updatedAt: Long)

/** Student client deliberately exposes no write operation. Authorization is enforced by database rules. */
class SharedAforoRepository(private val session: FirebaseSession) {
    fun fetch(): SharedAforo {
        val response = RestTransport.request("${session.databaseUrl}/aforo.json?auth=${FirebaseSession.encode(session.token())}")
        check(response.status in 200..299) { "No se pudo consultar el aforo. Revisa tu conexión o vuelve a iniciar sesión." }
        check(response.body != "null") { "El controlador aún no ha registrado el aforo." }
        val json = JSONObject(response.body)
        val people = json.getInt("currentPeople")
        val capacity = json.getInt("maxCapacity")
        check(capacity > 0 && people in 0..capacity) { "El servidor devolvió un aforo inválido." }
        return SharedAforo(people, capacity, json.getLong("lastUpdated"))
    }
}
