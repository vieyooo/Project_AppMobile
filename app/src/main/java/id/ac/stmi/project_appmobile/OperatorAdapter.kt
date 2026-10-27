package id.ac.stmi.project_appmobile

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class OperatorAdapter(
    private val items: List<Operator>,
    private var selectedId: Int?,
    private val onDetail: (Operator) -> Unit,    // klik kartu -> buka detail
    private val onSelected: (Operator) -> Unit   // klik tombol Pilih
) : RecyclerView.Adapter<OperatorAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val card: MaterialCardView = v.findViewById(R.id.cardOperator)
        val imgPhoto: ImageView = v.findViewById(R.id.imgPhoto)
        val tvInitials: TextView = v.findViewById(R.id.tvInitials)
        val tvName: TextView = v.findViewById(R.id.tvName)
        val tvSio: TextView = v.findViewById(R.id.tvSio)
        val tvRating: TextView = v.findViewById(R.id.tvRating)
        val tvPrice: TextView = v.findViewById(R.id.tvPrice)
        val btnSelect: MaterialButton = v.findViewById(R.id.btnSelect)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_operator, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    /** Dipanggil dari luar (mis. setelah memilih di halaman detail). */
    fun select(id: Int) {
        val oldIndex = items.indexOfFirst { it.id == selectedId }
        selectedId = id
        if (oldIndex >= 0) notifyItemChanged(oldIndex)
        val newIndex = items.indexOfFirst { it.id == id }
        if (newIndex >= 0) notifyItemChanged(newIndex)
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val op = items[position]
        val selected = selectedId != null && op.id == selectedId
        val density = h.itemView.resources.displayMetrics.density

        h.tvName.text = op.name
        h.tvSio.text = "SIO: ${op.sio}  •  ${op.hours} Jam"
        h.tvRating.text = "⭐ ${op.rating} (${op.reviews})  •  ${op.kelas}"
        h.tvPrice.text = "Rp " + "%,d".format(op.price).replace(',', '.')

        if (op.photoRes != null) {
            h.imgPhoto.visibility = View.VISIBLE
            h.tvInitials.visibility = View.GONE
            h.imgPhoto.setImageResource(op.photoRes)
        } else {
            h.imgPhoto.visibility = View.GONE
            h.tvInitials.visibility = View.VISIBLE
            h.tvInitials.text = op.name.split(" ").take(2)
                .joinToString("") { it.first().uppercase() }
        }

        h.card.strokeColor = Color.parseColor(if (selected) "#8D4B00" else "#E3E5E8")
        h.card.strokeWidth = ((if (selected) 2 else 1) * density).toInt()

        h.btnSelect.text = if (selected) "✓ Dipilih" else "Pilih"
        h.btnSelect.backgroundTintList =
            ColorStateList.valueOf(Color.parseColor(if (selected) "#8D4B00" else "#EDEEF0"))
        h.btnSelect.setTextColor(Color.parseColor(if (selected) "#FFFFFF" else "#6B6F76"))

        // Klik kartu -> detail operator
        h.card.setOnClickListener {
            android.util.Log.d("OPERATOR", "kartu diklik: ${op.name}")
            onDetail(op)
        }

        // Klik tombol -> pilih operator
        h.btnSelect.setOnClickListener {
            android.util.Log.d("OPERATOR", "tombol pilih diklik: ${op.name}")
            if (selectedId == op.id) return@setOnClickListener
            select(op.id)
            onSelected(op)
        }
    }
}
