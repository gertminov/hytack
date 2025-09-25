package software.heim.hytack.data.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IntakeDao {
    @Insert
    suspend fun insertIntake(intake: Intake)

    @Query("SELECT * FROM intake ORDER BY timestamp DESC")
    fun getAllIntakes(): Flow<List<Intake>>

    @Query("SELECT * FROM intake WHERE timestamp >= :startDate AND timestamp <= :endDate ORDER BY timestamp DESC")
    fun getIntakesByDateRange(startDate: Long, endDate: Long): Flow<List<Intake>>

    @Query("SELECT * FROM intake ORDER BY timestamp DESC LIMIT :n")
    fun getLastNIntakes(n: Int): Flow<List<Intake>>

    @Query("SELECT * FROM intake ORDER BY timestamp DESC")
    fun getPagedIntakes():PagingSource<Int, Intake>

    @Query("SELECT SUM(amountMl) FROM intake WHERE timestamp >= :startDate AND timestamp <= :endDate")
    suspend fun getTotalIntakeForPeriod(startDate: Long, endDate: Long): Double


    @Query("DELETE FROM intake WHERE id = :intakeId")
    suspend fun deleteIntake(intakeId: Int)
}