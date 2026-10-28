package id.ac.stmi.project_appmobile

import android.animation.ObjectAnimator
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator

class TrackingActivity : AppCompatActivity() {

    private class Step(val title: String, val subtitle: String, var time: String)

    private val steps = listOf(
        Step("Order Confirmed", "Pesanan & verifikasi pembayaran sukses", "08:00"),
        Step("Unit Loaded", "Alat berat dinaikkan ke truk pengangkut", "10:30"),
        Step("In Transit", "Truk pengangkut dalam perjalanan ke lokasi", "12:15"),
        Step("Arrived at Site", "Tiba di lokasi proyek customer", "--:--")
    )

    private enum class StepState { DONE, ACTIVE, PENDING }

    private var driverName = "Belum Ada Operator"
    private var driverRole = "Silakan pilih pesanan terlebih dahulu"
    private var driverPhotoRes = R.drawable.trk_ic_person
    private var driverVehicle = "---"
    private var driverPhoneDisplay = "-"
    private var driverPhoneDial = ""
    private var activeUnitName = "Belum Ada Unit Dipilih"
    private var isWithOperator = true
    private var hasActiveOrder = false

    private var currentStage = 0
    private var remainingMinutes = 0
    private val totalJourneyMinutes = 180
    private val tickDelayMs = 3000L

    private val handler = Handler(Looper.getMainLooper())
    private var liveDotAnimator: ObjectAnimator? = null

    private lateinit var stepContainer: LinearLayout
    private lateinit var tvStatusChip: TextView
    private lateinit var tvStageLabel: TextView
    private lateinit var tvPercent: TextView
    private lateinit var tvEtaTime: TextView
    private lateinit var tvEtaRemaining: TextView
    private lateinit var progress: LinearProgressIndicator
    private lateinit var viewLiveDot: View

