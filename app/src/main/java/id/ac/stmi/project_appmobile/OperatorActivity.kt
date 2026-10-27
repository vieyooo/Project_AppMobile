package id.ac.stmi.project_appmobile

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import id.ac.stmi.project_appmobile.databinding.ActivityOperatorBinding
import android.widget.Toast

class OperatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOperatorBinding
    private lateinit var adapter: OperatorAdapter
    private var withOperatorMode = true
    private var selectedOperator: Operator? = null

    // id HARUS UNIK
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
            photoRes = null,
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

        // Data dari halaman Detail Alat (kalau tidak ada, pakai teks bawaan)
        val namaUnit = intent.getStringExtra(DetailActivity.EXTRA_NAMA_UNIT)
        val tglMulai = intent.getStringExtra(DetailActivity.EXTRA_TGL_MULAI)
        val tglSelesai = intent.getStringExtra(DetailActivity.EXTRA_TGL_SELESAI)
        val jumlahHari = intent.getIntExtra(DetailActivity.EXTRA_JUMLAH_HARI, 0)
        val tarifUnit = intent.getIntExtra(DetailActivity.EXTRA_TARIF_UNIT, 4_500_000)

        binding.tvUnitName.text = namaUnit?.uppercase() ?: "EXCAVATOR CAT 336D L"
        binding.tvDateRange.text =
            if (tglMulai != null && tglSelesai != null) "$tglMulai - $tglSelesai"
            else "28 Okt - 04 Nov 2024"
        binding.tvWorkDays.text =
            if (jumlahHari > 0) "$jumlahHari HARI KERJA" else "7 HARI KERJA"

        // Default sesuai desain: Budi terpilih
        selectedOperator = operators.first { it.id == 1 }

        adapter = OperatorAdapter(
            items = operators,
            selectedId = selectedOperator?.id,
            onDetail = { op ->
                val hari = if (jumlahHari > 0) jumlahHari else 7
                val detailIntent = Intent(this, DetailOperatorActivity::class.java).apply {
                    putExtra(DetailOperatorActivity.EXTRA_OPERATOR, op)
                    putExtra(DetailOperatorActivity.EXTRA_UNIT_NAME, namaUnit ?: "Excavator CAT 336D L")
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
        binding.btnBack.setOnClickListener { finish() }

        // ----- Menu bawah -----
        binding.navCatalog.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
        binding.navTracking.setOnClickListener {
            // TODO: startActivity(Intent(this, TrackingActivity::class.java))
            Toast.makeText(this, "Pelacakan segera hadir", Toast.LENGTH_SHORT).show()
        }
        binding.navSettings.setOnClickListener {
            // TODO: startActivity(Intent(this, SettingsActivity::class.java))
            Toast.makeText(this, "Pengaturan segera hadir", Toast.LENGTH_SHORT).show()
        }

        setMode(true)
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
        binding.btnConfirm.text = when {
            !withOperatorMode -> "Konfirmasi Unit Saja  →"
            selectedOperator == null -> "Pilih Operator Terlebih Dahulu"
            else -> "Konfirmasi Operator (1 Terpilih)  →"
        }
    }

    companion object {
        const val EXTRA_UNIT_NAME = "extra_unit_name"
        const val EXTRA_DATE_RANGE = "extra_date_range"
        const val EXTRA_WORK_DAYS = "extra_work_days"
    }
}