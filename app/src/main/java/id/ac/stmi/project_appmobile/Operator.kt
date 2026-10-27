package id.ac.stmi.project_appmobile

import java.io.Serializable

data class Operator(
    val id: Int,
    val name: String,
    val sio: String,
    val hours: String,
    val rating: Double,
    val reviews: Int,
    val kelas: String,
    val price: Int,
    val photoRes: Int? = null,
    val role: String = "Senior Heavy Operator  •  Pool A Karawang",
    val sioCode: String = "#SIO-1234-JKT",
    val totalHours: String = "4.280 Jam",
    val experience: String = "8+ Tahun",
    val sioValid: String = "Aktif s/d 2027"
) : Serializable