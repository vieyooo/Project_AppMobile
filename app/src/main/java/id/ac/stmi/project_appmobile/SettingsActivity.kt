package id.ac.stmi.project_appmobile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import kotlin.jvm.java

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        bindProfile()
        setupPreferences()
        setupActions()
        setupBottomNav()
    }

    /** Data customer dibaca dari SharedPreferences "customer_session". */
    private fun bindProfile() {
        val nama = SessionManager.getName(this)
        val phone = SessionManager.getPhone(this)

        findViewById<TextView>(R.id.tvProfileName).text = nama
        findViewById<TextView>(R.id.tvProfilePhone).text = phone
        findViewById<TextView>(R.id.tvAvatarInitial).text =
            nama.firstOrNull { it.isLetter() }?.uppercase() ?: "?"
    }

    /**
     * Preferensi pemesanan, disimpan di SharedPreferences lewat AppPrefs.
     * - Spinner  : pool pengambilan default
     * - ListView : menu menuju Riwayat Sewa
     * (RadioGroup metode pembayaran ada di DetailActivity)
     */
    private fun setupPreferences() {
        // ----- Spinner -----
        val spPool = findViewById<Spinner>(R.id.spPool)
        val poolAdapter = ArrayAdapter.createFromResource(
            this, R.array.pool_list, R.layout.spinner_item
        )
        poolAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spPool.adapter = poolAdapter
        spPool.setSelection(AppPrefs.getPoolIndex(this))
        spPool.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                // Callback ini juga terpanggil saat awal tampil, jadi simpan hanya jika berubah
                if (position != AppPrefs.getPoolIndex(this@SettingsActivity)) {
                    AppPrefs.setPoolIndex(this@SettingsActivity, position)
                    Toast.makeText(
                        this@SettingsActivity,
                        "Pool default: ${parent?.getItemAtPosition(position)}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // ----- ListView: Riwayat Sewa -----
        findViewById<View>(R.id.rowRiwayat).setOnClickListener {
            startActivity(Intent(this, RiwayatActivity::class.java))
        }
    }

    private fun setupActions() {
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            // clear() sesi -> kembali ke LoginActivity (back stack dibersihkan)
            SessionManager.logout(this)
        }
    }

    /** Bottom navbar: tab "Lainnya" (navSettings) adalah halaman ini, jadi tanpa aksi. */
    private fun setupBottomNav() {
        findViewById<View>(R.id.navCatalog).setOnClickListener {
            navigateTo(MainActivity::class.java)
        }
        findViewById<View>(R.id.navOperator).setOnClickListener {
            navigateTo(OperatorActivity::class.java)
        }
        findViewById<View>(R.id.navTracking).setOnClickListener {
            navigateTo(TrackingActivity::class.java)
        }
    }

    private fun navigateTo(target: Class<*>) {
        startActivity(
            Intent(this, target).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                            Intent.FLAG_ACTIVITY_NO_ANIMATION
                )
            }
        )
        finish()
    }
}