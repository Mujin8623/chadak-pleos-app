package com.mujin.chadak.navi

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * NaviHelper has no POI search. Pick the nearest bundled shop, then either add
 * it as the first waypoint or request a route when the car has no active route.
 */
object NearestRepairShop {
    fun pick(origin: GeoPoint, shops: List<RepairShop>): RepairShop? {
        return shops.minByOrNull { shop ->
            distanceMeters(origin.latitude, origin.longitude, shop.latitude, shop.longitude)
        }
    }

    fun plan(origin: GeoPoint, shops: List<RepairShop>, hasActiveRoute: Boolean): WaypointPlan? {
        val shop = pick(origin, shops) ?: return null
        val action = if (hasActiveRoute) {
            WaypointAction.ADD_FIRST_WAYPOINT
        } else {
            WaypointAction.REQUEST_ROUTE
        }
        return WaypointPlan(shop, action)
    }

    fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6_371_000.0
        val latitudeDelta = Math.toRadians(lat2 - lat1)
        val longitudeDelta = Math.toRadians(lon2 - lon1)
        val a = sin(latitudeDelta / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(longitudeDelta / 2).pow(2)
        return 2 * earthRadius * asin(sqrt(a))
    }
}
