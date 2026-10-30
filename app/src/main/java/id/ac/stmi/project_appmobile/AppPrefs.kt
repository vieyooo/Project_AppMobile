package id.ac.stmi.project_appmobile

import android.content.Context

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
}