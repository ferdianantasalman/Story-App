package com.example.androidintermediate.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.androidintermediate.helper.Helper
import com.example.androidintermediate.utils.Constant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constant.preferenceName)

class SettingPreferences constructor(private val dataStore: DataStore<Preferences>) {

    private val token = stringPreferencesKey(Constant.UserPreferences.UserToken.name)
    private val uid = stringPreferencesKey(Constant.UserPreferences.UserUID.name)
    private val name = stringPreferencesKey(Constant.UserPreferences.UserName.name)
    private val email = stringPreferencesKey(Constant.UserPreferences.UserEmail.name)
    private val lastLogin = stringPreferencesKey(Constant.UserPreferences.UserLastLogin.name)

    fun getUserToken(): Flow<String> = dataStore.data.map { it[token] ?: Constant.preferenceDefaultValue }

    fun getUserUid(): Flow<String> = dataStore.data.map { it[uid] ?: Constant.preferenceDefaultValue }

    fun getUserName(): Flow<String> = dataStore.data.map { it[name] ?: Constant.preferenceDefaultValue }

    fun getUserEmail(): Flow<String> = dataStore.data.map { it[email] ?: Constant.preferenceDefaultValue }

    fun getUserLastLogin(): Flow<String> = dataStore.data.map { it[lastLogin] ?: Constant.preferenceDefaultDateValue }

    suspend fun saveLoginSession(userToken: String, userUid: String, userName:String, userEmail: String) {
        dataStore.edit { preferences ->
            preferences[token] = userToken
            preferences[uid] = userUid
            preferences[name] = userName
            preferences[email] = userEmail
            preferences[lastLogin] = Helper.getCurrentDateString()
        }
    }

    suspend fun clearLoginSession() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: SettingPreferences? = null
        fun getInstance(dataStore: DataStore<Preferences>): SettingPreferences {
            return INSTANCE ?: synchronized(this) {
                val instance = SettingPreferences(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}