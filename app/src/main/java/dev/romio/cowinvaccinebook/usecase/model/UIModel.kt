package dev.romio.cowinvaccinebook.usecase.model


enum class VaccineType(val vaccine: String) {
    NONE("NONE"),
    COVAXIN("COVAXIN"),
    COVISHIELD("COVISHIELD")
}

enum class FeeType(val type: String) {
    NONE("None"),
    FREE("Free"),
    PAID("Paid")
}

enum class AgeGroup(val age: Int) {
    FORTY_FIVE_PLUS(45), EIGHTEEN_PLUS(18)
}

enum class PinOrDistrictPref {
    PIN, DISTRICT
}