package com.example.papeleriaapp.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatUtils {

    fun money(value : Double) : String =
        "$" + String.format(Locale.US, "%.2f", value)

    fun date(timestamp : Long) : String {
        if (timestamp <= 0L) return "-"
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return format.format(Date(timestamp))
    }
}
