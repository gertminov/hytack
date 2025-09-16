package software.heim.hytack.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import software.heim.hytack.data.domain.Milliliter

@Entity(tableName = "intake")
data class Intake(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amountMl: Int,
    val timestamp: Long = System.currentTimeMillis()
)

fun Milliliter.toIntake() = Intake(amountMl = this.value)