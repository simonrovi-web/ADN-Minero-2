package com.example.model

enum class NavDestination(val label: String, val iconName: String) {
    INICIO("INICIO", "precision_manufacturing"),
    VOZ("VOZ", "record_voice_over"),
    ALERTAS("ALERTAS", "warning"),
    PANELES("PANELES", "monitoring")
}

data class MarketQuote(
    val symbol: String,
    val name: String,
    val price: String,
    val unit: String,
    val delta: String,
    val isPositive: Boolean
)

data class SeismicRecord(
    val id: String,
    val magnitude: Double,
    val location: String,
    val depthKm: Int,
    val timeAgo: String,
    val district: String,
    val status: String = "Sin novedad",
    val source: String = "CSN"
)

data class WeatherSite(
    val faena: String,
    val region: String,
    val altitudeM: Int,
    val tempC: Double,
    val windKmH: Int,
    val maxWindKmH: Int,
    val windStatus: String,
    val weatherDesc: String,
    val uvIndex: Int,
    val isAlert: Boolean = false,
    val alertMessage: String? = null
)

data class MetricItem(
    val label: String,
    val value: String,
    val isPositive: Boolean? = null
)

data class MiningPanel(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val category: String, // "markets", "operations", "lithium", "employment", "environment", "all"
    val sourceBadge: String,
    val isLive: Boolean = false,
    val metrics: List<MetricItem> = emptyList(),
    val progressPercent: Float? = null,
    val progressColorHex: Long = 0xFFF59E0B,
    val subTags: String = ""
)

data class ProcessStage(
    val stepNumber: String,
    val title: String,
    val machinery: String,
    val detail: String,
    val metricHighlight: String
)

data class DayThroughput(
    val day: String,
    val throughputKt: Double,
    val isPeak: Boolean = false
)

data class MineDetail(
    val id: String,
    val name: String,
    val company: String,
    val district: String,
    val basin: String,
    val coords: String,
    val altitudeMsnm: Int,
    val waterSource: String,
    val cuProjectionYear: String,
    val cuFulfillmentPercent: String,
    val cashCostC1: String,
    val seaWaterDirectPercent: String,
    val seaWaterDetail: String,
    val workforceShift: String,
    val workforceBreakdown: String,
    val accidentRate: String,
    val accidentStandard: String,
    val seismicCount7d: Int,
    val pitEsperanzaDesc: String,
    val pitEncuentroDesc: String,
    val tailingsDesc: String,
    val stages: List<ProcessStage>,
    val throughputList: List<DayThroughput>,
    val averageThroughput: String,
    val maxCapacity: String,
    val expansionTitle: String,
    val expansionDescription: String,
    val expansionCapex: String,
    val windReading: String,
    val tempReading: String,
    val humidityReading: String,
    val isothermReading: String,
    val uvLevel: String,
    val nearbySeismic: List<SeismicRecord>
)

data class AudioNewsChapter(
    val id: String,
    val timeLabel: String,
    val timeSeconds: Int,
    val title: String,
    val subtitle: String,
    val durationSeconds: Int = 195
)

data class ShiftInfo(
    val faenaName: String = "Faena Sierra Gorda / Centinela",
    val shiftName: String = "Turno 7x7 Día · Guardia A",
    val groupTag: String = "GRUPO 1",
    val changeHour: String = "19:00 HRS",
    val roadsStatus: String = "HABILITADOS · NORMAL",
    val dustStatus: String = "Polvo en suspensión controlado. Regadío activo en Rampa Sur.",
    val uvLevel: String = "Nivel 11+",
    val nextSunscreenHour: String = "14:30 hrs",
    val windRajoSpeed: Int = 26,
    val windRajoStatus: String = "FAENA VERDE",
    val windRajoAlertLimit: Int = 45,
    val seismicDistritalMag: Double = 3.8,
    val seismicDistritalDist: String = "32 km SSO",
    val seismicTimeAgo: String = "Hace 42m",
    val clinicaAnnex: String = "3302",
    val ccoAnnex: String = "8800",
    val rescueAnnex: String = "1407"
)
