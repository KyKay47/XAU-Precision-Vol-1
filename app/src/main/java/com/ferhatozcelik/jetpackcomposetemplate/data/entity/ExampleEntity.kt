package com.ferhatozcelik.jetpackcomposetemplate.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "example_table")
data class ExampleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)
