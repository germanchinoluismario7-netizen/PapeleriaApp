package com.example.papeleriaapp.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

/**
 * Componentes dibujados con Canvas (Jetpack Compose) para mejorar
 * la presentacion y la lectura de la informacion de la aplicacion.
 */

/** Estados visuales para las ilustraciones de listas vacias. */
enum class EmptyGlyph { BOX, RECEIPT, SEARCH, CART }

/**
 * Barra de nivel de stock dibujada con Canvas.
 * Verde: stock sano (>= 20), naranja: stock bajo (< 20), rojo: agotado.
 * Escala fija 0..100.
 */
@Composable
fun StockLevelBar(
    stock: Int,
    modifier: Modifier = Modifier,
    scaleMax: Int = 100
) {
    val fraction = if (scaleMax <= 0) 0f
    else (stock.toFloat() / scaleMax).coerceIn(0f, 1f)

    val levelColor = when {
        stock <= 0 -> MaterialTheme.colorScheme.error
        stock < 20 -> Color(0xFFFB8C00)
        else -> Color(0xFF43A047)
    }

    val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
    ) {
        val radius = CornerRadius(size.height / 2f, size.height / 2f)

        // Riel de fondo
        drawRoundRect(
            color = trackColor,
            cornerRadius = radius
        )

        // Nivel actual
        if (fraction > 0f) {
            val fillWidth = maxOf(size.height, size.width * fraction)
            drawRoundRect(
                color = levelColor,
                size = Size(width = fillWidth, height = size.height),
                cornerRadius = radius
            )
        }
    }
}

/**
 * Tarjeta de estadistica con medidor circular dibujado con Canvas.
 * Uso: panel del vendedor (proporcion de pedidos pendientes / entregados).
 */
