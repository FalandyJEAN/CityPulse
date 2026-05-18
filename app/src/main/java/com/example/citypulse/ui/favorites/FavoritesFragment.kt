package com.example.citypulse.ui.favorites

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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.citypulse.R
import com.example.citypulse.databinding.FragmentFavoritesBinding
import com.example.citypulse.model.Place
import com.example.citypulse.utils.SwipeToDeleteCallback
import com.example.citypulse.viewmodel.CityViewModel
import com.example.citypulse.viewmodel.CityViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CityViewModel by viewModels(
        ownerProducer = { requireActivity() },
        factoryProducer = { CityViewModelFactory(requireContext()) }
    )

    private lateinit var favoritesAdapter: FavoritesAdapter
    private var favoritesList: List<Place> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeFavorites()
    }

    private fun setupRecyclerView() {
        favoritesAdapter = FavoritesAdapter { place ->
            val action = FavoritesFragmentDirections
                .actionFavoritesFragmentToPlaceDetailFragment(place.id)
            findNavController().navigate(action)
        }

        binding.rvFavorites.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = favoritesAdapter
        }

        val swipeHandler = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val place = favoritesList.getOrNull(position) ?: return

                if (direction == ItemTouchHelper.LEFT) {
                    removeFavorite(place)
                } else if (direction == ItemTouchHelper.RIGHT) {
                    favoritesAdapter.notifyItemChanged(position)
                    findNavController().navigate(R.id.mapFragment)
                }
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.rvFavorites)
    }

    private fun setupListeners() {
        binding.layoutEmptyState.btnExplore.setOnClickListener {
            findNavController().navigate(R.id.placeListFragment)
        }
    }

    private fun observeFavorites() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.favorites.collect { list ->
                    favoritesList = list
                    favoritesAdapter.submitList(list)
                    updateHeader()
                    checkEmptyState()
                }
            }
        }
    }

    private fun removeFavorite(place: Place) {
        viewModel.removeFavorite(place)
        Snackbar.make(binding.root, "Lieu retiré des favoris", Snackbar.LENGTH_LONG)
            .setAction("ANNULER") { viewModel.toggleFavorite(place.copy(isFavorite = false)) }
            .setActionTextColor(resources.getColor(R.color.secondary, null))
            .show()
    }

    private fun updateHeader() {
        binding.tvFavoritesCount.text = "${favoritesList.size} lieux sauvegardés"
    }

    private fun checkEmptyState() {
        if (favoritesList.isEmpty()) {
            binding.layoutEmptyState.emptyStateRoot.visibility = View.VISIBLE
            binding.rvFavorites.visibility = View.GONE
            binding.tvFavoritesCount.visibility = View.GONE
        } else {
            binding.layoutEmptyState.emptyStateRoot.visibility = View.GONE
            binding.rvFavorites.visibility = View.VISIBLE
            binding.tvFavoritesCount.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
