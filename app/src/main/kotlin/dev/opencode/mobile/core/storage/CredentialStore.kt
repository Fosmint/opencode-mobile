package dev.opencode.mobile.core.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Stores server Basic-Auth passwords using Android Keystore-backed
 * encryption, never in the Room database or DataStore preferences in
 * plaintext (see requirement: "Секреты не храни в обычном plaintext
 * database").
 */
class CredentialStore(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "opencode_credentials",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun savePassword(credentialRef: String, password: String) {
        prefs.edit().putString(credentialRef, password).apply()
    }

    fun getPassword(credentialRef: String): String? = prefs.getString(credentialRef, null)

    fun deletePassword(credentialRef: String) {
        prefs.edit().remove(credentialRef).apply()
    }
}
