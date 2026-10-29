package id.ac.stmi.project_appmobile

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Tema aplikasi NoActionBar, jadi Toolbar di layout dijadikan ActionBar
        // supaya onCreateOptionsMenu() bisa menampilkan titik tiga.
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)

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

        // Navigasi ke TrackingActivity yang sudah dibuat
        findViewById<View>(R.id.navTracking).setOnClickListener {
            startActivity(Intent(this, TrackingActivity::class.java))
        }

        findViewById<View>(R.id.navSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    // ---------------------------------------------------------------
    // Options Menu (titik tiga di pojok kanan atas)
    // ---------------------------------------------------------------
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.menu_profile -> {
                tampilkanProfil()
                true
            }
            R.id.menu_help -> {
                tampilkanBantuan()
                true
            }
            R.id.menu_logout -> {
                SessionManager.logout(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun tampilkanProfil() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Profil Customer")
            .setMessage(
                "Nama: ${SessionManager.getName(this)}\n" +
                    "No. HP: ${SessionManager.getPhone(this)}"
            )
            .setPositiveButton("Tutup", null)
            .show()
    }

    private fun tampilkanBantuan() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Bantuan")
            .setMessage(
                "1. Pilih unit alat berat di katalog.\n" +
                    "2. Tentukan tanggal sewa, lalu lanjut ke pilihan operator.\n" +
                    "3. Pantau pengiriman unit di menu Pelacakan.\n" +
                    "4. Setelah unit tiba, isi Checklist K3 sebelum unit dioperasikan."
            )
            .setPositiveButton("Mengerti", null)
            .show()
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
