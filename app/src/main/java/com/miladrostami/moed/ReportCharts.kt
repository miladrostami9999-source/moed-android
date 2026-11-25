package com.miladrostami.moed

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miladrostami.moed.domain.*

/** 950 -> "950", 1500 -> "1.5K", 2_500_000 -> "2.5M", 3_200_000_000 -> "3.2B". */
fun compactAmount(value: Long): String {
    fun scaled(unit: Double, suffix: String): String {
        val v = Math.round(value / unit * 10) / 10.0
        return (if (v == v.toLong().toDouble()) v.toLong().toString() else v.toString()) + suffix
    }
    return when {
        value >= 1_000_000_000L -> scaled(1e9, "B")
        value >= 1_000_000L -> scaled(1e6, "M")
        value >= 1_000L -> scaled(1e3, "K")
        else -> value.toString()
    }
}

private val ltr = TextStyle(textDirection = TextDirection.Ltr)

/** Colour palette shared with the dashboard: blue, teal, periwinkle, sky, slate. */
private val RingPalette = listOf(Color(0xFF648BFF), Color(0xFF43C7AC), Color(0xFF9BAEF5), Color(0xFF76B5CD), Color(0xFF788A9B))

private fun fullDate(bar: ReportBar): String =
    if (bar.daily) "${faNumber(bar.date.day)} ${persianMonthName(bar.date.month)} ${faNumber(bar.date.year)}"
    else "${persianMonthName(bar.date.month)} ${faNumber(bar.date.year)}"

/**
 * Column chart: one gradient stem per day (or month) with a dot on top and the amount above it.
 * Tapping a column shows exactly what was paid on that date in the card above the chart.
 */
