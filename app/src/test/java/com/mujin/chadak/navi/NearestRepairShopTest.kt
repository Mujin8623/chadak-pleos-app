package com.mujin.chadak.navi

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NearestRepairShopTest {
    private val gangnam = shop("gangnam", 37.4979, 127.0276)
    private val jamsil = shop("jamsil", 37.5133, 127.1028)
    private val mapo = shop("mapo", 37.5563, 126.9236)
    private val shops = listOf(jamsil, mapo, gangnam)

    @Test
    fun picksTheNearestShop() {
        val nearest = NearestRepairShop.pick(GeoPoint(37.4980, 127.0280), shops)
        assertEquals("gangnam", nearest?.id)
    }

    @Test
    fun emptyCatalogHasNoPlan() {
        assertNull(NearestRepairShop.plan(GeoPoint(0.0, 0.0), emptyList(), hasActiveRoute = true))
    }

    @Test
    fun activeRouteAddsTheFirstWaypoint() {
        val plan = NearestRepairShop.plan(GeoPoint(37.4980, 127.0280), shops, hasActiveRoute = true)
        assertEquals(WaypointAction.ADD_FIRST_WAYPOINT, plan?.action)
    }

    @Test
    fun missingRouteRequestsANewRoute() {
        val plan = NearestRepairShop.plan(GeoPoint(37.4980, 127.0280), shops, hasActiveRoute = false)
        assertEquals(WaypointAction.REQUEST_ROUTE, plan?.action)
        assertEquals("gangnam", plan?.shop?.id)
    }

    @Test
    fun seedJsonParsesThreeShops() {
        val file = listOf(
            File("src/main/assets/repair_shops.json"),
            File("app/src/main/assets/repair_shops.json"),
        ).first { it.isFile }
        val parsed = RepairShopJson.parse(file.readText())
        assertEquals(listOf("gangnam", "jamsil", "mapo"), parsed.map { it.id })
        assertTrue(parsed.all { it.poiId.isNotBlank() && it.address.isNotBlank() })
    }

    private fun shop(id: String, latitude: Double, longitude: Double) = RepairShop(
        id = id,
        name = id,
        latitude = latitude,
        longitude = longitude,
        address = id,
        poiId = id,
    )
}
