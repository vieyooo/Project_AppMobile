package id.ac.stmi.project_appmobile

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.ac.stmi.project_appmobile.databinding.ActivityOperatorBinding

class OperatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOperatorBinding
    private lateinit var adapter: OperatorAdapter
    private var withOperatorMode = true
    private var selectedOperator: Operator? = null

    // Nama unit dari katalog. null = belum memilih unit
    private var namaUnit: String? = null

    // Data pesanan dari halaman Detail (dipakai untuk Riwayat Sewa)
    private var tglMulaiPesan: String? = null
    private var tglSelesaiPesan: String? = null
    private var hariPesan = 1
    private var tarifUnitPesan = 0

    // Daftar operator (Budi & Hendra dengan foto masing-masing)
    private val operators = listOf(
        Operator(
            id = 1, name = "Budi Santoso", sio = "1234-5678", hours = "4.2k",
            rating = 4.9, reviews = 98, kelas = "Kelas I Heavy", price = 350000,
            photoRes = R.drawable.foto_budi,
            role = "Senior Heavy Operator  •  Pool A Karawang",
            sioCode = "#SIO-1234-JKT",
            totalHours = "4.280 Jam",
            experience = "8+ Tahun",
            sioValid = "Aktif s/d 2027"
        ),
        Operator(
            id = 2, name = "Hendra Wijaya", sio = "5678-9012", hours = "3.6k",
            rating = 4.8, reviews = 84, kelas = "Kelas II", price = 350000,
            photoRes = R.drawable.foto_hendra,
            role = "Heavy Equipment Operator  •  Pool B Bekasi",
            sioCode = "#SIO-5678-BKS",
            totalHours = "3.640 Jam",
            experience = "6+ Tahun",
            sioValid = "Aktif s/d 2026"
        )
    )

    // Menerima hasil dari halaman Detail Operator
    private val detailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val id = result.data?.getIntExtra(DetailOperatorActivity.EXTRA_SELECTED_ID, -1) ?: -1
            operators.firstOrNull { it.id == id }?.let { op ->
                selectedOperator = op
                adapter.select(op.id)
                updateConfirmButton()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOperatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Data dari halaman Detail Alat (kosong kalau dibuka dari menu bawah)
        namaUnit = intent.getStringExtra(DetailActivity.EXTRA_NAMA_UNIT)
        val tglMulai = intent.getStringExtra(DetailActivity.EXTRA_TGL_MULAI)
        val tglSelesai = intent.getStringExtra(DetailActivity.EXTRA_TGL_SELESAI)
        val jumlahHari = intent.getIntExtra(DetailActivity.EXTRA_JUMLAH_HARI, 0)
        val tarifUnit = intent.getIntExtra(DetailActivity.EXTRA_TARIF_UNIT, 0)

        tglMulaiPesan = tglMulai
        tglSelesaiPesan = tglSelesai
        hariPesan = if (jumlahHari > 0) jumlahHari else 1
        tarifUnitPesan = tarifUnit

        binding.tvUnitName.text = namaUnit?.uppercase() ?: "BELUM ADA UNIT DIPILIH"

        binding.tvDateRange.text = if (tglMulai != null && tglSelesai != null) {
            "$tglMulai\n$tglSelesai"
        } else {
            "Belum ada tanggal sewa"
        }

        binding.tvWorkDays.text =
            if (jumlahHari > 0) "$jumlahHari HARI KERJA" else "- HARI KERJA"

        bindUnitPreview(tarifUnit, jumlahHari)

        // Tidak ada operator yang terpilih di awal
        selectedOperator = null

        adapter = OperatorAdapter(
            items = operators,
            selectedId = null,
            onDetail = { op ->
                val hari = if (jumlahHari > 0) jumlahHari else 1
                val detailIntent = Intent(this, DetailOperatorActivity::class.java).apply {
                    putExtra(DetailOperatorActivity.EXTRA_OPERATOR, op)
                    putExtra(DetailOperatorActivity.EXTRA_UNIT_NAME, namaUnit ?: "Belum ada unit")
                    putExtra(DetailOperatorActivity.EXTRA_PACKAGE_TOTAL, hari * (tarifUnit + op.price))
                    putExtra(DetailOperatorActivity.EXTRA_PACKAGE_DAYS, hari)
                }
                detailLauncher.launch(detailIntent)
            },
            onSelected = { op ->
                selectedOperator = op
                updateConfirmButton()
            }
        )
        binding.rvOperators.adapter = adapter

        binding.btnUnitOnly.setOnClickListener { setMode(false) }
        binding.btnUnitOperator.setOnClickListener { setMode(true) }

        // Tombol Konfirmasi: hanya lanjut kalau unit dan operator (bila perlu) sudah dipilih
        binding.btnConfirm.setOnClickListener {
            when {
                namaUnit == null ->
                    Toast.makeText(this, "Pilih unit di Katalog terlebih dahulu", Toast.LENGTH_SHORT).show()
                withOperatorMode && selectedOperator == null ->
                    Toast.makeText(this, "Pilih operator terlebih dahulu", Toast.LENGTH_SHORT).show()
                // Mode "Unit Saja": minta konfirmasi bahwa pesanan tanpa operator
                !withOperatorMode -> tampilkanDialogTanpaOperator()
                else -> lanjutKePelacakan()
            }
        }

        // Navigasi Bawah
        binding.navCatalog.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        binding.navTracking.setOnClickListener {
            startActivity(buildTrackingIntent())
            finish()
        }

        binding.navSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        setMode(true)
    }

    // Preview unit yang dipesan (tampil di mode "Unit Saja")
    private fun bindUnitPreview(tarifUnit: Int, jumlahHari: Int) {
        val nama = namaUnit
        if (nama == null) {
            binding.layoutUnitPreview.visibility = View.GONE
            return
        }

        val upper = nama.uppercase()
        binding.ivUnitPreview.setImageResource(
            when {
                upper.contains("EXCAVATOR") -> R.drawable.ic_excavator
                upper.contains("BULLDOZER") -> R.drawable.ic_bulldozer
                upper.contains("GRADER") -> R.drawable.ic_motorgrader
                upper.contains("LOADER") -> R.drawable.ic_wheelloader
                else -> R.drawable.ic_excavator
            }
        )

        val hari = if (jumlahHari > 0) jumlahHari else 1
        binding.tvUnitPreviewName.text = nama
        binding.tvUnitPreviewTarif.text = "${rupiah(tarifUnit)} / hari × $hari hari"
        binding.tvUnitPreviewTotal.text = "Estimasi total: ${rupiah(tarifUnit * hari)}"
    }

    private fun rupiah(nilai: Int) = "Rp " + "%,d".format(nilai).replace(',', '.')

    // Simpan pesanan ke Riwayat Sewa (SharedPreferences via AppPrefs)
    private fun simpanRiwayat() {
        val unit = namaUnit ?: return
        val op = if (withOperatorMode) selectedOperator else null
        val total = hariPesan.toLong() * (tarifUnitPesan + (op?.price ?: 0))
        val mulai = tglMulaiPesan
        val selesai = tglSelesaiPesan
        AppPrefs.addOrder(
            this,
            RiwayatOrder(
                unit = unit,
                tanggal = if (mulai != null && selesai != null) "$mulai - $selesai" else "-",
                total = total,
                status = "Berjalan",
                pembayaran = AppPrefs.getPayment(this),
                operator = op?.name ?: "Tanpa operator"
            )
        )
    }

    private fun lanjutKePelacakan() {
        simpanRiwayat()
        startActivity(buildTrackingIntent())
        finish()
    }

    private fun tampilkanDialogTanpaOperator() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Sewa Tanpa Operator")
            .setMessage(
                "Anda menyewa ${namaUnit ?: "unit"} tanpa operator.\n\n" +
                        "Unit akan dikirim tanpa pengemudi/operator alat berat. " +
                        "Pastikan Anda menyiapkan operator bersertifikat SIO sendiri."
            )
            .setPositiveButton("Lanjutkan") { _, _ -> lanjutKePelacakan() }
            .setNegativeButton("Kembali", null)
            .show()
    }

    // Data pesanan hanya dikirim kalau pesanan sudah lengkap.
    // Kalau belum, halaman Pelacakan menampilkan keadaan "belum ada pesanan".
    private fun buildTrackingIntent(): Intent {
        val intent = Intent(this, TrackingActivity::class.java)
        val unit = namaUnit
        val operator = selectedOperator
        val lengkap = unit != null && (!withOperatorMode || operator != null)

        if (lengkap) {
            intent.putExtra(TrackingActivity.EXTRA_ORDER_ID, "TR-2024-0891")
            intent.putExtra("EXTRA_UNIT_NAME", unit!!.uppercase())
            intent.putExtra("EXTRA_UNIT_TYPE", unit.uppercase())
            intent.putExtra("EXTRA_WITH_OPERATOR", withOperatorMode)
            if (withOperatorMode && operator != null) {
                intent.putExtra("EXTRA_DRIVER_NAME", operator.name)
                intent.putExtra("EXTRA_DRIVER_ROLE", operator.role)
                operator.photoRes?.let { intent.putExtra("EXTRA_DRIVER_PHOTO", it) }
            }
        }
        return intent
    }

    private fun setMode(withOperator: Boolean) {
        withOperatorMode = withOperator

        fun style(btn: MaterialButton, active: Boolean) {
            btn.backgroundTintList = ColorStateList.valueOf(
                if (active) Color.WHITE else Color.TRANSPARENT
            )
            btn.setTextColor(Color.parseColor(if (active) "#191C1E" else "#6B6F76"))
            btn.strokeWidth = if (active) 2 else 0
            btn.strokeColor = ColorStateList.valueOf(Color.parseColor("#E3E5E8"))
        }
        style(binding.btnUnitOperator, withOperator)
        style(binding.btnUnitOnly, !withOperator)

        binding.sectionOperators.visibility = if (withOperator) View.VISIBLE else View.GONE
        binding.cardInfoK3.visibility = if (withOperator) View.VISIBLE else View.GONE
        binding.sectionUnitOnly.visibility = if (withOperator) View.GONE else View.VISIBLE
        updateConfirmButton()
    }

    private fun updateConfirmButton() {
        val bisaLanjut = namaUnit != null && (!withOperatorMode || selectedOperator != null)
        binding.btnConfirm.text = when {
            namaUnit == null -> "Pilih Unit di Katalog Dulu"
            !withOperatorMode -> "Konfirmasi Unit Saja  →"
            selectedOperator == null -> "Pilih Operator Terlebih Dahulu"
            else -> "Konfirmasi Operator (1 Terpilih)  →"
        }
        binding.btnConfirm.alpha = if (bisaLanjut) 1f else 0.5f
    }

    companion object {
        const val EXTRA_UNIT_NAME = "extra_unit_name"
        const val EXTRA_DATE_RANGE = "extra_date_range"
        const val EXTRA_WORK_DAYS = "extra_work_days"
    }
}