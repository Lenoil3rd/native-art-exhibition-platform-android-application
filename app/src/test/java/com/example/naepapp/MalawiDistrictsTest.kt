package com.example.naepapp

import com.example.naepapp.core.common.MalawiDistricts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MalawiDistrictsTest {

    @Test
    fun districts_countAndContent() {
        val list = MalawiDistricts.all
        assertEquals(28, list.size)
        assertEquals(list.toSet().size, list.size) // No duplicates
        
        // Strictly alphabetical case-insensitive check
        val sorted = list.sortedWith(String.CASE_INSENSITIVE_ORDER)
        assertEquals(sorted, list)

        assertTrue(list.contains("Zomba"))
        assertTrue(list.contains("Nkhata Bay"))
    }

    @Test
    fun matchOrNull_testCases() {
        assertEquals("Zomba", MalawiDistricts.matchOrNull("Zomba, Malawi"))
        assertEquals("Zomba", MalawiDistricts.matchOrNull("zomba"))
        assertNull(MalawiDistricts.matchOrNull("Mzuzu"))
        assertNull(MalawiDistricts.matchOrNull(""))
        assertNull(MalawiDistricts.matchOrNull(null))
    }
}
