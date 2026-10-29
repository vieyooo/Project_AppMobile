package id.ac.stmi.project_appmobile

import android.app.Activity
import android.content.Context
import android.content.Intent


object SessionManager {

    private fun prefs(context: Context) =
        context.getSharedPreferences(LoginActivity.PREF_NAME, Context.MODE_PRIVATE)

    fun getName(context: Context): String =
        prefs(context).getString(LoginActivity.KEY_NAME, null) ?: "-"

    fun getPhone(context: Context): String =
        prefs(context).getString(LoginActivity.KEY_PHONE, null) ?: "-"

    /**
     * Hapus seluruh sesi lalu kembali ke LoginActivity.
     * Flag NEW_TASK + CLEAR_TASK membersihkan back stack, jadi tombol Back
     * di LoginActivity tidak akan membawa user kembali ke halaman katalog.
     */
    fun logout(activity: Activity) {
        prefs(activity).edit().clear().apply()

        val intent = Intent(activity, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        activity.startActivity(intent)
        activity.finish()
    }
}
