package com.example.naepapp

import com.example.naepapp.core.common.PriceFormatter
import org.junit.Assert.assertEquals
import org.junit.Test

class PriceFormatterTest {

    @Test
    fun format_nullPrice_returnsPriceOnRequest() {
        val result = PriceFormatter.format(null, "MWK", "Price on request")
        assertEquals("Price on request", result)
    }

    @Test
    fun format_zeroPrice_returnsPriceOnRequest() {
        val result = PriceFormatter.format(0.0, "MWK", "Price on request")
        assertEquals("Price on request", result)
    }

    @Test
    fun format_validPrice_returnsFormattedCurrency() {
        val result = PriceFormatter.format(150000.0, "MWK", "Price on request")
        assertEquals("MWK 150,000", result)
    }
}
