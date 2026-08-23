package com.ferhatozcelik.jetpackcomposetemplate.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ferhatozcelik.jetpackcomposetemplate.data.entity.ExampleEntity

@Dao
interface ExampleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ExampleEntity)

    @Query("SELECT * FROM example_table")
    suspend fun getAll(): List<ExampleEntity>
}
