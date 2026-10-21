package com.mujin.chadak.navi

interface WaypointNavigator {
    fun addRepairShopWaypoint(): String
}

/**
 * Plans a NaviHelper call and returns the sentence the UI shows.
 *
 * v0.3.0 will call, when [WaypointAction.ADD_FIRST_WAYPOINT]:
 * `addWaypoint(RequestWaypointInfo(latitude, longitude, poiName, WaypointIndex.FIRST, poiId, address, poiSubId))`.
 * When there is no route, it will call `requestRoute` with the shop as the destination.
 * Latitude is the first coordinate argument in NaviHelper 3.0.5.
 */
class PleosWaypointNavigator(
    private val shops: List<RepairShop>,
    private val location: LocationFix,
    private val hasActiveRoute: () -> Boolean = { false },
) : WaypointNavigator {
    override fun addRepairShopWaypoint(): String {
        val origin = location.lastKnown()
            ?: return "위치를 아직 몰라 정비소를 고르지 못했습니다."
        val plan = NearestRepairShop.plan(origin, shops, hasActiveRoute())
            ?: return "정비소 목록이 비어 있습니다."
        val action = when (plan.action) {
            WaypointAction.ADD_FIRST_WAYPOINT -> "경유지로 추가할 예정입니다"
            WaypointAction.REQUEST_ROUTE -> "새 경로로 안내할 예정입니다"
        }
        return "${plan.shop.name}을(를) $action. ${plan.shop.address}"
    }
}