@Composable
fun PaymentActivityChart(bars: List<ReportBar>) {
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val faint = MaterialTheme.colorScheme.outlineVariant
    val max = bars.maxOf { it.amount }.coerceAtLeast(1)
    val best = bars.indices.maxByOrNull { bars[it].amount } ?: 0
    var selected by remember(bars) { mutableIntStateOf(best) }
    val bar = bars[selected.coerceIn(0, bars.lastIndex)]
    val chartHeight = 130.dp
    val slot = if (bars.size <= 7) 0.dp else if (bars.size <= 12) 52.dp else 40.dp

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = primary.copy(alpha = .08f),
            border = BorderStroke(1.dp, primary.copy(alpha = .25f))
        ) {
            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(fullDate(bar), Modifier.weight(1f), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (bar.entries.isNotEmpty()) Text(
                        tx("${faNumber(bar.entries.size)} پرداخت", "${bar.entries.size} payment" + if (bar.entries.size == 1) "" else "s"),
                        fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(Modifier.padding(top = 2.dp), verticalAlignment = Alignment.Bottom) {
                    Text(money(bar.amount), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = primary, style = ltr)
                    Text(tx("تومان", "toman"), Modifier.padding(start = 6.dp, bottom = 3.dp), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (bar.entries.isEmpty()) {
                    Text(tx("پرداختی در این تاریخ نیست", "No payments on this date"), Modifier.padding(top = 4.dp), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else bar.entries.take(4).forEach {
                    Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(it.title, Modifier.weight(1f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(money(it.amount), Modifier.padding(start = 8.dp), fontSize = 12.sp, fontWeight = FontWeight.Medium, style = ltr)
                    }
                }
                if (bar.entries.size > 4) Text(
                    tx("و ${faNumber(bar.entries.size - 4)} مورد دیگر", "and ${bar.entries.size - 4} more"),
                    Modifier.padding(top = 4.dp), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        val desc = bars.filter { it.amount > 0 }.joinToString { "${it.label}: ${money(it.amount)} ${tx("تومان", "toman")}" }
        val scroll = rememberScrollState()
        Row(
            Modifier.fillMaxWidth().then(if (slot > 0.dp) Modifier.horizontalScroll(scroll) else Modifier).semantics { contentDescription = desc },
            horizontalArrangement = Arrangement.Start
        ) {
            bars.forEachIndexed { index, b ->
                val isSelected = index == selected
                val fraction by animateFloatAsState(if (b.amount == 0L) 0f else (b.amount.toFloat() / max).coerceIn(.04f, 1f), label = "bar")
                val slotModifier = if (slot > 0.dp) Modifier.width(slot) else Modifier.weight(1f)
                Column(
                    slotModifier.clip(RoundedCornerShape(10.dp)).clickable { selected = index }
                        .background(if (isSelected) primary.copy(alpha = .08f) else Color.Transparent).padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        if (b.amount > 0) compactAmount(b.amount) else "", fontSize = 9.sp, maxLines = 1, style = ltr,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Canvas(Modifier.fillMaxWidth().height(chartHeight).padding(top = 6.dp)) {
                        val centerX = size.width / 2
                        val stemWidth = (size.width * .34f).coerceIn(6.dp.toPx(), 16.dp.toPx())
                        val dot = 3.dp.toPx()
                        if (b.amount == 0L) {
                            drawCircle(faint, dot * .8f, Offset(centerX, size.height - dot))
                        } else {
                            val h = (size.height - dot * 2) * fraction
                            val top = size.height - h
                            drawRoundRect(
                                Brush.verticalGradient(listOf(primary.copy(alpha = if (isSelected) 1f else .75f), primary.copy(alpha = .08f)), startY = top, endY = size.height),
                                Offset(centerX - stemWidth / 2, top), Size(stemWidth, h),
                                androidx.compose.ui.geometry.CornerRadius(stemWidth / 2)
                            )
                            drawCircle(onSurface, dot * if (isSelected) 1.3f else 1f, Offset(centerX, top - dot * 1.2f))
                        }
                    }
                    Text(
                        b.label, Modifier.padding(top = 4.dp), fontSize = 9.sp, maxLines = 1, textAlign = TextAlign.Center,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Concentric rings, one per category (top 4 + "other"), longest ring = biggest share.
 * Legend shows percent and amount; tapping a row highlights its ring and shows its amount in the centre.
 */
@Composable
fun CategoryRingChart(categories: List<Pair<String, Long>>) {
    val total = categories.sumOf { it.second }.coerceAtLeast(1)
    val other = categories.drop(4).sumOf { it.second }
    val rows = categories.take(4).map { it.first to it.second } + if (other > 0) listOf("__other" to other) else emptyList()
    val track = MaterialTheme.colorScheme.surfaceVariant
    val outline = MaterialTheme.colorScheme.outline
    var selected by remember(categories) { mutableIntStateOf(-1) }
    fun colorOf(i: Int) = if (rows[i].first == "__other") outline else RingPalette[i % RingPalette.size]
    fun nameOf(i: Int) = if (rows[i].first == "__other") tx("سایر", "Other") else lx(rows[i].first)
    fun percent(v: Long) = Math.round(v * 100.0 / total).toInt()

    val desc = rows.indices.joinToString { "${nameOf(it)}: ${faNumber(percent(rows[it].second))}%, ${money(rows[it].second)} ${tx("تومان", "toman")}" }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.fillMaxWidth().height(190.dp).semantics { contentDescription = desc }, contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(190.dp)) {
                val stroke = 11.dp.toPx()
                val gap = 5.dp.toPx()
                rows.forEachIndexed { i, row ->
                    val radius = size.minDimension / 2 - stroke / 2 - i * (stroke + gap)
                    if (radius <= stroke) return@forEachIndexed
                    val topLeft = Offset(size.width / 2 - radius, size.height / 2 - radius)
                    val arc = Size(radius * 2, radius * 2)
                    val dim = selected >= 0 && selected != i
                    drawArc(track, 135f, 270f, false, topLeft, arc, style = Stroke(stroke, cap = StrokeCap.Round))
                    val sweep = (270f * row.second / total).coerceAtLeast(6f)
                    drawArc(colorOf(i).copy(alpha = if (dim) .3f else 1f), 135f, sweep, false, topLeft, arc, style = Stroke(stroke, cap = StrokeCap.Round))
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(78.dp)) {
                val shown = if (selected in rows.indices) rows[selected].second else total
                Text(if (selected in rows.indices) nameOf(selected) else tx("مجموع", "Total"), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(compactAmount(shown), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, style = ltr)
                Text(if (selected in rows.indices) "${faNumber(percent(shown))}%" else tx("تومان", "toman"), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        rows.forEachIndexed { i, row ->
            val isSelected = selected == i
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) colorOf(i).copy(alpha = .12f) else Color.Transparent)
                    .clickable { selected = if (isSelected) -1 else i }.padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(colorOf(i)))
                Text(nameOf(i), Modifier.padding(start = 10.dp).weight(1f), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${faNumber(percent(row.second))}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, style = ltr)
                Text(money(row.second), Modifier.padding(start = 12.dp).widthIn(min = 72.dp), fontSize = 12.sp, textAlign = TextAlign.End, color = MaterialTheme.colorScheme.onSurfaceVariant, style = ltr)
            }
        }
    }
}
