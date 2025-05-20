package com.example.rtog.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.rtog.MainActivity
import com.example.rtog.R
import com.example.rtog.databinding.FragmentHomeBinding
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider

class HomeFragment : Fragment(), UserLocationObjectListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var mapView: MapView
    private lateinit var userLocationLayer: UserLocationLayer
    var userLocation: Point? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        mapView = binding.mapview

        // Включение геолокации
        val mapKit = MapKitFactory.getInstance()
        userLocationLayer = mapKit.createUserLocationLayer(mapView.mapWindow)
        userLocationLayer.isVisible = true
        userLocationLayer.isHeadingEnabled = true
        userLocationLayer.setObjectListener(this)

        val map = mapView.mapWindow.map
        map.isRotateGesturesEnabled = false


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*val mainActivity = activity as? MainActivity

        // Обработка нажатия на кнопку "К моей геопозиции"
        binding.btnMyLocation.setOnClickListener {
            val userLocation = mainActivity?.userLocation
            if (userLocation != null) {
                mapView?.map?.move(
                    CameraPosition(
                        Point(userLocation.latitude, userLocation.longitude),
                        16.0f, 0.0f, 0.0f
                    ),
                    Animation(Animation.Type.SMOOTH, 1.0f),
                    null
                )
            } else {
                Toast.makeText(requireContext(), "Геопозиция не получена", Toast.LENGTH_SHORT).show()
            }
        }*/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    // --- Реализация UserLocationObjectListener ---
    override fun onObjectAdded(userLocationView: UserLocationView) {
        val mainActivity = activity as? MainActivity
        userLocationView.arrow.setIcon(ImageProvider.fromResource(mainActivity, R.drawable.user_arrow))
        userLocationView.pin.setIcon(ImageProvider.fromResource(mainActivity, R.drawable.user_pin))
    }

    override fun onObjectRemoved(userLocationView: UserLocationView) {}
    override fun onObjectUpdated(
        p0: UserLocationView,
        p1: ObjectEvent
    ) {
        /* TODO("Not yet implemented") */
    }

    fun onObjectUpdated(userLocationView: UserLocationView, placemark: PlacemarkMapObject) {
        userLocation = placemark.geometry // <-- Сохраняем точку
    }
}
