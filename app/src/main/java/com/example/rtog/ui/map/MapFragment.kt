package com.example.rtog.ui.map

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.rtog.MainActivity
import com.example.rtog.R
import com.example.rtog.databinding.FragmentSidenavMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider

class MapFragment : Fragment(), UserLocationObjectListener {

    private var _binding: FragmentSidenavMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var mapView: MapView
    private lateinit var userLocationLayer: UserLocationLayer
    var userLocation: Point? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSidenavMapBinding.inflate(inflater, container, false)

        mapView = binding.mapview

        // Включение геолокации
        val mapKit = MapKitFactory.getInstance()
        userLocationLayer = mapKit.createUserLocationLayer(mapView.mapWindow)
        userLocationLayer.isVisible = true
        userLocationLayer.isHeadingEnabled = true
        userLocationLayer.setObjectListener(this)

        val map = mapView.mapWindow.map
        map.isRotateGesturesEnabled = false

        // Инициализация Google Location Services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    userLocation = Point(location.latitude, location.longitude)
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity = activity as? MainActivity

        // Обработка нажатия на кнопку "К моей геопозиции"
        binding.btnMyLocation.setOnClickListener {
            userLocation?.let {
                mapView.map?.move(
                    CameraPosition(
                        Point(it.latitude, it.longitude),
                        16.0f, 0.0f, 0.0f
                    ),
                    Animation(Animation.Type.SMOOTH, 1.0f),
                    null
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()

        // Проверка разрешений
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val request = LocationRequest.Builder(3000L).setPriority(Priority.PRIORITY_HIGH_ACCURACY).build()
            fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    override fun onStop() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    // --- Реализация UserLocationObjectListener ---
    override fun onObjectAdded(userLocationView: UserLocationView) {
        val mainActivity = activity as? MainActivity
        userLocationView.arrow.setIcon(ImageProvider.fromResource(mainActivity, R.drawable.user_arrow))
        //userLocationView.pin.setIcon(ImageProvider.fromResource(mainActivity, R.drawable.user_pin))
    }

    override fun onObjectRemoved(userLocationView: UserLocationView) {}
    override fun onObjectUpdated(
        userLocationView: UserLocationView,
        objectEvent: ObjectEvent
    ) {
        //userLocation = userLocationView.arrow.geometry
    }
}
