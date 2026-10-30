package id.ac.stmi.project_appmobile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.jvm.java

class MainActivity : AppCompatActivity() {

    private data class UnitInfo(val nama: String, val tarif: Int, val pool: String)

    // Data tiap kartu unit di katalog
    private val units = mapOf(
        R.id.cardUnit1 to UnitInfo("Excavator CAT 336D L", 4500000, "Pool A"),
        R.id.cardUnit2 to UnitInfo("Bulldozer CAT D6R", 3800000, "Pool A"),
        R.id.cardUnit3 to UnitInfo("Motor Grader CAT 140", 4100000, "Pool A"),
        R.id.cardUnit4 to UnitInfo("Wheel Loader CAT 950 GC", 3500000, "Pool C")
    )

    // Unit yang sedang ditekan lama (untuk Context Menu)
    private var unitTerpilih: UnitInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Tema aplikasi NoActionBar, jadi Toolbar di layout dijadikan ActionBar
        // supaya onCreateOptionsMenu() bisa menampilkan titik tiga.
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // ----- Klik kartu unit -> buka halaman detail -----
        // ----- Tekan lama kartu unit -> Context Menu -----
        units.forEach { (cardId, unit) ->
            val card = findViewById<View>(cardId)
            card.setOnClickListener { bukaDetail(unit.nama, unit.tarif, unit.pool) }
            registerForContextMenu(card)
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

    // ---------------------------------------------------------------
    // Context Menu (tekan lama pada kartu unit)
    // ---------------------------------------------------------------
    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val unit = units[v.id] ?: return
        unitTerpilih = unit
        menu.setHeaderTitle(unit.nama)
        menuInflater.inflate(R.menu.context_menu_unit, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val unit = unitTerpilih ?: return super.onContextItemSelected(item)
        return when (item.itemId) {
            R.id.ctx_detail -> {
                bukaDetail(unit.nama, unit.tarif, unit.pool)
                true
            }
            R.id.ctx_copy -> {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Nama unit", unit.nama))
                Toast.makeText(this, "Nama unit disalin", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.ctx_share -> {
                val teks = "${unit.nama}\n" +
                        "Tarif: Rp " + "%,d".format(unit.tarif).replace(',', '.') + " / hari\n" +
                        "Lokasi: ${unit.pool}"
                val kirim = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, teks)
                }
                startActivity(Intent.createChooser(kirim, "Bagikan unit"))
                true
            }
            else -> super.onContextItemSelected(item)
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