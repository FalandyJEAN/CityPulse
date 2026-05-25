package com.example.citypulse.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.citypulse.MainActivity
import com.example.citypulse.R
import com.example.citypulse.databinding.ActivityOnboardingBinding
import com.example.citypulse.model.OnboardingSlide
import com.google.android.material.tabs.TabLayoutMediator

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()

        binding.btnSkip.setOnClickListener { finishOnboarding() }
        
        binding.btnNext.setOnClickListener {
            if (binding.viewPager.currentItem + 1 < 3) {
                binding.viewPager.currentItem += 1
            } else {
                finishOnboarding()
            }
        }
    }

    private fun setupViewPager() {
        val slides = listOf(
            OnboardingSlide(
                "Explorez votre ville",
                "Découvrez les lieux incontournables autour de vous en temps réel",
                android.R.drawable.ic_menu_mylocation
            ),
            OnboardingSlide(
                "Sauvegardez vos favoris",
                "Gardez vos lieux préférés accessibles même sans connexion",
                android.R.drawable.btn_star_big_on
            ),
            OnboardingSlide(
                "Partagez vos découvertes",
                "Envoyez vos spots favoris à vos amis en un tap",
                android.R.drawable.ic_menu_share
            )
        )

        val adapter = OnboardingAdapter(slides)
        binding.viewPager.adapter = adapter
        
        // Liaison avec les indicateurs (dots)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { _, _ -> }.attach()

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 2) {
                    binding.btnNext.text = "Commencer"
                    binding.btnNext.backgroundTintList =
                        ContextCompat.getColorStateList(this@OnboardingActivity, R.color.primary)
                    binding.btnNext.setTextColor(getColor(R.color.white))
                } else {
                    binding.btnNext.text = "Suivant"
                    binding.btnNext.backgroundTintList =
                        ContextCompat.getColorStateList(this@OnboardingActivity, R.color.white)
                    binding.btnNext.setTextColor(getColor(R.color.primary))
                }
            }
        })

        // Animation de transition (Fade + Slide)
        binding.viewPager.setPageTransformer { page, position ->
            page.apply {
                translationX = -position * width
                alpha = 1 - Math.abs(position)
            }
        }
    }

    private fun finishOnboarding() {
        val sharedPref = getSharedPreferences("citypulse_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().putBoolean("onboarding_finished", true).apply()
        
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
