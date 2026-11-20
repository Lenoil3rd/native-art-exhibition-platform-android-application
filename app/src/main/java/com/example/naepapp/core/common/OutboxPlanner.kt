package com.example.naepapp.core.common

import com.example.naepapp.core.db.entity.PendingOperationEntity

class OutboxPlanner {
    fun planOperation(
        existingOperations: List<PendingOperationEntity>,
        newOp: PendingOperationEntity
    ): List<PendingOperationEntity> {
        val ops = existingOperations.toMutableList()

        when (newOp.type) {
            "LIKE_SET" -> {
                ops.removeAll { it.type == "LIKE_SET" && it.entityId == newOp.entityId }
                ops.add(newOp)
            }
            "AVAILABILITY_SET" -> {
                ops.removeAll { it.type == "AVAILABILITY_SET" && it.entityId == newOp.entityId }
                ops.add(newOp)
            }
            "CREATE_CREATION" -> {
                ops.add(newOp)
            }
            "UPDATE_CREATION" -> {
                val pendingCreate = ops.find { it.type == "CREATE_CREATION" && it.entityId == newOp.entityId }
                if (pendingCreate != null) {
                    val index = ops.indexOf(pendingCreate)
                    ops[index] = newOp.copy(type = "CREATE_CREATION", id = pendingCreate.id)
                } else {
                    ops.removeAll { it.type == "UPDATE_CREATION" && it.entityId == newOp.entityId }
                    ops.add(newOp)
                }
            }
            "DELETE_CREATION" -> {
                val pendingCreate = ops.find { it.type == "CREATE_CREATION" && it.entityId == newOp.entityId }
                if (pendingCreate != null) {
                    ops.remove(pendingCreate)
                    ops.removeAll { it.entityId == newOp.entityId }
                } else {
                    ops.removeAll { it.entityId == newOp.entityId }
                    ops.add(newOp)
                }
            }
            "RECORD_VIEW" -> {
                ops.removeAll { it.type == "RECORD_VIEW" && it.entityId == newOp.entityId }
                ops.add(newOp)
            }
        }
        return ops
    }
}
