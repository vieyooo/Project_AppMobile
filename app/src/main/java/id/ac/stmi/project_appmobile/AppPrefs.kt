package id.ac.stmi.project_appmobile

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Satu pesanan yang tampil di Riwayat Sewa. */
data class RiwayatOrder(
    val unit: String,
    val tanggal: String,
    val total: Long,
    val status: String,
    val pembayaran: String,
    val operator: String
)

/**
 * Preferensi aplikasi (disimpan di SharedPreferences).
 *
 * Sengaja dipisah dari "customer_session" (SessionManager) supaya pilihan
 * Pool & metode pembayaran TIDAK ikut terhapus saat user menekan Logout
 * (SessionManager.logout() memanggil clear() pada file sesi).
 */
object AppPrefs {

    private const val PREF_NAME = "app_preferences"
    private const val KEY_POOL_INDEX = "KEY_POOL_INDEX"
    private const val KEY_PAYMENT = "KEY_PAYMENT"
    private const val KEY_ORDERS = "KEY_ORDERS"

    const val PAYMENT_TRANSFER = "Transfer Bank"
    const val PAYMENT_TUNAI = "Tunai"
    const val PAYMENT_TERMIN = "Termin"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun getPoolIndex(context: Context): Int =
        prefs(context).getInt(KEY_POOL_INDEX, 0)

    fun setPoolIndex(context: Context, index: Int) {
        prefs(context).edit().putInt(KEY_POOL_INDEX, index).apply()
    }

    fun getPayment(context: Context): String =
        prefs(context).getString(KEY_PAYMENT, PAYMENT_TRANSFER) ?: PAYMENT_TRANSFER

    fun setPayment(context: Context, value: String) {
        prefs(context).edit().putString(KEY_PAYMENT, value).apply()
    }

    // ---------------------------------------------------------------
    // Riwayat pesanan (disimpan sebagai JSON string di SharedPreferences)
    // ---------------------------------------------------------------

    /** Tambah pesanan baru; yang terbaru selalu di urutan paling atas. */
    fun addOrder(context: Context, order: RiwayatOrder) {
        val lama = JSONArray(prefs(context).getString(KEY_ORDERS, "[]") ?: "[]")
        val baru = JSONArray()
        baru.put(
            JSONObject()
                .put("unit", order.unit)
                .put("tanggal", order.tanggal)
                .put("total", order.total)
                .put("status", order.status)
                .put("pembayaran", order.pembayaran)
                .put("operator", order.operator)
        )
        for (i in 0 until lama.length()) baru.put(lama.getJSONObject(i))
        prefs(context).edit().putString(KEY_ORDERS, baru.toString()).apply()
    }

    fun getOrders(context: Context): List<RiwayatOrder> {
        val arr = JSONArray(prefs(context).getString(KEY_ORDERS, "[]") ?: "[]")
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            RiwayatOrder(
                unit = o.optString("unit"),
                tanggal = o.optString("tanggal"),
                total = o.optLong("total"),
                status = o.optString("status"),
                pembayaran = o.optString("pembayaran"),
                operator = o.optString("operator")
            )
        }
    }
}
