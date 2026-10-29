package id.ac.stmi.project_appmobile

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class ChecklistK3Activity : AppCompatActivity() {

    private lateinit var checkBoxes: List<CheckBox>
    private lateinit var cardStatus: MaterialCardView
    private lateinit var tvStatus: TextView
    private lateinit var tvStatusDetail: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checklist_k3)

        cardStatus = findViewById(R.id.cardStatus)
        tvStatus = findViewById(R.id.tvStatus)
        tvStatusDetail = findViewById(R.id.tvStatusDetail)

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
    }

    companion object {
        const val EXTRA_UNIT_NAME = "EXTRA_UNIT_NAME"
    }
}
