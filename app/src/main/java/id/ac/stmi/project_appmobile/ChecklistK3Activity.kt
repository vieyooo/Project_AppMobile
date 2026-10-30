package id.ac.stmi.project_appmobile

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.content.res.ColorStateList
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.jvm.java

class ChecklistK3Activity : AppCompatActivity() {

    private lateinit var checkBoxes: List<CheckBox>
    private lateinit var cardStatus: MaterialCardView
    private lateinit var tvStatus: TextView
    private lateinit var tvStatusDetail: TextView
    private lateinit var cardTindakan: MaterialCardView
    private lateinit var tvTindakanJudul: TextView
    private lateinit var tvTindakan: TextView
    private lateinit var btnAksi: MaterialButton
    private var unitSiap = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checklist_k3)

        cardStatus = findViewById(R.id.cardStatus)
        tvStatus = findViewById(R.id.tvStatus)
        tvStatusDetail = findViewById(R.id.tvStatusDetail)
        cardTindakan = findViewById(R.id.cardTindakan)
        tvTindakanJudul = findViewById(R.id.tvTindakanJudul)
        tvTindakan = findViewById(R.id.tvTindakan)
        btnAksi = findViewById(R.id.btnAksi)
        btnAksi.setOnClickListener { onAksiClicked() }

        // Nama unit dikirim dari TrackingActivity
        val namaUnit = intent.getStringExtra(EXTRA_UNIT_NAME)
        findViewById<TextView>(R.id.tvUnitName).text =
            if (namaUnit.isNullOrEmpty()) "UNIT" else namaUnit

        checkBoxes = listOf(
            findViewById(R.id.cbOli),
            findViewById(R.id.cbRem),
            findViewById(R.id.cbBbm),
            findViewById(R.id.cbFisik)
        )

        // Setiap perubahan centang langsung memperbarui status
        checkBoxes.forEach { cb ->
            cb.setOnCheckedChangeListener { _, _ -> updateStatus() }
        }

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        setupBottomNav()
        updateStatus() // kondisi awal: belum ada yang dicentang
    }

    private fun updateStatus() {
        val terverifikasi = checkBoxes.count { it.isChecked }
        val siap = terverifikasi == checkBoxes.size

        if (siap) {
            tvStatus.text = "Siap Beroperasi"
            tvStatus.setTextColor(Color.parseColor("#2E7D32"))
            cardStatus.setCardBackgroundColor(Color.parseColor("#E8F5E9"))
            cardStatus.strokeColor = Color.parseColor("#A5D6A7")
        } else {
            tvStatus.text = "Butuh Penanganan Mekanik"
            tvStatus.setTextColor(Color.parseColor("#7C2D12"))
            cardStatus.setCardBackgroundColor(Color.parseColor("#FFEDD5"))
            cardStatus.strokeColor = Color.parseColor("#FDBA74")
        }
        tvStatusDetail.text = "$terverifikasi dari ${checkBoxes.size} item terverifikasi"

        updateTindakan(siap, terverifikasi)
    }

    /**
     * Tindakan yang harus dilakukan, mengikuti hasil checklist:
     * - Semua item terverifikasi -> unit boleh beroperasi
     * - Ada item belum terverifikasi -> tindakan per item, unit belum boleh dipakai
     */
    private fun updateTindakan(siap: Boolean, terverifikasi: Int) {
        if (siap) {
            tvTindakanJudul.text = "TINDAKAN: UNIT BOLEH BEROPERASI"
            tvTindakan.text = listOf(
                "Catat hasil inspeksi beserta tanggal & jam pemeriksaan.",
                "Pastikan operator membawa SIO yang masih berlaku.",
                "Panaskan mesin beberapa menit sebelum mulai bekerja.",
                "Ulangi pemeriksaan ini setiap awal shift."
            ).joinToString("\n") { "• $it" }
            tvTindakan.setTextColor(Color.parseColor("#2E7D32"))
            cardTindakan.setCardBackgroundColor(Color.parseColor("#F1F8F2"))
            cardTindakan.strokeColor = Color.parseColor("#A5D6A7")

            // Tujuan: unit siap -> konfirmasi, lalu kembali ke Katalog
            unitSiap = true
            btnAksi.text = "MULAI OPERASI  ➔"
            btnAksi.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#2E7D32"))
            return
        }

        val tindakan = mutableListOf<String>()
        checkBoxes.forEach { cb ->
            if (!cb.isChecked) {
                when (cb.id) {
                    R.id.cbOli -> tindakan.add(
                        "Oli: jangan hidupkan mesin dulu. Tambah oli sampai batas level, " +
                                "lalu cari sumber kebocoran. Jika bocor, hubungi mekanik."
                    )
                    R.id.cbRem -> tindakan.add(
                        "Rem: unit TIDAK boleh dioperasikan. Hubungi mekanik untuk " +
                                "memeriksa sistem rem sebelum dipakai."
                    )
                    R.id.cbBbm -> tindakan.add(
                        "BBM: isi tangki sampai cukup untuk satu shift kerja."
                    )
                    R.id.cbFisik -> tindakan.add(
                        "Kondisi fisik: foto bagian yang rusak, laporkan ke pool/dispatch, " +
                                "dan jangan operasikan sampai dinyatakan aman."
                    )
                }
            }
        }

        tvTindakanJudul.text = "TINDAKAN: UNIT BELUM BOLEH BEROPERASI"
        val awal = if (terverifikasi == 0) {
            "Belum ada item yang diperiksa. Periksa setiap item di atas lalu centang."
        } else {
            "Selesaikan tindakan berikut untuk item yang belum terverifikasi:"
        }
        tvTindakan.text = awal + "\n\n" +
                tindakan.joinToString("\n") { "• $it" } +
                "\n\nUnit baru boleh beroperasi setelah semua item tercentang."
        tvTindakan.setTextColor(Color.parseColor("#7C2D12"))
        cardTindakan.setCardBackgroundColor(Color.parseColor("#FFF7ED"))
        cardTindakan.strokeColor = Color.parseColor("#FDBA74")

        // Tujuan: unit belum siap -> telepon mekanik (aplikasi telepon terbuka)
        unitSiap = false
        btnAksi.text = "HUBUNGI MEKANIK"
        btnAksi.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#C62828"))
    }

    /**
     * Tombol aksi di kartu tindakan:
     * - Semua tercentang -> dialog konfirmasi -> Katalog (MainActivity)
     * - Belum lengkap    -> buka dialer ke nomor mekanik
     */
    private fun onAksiClicked() {
        if (unitSiap) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Unit Siap Beroperasi")
                .setMessage("Hasil inspeksi K3 tercatat. Unit boleh mulai dioperasikan di lokasi proyek.")
                .setPositiveButton("Selesai") { _, _ ->
                    startActivity(
                        Intent(this, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        }
                    )
                    finish()
                }
                .setNegativeButton("Batal", null)
                .show()
        } else {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$NOMOR_MEKANIK")))
        }
    }

    /**
     * Bottom navbar. Halaman ini dibuka dari TrackingActivity, jadi tab
     * "Pelacakan" (aktif) cukup finish() untuk kembali ke sana.
     */
    private fun setupBottomNav() {
        findViewById<View>(R.id.navCatalog).setOnClickListener {
            navigateTo(MainActivity::class.java)
        }
        findViewById<View>(R.id.navOperator).setOnClickListener {
            navigateTo(OperatorActivity::class.java)
        }
        findViewById<View>(R.id.navTracking).setOnClickListener { finish() }
        findViewById<View>(R.id.navSettings).setOnClickListener {
            navigateTo(SettingsActivity::class.java)
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

    companion object {
        const val EXTRA_UNIT_NAME = "EXTRA_UNIT_NAME"

        // Nomor contoh (dummy) -- ganti dengan nomor mekanik/dispatch yang sebenarnya
        private const val NOMOR_MEKANIK = "+6281234567890"
    }
}