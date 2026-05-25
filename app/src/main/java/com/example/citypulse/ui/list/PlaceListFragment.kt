package com.example.citypulse.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.citypulse.databinding.FragmentPlaceListBinding
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
        setupSearch()
        observePlaces()

        binding.btnRetry.setOnClickListener { observePlaces() }
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

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.setSearchQuery(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText.orEmpty())
                return true
            }
        })
    }

    private fun observePlaces() {
        showLoading(true)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
