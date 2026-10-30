package id.ac.stmi.project_appmobile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import id.ac.stmi.project_appmobile.databinding.ActivityDetailOperatorBinding

class DetailOperatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailOperatorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailOperatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        val op = intent.getSerializableExtra(EXTRA_OPERATOR) as? Operator
        if (op == null) {
            android.util.Log.d("OPERATOR", "detail: data operator NULL, activity ditutup")
            finish()
            return
        }
        android.util.Log.d("OPERATOR", "detail terbuka: ${op.name}")

        // ---- Isi data operator ----
        binding.tvSioCode.text = op.sioCode
        binding.tvName.text = op.name
        binding.tvRole.text = op.role
        binding.tvPrice.text = rupiah(op.price)
        binding.tvHeroHours.text = "🕒 ${op.hours} Jam"
        binding.tvStatHours.text = op.totalHours
        binding.tvStatRating.text = "${op.rating} / 5.0"
        binding.tvStatExperience.text = op.experience
        binding.tvSioDesc.text = "Reg. Reguler ${op.kelas} Alat Berat"
        binding.tvSioValid.text = op.sioValid

        // Paket sewa + operator (data dikirim dari halaman Operator)
        val unitName = intent.getStringExtra(EXTRA_UNIT_NAME) ?: "Excavator Cat 320 CC"
        val packageTotal = intent.getIntExtra(EXTRA_PACKAGE_TOTAL, 33_950_000)
        binding.tvPackageUnit.text = unitName
        binding.tvPackageTotal.text = rupiah(packageTotal)

        val packageDays = intent.getIntExtra(EXTRA_PACKAGE_DAYS, 7)
        binding.tvPackageTitle.text = "PAKET SEWA $packageDays HARI + OPERATOR"

        // Menampilkan foto operator secara dinamis (Budi / Hendra)
        if (op.photoRes != null) {
            binding.imgHero.setImageResource(op.photoRes)
            binding.tvHeroEmoji.visibility = View.GONE
        }

        // ---- Aksi Tombol ----
        binding.btnBack.setOnClickListener { finish() }

        binding.btnShare.setOnClickListener {
            val share = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Operator ${op.name} (${op.sioCode}) - ${rupiah(op.price)} / hari")
            }
            startActivity(Intent.createChooser(share, "Bagikan operator"))
        }

        // Tombol pilih operator di halaman detail
        binding.btnChoose.setOnClickListener {
            setResult(RESULT_OK, Intent().putExtra(EXTRA_SELECTED_ID, op.id))
            finish()
        }

        // ---- Menu Navigasi Bawah ----
        binding.navCatalog.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        binding.navOperator.setOnClickListener {
            finish() // kembali ke daftar operator
        }

        // Diarahkan langsung ke halaman pelacakan dengan membawa data operator aktif (op)
        binding.navTracking.setOnClickListener {
            val intent = Intent(this, TrackingActivity::class.java).apply {
                putExtra(TrackingActivity.EXTRA_ORDER_ID, "TR-2024-0891")
                putExtra("EXTRA_UNIT_NAME", unitName)
                putExtra("EXTRA_DRIVER_NAME", op.name)
                putExtra("EXTRA_DRIVER_ROLE", op.role)
                putExtra("EXTRA_DRIVER_PHOTO", op.photoRes ?: R.drawable.foto_budi)
                this@DetailOperatorActivity.intent.getStringExtra(EXTRA_LOKASI)?.let { putExtra(TrackingActivity.EXTRA_LOKASI, it) }
            }
            startActivity(intent)
            finish()
        }

        binding.navSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun rupiah(value: Int): String =
        "Rp " + "%,d".format(value).replace(',', '.')

    companion object {
        const val EXTRA_OPERATOR = "extra_operator"
        const val EXTRA_SELECTED_ID = "extra_selected_id"
        const val EXTRA_UNIT_NAME = "extra_unit_name"
        const val EXTRA_PACKAGE_TOTAL = "extra_package_total"
        const val EXTRA_PACKAGE_DAYS = "extra_package_days"
        const val EXTRA_LOKASI = "extra_lokasi"
    }
}