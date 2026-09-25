package com.example.data

import com.example.model.*

object MiningSampleData {

    val marketQuotes = listOf(
        MarketQuote("CU", "Cobre Spot LME", "US$ 4.38", "/lb", "+1.42%", true),
        MarketQuote("USD", "Dólar Obs.", "$942.50", "CLP", "-0.35%", false),
        MarketQuote("LI2CO3", "Litio Carbonato", "$13,850", "/t", "+0.8%", true),
        MarketQuote("MO", "Molibdeno", "US$ 20.40", "/lb", "+0.5%", true),
        MarketQuote("AU", "Oro Spot", "US$ 2,680", "/oz", "+0.3%", true)
    )

    val seismicEvents = listOf(
        SeismicRecord("s-1", 3.8, "San Pedro de Atacama", 118, "14 min", "Distrito Litio & Salar", "Sin novedad"),
        SeismicRecord("s-2", 4.1, "Sierra Gorda (Calama)", 84, "58 min", "Cercano a Spence & Centinela", "Sin novedad"),
        SeismicRecord("s-3", 3.2, "Machalí (El Teniente)", 12, "2h 11m", "Cordillera Central", "Sin novedad"),
        SeismicRecord("s-4", 3.5, "Diego de Almagro (El Salvador)", 92, "4h 05m", "Distrito Atacama", "Sin novedad"),
        SeismicRecord("s-5", 4.0, "Iquique (Cerro Colorado)", 65, "6h 20m", "Tarapacá Cordillera", "Sin novedad")
    )

    val weatherSites = listOf(
        WeatherSite(
            faena = "Chuquicamata",
            region = "Antofagasta",
            altitudeM = 3100,
            tempC = 18.0,
            windKmH = 14,
            maxWindKmH = 22,
            windStatus = "Despejado",
            weatherDesc = "Viento 14 km/h · Despejado",
            uvIndex = 11
        ),
        WeatherSite(
            faena = "Collahuasi",
            region = "Tarapacá",
            altitudeM = 4400,
            tempC = 6.0,
            windKmH = 45,
            maxWindKmH = 55,
            windStatus = "ALERTA VIENTO",
            weatherDesc = "ALERTA VIENTO 45 km/h",
            uvIndex = 12,
            isAlert = true,
            alertMessage = "Ráfagas sobre límite preventivo. Amarre de equipos livianos."
        ),
        WeatherSite(
            faena = "El Teniente",
            region = "O'Higgins",
            altitudeM = 2200,
            tempC = 22.0,
            windKmH = 8,
            maxWindKmH = 15,
            windStatus = "Óptimo",
            weatherDesc = "Viento 8 km/h · Óptimo",
            uvIndex = 9
        ),
        WeatherSite(
            faena = "Minera Centinela",
            region = "Antofagasta",
            altitudeM = 2300,
            tempC = 16.4,
            windKmH = 24,
            maxWindKmH = 38,
            windStatus = "Faena Verde",
            weatherDesc = "Viento 24 km/h · Rachas 38 km/h",
            uvIndex = 11,
            isAlert = true,
            alertMessage = "Alerta UV Extrema Nivel 11+. Uso obligatorio de cubrenuca legionario."
        )
    )

    val audioChapters = listOf(
        AudioNewsChapter(
            id = "c-1",
            timeLabel = "00:00",
            timeSeconds = 0,
            title = "Cochilco eleva proyección de precio del cobre por demanda en electromovilidad mundial.",
            subtitle = "En reproducción · Transición energética y déficit global"
        ),
        AudioNewsChapter(
            id = "c-2",
            timeLabel = "01:15",
            timeSeconds = 75,
            title = "Minera Centinela avanza en obras de Segunda Concentradora utilizando un 88% de agua de mar.",
            subtitle = "Infraestructura y Sustentabilidad"
        ),
        AudioNewsChapter(
            id = "c-3",
            timeLabel = "02:10",
            timeSeconds = 130,
            title = "Sernageomin reporta baja histórica en tasa de accidentabilidad del sector minero nacional.",
            subtitle = "Seguridad Faena Cero Daño"
        ),
        AudioNewsChapter(
            id = "c-4",
            timeLabel = "02:45",
            timeSeconds = 165,
            title = "Reporte del clima y vientos en faenas de altura en la macrozona norte (Antofagasta / Tarapacá).",
            subtitle = "Meteorología Operacional"
        )
    )

