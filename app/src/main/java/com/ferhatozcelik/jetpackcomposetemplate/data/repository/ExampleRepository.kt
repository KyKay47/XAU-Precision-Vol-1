package com.ferhatozcelik.jetpackcomposetemplate.data.repository

import com.ferhatozcelik.jetpackcomposetemplate.data.dao.ExampleDao
import com.ferhatozcelik.jetpackcomposetemplate.data.entity.ExampleEntity
import javax.inject.Inject

class ExampleRepository @Inject constructor(
    private val exampleDao: ExampleDao
) {
    suspend fun getEntities(): List<ExampleEntity> {
        return exampleDao.getAll()
    }

    suspend fun insertEntity(entity: ExampleEntity) {
        exampleDao.insert(entity)
    }
}
