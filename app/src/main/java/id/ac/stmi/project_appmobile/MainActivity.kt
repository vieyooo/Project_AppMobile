package id.ac.stmi.project_appmobile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import id.ac.stmi.project_appmobile.DetailActivity
import kotlin.jvm.java
import id.ac.stmi.project_appmobile.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ----- Klik kartu unit -> buka halaman detail -----
        findViewById<View>(R.id.cardUnit1).setOnClickListener {
            bukaDetail("Excavator CAT 336D L", 4500000, "Pool A")
        }
        findViewById<View>(R.id.cardUnit2).setOnClickListener {
            bukaDetail("Bulldozer CAT D6R", 3800000, "Pool A")
        }
        findViewById<View>(R.id.cardUnit3).setOnClickListener {
            bukaDetail("Motor Grader CAT 140", 4100000, "Pool A")
        }
        findViewById<View>(R.id.cardUnit4).setOnClickListener {
            bukaDetail("Wheel Loader CAT 950 GC", 3500000, "Pool C")
        }

        // ----- Menu bawah (Katalog = halaman ini, tidak perlu aksi) -----
        findViewById<View>(R.id.navOperator).setOnClickListener {
            startActivity(Intent(this, OperatorActivity::class.java))
        }

        findViewById<View>(R.id.navTracking).setOnClickListener {
            // TODO: startActivity(Intent(this, TrackingActivity::class.java))
            Toast.makeText(this, "Pelacakan segera hadir", Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.navSettings).setOnClickListener {
            // TODO: startActivity(Intent(this, SettingsActivity::class.java))
            Toast.makeText(this, "Pengaturan segera hadir", Toast.LENGTH_SHORT).show()
        }
    }

    private fun bukaDetail(nama: String, tarif: Int, pool: String) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra(EXTRA_NAMA, nama)
        intent.putExtra(EXTRA_TARIF, tarif)
        intent.putExtra(EXTRA_POOL, pool)
        startActivity(intent)
    }

    // Key untuk data yang dikirim ke DetailActivity
    companion object {
        const val EXTRA_NAMA = "EXTRA_NAMA"
        const val EXTRA_TARIF = "EXTRA_TARIF"
        const val EXTRA_POOL = "EXTRA_POOL"
    }
}