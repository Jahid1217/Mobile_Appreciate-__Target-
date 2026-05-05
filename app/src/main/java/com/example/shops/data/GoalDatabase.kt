package com.example.shops.data

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val type: String,
    @ColumnInfo(name = "target_value") val targetValue: Float,
    val unit: String,
    @ColumnInfo(name = "start_date") val startDate: String,
    @ColumnInfo(name = "end_date") val endDate: String,
    @ColumnInfo(name = "reminder_enabled") val reminderEnabled: Boolean,
    @ColumnInfo(name = "reminder_hour") val reminderHour: Int,
    @ColumnInfo(name = "reminder_minute") val reminderMinute: Int
)

@Entity(
    tableName = "check_ins",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goal_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("goal_id"), Index("entry_date")]
)
data class CheckInEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "goal_id") val goalId: String,
    @ColumnInfo(name = "entry_date") val entryDate: String,
    val value: Float
)

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY start_date ASC, name ASC")
    fun getGoalsFlow(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM check_ins")
    fun getCheckInsFlow(): Flow<List<CheckInEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGoal(goal: GoalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: CheckInEntity)

    @Query("DELETE FROM goals WHERE id = :goalId")
    suspend fun deleteGoal(goalId: String)
}

@Database(entities = [GoalEntity::class, CheckInEntity::class], version = 1, exportSchema = false)
abstract class GoalDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao

    companion object {
        @Volatile
        private var instance: GoalDatabase? = null

        fun getInstance(context: Context): GoalDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    GoalDatabase::class.java,
                    "goal_tracker.db"
                ).build().also { instance = it }
            }
        }
    }
}
