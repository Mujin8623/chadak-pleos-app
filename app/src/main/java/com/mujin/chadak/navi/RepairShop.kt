package com.mujin.chadak.navi

data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
)

data class RepairShop(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val poiId: String,
    val poiSubId: String = "",
)

enum class WaypointAction {
    /** `NaviHelper.addWaypoint(RequestWaypointInfo(..., WaypointIndex.FIRST, ...))` */
    ADD_FIRST_WAYPOINT,

    /** No active route. `NaviHelper.requestRoute` with the shop as the destination. */
    REQUEST_ROUTE,
}

data class WaypointPlan(
    val shop: RepairShop,
    val action: WaypointAction,
)

fun interface LocationFix {
    fun lastKnown(): GeoPoint?
}

/**
 * Stub until `FusedLocation.getLastKnownLocation` is wired. Debug builds inject a fix.
 */
class PleosFusedLocationFix : LocationFix {
    override fun lastKnown(): GeoPoint? = null
}
