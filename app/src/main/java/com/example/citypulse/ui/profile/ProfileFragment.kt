package com.example.citypulse.ui.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.citypulse.databinding.FragmentProfileBinding
import com.example.citypulse.databinding.ItemSettingsRowBinding
import com.example.citypulse.viewmodel.CityViewModel
import com.example.citypulse.viewmodel.CityViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CityViewModel by viewModels(
        ownerProducer = { requireActivity() },
        factoryProducer = { CityViewModelFactory(requireContext()) }
    )

    private val prefs by lazy {
        requireActivity().getSharedPreferences("citypulse_prefs", Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserProfile()
        setupSettingsItems()
        setupListeners()
        observeStats()

        binding.avatarContainer.setOnClickListener { showEditNameDialog() }
        binding.tvProfileName.setOnClickListener { showEditNameDialog() }
    }

    private fun loadUserProfile() {
        val name = prefs.getString("user_name", null)
        if (name.isNullOrBlank()) {
            showEditNameDialog(firstTime = true)
        } else {
            updateProfileUI(name)
        }
    }

    private fun updateProfileUI(name: String) {
        binding.tvProfileName.text = name
        val initials = name.trim()
            .split("\\s+".toRegex())
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString("") { it.first().uppercaseChar().toString() }
        binding.tvProfileInitials.text = initials.ifEmpty { "?" }
    }

    private fun showEditNameDialog(firstTime: Boolean = false) {
        val input = EditText(requireContext()).apply {
            hint = "Prénom Nom"
            setText(prefs.getString("user_name", ""))
            setPadding(64, 32, 64, 32)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (firstTime) "Bienvenue !" else "Modifier le profil")
            .setMessage(if (firstTime) "Comment souhaitez-vous vous appeler ?" else null)
            .setView(input)
            .setPositiveButton("Confirmer") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotBlank()) {
                    prefs.edit().putString("user_name", name).apply()
                    updateProfileUI(name)
                }
            }
            .apply { if (!firstTime) setNegativeButton("Annuler", null) }
            .setCancelable(!firstTime)
            .show()
    }

    private fun observeStats() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.favorites.collect { favorites ->
                    binding.tvStatFavorites.text = favorites.size.toString()
                    val notesCount = favorites.count { !it.userNote.isNullOrBlank() }
                    binding.tvStatNotes.text = notesCount.toString()
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.places.collect { places ->
                    binding.tvStatVisited.text = places.size.toString()
                }
            }
        }
    }

    private fun setupSettingsItems() {
        setupRow(binding.itemDarkMode, "Thème sombre", android.R.drawable.ic_menu_preferences, isSwitch = true) { isChecked ->
            val mode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(mode)
        }

        setupRow(binding.itemNotifications, "Notifications", android.R.drawable.ic_popup_reminder, isSwitch = true) { _ -> }

        setupRow(binding.itemLocation, "Localisation arrière-plan", android.R.drawable.ic_menu_mylocation, isSwitch = true) { isChecked ->
            if (isChecked) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", requireContext().packageName, null)
                }
                startActivity(intent)
            }
        }

        setupRow(binding.itemLanguage, "Langue", android.R.drawable.ic_menu_mapmode, value = "Français") {
            showLanguageDialog()
        }

        setupRow(binding.itemAbout, "À propos", android.R.drawable.ic_menu_info_details) {
            showAboutDialog()
        }
    }

    private fun setupRow(
        rowBinding: ItemSettingsRowBinding,
        title: String,
        iconRes: Int,
        isSwitch: Boolean = false,
        value: String? = null,
        onAction: (Boolean) -> Unit = {}
    ) {
        rowBinding.apply {
            tvSettingTitle.text = title
            ivSettingIcon.setImageResource(iconRes)

            if (isSwitch) {
                switchSetting.visibility = View.VISIBLE
                ivChevron.visibility = View.GONE
                switchSetting.setOnCheckedChangeListener { _, isChecked -> onAction(isChecked) }
                if (title == "Thème sombre") {
                    switchSetting.isChecked = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
                }
            } else {
                switchSetting.visibility = View.GONE
                ivChevron.visibility = View.VISIBLE
                if (value != null) {
                    tvSettingValue.text = value
                    tvSettingValue.visibility = View.VISIBLE
                }
                root.setOnClickListener { onAction(true) }
            }
        }
    }

    private fun setupListeners() {
        binding.btnResetApp.setOnClickListener { showResetConfirmation() }
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("Français", "English")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Sélectionner la langue")
            .setItems(languages) { _, which ->
                binding.itemLanguage.tvSettingValue.text = languages[which]
            }
            .show()
    }

    private fun showAboutDialog() {
        val name = prefs.getString("user_name", "l'équipe CityPulse") ?: "l'équipe CityPulse"
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("À propos de CityPulse")
            .setMessage("Version 1.0.0\n\nDéveloppé avec ❤️ par $name\n\nApplication native Kotlin - Material 3")
            .setPositiveButton("Fermer", null)
            .show()
    }

    private fun showResetConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Réinitialiser l'application ?")
            .setMessage("Cette action supprimera tous vos favoris, vos notes et votre profil. Elle est irréversible.")
            .setNegativeButton("Annuler", null)
            .setPositiveButton("Réinitialiser") { _, _ ->
                prefs.edit().clear().apply()
                requireActivity().finish()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
