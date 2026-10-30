package id.ac.stmi.project_appmobile

import android.graphics.Color
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
 * Riwayat sewa. Memakai ListView + ArrayAdapter (Pertemuan 5).
 * Data pesanan dibaca dari SharedPreferences (Pertemuan 7) lewat AppPrefs.
 * Pesanan dicatat saat customer menekan Konfirmasi di OperatorActivity.
 */
class RiwayatActivity : AppCompatActivity() {

    private lateinit var daftar: List<RiwayatOrder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        daftar = AppPrefs.getOrders(this)

        val listView = findViewById<ListView>(R.id.lvRiwayat)
        listView.emptyView = findViewById(R.id.tvKosong) // tampil otomatis kalau daftar kosong
        listView.adapter = RiwayatAdapter()

        // Klik item -> dialog rincian
        listView.setOnItemClickListener { _, _, position, _ ->
            val item = daftar[position]
            MaterialAlertDialogBuilder(this)
                .setTitle(item.unit)
                .setMessage(
                    "Tanggal: ${item.tanggal}\n" +
                            "Operator: ${item.operator}\n" +
                            "Total: ${rupiah(item.total)}\n" +
                            "Pembayaran: ${item.pembayaran}\n" +
                            "Status: ${item.status}"
                )
                .setPositiveButton("Tutup", null)
                .show()
        }
    }

    private fun rupiah(nilai: Long) = "Rp " + "%,d".format(nilai).replace(',', '.')

    private inner class RiwayatAdapter :
        ArrayAdapter<RiwayatOrder>(this@RiwayatActivity, R.layout.item_riwayat, daftar) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val row = convertView
                ?: LayoutInflater.from(context).inflate(R.layout.item_riwayat, parent, false)
            val item = getItem(position)!!

            row.findViewById<TextView>(R.id.tvRiwayatUnit).text = item.unit
            row.findViewById<TextView>(R.id.tvRiwayatTanggal).text = item.tanggal
            row.findViewById<TextView>(R.id.tvRiwayatTotal).text = "Total: ${rupiah(item.total)}"

            val tvStatus = row.findViewById<TextView>(R.id.tvRiwayatStatus)
            tvStatus.text = item.status
            tvStatus.setTextColor(
                Color.parseColor(
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
