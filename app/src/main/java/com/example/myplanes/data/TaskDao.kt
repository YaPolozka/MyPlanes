package com.example.myplanes.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    // Активные задачи сверху, затем выполненные, затем удалённые; внутри групп по дедлайну
    @Query(
        """
        SELECT * FROM tasks
        ORDER BY deletedAt IS NOT NULL, completedAt IS NOT NULL, deadline IS NULL, deadline ASC
        """
    )
    fun observeAll(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): Task?

    @Insert
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    // completedAt = null снимает галочку
    @Query("UPDATE tasks SET completedAt = :completedAt WHERE id = :id")
    suspend fun setCompleted(id: Long, completedAt: Long?)

    // deletedAt = null восстановит задачу, если понадобится
    @Query("UPDATE tasks SET deletedAt = :deletedAt WHERE id = :id")
    suspend fun setDeleted(id: Long, deletedAt: Long?)

    // Окончательно убирает задачи, у которых позднейшая из дат
    // (выполнения или удаления) старше threshold
    @Query(
        """
        DELETE FROM tasks
        WHERE (completedAt IS NOT NULL OR deletedAt IS NOT NULL)
          AND MAX(IFNULL(completedAt, 0), IFNULL(deletedAt, 0)) < :threshold
        """
    )
    suspend fun purgeOlderThan(threshold: Long)
}