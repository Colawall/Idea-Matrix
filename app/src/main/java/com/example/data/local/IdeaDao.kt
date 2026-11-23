package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface IdeaDao {
    @Query("SELECT * FROM ideas ORDER BY readinessScore DESC, id DESC")
    fun getAllIdeas(): Flow<List<IdeaEntity>>

    @Query("SELECT * FROM ideas WHERE id = :id LIMIT 1")
    fun getIdeaById(id: Long): Flow<IdeaEntity?>

    @Query("SELECT * FROM ideas WHERE firestoreId = :firestoreId LIMIT 1")
    suspend fun getIdeaByFirestoreId(firestoreId: String): IdeaEntity?

    @Query("SELECT * FROM ideas ORDER BY readinessScore DESC LIMIT 2")
    fun getTopFeaturedIdeas(): Flow<List<IdeaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIdea(idea: IdeaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ideas: List<IdeaEntity>)

    @Update
    suspend fun updateIdea(idea: IdeaEntity)

    @Delete
    suspend fun deleteIdea(idea: IdeaEntity)

    @Query("DELETE FROM ideas WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM ideas WHERE firestoreId = :firestoreId")
    suspend fun deleteByFirestoreId(firestoreId: String)

    @Query("SELECT COUNT(*) FROM ideas")
    suspend fun getCount(): Int
}
