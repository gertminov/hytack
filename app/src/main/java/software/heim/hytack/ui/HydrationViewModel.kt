package software.heim.hytack.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import software.heim.hytack.data.database.AppDataBase
import software.heim.hytack.data.database.Intake
import software.heim.hytack.data.database.toIntake
import software.heim.hytack.data.domain.Milliliter
import software.heim.hytack.data.preferences.UserPreferencesRepository
import java.util.Calendar

class HydrationViewModel(application: Application) : AndroidViewModel(application) {
    private val intakeDao = AppDataBase.getDatabase(application).intakeDao()
    private val userPreferencesRepository = UserPreferencesRepository(application)
    val shortcuts = combine(
        userPreferencesRepository.shortcutOneMl,
        userPreferencesRepository.shortcutTwoMl,
        userPreferencesRepository.shortcutThreeMl,
    ) { shortcutOneMl, shortcutTwoMl, shortcutThreeMl ->
        listOf<Triple<Milliliter?, () -> Unit, (Milliliter) -> Unit>>(
            Triple(
                shortcutOneMl,
                { addShortcutIntake(shortcutOneMl) },
                { ml: Milliliter ->
                    Log.d("HydrationViewModel", "editShortcut: $ml")
                    editShortcut(0, ml)
                }),
            Triple(
                shortcutTwoMl,
                { addShortcutIntake(shortcutTwoMl) },
                { ml: Milliliter -> editShortcut(1, ml) }),
            Triple(
                shortcutThreeMl,
                { addShortcutIntake(shortcutThreeMl) },
                { ml: Milliliter -> editShortcut(2, ml) }),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private fun addShortcutIntake(amount: Milliliter?) {
        amount?.let { addIntake(it) }
    }


    val dailyGoal = userPreferencesRepository.dailyGoalMl.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Milliliter(2000)
    )

    fun addIntake(amount: Milliliter) {
        viewModelScope.launch {
            intakeDao.insertIntake(amount.toIntake())
        }
    }

    fun editShortcut(index: Int, amount: Milliliter) {
        viewModelScope.launch {
            when (index) {
                0 -> userPreferencesRepository.updateShortcutOneMl(amount)
                1 -> userPreferencesRepository.updateShortcutTwoMl(amount)
                2 -> userPreferencesRepository.updateShortcutThreeMl(amount)
            }
        }
    }

    fun updateDailyGoal(amount: Milliliter) {
        viewModelScope.launch {
            userPreferencesRepository.updateDailyGoalMl(amount)
        }
    }

    fun getTodaysIntake(): Flow<List<Intake>> {
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayEnd = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return intakeDao.getIntakesByDateRange(todayStart.timeInMillis, todayEnd.timeInMillis)
    }

    fun getTodaysTotal(): StateFlow<Milliliter> {
        return getTodaysIntake()
            .map { list -> Milliliter(list.sumOf { it.amountMl }) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = Milliliter(100)
            )
    }
}
