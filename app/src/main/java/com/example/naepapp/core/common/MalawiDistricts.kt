package com.example.naepapp.core.common

object MalawiDistricts {
    val all: List<String> = listOf(
        "Balaka", "Blantyre", "Chikwawa", "Chiradzulu", "Chitipa", "Dedza", "Dowa",
        "Karonga", "Kasungu", "Likoma", "Lilongwe", "Machinga", "Mangochi", "Mchinji",
        "Mulanje", "Mwanza", "Mzimba", "Neno", "Nkhata Bay", "Nkhotakota", "Nsanje",
        "Ntcheu", "Ntchisi", "Phalombe", "Rumphi", "Salima", "Thyolo", "Zomba"
    ).sortedWith(String.CASE_INSENSITIVE_ORDER)

    fun matchOrNull(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val cleaned = raw.trim().substringBefore(',').trim()
        return all.firstOrNull { it.equals(cleaned, ignoreCase = true) }
    }
}
