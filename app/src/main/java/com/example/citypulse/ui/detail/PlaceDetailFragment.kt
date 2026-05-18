package com.example.citypulse.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.citypulse.databinding.FragmentPlaceDetailBinding
import com.example.citypulse.model.Place
import com.example.citypulse.utils.ShareUtils
import com.example.citypulse.viewmodel.CityViewModel
import com.example.citypulse.viewmodel.CityViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class PlaceDetailFragment : Fragment() {

    private var _binding: FragmentPlaceDetailBinding? = null
    private val binding get() = _binding!!

    private val args: PlaceDetailFragmentArgs by navArgs()
    private val viewModel: CityViewModel by viewModels(
        ownerProducer = { requireActivity() },
        factoryProducer = { CityViewModelFactory(requireContext()) }
    )

    private var currentPlace: Place? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaceDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        setupListeners()
        loadPlaceData()
    }

    private fun setupListeners() {
        binding.ivFavoriteDetail.setOnClickListener {
            animateFavoriteButton()
            currentPlace?.let {
                viewModel.toggleFavorite(it)
                currentPlace = it.copy(isFavorite = !it.isFavorite)
                updateFavoriteIcon(currentPlace!!.isFavorite)
                Snackbar.make(binding.root, "Favoris mis à jour", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.fabShare.setOnClickListener { sharePlace() }

        binding.btnSaveNotes.setOnClickListener {
            val note = binding.etNotes.text.toString()
            currentPlace?.let {
                viewModel.saveNote(it, note)
                currentPlace = it.copy(userNote = note)
            }
            Snackbar.make(binding.root, "Note sauvegardée !", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun loadPlaceData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val place = viewModel.getPlace(args.placeId) ?: return@launch
            currentPlace = place
            renderPlace(place)
        }
    }

    private fun renderPlace(place: Place) {
        binding.apply {
            tvDetailName.text = place.name
            chipDetailCategory.text = place.category
            tvDetailAddress.text = place.address ?: "Adresse non renseignée"
            tvDetailCoords.text = "Lat: ${place.latitude}, Lng: ${place.longitude}"
            etNotes.setText(place.userNote.orEmpty())
        }
        updateFavoriteIcon(place.isFavorite)
    }

    private fun updateFavoriteIcon(isFavorite: Boolean) {
        val icon = if (isFavorite) android.R.drawable.btn_star_big_on
        else android.R.drawable.btn_star_big_off
        binding.ivFavoriteDetail.setImageResource(icon)
    }

    private fun animateFavoriteButton() {
        val anim = ScaleAnimation(
            1.0f, 1.3f, 1.0f, 1.3f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 200
            repeatCount = 1
            repeatMode = Animation.REVERSE
        }
        binding.ivFavoriteDetail.startAnimation(anim)
    }

    private fun sharePlace() {
        currentPlace?.let { place ->
            ShareUtils.partagerLieu(requireContext(), place.name, place.latitude, place.longitude)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
