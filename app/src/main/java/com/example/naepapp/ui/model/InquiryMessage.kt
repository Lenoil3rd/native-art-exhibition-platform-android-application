package com.example.naepapp.ui.model

import java.text.NumberFormat
import java.util.Locale

// ui/model/InquiryMessage.kt  — MODEL: the pre-filled WhatsApp text (FR-13), built from the creation's details
fun inquiryMessage(c: CreationUi): String = buildString {
    appendLine("Hello, I am interested in this creation on NAEP:")
    appendLine("Title: ${c.title}")
    appendLine("Category: ${c.categoryName}")
    appendLine("Dimensions: ${c.dimensions}")
    appendLine("Description: ${c.description}")
    c.price?.let { appendLine("Price: ${c.currency} ${NumberFormat.getNumberInstance(Locale.US).format(it)}") }
    appendLine("Availability: ${if (c.available) "Readily Available" else "Unavailable"}")
    append("View creation: https://example.com/creations/${c.id}")   // TODO real domain / App Link later
}