package com.practicum.playlistmaker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textview.MaterialTextView

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setupToolbar()
        setupShareButton()
        setupSupportButton()
        setupTermsButton()
    }

    private fun setupToolbar() {
        findViewById<MaterialToolbar>(R.id.settings_toolbar).setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupShareButton() {
        findViewById<MaterialTextView>(R.id.shareTextView).setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message))
                startActivity(Intent.createChooser(this, null))
            }
        }
    }

    private fun setupSupportButton() {
        findViewById<MaterialTextView>(R.id.supportTextView).setOnClickListener {
            Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.support_email)))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_subject))
                putExtra(Intent.EXTRA_TEXT, getString(R.string.support_message))
                startActivity(Intent.createChooser(this, getString(R.string.choose_email_client)))
            }
        }
    }

    private fun setupTermsButton() {
        findViewById<MaterialTextView>(R.id.termsTextView).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.terms_url))))
        }
    }
}