    val miningPanels = listOf(
        MiningPanel(
            id = "p-1",
            title = "Estrategia y Desarrollo: La Foto País",
            subtitle = "Macroeconomía Minera",
            description = "Balance macroeconómico: 5.25M toneladas métricas de cobre fino anuales, recaudación de royalty a la minería y proyecciones de gasto público hacia el 2030.",
            category = "markets",
            sourceBadge = "OFICIAL COCHILCO / DIPRES",
            metrics = listOf(
                MetricItem("Producción 2024:", "5.25 Mt Cu"),
                MetricItem("Canon & Royalty:", "+18.4% vs 2023", true)
            ),
            progressPercent = 0.78f,
            progressColorHex = 0xFFF59E0B,
            subTags = "Macroeconomía · Royalty Minero · Recaudación Fiscal"
        ),
        MiningPanel(
            id = "p-2",
            title = "Precios y Mercados de Metales",
            subtitle = "Cotizaciones LME / COMEX",
            description = "Cotización intradía de metales críticos: Cobre, Molibdeno (US$20.4/lb), Oro (US$2.680/oz), Plata y concentrados con tasas de tratamiento y refinación (TC/RC).",
            category = "markets",
            sourceBadge = "EN VIVO LME / COMEX",
            isLive = true,
            metrics = listOf(
                MetricItem("LME 3M Cu:", "US$ 9,640 / t", true),
                MetricItem("Molibdeno:", "US$ 20.40 / lb", true)
            ),
            subTags = "Metales Críticos · Cotización Intradía · Cobre LME"
        ),
        MiningPanel(
            id = "p-3",
            title = "Mapa y Ficha de Faenas de Chile",
            subtitle = "Directorio Georreferenciado",
            description = "Georreferenciación completa de rajos abiertos y minas subterráneas. Capacidad de molienda, leyes de corte de mineral, dotación contratista y matrices energéticas.",
            category = "operations",
            sourceBadge = "INTERACTIVO · 34 FAENAS",
            metrics = listOf(
                MetricItem("Faenas Monitoreadas:", "34 Operaciones"),
                MetricItem("Método dominante:", "62% Rajo abierto")
            ),
            subTags = "Escondida · Collahuasi · El Teniente · Chuqui · Centinela"
        ),
        MiningPanel(
            id = "p-4",
            title = "Litio: Salar de Atacama & Red Nacional",
            subtitle = "Estrategia Nacional del Litio",
            description = "Cuotas de extracción de salmuera, balances de Carbonato de Litio Grado Batería, recaudación fiscal de Corfo por contratos SQM y Albemarle, y modelo Codelco-SQM.",
            category = "lithium",
            sourceBadge = "ESTRATÉGICO · CORFO",
            metrics = listOf(
                MetricItem("Spot LCE:", "US$ 13,850 / t"),
                MetricItem("Aporte Acumulado:", "US$ 3.100M (2023)", true)
            ),
            subTags = "Acuerdo Corfo 2025–2060 activo · Salar Atacama"
        ),
        MiningPanel(
            id = "p-5",
            title = "Empleo, Turnos y Sueldos Mineros",
            subtitle = "Consejo de Competencias Mineras",
            description = "Estructuras laborales 7x7, 4x3 y 5x2. Remuneraciones del Consejo de Competencias Mineras (CCM), brechas de género, inclusión de comunidades locales y demanda técnica.",
            category = "employment",
            sourceBadge = "COMUNIDAD & LABORAL",
            metrics = listOf(
                MetricItem("Dotación País:", "285.000 trab."),
                MetricItem("Participación Mujeres:", "18.0% (En alza)", true)
            ),
            subTags = "CCM-Eleva · Estudio de Fuerza Laboral"
        ),
        MiningPanel(
            id = "p-6",
            title = "Seguridad y Accidentabilidad",
            subtitle = "Sernageomin Oficial",
            description = "Tasa de frecuencia de accidentes, registros de fatalidades por faena, ranking de cumplimiento preventivo y fiscalizaciones geomecánicas en tranques de relaves.",
            category = "operations",
            sourceBadge = "SERNAGEOMIN OFICIAL",
            metrics = listOf(
                MetricItem("Tasa de Frecuencia:", "1.41 (Mínimo histórico)", true),
                MetricItem("Fiscalizaciones 2024:", "10.420 inspecciones")
            ),
            subTags = "Campaña Cero Fatalidad · Rescate Minero"
        ),
        MiningPanel(
            id = "p-7",
            title = "Agua Desalada e Innovación Verde",
            subtitle = "Transición Hídrica Minera",
            description = "Proyección de uso de agua de mar en procesos de concentración y lixiviación. Reducción del uso de acuíferos continentales al 32% y contratos PPA renovables.",
            category = "environment",
            sourceBadge = "TRANSICIÓN HÍDRICA",
            metrics = listOf(
                MetricItem("Agua de Mar en Faenas:", "40% Total País"),
                MetricItem("Meta al 2033:", "68% Agua de mar", true)
            ),
            progressPercent = 0.58f,
            progressColorHex = 0xFF4CD7F6,
            subTags = "Desaladoras · Cero Extracción Continental"
        ),
        MiningPanel(
            id = "p-8",
            title = "Glosario y Aprendizaje Minero",
            subtitle = "Academia & Divulgación",
            description = "Guía didáctica ilustrada para estudiantes y profesionales: de la perforación y voladura al chancado, lixiviación, flotación, electroobtención y fundición de ánodos.",
            category = "all",
            sourceBadge = "ACADEMIA & COMUNIDAD",
            metrics = listOf(
                MetricItem("Términos Explicados:", "240 conceptos"),
                MetricItem("Infografías 3D:", "18 diagramas")
            ),
            subTags = "Abierto a liceos técnicos y universidades"
        )
    )

