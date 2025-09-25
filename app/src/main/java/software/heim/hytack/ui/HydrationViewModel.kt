package software.heim.hytack.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.insertSeparators
import androidx.paging.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import software.heim.hytack.data.database.AppDataBase
import software.heim.hytack.data.database.Intake
import software.heim.hytack.data.database.toIntake
import software.heim.hytack.data.domain.Milliliter
import software.heim.hytack.data.preferences.UserPreferencesRepository
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed interface TimelineItem {
    val id: Long

    data class IntakeItem(val intake: Intake, override val id: Long = intake.timestamp) :
        TimelineItem

    data class DaySeperator(val dateTString: String, override val id: Long) :
        TimelineItem
}

class HydrationViewModel(application: Application) : AndroidViewModel(application) {
    private val intakeDao = AppDataBase.getDatabase(application).intakeDao()
    private val userPreferencesRepository = UserPreferencesRepository(application)
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY)

    val historyVisible = MutableSharedFlow<Boolean>(1)
    val intakePager = Pager(
        config = PagingConfig(20),
        pagingSourceFactory = { intakeDao.getPagedIntakes() }
    ).flow.map { pagingData ->
        pagingData.map { intake ->
            TimelineItem.IntakeItem(intake)
        }.insertSeparators { before: TimelineItem.IntakeItem?, after: TimelineItem.IntakeItem? ->
            if (before == null) {
                return@insertSeparators null
            }

            val beforeDay = getDayTimestamp(before.intake.timestamp)
            val dateString = if (isTimeStampToday(beforeDay)) "Today" else formatTimestamp(
                beforeDay
            )
            if (after == null) {
                return@insertSeparators TimelineItem.DaySeperator(
                    dateString, beforeDay
                )
            }
            val afterDay = getDayTimestamp(after.intake.timestamp)
            if (beforeDay != afterDay) {
                TimelineItem.DaySeperator(dateString, beforeDay)
            } else {
                null
            }

        }
    }

    private fun getDayTimestamp(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    fun isTimeStampToday(timestamp: Long): Boolean {
        val day = getDayTimestamp(timestamp)
        val today = getDayTimestamp(System.currentTimeMillis())
        Log.d("HydrationViewModel", "isTimeStampToday: $day $today")
        return day == today
    }

    fun formatTimestamp(timestamp: Long): String {
        val date = Date(timestamp)
        return dateFormatter.format(date)
    }

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

    fun getTodaysIntake(): StateFlow<List<Intake>> {
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
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )
    }

    fun deleteIntake(intake: Intake) {
        viewModelScope.launch {
            intakeDao.deleteIntake(intake.id)
        }
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

    fun exportHistory() {

        val dateTimeFormater = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY)
        viewModelScope.launch {
            intakeDao.getAllIntakes().collect { list ->
                val historyCSV = list.joinToString("\n") { intake ->
                    val data = Date(intake.timestamp)
                    "${dateTimeFormater.format(intake.timestamp)},${intake.amountMl}"
                }
                try {
                    val file = createCsvFile(application, historyCSV)
                    shareCsvFile(application, file )
                } catch (e: Exception) {
                    Log.e("HydrationViewModel", "Error creating CSV file", e)
                }
            }
        }
    }
    private suspend fun createCsvFile(context: Context, csvData: String): File {
        return withContext(Dispatchers.IO) { // Perform file operations on IO dispatcher
            val cachePath = File(context.cacheDir, "exports/")
            cachePath.mkdirs() // Create 'exports' directory if it doesn't exist
            val file = File(cachePath, "hydration_history.csv")
            FileOutputStream(file).use {
                it.write(csvData.toByteArray())
            }
            file
        }
    }

    private fun shareCsvFile(context: Context, file: File) {
        // Use FileProvider to get a content URI
        // Make sure to configure FileProvider in your AndroidManifest.xml
        // and create a provider_paths.xml file
        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider", // Authority matches your FileProvider definition
            file
        )

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, contentUri)
            type = "text/csv"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grant read permission to the receiving app
        }
        // Create a chooser so the user can pick an app
        val chooserIntent = Intent.createChooser(shareIntent, "Share History As")
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Necessary when starting from a non-Activity context

        context.startActivity(chooserIntent)
    }
}
