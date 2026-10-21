package com.mujin.chadak.navi

/**
 * Parses the flat seed array in assets/repair_shops.json. The file is a list of
 * objects with string and number fields only, so the app does not take a JSON library.
 */
object RepairShopJson {
    fun parse(json: String): List<RepairShop> {
        val shops = mutableListOf<RepairShop>()
        val objects = Regex("""\{([^{}]*)\}""")
        for (match in objects.findAll(json)) {
            val body = match.groupValues[1]
            shops += RepairShop(
                id = stringField(body, "id"),
                name = stringField(body, "name"),
                latitude = numberField(body, "latitude"),
                longitude = numberField(body, "longitude"),
                address = stringField(body, "address"),
                poiId = stringField(body, "poiId"),
                poiSubId = optionalString(body, "poiSubId").orEmpty(),
            )
        }
        return shops
    }

    private fun stringField(body: String, name: String): String {
        return optionalString(body, name) ?: error("repair shop is missing $name")
    }

    private fun optionalString(body: String, name: String): String? {
        return Regex(""""${Regex.escape(name)}"\s*:\s*"([^"]*)"""").find(body)?.groupValues?.get(1)
    }

    private fun numberField(body: String, name: String): Double {
        val raw = Regex(""""${Regex.escape(name)}"\s*:\s*(-?\d+(?:\.\d+)?)""")
            .find(body)
            ?.groupValues
            ?.get(1)
            ?: error("repair shop is missing $name")
        return raw.toDouble()
    }
}
