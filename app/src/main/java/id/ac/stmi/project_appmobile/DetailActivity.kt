package id.ac.stmi.project_appmobile

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import android.content.Intent

class DetailActivity : AppCompatActivity() {

    // Data dari katalog
    private var namaUnit = "-"
    private var poolUnit = "-"
    private var tarifPerHari = 0

    // Tanggal terpilih (milidetik UTC dari MaterialDatePicker), null = belum dipilih
    private var tglMulai: Long? = null
    private var tglSelesai: Long? = null

    private lateinit var tvTglMulai: TextView
    private lateinit var tvTglSelesai: TextView
    private lateinit var tvRincian: TextView
    private lateinit var tvTotal: TextView
    private lateinit var btnLanjut: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // Ambil data yang dikirim MainActivity
        namaUnit = intent.getStringExtra(MainActivity.EXTRA_NAMA) ?: "-"
        tarifPerHari = intent.getIntExtra(MainActivity.EXTRA_TARIF, 0)
        poolUnit = intent.getStringExtra(MainActivity.EXTRA_POOL) ?: "-"

        tvTglMulai = findViewById(R.id.tvTglMulai)
        tvTglSelesai = findViewById(R.id.tvTglSelesai)
        tvRincian = findViewById(R.id.tvRincian)
        tvTotal = findViewById(R.id.tvTotal)
        btnLanjut = findViewById(R.id.btnLanjut)

