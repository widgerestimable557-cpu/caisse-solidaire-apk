package com.caissesolidaire.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.caissesolidaire.app.databinding.ActivitySetupBinding

class SetupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("caisse_prefs", Context.MODE_PRIVATE)
        val saved = prefs.getString("webapp_url", null)
        if (!saved.isNullOrBlank()) {
            startActivity(Intent(this, MainActivity::class.java).putExtra("url", saved))
            finish(); return
        }
        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnConnect.setOnClickListener {
            val url = binding.etUrl.text.toString().trim()
            if (url.startsWith("https://script.google.com/") || url.startsWith("https://script.googleusercontent.com/")) {
                prefs.edit().putString("webapp_url", url).apply()
                startActivity(Intent(this, MainActivity::class.java).putExtra("url", url))
                finish()
            } else {
                binding.tvError.text = "URL invalide. Doit commencer par https://script.google.com/"
                binding.tvError.visibility = View.VISIBLE
            }
        }
        binding.btnPaste.setOnClickListener {
            val cb = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val t = cb.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
            if (t.isNotBlank()) binding.etUrl.setText(t)
        }
    }
    companion object {
        const val PREFS_NAME = "caisse_prefs"
        const val PREF_URL = "webapp_url"
        const val EXTRA_URL = "url"
    }
}
