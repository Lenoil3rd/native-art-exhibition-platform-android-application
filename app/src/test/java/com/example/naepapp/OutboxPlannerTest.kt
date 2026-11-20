package com.example.naepapp

import com.example.naepapp.core.common.OutboxPlanner
import com.example.naepapp.core.db.entity.PendingOperationEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OutboxPlannerTest {

    private val planner = OutboxPlanner()

    @Test
    fun likeThenUnlike_keepsLatest() {
        val op1 = PendingOperationEntity(id = 1, userId = "u1", type = "LIKE_SET", entityId = "c1", payloadJson = "{\"liked\":true}")
        val op2 = PendingOperationEntity(id = 2, userId = "u1", type = "LIKE_SET", entityId = "c1", payloadJson = "{\"liked\":false}")

        val result1 = planner.planOperation(emptyList(), op1)
        val result2 = planner.planOperation(result1, op2)

        assertEquals(1, result2.size)
        assertEquals("{\"liked\":false}", result2[0].payloadJson)
    }

    @Test
    fun createThenDelete_removesBoth() {
        val createOp = PendingOperationEntity(id = 1, userId = "u1", type = "CREATE_CREATION", entityId = "c1", payloadJson = "{}")
        val deleteOp = PendingOperationEntity(id = 2, userId = "u1", type = "DELETE_CREATION", entityId = "c1", payloadJson = "{}")

        val result1 = planner.planOperation(emptyList(), createOp)
        val result2 = planner.planOperation(result1, deleteOp)

        assertTrue(result2.isEmpty())
    }

    @Test
    fun updateMergedIntoCreate() {
        val createOp = PendingOperationEntity(id = 1, userId = "u1", type = "CREATE_CREATION", entityId = "c1", payloadJson = "{\"title\":\"Old\"}")
        val updateOp = PendingOperationEntity(id = 2, userId = "u1", type = "UPDATE_CREATION", entityId = "c1", payloadJson = "{\"title\":\"New\"}")

        val result1 = planner.planOperation(emptyList(), createOp)
        val result2 = planner.planOperation(result1, updateOp)

        assertEquals(1, result2.size)
        assertEquals("CREATE_CREATION", result2[0].type)
        assertEquals("{\"title\":\"New\"}", result2[0].payloadJson)
    }
}