        tampilkanDataUnit()
        hitungEstimasi()

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.boxMulai).setOnClickListener { pilihTanggalMulai() }
        findViewById<View>(R.id.boxSelesai).setOnClickListener { pilihTanggalSelesai() }
        btnLanjut.setOnClickListener { lanjutKeOperator() }

        // Menu bawah (Katalog = kembali ke katalog)
        findViewById<View>(R.id.navCatalog).setOnClickListener { finish() }
        findViewById<View>(R.id.navOperator).setOnClickListener {
            startActivity(Intent(this, OperatorActivity::class.java))
        }
        findViewById<View>(R.id.navTracking).setOnClickListener {
            val intent = Intent(this, TrackingActivity::class.java).apply {
                putExtra(TrackingActivity.EXTRA_ORDER_ID, "TR-2024-0891")
                putExtra("EXTRA_UNIT_NAME", namaUnit.uppercase())
                // Dari Detail belum ada operator terpilih -> mode "Tanpa Operator"
                // (nama driver & nomor telepon terisi otomatis di TrackingActivity)
                putExtra("EXTRA_WITH_OPERATOR", false)
            }
            startActivity(intent)
        }
        findViewById<View>(R.id.navSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    // ---------------------------------------------------------------
    // Data unit (dummy) berdasarkan nama unit dari katalog
    // ---------------------------------------------------------------
    private fun tampilkanDataUnit() {
        val kode: String
        val labelKapasitas: String
        val kapasitas: String
        val tonase: String
        val bbm: String
        val jamKerja: String
        val kategori: String
        val gambar: Int

        when (namaUnit) {
            "Bulldozer CAT D6R" -> {
                kode = "UNIT #CAT-D6R-JKT"; labelKapasitas = "BLADE"; kapasitas = "3.8 m³"
                tonase = "20.5 T"; bbm = "18.0 L/j"; jamKerja = "3.120 Jam Kerja"
                kategori = "Land Clearing"; gambar = R.drawable.ic_bulldozer
            }
            "Motor Grader CAT 140" -> {
                kode = "UNIT #CAT-140-JKT"; labelKapasitas = "MOLDBOARD"; kapasitas = "4.2 m"
                tonase = "19.0 T"; bbm = "17.0 L/j"; jamKerja = "2.740 Jam Kerja"
                kategori = "Road Grading"; gambar = R.drawable.ic_motorgrader
            }
            "Wheel Loader CAT 950 GC" -> {
                kode = "UNIT #CAT-950-JKT"; labelKapasitas = "BUCKET"; kapasitas = "3.1 m³"
                tonase = "18.6 T"; bbm = "16.5 L/j"; jamKerja = "2.050 Jam Kerja"
                kategori = "Material Handling"; gambar = R.drawable.ic_wheelloader
            }
            else -> { // Excavator CAT 336D L
                kode = "UNIT #CAT-336-JKT"; labelKapasitas = "BUCKET"; kapasitas = "1.9 m³"
                tonase = "36.2 T"; bbm = "22.5 L/j"; jamKerja = "2.418 Jam Kerja"
                kategori = "Heavy Earthmoving"; gambar = R.drawable.ic_excavator
            }
        }

        findViewById<TextView>(R.id.tvKode).text = "$kode · ${poolUnit.uppercase()}"
        findViewById<TextView>(R.id.tvNama).text = namaUnit
        findViewById<TextView>(R.id.tvTarif).text = "Rp ${formatRupiah(tarifPerHari.toLong())}"
        findViewById<TextView>(R.id.tvLabelKapasitas).text = labelKapasitas
        findViewById<TextView>(R.id.tvKapasitas).text = kapasitas
        findViewById<TextView>(R.id.tvTonase).text = tonase
        findViewById<TextView>(R.id.tvBbm).text = bbm
        findViewById<TextView>(R.id.tvKategori).text = kategori
        findViewById<TextView>(R.id.tvJamKerja).text = jamKerja
        findViewById<ImageView>(R.id.imgHero).setImageResource(gambar)
    }

    // ---------------------------------------------------------------
    // MaterialDatePicker
    // ---------------------------------------------------------------
    private fun pilihTanggalMulai() {
        val batas = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.now())
            .build()

        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Pilih tanggal mulai sewa")
            .setSelection(tglMulai ?: MaterialDatePicker.todayInUtcMilliseconds())
            .setCalendarConstraints(batas)
            .build()

        picker.addOnPositiveButtonClickListener { pilihan ->
            tglMulai = pilihan
            tvTglMulai.text = formatTanggal(pilihan)

            val selesai = tglSelesai
            if (selesai != null && selesai < pilihan) {
                tglSelesai = null
                tvTglSelesai.text = "Pilih tanggal"
            }
            hitungEstimasi()
        }
        picker.show(supportFragmentManager, "TGL_MULAI")
    }

    private fun pilihTanggalSelesai() {
        val mulai = tglMulai
        if (mulai == null) {
            Toast.makeText(this, "Pilih tanggal mulai dulu", Toast.LENGTH_SHORT).show()
            return
        }

        val batas = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.from(mulai))
            .build()

        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Pilih tanggal selesai sewa")
            .setSelection(tglSelesai ?: mulai)
            .setCalendarConstraints(batas)
            .build()

        picker.addOnPositiveButtonClickListener { pilihan ->
            tglSelesai = pilihan
            tvTglSelesai.text = formatTanggal(pilihan)
            hitungEstimasi()
        }
        picker.show(supportFragmentManager, "TGL_SELESAI")
    }

    // ---------------------------------------------------------------
    // Kalkulator sewa: total = tarif per hari x jumlah hari
    // ---------------------------------------------------------------
    private fun hitungEstimasi() {
        val mulai = tglMulai
        val selesai = tglSelesai

        if (mulai == null || selesai == null) {
            tvRincian.text = "Pilih tanggal sewa dulu"
            tvTotal.text = "Rp 0"
            btnLanjut.alpha = 0.5f
            return
        }

        // DIPERBAIKI: Perhitungan selisih hari disatukan dalam satu baris
        val selisihHari = ((selesai - mulai) / MILIDETIK_PER_HARI).toInt()
        val jumlahHari = if (selisihHari < 1) 1 else selisihHari
        val total = tarifPerHari.toLong() * jumlahHari

        tvRincian.text = "Rp ${formatRupiah(tarifPerHari.toLong())} × $jumlahHari hari"
        tvTotal.text = "Rp ${formatRupiah(total)}"
        btnLanjut.alpha = 1f
    }

    private fun lanjutKeOperator() {
        val mulai = tglMulai
        val selesai = tglSelesai
        if (mulai == null || selesai == null) {
            Toast.makeText(this, "Lengkapi tanggal sewa dulu", Toast.LENGTH_SHORT).show()
            return
        }

        // DIPERBAIKI: Perhitungan selisih hari disatukan dalam satu baris
        val selisihHari = ((selesai - mulai) / MILIDETIK_PER_HARI).toInt()
        val jumlahHari = if (selisihHari < 1) 1 else selisihHari

        val intent = Intent(this, OperatorActivity::class.java)
        intent.putExtra(EXTRA_NAMA_UNIT, namaUnit)
        intent.putExtra(EXTRA_TARIF_UNIT, tarifPerHari)
        intent.putExtra(EXTRA_TGL_MULAI, formatTanggal(mulai))
        intent.putExtra(EXTRA_TGL_SELESAI, formatTanggal(selesai))
        intent.putExtra(EXTRA_JUMLAH_HARI, jumlahHari)
        startActivity(intent)
    }

    // ---------------------------------------------------------------
    // Format
    // ---------------------------------------------------------------
    private fun formatTanggal(millis: Long): String {
        val format = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID"))
        format.timeZone = TimeZone.getTimeZone("UTC")
        return format.format(millis)
    }

    private fun formatRupiah(nilai: Long): String =
        String.format(Locale.forLanguageTag("id-ID"), "%,d", nilai)

    companion object {
        // DIPERBAIKI: Penulisan konstanta milidetik per hari disatukan dalam satu baris
        private const val MILIDETIK_PER_HARI = 24L * 60L * 60L * 1000L

        const val EXTRA_NAMA_UNIT = "EXTRA_NAMA_UNIT"
        const val EXTRA_TARIF_UNIT = "EXTRA_TARIF_UNIT"
        const val EXTRA_TGL_MULAI = "EXTRA_TGL_MULAI"
        const val EXTRA_TGL_SELESAI = "EXTRA_TGL_SELESAI"
        const val EXTRA_JUMLAH_HARI = "EXTRA_JUMLAH_HARI"
    }
}