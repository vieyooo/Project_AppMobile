package id.ac.stmi.project_appmobile

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Data customer dibaca dari SharedPreferences "customer_session"
        val nama = SessionManager.getName(this)
        val phone = SessionManager.getPhone(this)

        findViewById<TextView>(R.id.tvProfileName).text = nama
        findViewById<TextView>(R.id.tvProfilePhone).text = phone
        findViewById<TextView>(R.id.tvAvatarInitial).text =
            nama.firstOrNull { it.isLetter() }?.uppercase() ?: "?"

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            // clear() sesi -> kembali ke LoginActivity (back stack dibersihkan)
            SessionManager.logout(this)
        }
    }
}