@Composable
fun ArcStatCard(
    label: String,
    value: String,
    progress: Float,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Row(
            modifier = Modifier.padding(all = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(52.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 6.dp.toPx()
                    val inset = stroke / 2f
                    val arcSize = Size(size.width - stroke, size.height - stroke)
                    val topLeft = Offset(inset, inset)
                    val arcStroke = Stroke(width = stroke, cap = StrokeCap.Round)

                    // Riel del medidor (270 grados)
                    drawArc(
                        color = accent.copy(alpha = 0.20f),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = arcStroke
                    )

                    val safeProgress = progress.coerceIn(0f, 1f)
                    if (safeProgress > 0f) {
                        drawArc(
                            color = accent,
                            startAngle = 135f,
                            sweepAngle = 270f * safeProgress,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = arcStroke
                        )
                    }
                }

                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    color = accent
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Ilustracion de lista vacia dibujada con Canvas: una hoja de papel
 * con el glifo correspondiente al contexto (cajon, recibo, busqueda, carrito).
 */
@Composable
fun EmptyStateIllustration(
    glyph: EmptyGlyph,
    modifier: Modifier = Modifier
) {
    val paperColor = MaterialTheme.colorScheme.surface
    val lineColor = MaterialTheme.colorScheme.outline
    val glyphColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)

    Canvas(modifier = modifier.size(132.dp)) {
        val paperW = size.width * 0.62f
        val paperH = size.height * 0.76f
        val left = (size.width - paperW) / 2f
        val top = (size.height - paperH) / 2f
        val radius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
        val stroke = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)

        // Sombra suave de la hoja
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.06f),
            topLeft = Offset(left + 3.dp.toPx(), top + 5.dp.toPx()),
            size = Size(paperW, paperH),
            cornerRadius = radius
        )

        // Hoja de papel
        drawRoundRect(
            color = paperColor,
            topLeft = Offset(left, top),
            size = Size(paperW, paperH),
            cornerRadius = radius
        )
        drawRoundRect(
            color = lineColor.copy(alpha = 0.55f),
            topLeft = Offset(left, top),
            size = Size(paperW, paperH),
            cornerRadius = radius,
            style = Stroke(width = 2.dp.toPx())
        )

        val cx = left + paperW / 2f
        val cy = top + paperH / 2f

        when (glyph) {
            EmptyGlyph.RECEIPT -> {
                val w = paperW * 0.46f
                val h = paperH * 0.58f
                val x0 = cx - w / 2f
                val y0 = cy - h / 2f
                val tooth = 7.dp.toPx()

                val path = Path().apply {
                    moveTo(x0, y0)
                    lineTo(x0 + w, y0)
                    lineTo(x0 + w, y0 + h - tooth)
                    val teeth = 6
                    val step = w / teeth
                    var x = x0 + w
                    var up = false
                    repeat(teeth) {
                        x -= step
                        lineTo(x, y0 + h - if (up) 0f else tooth)
                        up = !up
                    }
                    close()
                }
                drawPath(path, color = glyphColor, style = stroke)

                // Lineas de contenido
                repeat(3) { i ->
                    val y = y0 + h * (0.26f + 0.20f * i)
                    drawLine(
                        color = glyphColor,
                        start = Offset(x0 + w * 0.18f, y),
                        end = Offset(x0 + w * 0.82f, y),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            EmptyGlyph.BOX -> {
                val bw = paperW * 0.52f
                val bh = paperH * 0.34f
                val x0 = cx - bw / 2f
                val y0 = cy - bh / 2f + 4.dp.toPx()

                // Cuerpo de la caja
                drawRect(
                    color = glyphColor,
                    topLeft = Offset(x0, y0),
                    size = Size(bw, bh),
                    style = stroke
                )

                // Costura de la tapa
                drawLine(
                    color = glyphColor,
                    start = Offset(x0, y0 + bh * 0.30f),
                    end = Offset(x0 + bw, y0 + bh * 0.30f),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Cinta central
                drawLine(
                    color = glyphColor,
                    start = Offset(cx, y0),
                    end = Offset(cx, y0 + bh * 0.30f),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Aletas abiertas
                val flap = 16.dp.toPx()
                drawLine(
                    color = glyphColor,
                    start = Offset(x0, y0),
                    end = Offset(x0 - flap, y0 - flap),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = glyphColor,
                    start = Offset(x0 + bw, y0),
                    end = Offset(x0 + bw + flap, y0 - flap),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            EmptyGlyph.SEARCH -> {
                val r = paperW * 0.19f
                val center = Offset(cx - r * 0.35f, cy - r * 0.55f)

                drawCircle(
                    color = glyphColor,
                    radius = r,
                    center = center,
                    style = stroke
                )
                drawLine(
                    color = glyphColor,
                    start = Offset(
                        center.x + r * 0.72f,
                        center.y + r * 0.72f
                    ),
                    end = Offset(
                        center.x + r * 1.7f,
                        center.y + r * 1.7f
                    ),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Lineas de "resultado"
                repeat(2) { i ->
                    val y = cy + paperH * 0.22f + i * 12.dp.toPx()
                    drawLine(
                        color = glyphColor.copy(alpha = 0.6f),
                        start = Offset(cx - paperW * 0.20f, y),
                        end = Offset(cx + paperW * 0.20f, y),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            EmptyGlyph.CART -> {
                val basketW = paperW * 0.52f
                val basketH = paperH * 0.26f
                val x0 = cx - basketW / 2f
                val y0 = cy - basketH / 2f + 4.dp.toPx()

                // Canasta
                val basket = Path().apply {
                    moveTo(x0, y0)
                    lineTo(x0 + basketW, y0)
                    lineTo(x0 + basketW * 0.82f, y0 + basketH)
                    lineTo(x0 + basketW * 0.18f, y0 + basketH)
                    close()
                }
                drawPath(basket, color = glyphColor, style = stroke)

                // Empujador
                drawLine(
                    color = glyphColor,
                    start = Offset(x0 - 14.dp.toPx(), y0 - 12.dp.toPx()),
                    end = Offset(x0, y0),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = glyphColor,
                    start = Offset(x0 - 14.dp.toPx(), y0 - 12.dp.toPx()),
                    end = Offset(x0 - 22.dp.toPx(), y0 - 12.dp.toPx()),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Ruedas
                val wheelR = 6.dp.toPx()
                val wheelY = y0 + basketH + 12.dp.toPx()
                drawCircle(
                    color = glyphColor,
                    radius = wheelR,
                    center = Offset(x0 + basketW * 0.30f, wheelY),
                    style = stroke
                )
                drawCircle(
                    color = glyphColor,
                    radius = wheelR,
                    center = Offset(x0 + basketW * 0.72f, wheelY),
                    style = stroke
                )
            }
        }
    }
}

/**
 * Banner de marca para la pantalla de login: fondo degradado con
 * decoracion dibujada con Canvas (circulos, rayas y lapiz).
 * [content] se superpone centrado (icono + titulo).
 */
@Composable
fun BrandHeader(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
        ) {
            // Fondo degradado
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(primary, tertiary),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height)
                )
            )

            // Circulos decorativos
            drawCircle(
                color = Color.White.copy(alpha = 0.12f),
                radius = 90.dp.toPx(),
                center = Offset(size.width - 26.dp.toPx(), 6.dp.toPx())
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.10f),
                radius = 52.dp.toPx(),
                center = Offset(22.dp.toPx(), size.height - 24.dp.toPx())
            )

            // Rayas diagonales (papel rayado)
            for (i in 0..3) {
                val startX = 26.dp.toPx() + i * 44.dp.toPx()
                drawLine(
                    color = Color.White.copy(alpha = 0.07f),
                    start = Offset(startX, size.height),
                    end = Offset(startX + 66.dp.toPx(), size.height - 66.dp.toPx()),
                    strokeWidth = 9.dp.toPx()
                )
            }

            // Puntos de cuadricula
            repeat(5) { i ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.35f),
                    radius = 3.dp.toPx(),
                    center = Offset(
                        size.width * 0.14f * (i + 1),
                        24.dp.toPx() + (i % 2) * 14.dp.toPx()
                    )
                )
            }

            // Lapiz en la esquina inferior derecha
            val pivot = Offset(size.width - 46.dp.toPx(), size.height - 26.dp.toPx())
            rotate(degrees = 38f, pivot = pivot) {
                val px = size.width - 96.dp.toPx()
                val py = size.height - 34.dp.toPx()
                val bodyW = 74.dp.toPx()
                val bodyH = 15.dp.toPx()

                drawRoundRect(
                    color = Color.White.copy(alpha = 0.92f),
                    topLeft = Offset(px, py),
                    size = Size(bodyW, bodyH),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                )

                // Punta
                val tip = Path().apply {
                    moveTo(px + bodyW, py)
                    lineTo(px + bodyW + 18.dp.toPx(), py + bodyH / 2f)
                    lineTo(px + bodyW, py + bodyH)
                    close()
                }
                drawPath(tip, color = Color(0xFFFFF3E0))
                drawCircle(
                    color = Color(0xFF3E2723),
                    radius = 3.dp.toPx(),
                    center = Offset(px + bodyW + 13.dp.toPx(), py + bodyH / 2f)
                )

                // Goma
                drawRect(
                    color = Color(0xFFEF9A9A),
                    topLeft = Offset(px, py),
                    size = Size(12.dp.toPx(), bodyH)
                )
            }

            // Libreta pequena a la izquierda
            val nbW = 44.dp.toPx()
            val nbH = 54.dp.toPx()
            val nbX = 34.dp.toPx()
            val nbY = size.height - nbH - 40.dp.toPx()
            drawRoundRect(
                color = Color.White.copy(alpha = 0.85f),
                topLeft = Offset(nbX, nbY),
                size = Size(nbW, nbH),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )
            // Espiral
            repeat(4) { i ->
                val y = nbY + 10.dp.toPx() + i * 11.dp.toPx()
                drawLine(
                    color = primary.copy(alpha = 0.7f),
                    start = Offset(nbX - 4.dp.toPx(), y),
                    end = Offset(nbX + 9.dp.toPx(), y),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            // Renglones
            repeat(3) { i ->
                val y = nbY + 16.dp.toPx() + i * 11.dp.toPx()
                drawLine(
                    color = primary.copy(alpha = 0.45f),
                    start = Offset(nbX + 15.dp.toPx(), y),
                    end = Offset(nbX + nbW - 8.dp.toPx(), y),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}

/**
 * Indicador de avance del pedido dibujado con Canvas:
 * tres puntos conectados (Pendiente -> En preparacion -> Entregado)
 * con la etapa actual resaltada, mas sus etiquetas.
 */
@Composable
fun OrderStatusStepper(
    status: String,
    modifier: Modifier = Modifier
) {
    val steps = listOf("PENDIENTE", "EN_PREPARACION", "ENTREGADO")
    val labels = listOf("Pendiente", "Preparación", "Entregado")
    val currentIndex = steps.indexOf(status).coerceIn(0, steps.lastIndex)

    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
        ) {
            val dotR = 6.dp.toPx()
            val pad = dotR + 2.dp.toPx()
            val y = size.height / 2f
            val xs = listOf(pad, size.width / 2f, size.width - pad)

            // Riel completo
            drawLine(
                color = inactiveColor,
                start = Offset(xs[0], y),
                end = Offset(xs[2], y),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Avance hasta la etapa actual
            if (currentIndex > 0) {
                drawLine(
                    color = activeColor,
                    start = Offset(xs[0], y),
                    end = Offset(xs[currentIndex], y),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Puntos
            xs.forEachIndexed { index, x ->
                if (index < currentIndex) {
                    drawCircle(color = activeColor, radius = dotR, center = Offset(x, y))
                } else if (index == currentIndex) {
                    drawCircle(
                        color = activeColor.copy(alpha = 0.25f),
                        radius = dotR + 6.dp.toPx(),
                        center = Offset(x, y)
                    )
                    drawCircle(color = activeColor, radius = dotR, center = Offset(x, y))
                } else {
                    drawCircle(
                        color = inactiveColor,
                        radius = dotR,
                        center = Offset(x, y),
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            labels.forEachIndexed { index, label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (index <= currentIndex) activeColor
                    else inactiveColor,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
