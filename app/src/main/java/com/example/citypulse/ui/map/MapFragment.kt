package com.example.citypulse.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.citypulse.R
import com.example.citypulse.databinding.FragmentMapBinding
import com.example.citypulse.model.Place
import com.example.citypulse.viewmodel.CityViewModel
import com.example.citypulse.viewmodel.CityViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsDisplay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CityViewModel by viewModels(
        ownerProducer = { requireActivity() },
        factoryProducer = { CityViewModelFactory(requireContext()) }
    )

    private lateinit var locationOverlay: MyLocationNewOverlay
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private var selectedPlaceId: String? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) setupLocationOverlay() else {
            showPermissionDeniedMessage()
            centerOnDefaultLocation()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMap()
        setupBottomSheet()
        checkLocationPermission()
        observePlaces()

        binding.fabMyLocation.setOnClickListener {
            if (::locationOverlay.isInitialized && locationOverlay.isMyLocationEnabled) {
                val myLocation = locationOverlay.myLocation
                if (myLocation != null) {
                    binding.mapView.controller.animateTo(myLocation)
                } else {
                    Snackbar.make(binding.root, "Localisation en cours...", Snackbar.LENGTH_SHORT).show()
                }
            } else {
                checkLocationPermission()
            }
        }

        binding.root.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_details)
            .setOnClickListener {
                val placeId = selectedPlaceId ?: return@setOnClickListener
                val action = MapFragmentDirections.actionMapFragmentToPlaceDetailFragment(placeId)
                findNavController().navigate(action)
            }
    }

    private fun setupMap() {
        binding.mapView.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.display.setPositions(
                false,
                CustomZoomButtonsDisplay.HorizontalPosition.RIGHT,
                CustomZoomButtonsDisplay.VerticalPosition.CENTER
            )
            controller.setZoom(15.0)
        }
    }

    private fun setupBottomSheet() {
        val bottomSheet = binding.root.findViewById<View>(R.id.bottom_sheet_place)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun observePlaces() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.places.collect { places ->
                    refreshMarkers(places)
                }
            }
        }
    }

    private fun refreshMarkers(places: List<Place>) {
        binding.mapView.overlays.removeAll(
            binding.mapView.overlays.filterIsInstance<Marker>()
        )
        places.forEach { place ->
            addMarker(GeoPoint(place.latitude, place.longitude), place.name, place.category, place.id)
        }
        binding.mapView.invalidate()

        if (places.isNotEmpty() && !::locationOverlay.isInitialized) {
            val first = places.first()
            binding.mapView.controller.setCenter(GeoPoint(first.latitude, first.longitude))
        }
    }

    private fun addMarker(point: GeoPoint, title: String, category: String, placeId: String) {
        val marker = Marker(binding.mapView)
        marker.position = point
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = title
        marker.icon = ContextCompat.getDrawable(requireContext(), R.drawable.marker_bg)

        marker.setOnMarkerClickListener { _, _ ->
            selectedPlaceId = placeId
            showPlacePreview(title, category)
            true
        }

        binding.mapView.overlays.add(marker)
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> setupLocationOverlay()
            else -> requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun setupLocationOverlay() {
        locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), binding.mapView)
        locationOverlay.enableMyLocation()
        locationOverlay.enableFollowLocation()
        binding.mapView.overlays.add(locationOverlay)

        locationOverlay.runOnFirstFix {
            activity?.runOnUiThread {
                binding.mapView.controller.animateTo(locationOverlay.myLocation)
            }
        }
    }

    private fun centerOnDefaultLocation() {
        val paris = GeoPoint(48.8566, 2.3522)
        binding.mapView.controller.setCenter(paris)
    }

    private fun showPermissionDeniedMessage() {
        Snackbar.make(
            binding.root,
            "La localisation est nécessaire pour afficher votre position sur la carte.",
            Snackbar.LENGTH_LONG
        ).setAction("Réessayer") { checkLocationPermission() }.show()
    }

    private fun showPlacePreview(name: String, category: String) {
        binding.root.findViewById<android.widget.TextView>(R.id.tv_place_name).text = name
        binding.root.findViewById<com.google.android.material.chip.Chip>(R.id.chip_category).text = category
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
