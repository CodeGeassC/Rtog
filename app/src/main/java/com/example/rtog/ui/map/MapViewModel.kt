package com.example.rtog.ui.map

import androidx.lifecycle.ViewModel
import com.example.rtog.types.RouteRole
import com.yandex.mapkit.directions.driving.DrivingRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MapViewModel : ViewModel() {

    private val _currentRoute = MutableStateFlow<DrivingRoute?>(null)
    val currentRoute: StateFlow<DrivingRoute?> = _currentRoute
    fun setRoute(newRoute: DrivingRoute?) {
        _currentRoute.value = newRoute
    }

    private val _routeRole = MutableStateFlow<RouteRole?>(null)
    val routeRole: StateFlow<RouteRole?> = _routeRole
    fun setRouteRole(newRole: RouteRole?) {
        _routeRole.value = newRole
    }

}