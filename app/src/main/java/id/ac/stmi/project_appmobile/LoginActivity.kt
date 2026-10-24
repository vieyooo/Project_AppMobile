package id.ac.stmi.project_appmobile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {

    private lateinit var tilName: TextInputLayout
    private lateinit var tilPhone: TextInputLayout
    private lateinit var etName: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var btnLogin: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Kalau sudah pernah login, langsung ke katalog
        if (isLoggedIn()) {
            goToCatalog()
            return
        }

        setContentView(R.layout.activity_login)

        tilName = findViewById(R.id.tilName)
        tilPhone = findViewById(R.id.tilPhone)
        etName = findViewById(R.id.editTextText)
        etPhone = findViewById(R.id.editTextPhone)
        btnLogin = findViewById(R.id.button)

        btnLogin.setOnClickListener { attemptLogin() }

        // Tombol "Done" di keyboard juga memicu login
        etPhone.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin()
                true
            } else {
                false
            }
        }
    }

    private fun attemptLogin() {
        val name = etName.text.toString().trim()
        val phone = etPhone.text.toString().trim()

        tilName.error = null
        tilPhone.error = null

        var valid = true

        if (name.isEmpty()) {
            tilName.error = "Nama customer wajib diisi"
            valid = false
        }

        if (phone.isEmpty()) {
            tilPhone.error = "Nomor HP wajib diisi"
            valid = false
        } else if (!PHONE_REGEX.matches(phone)) {
            tilPhone.error = "Nomor HP tidak valid (contoh: 082213232393)"
            valid = false
        }

        if (!valid) return

        saveSession(name, phone)
        Toast.makeText(this, "Selamat datang, $name!", Toast.LENGTH_SHORT).show()
        goToCatalog()
    }

    private fun saveSession(name: String, phone: String) {
        getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_NAME, name)
            .putString(KEY_PHONE, phone)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    private fun isLoggedIn(): Boolean =
        getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_IS_LOGGED_IN, false)

    private fun goToCatalog() {
        // MainActivity = halaman katalog (dikerjakan Ka Bagus)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    // Bisa dipakai Activity lain, mis. SettingsActivity:
    // LoginActivity.PREF_NAME, LoginActivity.KEY_NAME
    companion object {
        const val PREF_NAME = "customer_session"
        const val KEY_NAME = "KEY_NAME"
        const val KEY_PHONE = "KEY_PHONE"
        const val KEY_IS_LOGGED_IN = "KEY_IS_LOGGED_IN"

        // Format nomor HP Indonesia: diawali 08, total 10-13 digit
        private val PHONE_REGEX = Regex("^08\\d{8,11}$")
    }
}