package com.example.citypulse.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.citypulse.R
import com.example.citypulse.databinding.FragmentPlaceListBinding
import com.example.citypulse.model.Place
import com.example.citypulse.viewmodel.CityViewModel
import com.example.citypulse.viewmodel.CityViewModelFactory
import kotlinx.coroutines.launch

class PlaceListFragment : Fragment() {

    private var _binding: FragmentPlaceListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CityViewModel by viewModels(
        ownerProducer = { requireActivity() },
        factoryProducer = { CityViewModelFactory(requireContext()) }
    )

    private lateinit var placeAdapter: PlaceListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaceListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFilters()

        viewModel.seed(seedPlaces())

        showLoading(true)
        observePlaces()

        binding.btnRetry.setOnClickListener {
            showLoading(true)
            viewModel.seed(seedPlaces())
        }
    }

    private fun setupRecyclerView() {
        placeAdapter = PlaceListAdapter(
            onPlaceClick = { place ->
                val action = PlaceListFragmentDirections
                    .actionPlaceListFragmentToPlaceDetailFragment(place.id)
                findNavController().navigate(action)
            },
            onFavoriteClick = { place -> viewModel.toggleFavorite(place) }
        )

        binding.rvPlaces.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = placeAdapter
        }
    }

    private fun setupFilters() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener { group, checkedIds ->
            val categorie = checkedIds.firstOrNull()
                ?.let { group.findViewById<com.google.android.material.chip.Chip>(it)?.text?.toString() }
                ?.takeIf { !it.equals("Tous", ignoreCase = true) }
            viewModel.filtrerParCategorie(categorie)
        }
    }

    private fun observePlaces() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.places.collect { list ->
                    showLoading(false)
                    placeAdapter.submitList(list)
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.shimmerViewContainer.startShimmer()
            binding.shimmerViewContainer.visibility = View.VISIBLE
            binding.rvPlaces.visibility = View.GONE
        } else {
            binding.shimmerViewContainer.stopShimmer()
            binding.shimmerViewContainer.visibility = View.GONE
            binding.rvPlaces.visibility = View.VISIBLE
        }
    }

    private fun seedPlaces(): List<Place> = listOf(
        Place("1", "Le Gourmet", 48.8566, 2.3522, "Restaurants", "12 Rue de Rivoli", null),
        Place("2", "Parc Central", 48.8584, 2.2945, "Parcs", "Avenue Gustave Eiffel", null),
        Place("3", "Musée d'Art", 48.8606, 2.3376, "Musées", "Palais du Louvre", null),
        Place("4", "Café de Flore", 48.8542, 2.3331, "Restaurants", "172 Bd Saint-Germain", null),
        Place("5", "Galerie Lafayette", 48.8736, 2.3320, "Commerces", "40 Bd Haussmann", null)
    )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
