package software.heim.hytack.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Intake::class], version = 1, exportSchema = false)
abstract class AppDataBase: RoomDatabase() {
    abstract fun intakeDao(): IntakeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDataBase? = null

        fun getDatabase(context: Context): AppDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context = context.applicationContext,
                    klass = AppDataBase::class.java,
                    name = "inake_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}