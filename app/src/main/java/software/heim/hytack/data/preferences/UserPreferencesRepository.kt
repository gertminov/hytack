package software.heim.hytack.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import software.heim.hytack.data.domain.Milliliter
import software.heim.hytack.data.domain.mapper
import software.heim.hytack.data.domain.milliliter

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val DAILY_GOAL = intPreferencesKey("daily_goal")
        val SHORTCUT_ONE_ML = intPreferencesKey("shortcut_one")
        val SHORTCUT_TWO_ML = intPreferencesKey("shortcut_two")
        val SHORTCUT_THREE_ML = intPreferencesKey("shortcut_three")
    }

    val dailyGoalMl = getMl(PreferencesKeys.DAILY_GOAL, 2000)
    val shortcutOneMl = getMlOrNull(PreferencesKeys.SHORTCUT_ONE_ML)
    val shortcutTwoMl  = getMlOrNull(PreferencesKeys.SHORTCUT_TWO_ML )
    val shortcutThreeMl = getMlOrNull(PreferencesKeys.SHORTCUT_THREE_ML)


    suspend fun updateDailyGoalMl(dailyGoalMl: Milliliter) = updateML(PreferencesKeys.DAILY_GOAL, dailyGoalMl)
    suspend fun updateShortcutOneMl(shortcutOneMl: Milliliter) = updateML(PreferencesKeys.SHORTCUT_ONE_ML, shortcutOneMl)
    suspend fun updateShortcutTwoMl(shortcutTwoMl: Milliliter) = updateML(PreferencesKeys.SHORTCUT_TWO_ML, shortcutTwoMl)
    suspend fun updateShortcutThreeMl(shortcutThreeMl: Milliliter) = updateML(PreferencesKeys.SHORTCUT_THREE_ML, shortcutThreeMl)

    private suspend fun updateML(key: Preferences.Key<Int>, ml: Milliliter) {
        context.dataStore.edit { preferences ->
            preferences[key] = ml.value
        }
    }

    private fun getMl(key: Preferences.Key<Int>, fallback: Int): Flow<Milliliter> {
        return context.dataStore.data
            .map { preferences ->
                toMl( preferences[key] ?: fallback)
            }
    }
    private fun getMlOrNull(key: Preferences.Key<Int> ): Flow<Milliliter?> {
        return context.dataStore.data
            .map { preferences ->
                val value = preferences[key]
                value?.let { toMl(it) }
            }
    }

    private fun toMl(value: Int) = value.mapper().milliliter()
}