    val mineraCentinelaDetail = MineDetail(
        id = "CEN-7049",
        name = "MINERA CENTINELA",
        company = "Antofagasta Minerals S.A.",
        district = "DISTRITO ANTOFAGASTA NORTE",
        basin = "SIERRA GORDA BASIN",
        coords = "Sierra Gorda, II Región (22°55'S 69°13'W)",
        altitudeMsnm = 2300,
        waterSource = "Agua Cruda de Mar (Punta Churute, Mejillones)",
        cuProjectionYear = "245.000 tMF",
        cuFulfillmentPercent = "▲ Cumplimiento 102.3%",
        cashCostC1 = "US$ 1.82 / lb",
        seaWaterDirectPercent = "88.4%",
        seaWaterDetail = "Sin desalar · 145 km bombeo",
        workforceShift = "6.550 P/C",
        workforceBreakdown = "2.450 Planta / 4.100 EETT",
        accidentRate = "0.82 SERNAG.",
        accidentStandard = "Categoría A (Sobre Estándar)",
        seismicCount7d = 2,
        pitEsperanzaDesc = "Rajo Esperanza (Sulfuros): Ley Media 0.54% Cu · 0.18 g/t Au · Extracción continua: 95 kt/día",
        pitEncuentroDesc = "Rajo Encuentro (Óxidos): Lixiviación Rom / Dinámica · Cátodos SX-EW Grado A",
        tailingsDesc = "Tranque de Relaves Espesados: 67% Sólidos (Pionero Global) · Recuperación agua: 82%",
        stages = listOf(
            ProcessStage("01", "Extracción", "Palas P&H 4100XPC", "Camiones Komatsu 930E", "Rendimiento: 98%"),
            ProcessStage("02", "Conminución", "Chancador Giratorio 60x89", "1 SAG 40' + 2 Bolas 27'", "P80: 165 micrones"),
            ProcessStage("03", "Recuperación", "Celdas Rougher 300 m³", "Agua de Mar Cruda", "pH Operacional 10.2"),
            ProcessStage("04", "Separación", "Espesamiento relaves", "Planta Molibdeno", "Rec. Cu: 86.2%"),
            ProcessStage("05", "Despacho", "Concentraducto 145 km", "Puerto Michilla · Cátodos LME Grade A", "27.8% Ley Cu conc.")
        ),
        throughputList = listOf(
            DayThroughput("LUN", 103.8),
            DayThroughput("MAR", 98.5),
            DayThroughput("MIÉ", 105.1),
            DayThroughput("JUE", 106.0),
            DayThroughput("VIE", 101.2),
            DayThroughput("SÁB", 104.9),
            DayThroughput("DOM", 107.4, isPeak = true)
        ),
        averageThroughput = "104.2 ktpd",
        maxCapacity = "105 ktpd",
        expansionTitle = "Hito de Expansión: Nueva Centinela (Segunda Concentradora)",
        expansionDescription = "Inversión aprobada US$ 4.400M para adicionar 170.000 t Cu-Eq anuales hacia 2027.",
        expansionCapex = "CAPEX US$ 4.4B",
        windReading = "24 km/h (Rachas 38 km/h)",
        tempReading = "16.4 °C (Mín: 4°C · Máx: 23°C)",
        humidityReading = "14% (Extrema aridez)",
        isothermReading = "4.600 m (Sin riesgo aluvional)",
        uvLevel = "Nivel 11+ (Alerta UV Extrema)",
        nearbySeismic = listOf(
            SeismicRecord("ns-1", 3.8, "32 km al SE de Sierra Gorda", 114, "Hace 24h", "Sierra Gorda Basin"),
            SeismicRecord("ns-2", 3.2, "47 km al SO de Calama", 98, "Hace 4d", "Calama Basin")
        )
    )
}
