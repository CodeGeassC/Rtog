package com.example.rtog.ui.map

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.rtog.MainActivity
import com.example.rtog.R
import com.example.rtog.databinding.FragmentSidenavMapBinding
import com.example.rtog.types.RouteRole
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingRouter
import com.yandex.mapkit.directions.driving.DrivingRouterType
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.directions.driving.VehicleOptions
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.GeoObjectTapEvent
import com.yandex.mapkit.layers.GeoObjectTapListener
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.PlacemarkCreatedCallback
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManager
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.Session
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.launch

class MapFragment : Fragment(), UserLocationObjectListener {

    private var _binding: FragmentSidenavMapBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MapViewModel by viewModels()
    private lateinit var mapView: MapView
    private lateinit var userLocationLayer: UserLocationLayer
    var userLocation: Point? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var searchManager: SearchManager
    private lateinit var searchSession: Session
    private lateinit var searchAdapter: ArrayAdapter<String>
    private lateinit var drivingRouter: DrivingRouter
    private var drivingSession: DrivingSession? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSidenavMapBinding.inflate(inflater, container, false)

        mapView = binding.mapview

        val mapKit = MapKitFactory.getInstance()
        userLocationLayer = mapKit.createUserLocationLayer(mapView.mapWindow)
        userLocationLayer.isVisible = true
        userLocationLayer.isHeadingModeActive = true
        userLocationLayer.setObjectListener(this)

        val map = mapView.mapWindow.map
        map.isRotateGesturesEnabled = false

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let {
                    userLocation = Point(it.latitude, it.longitude)
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity = activity as? MainActivity

        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter(DrivingRouterType.COMBINED)
        searchAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, mutableListOf())
        binding.listSearchResults.adapter = searchAdapter

        binding.etSearchAddress.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim() ?: return
                if (query.length < 3) return

                if (::searchSession.isInitialized) searchSession.cancel()

                searchSession = searchManager.submit(
                    query,
                    Geometry.fromPoint(userLocation ?: Point(55.751244, 37.618423)),
                    SearchOptions().apply { resultPageSize = 10 },
                    object : Session.SearchListener {
                        override fun onSearchResponse(response: Response) {
                            searchAdapter.clear()
                            response.collection.children.forEach {
                                it.obj?.name?.let(searchAdapter::add)
                            }
                            binding.listSearchResults.visibility = View.VISIBLE
                        }

                        override fun onSearchError(error: com.yandex.runtime.Error) {
                            Toast.makeText(requireContext(), "Ошибка поиска", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        })

        binding.listSearchResults.setOnItemClickListener { _, _, position, _ ->
            val query = searchAdapter.getItem(position) ?: return@setOnItemClickListener
            searchSession = searchManager.submit(
                query,
                Geometry.fromPoint(userLocation ?: Point(55.751244, 37.618423)),
                SearchOptions().apply { resultPageSize = 1 },
                object : Session.SearchListener {
                    override fun onSearchResponse(response: Response) {
                        val result = response.collection.children.firstOrNull()?.obj ?: return
                        val point = result.geometry.firstOrNull()?.point ?: return
                        mapView.map.mapObjects.clear()
                        val placemark = mapView.map.mapObjects.addPlacemark(point)
                        placemark.setIcon(ImageProvider.fromResource(requireContext(), R.drawable.user_pin))
                        placemark.userData = result.name
                        mapView.map.move(
                            CameraPosition(point, 16f, 0f, 0f),
                            Animation(Animation.Type.SMOOTH, 1f), null
                        )
                        binding.listSearchResults.visibility = View.GONE
                        binding.etSearchAddress.text.clear()

                        userLocation?.let { start ->
                            buildRoute(start, point)
                        }
                    }

                    override fun onSearchError(error: com.yandex.runtime.Error) {
                        Toast.makeText(requireContext(), "Ошибка поиска места", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        mapView.map.addTapListener(object : GeoObjectTapListener {
            override fun onObjectTap(geoObjectTapEvent: GeoObjectTapEvent): Boolean {
                val name = geoObjectTapEvent.geoObject.name ?: "Без названия"
                val description = geoObjectTapEvent.geoObject.descriptionText ?: "Описание отсутствует"
                val point = geoObjectTapEvent.geoObject.geometry.firstOrNull()?.point ?: return true

                Toast.makeText(requireContext(), "$name\n$description", Toast.LENGTH_LONG).show()

                userLocation?.let { start ->
                    buildRoute(start, point)
                }

                return true
            }
        })

        binding.btnMyLocation.setOnClickListener {
            moveMapToUserLocation()
        }

        binding.btnBeDriver.setOnClickListener {
            viewModel.setRouteRole(RouteRole.DRIVER)
        }

        binding.btnBePassenger.setOnClickListener {
            viewModel.setRouteRole(RouteRole.PASSENGER)
        }

        binding.btnCloseRoute.setOnClickListener {
            viewModel.setRoute(null)
            viewModel.setRouteRole(null)
        }

        lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.currentRoute.collect { route ->
                        mapView.map.mapObjects.clear()
                        if (route != null) {
                            val destinationPoint = route.requestPoints?.last()!!.point
                            mapView.map.mapObjects.addPolyline(route.geometry)
                            mapView.map.mapObjects.addPlacemark(object : PlacemarkCreatedCallback {
                                override fun onPlacemarkCreated(placemark: PlacemarkMapObject) {
                                    placemark.geometry = destinationPoint
                                    placemark.setIcon(ImageProvider.fromResource(requireContext(), com.yandex.maps.mobile.R.drawable.search_layer_pin_selected_default))
                                }
                            })
                        }
                        binding.routesConfirmButtons.visibility = when (route) {
                            null -> View.GONE
                            else -> View.VISIBLE
                        }
                    }
                }
                launch {
                    viewModel.routeRole.collect { role ->
                        binding.routeViewSwitcher.displayedChild = when (role) {
                            null -> 0
                            else -> 1
                        }
                        if (role != null)
                            moveMapToUserLocation()
                    }
                }
            }
        }

    }

    private fun moveMapToUserLocation() {
        userLocation?.let {
            mapView.map.move(
                CameraPosition(
                    Point(it.latitude, it.longitude),
                    16.0f, 0.0f, 0.0f
                ),
                Animation(Animation.Type.SMOOTH, 1.0f),
                null
            )
        }
    }

    private fun buildRoute(startPoint: Point, endPoint: Point) {
        val requestPoints = listOf(
            RequestPoint(startPoint, RequestPointType.WAYPOINT, null, null, null),
            RequestPoint(endPoint, RequestPointType.WAYPOINT, null, null, null)
        )

        drivingSession?.cancel()
        drivingSession = drivingRouter.requestRoutes(
            requestPoints,
            DrivingOptions(),
            VehicleOptions(),
            object : DrivingSession.DrivingRouteListener {
                override fun onDrivingRoutes(routes: List<DrivingRoute>) {
                    viewModel.setRoute(routes[0])
                }

                override fun onDrivingRoutesError(error: com.yandex.runtime.Error) {
                    Toast.makeText(requireContext(), "Ошибка построения маршрута", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun updateRoute() {
        // отправить текущие координаты, получить
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val request = LocationRequest.Builder(3000L).setPriority(Priority.PRIORITY_HIGH_ACCURACY).build()
            fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        }
    }

    override fun onStop() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onObjectAdded(userLocationView: UserLocationView) {}
    override fun onObjectRemoved(userLocationView: UserLocationView) {}
    override fun onObjectUpdated(userLocationView: UserLocationView, objectEvent: ObjectEvent) {}
}
