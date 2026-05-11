package com.example.pr_6_3.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class TokenDataStore(private val context: Context) {

    private val tokenKey = stringPreferencesKey("auth_token")

    suspend fun saveToken(token: String) {
        context.dataStore.edit { it[tokenKey] = token }
    }

    suspend fun getToken(): String? =
        context.dataStore.data.map { it[tokenKey] }.first()

    suspend fun clearToken() {
        context.dataStore.edit { it.remove(tokenKey) }
    }
}