    private val ticker = object : Runnable {
        override fun run() {
            if (hasActiveOrder && remainingMinutes > 0) {
                remainingMinutes -= 10

                val elapsedMinutes = totalJourneyMinutes - remainingMinutes
                var calculatedPercent = (elapsedMinutes * 100) / totalJourneyMinutes
                if (calculatedPercent > 99) calculatedPercent = 99

                currentStage = when {
                    calculatedPercent < 25 -> 1
                    calculatedPercent < 50 -> 2
                    calculatedPercent < 100 -> 3
                    else -> 4
                }

                renderStage()
                updateEta()

                if (remainingMinutes <= 0) {
                    onArrived()
                } else {
                    handler.postDelayed(this, tickDelayMs)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking)

        stepContainer = findViewById(R.id.stepContainer)
        tvStatusChip = findViewById(R.id.tvStatusChip)
        tvStageLabel = findViewById(R.id.tvStageLabel)
        tvPercent = findViewById(R.id.tvPercent)
        tvEtaTime = findViewById(R.id.tvEtaTime)
        tvEtaRemaining = findViewById(R.id.tvEtaRemaining)
        progress = findViewById(R.id.progressDelivery)
        viewLiveDot = findViewById(R.id.viewLiveDot)

        // Cek apakah halaman dibuka dengan membawa data unit yang valid
        val unitNameExtra = intent.getStringExtra("EXTRA_UNIT_NAME")
        hasActiveOrder = !unitNameExtra.isNullOrEmpty()

        val orderId = intent.getStringExtra(EXTRA_ORDER_ID) ?: "TR-2024-0891"
        findViewById<TextView>(R.id.tvOrderId).text = if (hasActiveOrder) "STATUS REAL-TIME • ORDER #$orderId" else "STATUS REAL-TIME • BELUM ADA PESANAN"

        activeUnitName = unitNameExtra ?: "Belum Ada Unit Dipilih"
        findViewById<TextView>(R.id.tvUnitName).text = activeUnitName
        findViewById<TextView>(R.id.tvUnitLocation).text = if (hasActiveOrder) "Karawang • Lokasi Proyek" else "Silakan pilih unit & operator"

        // Menangkap Tanggal Sewa
        val rentDateExtra = intent.getStringExtra("EXTRA_TGL_MULAI") ?: intent.getStringExtra("EXTRA_DATE_RANGE")
        val tvRentDate = findViewById<TextView>(R.id.tvRentDate)
        if (hasActiveOrder) {
            tvRentDate.text = rentDateExtra?.split("-")?.firstOrNull()?.trim() ?: "28 Okt"
        } else {
            tvRentDate.text = "-"
        }

        val ivUnit = findViewById<ImageView>(R.id.ivUnit)
        if (hasActiveOrder) {
            val upperName = activeUnitName.uppercase()
            when {
                upperName.contains("EXCAVATOR") -> ivUnit.setImageResource(R.drawable.ic_excavator)
                upperName.contains("BULLDOZER") -> ivUnit.setImageResource(R.drawable.ic_bulldozer)
                upperName.contains("GRADER") -> ivUnit.setImageResource(R.drawable.ic_motorgrader)
                upperName.contains("LOADER") -> ivUnit.setImageResource(R.drawable.ic_wheelloader)
                else -> ivUnit.setImageResource(R.drawable.ic_excavator)
            }
        } else {
            ivUnit.setImageResource(R.drawable.ic_logo)
        }

        isWithOperator = intent.getBooleanExtra("EXTRA_WITH_OPERATOR", true)

        if (!hasActiveOrder) {
            // Kondisi Kosong / Belum Pilih (Dibuka dari menu navigasi bawah)
            updateStatusChip(0)
            currentStage = 0
            remainingMinutes = 0
            driverName = "Belum Ada Operator"
            driverRole = "Pesan unit & operator terlebih dahulu"
            driverVehicle = "---"
            driverPhoneDisplay = "-"
            driverPhotoRes = R.drawable.trk_ic_person

            findViewById<TextView>(R.id.tvDriverShort)?.text = "Pilih Operator"
            findViewById<TextView>(R.id.tvDriverVehicle)?.text = "---"
            tvEtaTime.text = "-"
            tvEtaRemaining.text = "Belum ada pengiriman terpilih"
        } else {
            // Kondisi Aktif (Real-time setelah dipilih)
            currentStage = 1
            remainingMinutes = 180

            if (!isWithOperator) {
                driverName = "Tanpa Operator"
                driverRole = "Sewa Mandiri • Pengiriman Logistik"
                driverVehicle = "B 9021 TRK"
                driverPhoneDisplay = "0811-1234-5678"
                driverPhoneDial = "+6281112345678"
                driverPhotoRes = R.drawable.ic_logo
                findViewById<TextView>(R.id.tvDriverShort)?.text = "Tanpa Operator"
            } else {
                intent.getStringExtra("EXTRA_DRIVER_NAME")?.let { name ->
                    driverName = name
                    val firstName = name.split(" ").firstOrNull() ?: name
                    findViewById<TextView>(R.id.tvDriverShort)?.text = "$firstName S."

                    if (name.contains("Hendra", ignoreCase = true)) {
                        driverRole = "Heavy Equipment Operator • Pool B Bekasi"
                        driverVehicle = "B 4122 TRK"
                        driverPhoneDisplay = "0813-9876-5432"
                        driverPhoneDial = "+6281398765432"
                    } else {
                        driverRole = "Senior Heavy Operator • Pool A Karawang"
                        driverVehicle = "B 9021 TRK"
                        driverPhoneDisplay = "0812-3456-7890"
                        driverPhoneDial = "+6281234567890"
                    }
                }
                driverPhotoRes = intent.getIntExtra("EXTRA_DRIVER_PHOTO", R.drawable.foto_budi)
            }

            val unitCategory = activeUnitName.split(" ").firstOrNull() ?: "Unit"
            findViewById<TextView>(R.id.tvDriverVehicle)?.text = "$unitCategory • $driverVehicle"
            tvEtaTime.text = "14:30"
            updateEta()
        }

        findViewById<View>(R.id.btnBack)?.setOnClickListener { finish() }

        findViewById<View>(R.id.btnContactDispatch).setOnClickListener { showDriverDialog() }
        findViewById<View>(R.id.cardDriver).setOnClickListener { showDriverDialog() }
        findViewById<View>(R.id.btnCallQuick).setOnClickListener { showDriverDialog() }

        setupBottomNav()
        renderStage()
        if (hasActiveOrder) startLiveCap()
    }

    override fun onResume() {
        super.onResume()
        if (hasActiveOrder && currentStage < 4) {
            handler.removeCallbacks(ticker)
            handler.postDelayed(ticker, tickDelayMs)
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(ticker)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        liveDotAnimator?.cancel()
    }

    private fun setupBottomNav() {
        findViewById<View>(R.id.navCatalog).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            })
            finish()
        }
        findViewById<View>(R.id.navOperator).setOnClickListener {
            startActivity(Intent(this, OperatorActivity::class.java))
            finish()
        }
        findViewById<View>(R.id.navSettings).setOnClickListener {
            Toast.makeText(this, "Pengaturan segera hadir", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateStatusChip(stage: Int) {
        when (stage) {
            1 -> {
                tvStatusChip.text = "ORDER CONFIRMED"
                tvStatusChip.setTextColor(Color.parseColor("#1E3A8A"))
                tvStatusChip.background = GradientDrawable().apply {
                    cornerRadius = 20f * resources.displayMetrics.density
                    setColor(Color.parseColor("#DBEAFE"))
                }
            }
            2 -> {
                tvStatusChip.text = "UNIT LOADED"
                tvStatusChip.setTextColor(Color.parseColor("#7C2D12"))
                tvStatusChip.background = GradientDrawable().apply {
                    cornerRadius = 20f * resources.displayMetrics.density
                    setColor(Color.parseColor("#FFEDD5"))
                }
            }
            3 -> {
                tvStatusChip.text = "IN TRANSIT"
                tvStatusChip.setTextColor(Color.parseColor("#7A4A00"))
                tvStatusChip.background = GradientDrawable().apply {
                    cornerRadius = 20f * resources.displayMetrics.density
                    setColor(Color.parseColor("#FDE68A"))
                }
            }
            4 -> {
                tvStatusChip.text = "ARRIVED"
                tvStatusChip.setTextColor(Color.parseColor("#2E7D32"))
                tvStatusChip.background = GradientDrawable().apply {
                    cornerRadius = 20f * resources.displayMetrics.density
                    setColor(Color.parseColor("#E8F5E9"))
                }
            }
            else -> {
                tvStatusChip.text = "-"
                tvStatusChip.setTextColor(Color.parseColor("#191C1E"))
                tvStatusChip.background = GradientDrawable().apply {
                    cornerRadius = 20f * resources.displayMetrics.density
                    setColor(Color.parseColor("#E3E5E8"))
                }
            }
        }
    }

    private fun renderStage() {
        val progressPercent = when (currentStage) {
            1 -> 25
            2 -> 50
            3 -> 75
            4 -> 100
            else -> 0
        }

        progress.setProgressCompat(progressPercent, true)
        tvPercent.text = "$progressPercent%"
        tvStageLabel.text = "TAHAP $currentStage DARI ${steps.size}"

        updateStatusChip(currentStage)
        buildStepper()
    }

    private fun buildStepper() {
        stepContainer.removeAllViews()
        if (!hasActiveOrder) {
            val emptyNotice = TextView(this).apply {
                text = "Belum ada pengiriman terpilih. Silakan pesan unit & operator terlebih dahulu."
                setTextColor(Color.parseColor("#6B6F76"))
                textSize = 14f
                setPadding(16, 16, 16, 16)
            }
            stepContainer.addView(emptyNotice)
            return
        }

        val inflater = LayoutInflater.from(this)
        steps.forEachIndexed { index, step ->
            val number = index + 1
            val state = when {
                number < currentStage -> StepState.DONE
                number == currentStage && currentStage < steps.size -> StepState.ACTIVE
                number == currentStage -> StepState.DONE
                else -> StepState.PENDING
            }
            val row = inflater.inflate(R.layout.item_tracking_step, stepContainer, false)
            bindStep(row, number, step, state, isLast = index == steps.lastIndex)
            stepContainer.addView(row)
        }
    }

    private fun bindStep(row: View, number: Int, step: Step, state: StepState, isLast: Boolean) {
        val primary = ContextCompat.getColor(this, R.color.trk_primary)
        val orange = ContextCompat.getColor(this, R.color.trk_orange)
        val line = ContextCompat.getColor(this, R.color.trk_line)
        val dark = ContextCompat.getColor(this, R.color.trk_text_dark)
        val muted = ContextCompat.getColor(this, R.color.trk_text_muted)

        val circle = row.findViewById<TextView>(R.id.tvStepNumber)
        val connector = row.findViewById<View>(R.id.viewConnector)
        val title = row.findViewById<TextView>(R.id.tvStepTitle)
        val badge = row.findViewById<TextView>(R.id.tvStepBadge)
        val subtitle = row.findViewById<TextView>(R.id.tvStepSubtitle)
        val time = row.findViewById<TextView>(R.id.tvStepTime)

        val circleColor = when (state) {
            StepState.DONE -> primary
            StepState.ACTIVE -> orange
            StepState.PENDING -> line
        }
        circle.backgroundTintList = ColorStateList.valueOf(circleColor)
        circle.text = if (state == StepState.DONE) "✓" else number.toString()
        circle.setTextColor(if (state == StepState.PENDING) muted else Color.WHITE)

        connector.visibility = if (isLast) View.INVISIBLE else View.VISIBLE
        connector.setBackgroundColor(if (state == StepState.DONE) primary else line)

        title.text = step.title
        title.setTextColor(if (state == StepState.PENDING) muted else dark)
        subtitle.text = step.subtitle

        badge.visibility = if (state == StepState.ACTIVE) View.VISIBLE else View.GONE

        time.text = if (state == StepState.PENDING) "--:--" else step.time
        time.setTextColor(if (state == StepState.ACTIVE) orange else muted)

        if (state == StepState.ACTIVE) {
            row.setBackgroundResource(R.drawable.trk_bg_step_active)
        }
    }

    private fun updateEta() {
        if (!hasActiveOrder) {
            tvEtaRemaining.text = "Belum ada pengiriman terpilih"
            return
        }
        val h = remainingMinutes / 60
        val m = remainingMinutes % 60
        tvEtaRemaining.text = when {
            h > 0 && m > 0 -> "$h jam $m mnt lagi"
            h > 0 -> "$h jam lagi"
            else -> "$m mnt lagi"
        }
    }

    private fun onArrived() {
        currentStage = 4
        steps[3].time = tvEtaTime.text.toString()
        renderStage()

        tvEtaRemaining.text = "Unit sudah tiba"
        updateStatusChip(4)

        liveDotAnimator?.cancel()
        viewLiveDot.alpha = 1f
        Toast.makeText(this, "Unit telah tiba di lokasi proyek", Toast.LENGTH_LONG).show()
    }

    private fun startLiveCap() {
        liveDotAnimator = ObjectAnimator.ofFloat(viewLiveDot, View.ALPHA, 1f, 0.2f).apply {
            duration = 800
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
            interpolator = LinearInterpolator()
            start()
        }
    }

    private fun showDriverDialog() {
        if (!hasActiveOrder) {
            Toast.makeText(this, "Silakan pesan unit & operator terlebih dahulu.", Toast.LENGTH_SHORT).show()
            return
        }

        val view = layoutInflater.inflate(R.layout.dialog_driver_contact, null)
        val dialog = MaterialAlertDialogBuilder(this).setView(view).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val unitCategory = activeUnitName.split(" ").firstOrNull() ?: "Unit"

        if (!isWithOperator) {
            view.findViewById<TextView>(R.id.tvDialogDriverName).text = "Tanpa Operator"
            view.findViewById<TextView>(R.id.tvDialogDriverRole).text = "Sewa Mandiri • Pengiriman Logistik"
            view.findViewById<TextView>(R.id.tvDialogVehicle).text = "$unitCategory • $driverVehicle"
        } else {
            view.findViewById<TextView>(R.id.tvDialogDriverName).text = driverName
            view.findViewById<TextView>(R.id.tvDialogDriverRole).text = driverRole
            view.findViewById<TextView>(R.id.tvDialogVehicle).text = "$unitCategory • $driverVehicle"
        }

        view.findViewById<TextView>(R.id.tvDialogPhone).text = driverPhoneDisplay
        view.findViewById<TextView>(R.id.tvDialogEta).text = "${tvEtaTime.text} WIB"

        val ivDialogAvatar = view.findViewById<ImageView>(R.id.ivDialogAvatar)
        ivDialogAvatar?.setImageResource(driverPhotoRes)

        view.findViewById<View>(R.id.btnDialogClose).setOnClickListener { dialog.dismiss() }
        view.findViewById<View>(R.id.btnDialogDismiss).setOnClickListener { dialog.dismiss() }

        view.findViewById<View>(R.id.btnDialogCall).setOnClickListener {
            if (driverPhoneDial.isNotEmpty()) {
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$driverPhoneDial")))
            } else {
                Toast.makeText(this, "Nomor telepon tidak tersedia", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        view.findViewById<View>(R.id.btnDialogMessage).setOnClickListener {
            if (driverPhoneDial.isNotEmpty()) {
                val smsMessage = if (isWithOperator) {
                    "Halo Pak $driverName, saya menanyakan posisi unit sewa saya."
                } else {
                    "Halo, saya menanyakan status pengiriman unit sewa."
                }
                val sms = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$driverPhoneDial")).apply {
                    putExtra("sms_body", smsMessage)
                }
                startActivity(sms)
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    companion object {
        const val EXTRA_ORDER_ID = "EXTRA_ORDER_ID"
    }
}