package com.example.naepapp.core.common

import java.text.NumberFormat
import java.util.Locale

object PriceFormatter {
    fun format(price: Double?, currency: String, priceOnRequest: String): String {
        return if (price == null || price <= 0.0) {
            priceOnRequest
        } else {
            "$currency ${NumberFormat.getNumberInstance(Locale.US).format(price)}"
        }
    }
}
