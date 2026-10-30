package id.ac.stmi.project_appmobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Riwayat sewa unit. Memakai ListView + ArrayAdapter (Pertemuan 5).
 * Data masih dummy; metode pembayaran diambil dari SharedPreferences (Pertemuan 7).
 */
class RiwayatActivity : AppCompatActivity() {

    private data class Riwayat(
        val unit: String,
        val tanggal: String,
        val total: Long,
        val status: String
    )

    private val daftar = listOf(
        Riwayat("Excavator CAT 336D L", "12 Okt 2026 - 16 Okt 2026", 22_500_000, "Berjalan"),
        Riwayat("Bulldozer CAT D6R", "01 Okt 2026 - 05 Okt 2026", 15_200_000, "Selesai"),
        Riwayat("Wheel Loader CAT 950 GC", "15 Sep 2026 - 18 Sep 2026", 10_500_000, "Selesai"),
        Riwayat("Motor Grader CAT 140", "02 Sep 2026 - 03 Sep 2026", 4_100_000, "Dibatalkan")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        val listView = findViewById<ListView>(R.id.lvRiwayat)
        listView.adapter = RiwayatAdapter()

        // Klik item -> dialog rincian
        listView.setOnItemClickListener { _, _, position, _ ->
            val item = daftar[position]
            MaterialAlertDialogBuilder(this)
                .setTitle(item.unit)
                .setMessage(
                    "Tanggal: ${item.tanggal}\n" +
                            "Total: ${rupiah(item.total)}\n" +
                            "Status: ${item.status}\n" +
                            "Pembayaran: ${AppPrefs.getPayment(this)}"
                )
                .setPositiveButton("Tutup", null)
                .show()
        }
    }

    private fun rupiah(nilai: Long) = "Rp " + "%,d".format(nilai).replace(',', '.')

    private inner class RiwayatAdapter :
        ArrayAdapter<Riwayat>(this@RiwayatActivity, R.layout.activity_item_riwayat, daftar) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val row = convertView
                ?: LayoutInflater.from(context).inflate(R.layout.activity_item_riwayat, parent, false)
            val item = getItem(position)!!

            row.findViewById<TextView>(R.id.tvRiwayatUnit).text = item.unit
            row.findViewById<TextView>(R.id.tvRiwayatTanggal).text = item.tanggal
            row.findViewById<TextView>(R.id.tvRiwayatTotal).text = "Total: ${rupiah(item.total)}"

            val tvStatus = row.findViewById<TextView>(R.id.tvRiwayatStatus)
            tvStatus.text = item.status
            tvStatus.setTextColor(
                android.graphics.Color.parseColor(
                    when (item.status) {
                        "Selesai" -> "#2E7D32"
                        "Berjalan" -> "#8D4B00"
                        else -> "#C62828"
                    }
                )
            )
            return row
        }
    }
}