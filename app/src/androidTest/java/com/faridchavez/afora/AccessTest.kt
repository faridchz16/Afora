package com.faridchavez.afora

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

class AccessTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun validatesRegistrationBeforeConnecting() {
        compose.setContent { PantallaLogin(FirebaseSession(context)) {} }
        compose.onNodeWithText("Ingresar").performClick()
        compose.onNodeWithText("Completa todos los campos").assertIsDisplayed()
        compose.onNodeWithText("Crear una cuenta").performClick()
        compose.onNodeWithText("Nombre").performTextInput("Ana AFORA")
        compose.onNodeWithText("Correo").performTextInput("ana@example.test")
        compose.onNodeWithText("Contraseña").performTextInput("Password123")
        compose.onNodeWithText("Confirmar contraseña").performTextInput("OtraClave123")
        compose.onNodeWithText("Registrarme").performScrollTo().performClick()
        compose.onNodeWithText("Las contraseñas no coinciden").assertIsDisplayed()
    }

    @Test fun sessionSurvivesRecreationEncryptedAndLogoutClearsIt() {
        val store = SessionStore(context)
        store.clear()
        try {
            store.save("fake-refresh-token-for-test")
            assertEquals("fake-refresh-token-for-test", SessionStore(context).read())
            val raw = context.getSharedPreferences("afora_session", 0).getString("token", "")!!
            assertFalse(raw.contains("fake-refresh-token-for-test"))
            store.clear()
            assertNull(SessionStore(context).read())
        } finally { store.clear() }
    }
}
