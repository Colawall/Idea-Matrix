package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditDao {
    @Query("SELECT * FROM audit_reports ORDER BY generatedAtMillis DESC")
    fun getAllAudits(): Flow<List<AuditEntity>>

    @Query("SELECT * FROM audit_reports WHERE ideaId = :ideaId ORDER BY generatedAtMillis DESC")
    fun getAuditsForIdea(ideaId: Long): Flow<List<AuditEntity>>

    @Query("SELECT * FROM audit_reports WHERE id = :id LIMIT 1")
    suspend fun getAuditById(id: Long): AuditEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudit(audit: AuditEntity): Long

    @Delete
    suspend fun deleteAudit(audit: AuditEntity)

    @Query("DELETE FROM audit_reports WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM audit_reports")
    suspend fun getCount(): Int
}
