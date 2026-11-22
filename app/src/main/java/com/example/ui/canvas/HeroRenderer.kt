package com.example.ui.canvas

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

private val InkColor = Color(0xFF0F172A)

fun DrawScope.drawHeroWithEffects(
    heroId: String,
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    dir: Int = 1,
    t: Float = 0f,
    vy: Float = 0f,
    isShopPreview: Boolean = false,
    tiltDeg: Float = 0f
) {
    val cx = x + w / 2f
    val cy = y + h / 2f

    withTransform({
        translate(cx, cy)
        if (tiltDeg != 0f) rotate(tiltDeg, Offset.Zero)
    }) {
        val blink = sin(t * 2.3f) > 0.96f
        val eyeY = max(-2.5f, min(2.5f, vy * 0.22f))
        val lookX = if (isShopPreview) sin(t * 1.8f) * 2.2f else dir * 2.2f
        val radius = max(w, h) * 0.68f

        drawHeroAura(heroId, w, h, radius, t)

        val hw = w / 2f - 2f
        val hh = h / 2f - 2f
        val strokeW = 3.0f

        when (heroId) {
            "om_de_zapada" -> {
                // Fluttering red scarf tail trailing behind
                val tailDir = if (dir >= 0) -1f else 1f
                val flutter = sin(t * 7.5f) * 3.2f
                val scarfTail = Path().apply {
                    moveTo(tailDir * (hw - 3f), hh - 5.5f)
                    quadraticTo(tailDir * (hw + 7f), hh - 4f + flutter * 0.5f, tailDir * (hw + 12f), hh + 1f + flutter)
                    lineTo(tailDir * (hw + 9f), hh + 5.5f + flutter)
                    quadraticTo(tailDir * (hw + 3f), hh + 1f, tailDir * (hw - 4f), hh - 1.5f)
                    close()
                }
                drawPath(scarfTail, Color(0xFFE03131))
                drawPath(scarfTail, InkColor, style = Stroke(1.8f))

                // Top hat with golden holly buckle
                drawRect(Color(0xFF1E293B), Offset(-hw * 0.65f, -hh - 9f), Size(hw * 1.3f, 9f))
                drawRect(InkColor, Offset(-hw * 0.65f, -hh - 9f), Size(hw * 1.3f, 9f), style = Stroke(2.2f))
                drawRect(Color(0xFFFA5252), Offset(-hw * 0.65f, -hh - 3f), Size(hw * 1.3f, 3f))
                drawRect(Color(0xFFFCC419), Offset(-2.5f, -hh - 3.5f), Size(5f, 3.8f))
                drawRect(InkColor, Offset(-hw * 0.9f, -hh - 1f), Size(hw * 1.8f, 3.5f))

                // Snow body with icy winter gradient sheen
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White, Color(0xFFE7F5FF), Color(0xFFBAE6FD)),
                        center = Offset(-4f, -4f),
                        radius = (hw + 2f) * 1.45f
                    ),
                    radius = hw + 1f,
                    center = Offset.Zero
                )
                drawCircle(InkColor, hw + 1f, Offset.Zero, style = Stroke(strokeW))

                // Rosy cheeks + red scarf + coal buttons
                drawCircle(Color(0xFFFF8787).copy(alpha = 0.55f), 2.8f, Offset(-8.5f + lookX * 0.5f, 4f + eyeY))
                drawCircle(Color(0xFFFF8787).copy(alpha = 0.55f), 2.8f, Offset(8.5f + lookX * 0.5f, 4f + eyeY))
                drawRoundRect(Color(0xFFFA5252), Offset(-hw + 1f, hh - 7f), Size((hw - 1f) * 2, 5.5f), CornerRadius(3f, 3f))
                drawRoundRect(InkColor, Offset(-hw + 1f, hh - 7f), Size((hw - 1f) * 2, 5.5f), CornerRadius(3f, 3f), style = Stroke(1.6f))
                drawCircle(InkColor, 1.8f, Offset(0f, hh - 9.2f))

                drawHeroFace(lookX, -2f + eyeY, blink, hasSmile = false)
                // Carrot nose
                val nose = Path().apply {
                    moveTo(lookX, 1f + eyeY)
                    lineTo(lookX + 8.5f * if (dir >= 0) 1f else -1f, 3f + eyeY)
                    lineTo(lookX, 5f + eyeY)
                    close()
                }
                drawPath(nose, Color(0xFFFF922B))
                drawPath(nose, InkColor, style = Stroke(1.6f))
            }

            "turta_dulce" -> {
                drawRoundRect(Color(0xFFC06C2B), Offset(-hw, -hh), Size(hw * 2, hh * 2), CornerRadius(12f, 12f))
                drawRoundRect(InkColor, Offset(-hw, -hh), Size(hw * 2, hh * 2), CornerRadius(12f, 12f), style = Stroke(strokeW))
                // Frosting wave
                drawRect(Color.White, Offset(-hw + 3f, -hh + 3f), Size((hw - 3f) * 2, 3.5f))
                drawHeroFace(lookX, -1f + eyeY, blink, hasSmile = true)
                drawCircle(Color(0xFFFA5252), 2.5f, Offset(0f, hh - 4.5f))
            }

            "cadou_magic" -> {
                // Ribbon bow
                drawCircle(Color(0xFFFCC419), 5f, Offset(-5f, -hh - 3f))
                drawCircle(InkColor, 5f, Offset(-5f, -hh - 3f), style = Stroke(2.2f))
                drawCircle(Color(0xFFFCC419), 5f, Offset(5f, -hh - 3f))
                drawCircle(InkColor, 5f, Offset(5f, -hh - 3f), style = Stroke(2.2f))
                // Red box
                drawRoundRect(Color(0xFFE03131), Offset(-hw, -hh), Size(hw * 2, hh * 2), CornerRadius(6f, 6f))
                drawRect(Color(0xFFFCC419), Offset(-3f, -hh), Size(6f, hh * 2))
                drawRect(Color(0xFFFCC419), Offset(-hw, -3f), Size(hw * 2, 6f))
                drawRoundRect(InkColor, Offset(-hw, -hh), Size(hw * 2, hh * 2), CornerRadius(6f, 6f), style = Stroke(strokeW))
                drawHeroFace(lookX, -1f + eyeY, blink, hasSmile = true)
            }

            "bradut_vesel" -> {
                val tree = Path().apply {
                    moveTo(0f, -hh - 4f)
                    lineTo(hw + 3f, hh)
                    lineTo(-hw - 3f, hh)
                    close()
                }
                drawPath(tree, Color(0xFF2FB344))
                drawPath(tree, InkColor, style = Stroke(strokeW))
                // Blinking ornaments
                val l1 = if (sin(t * 6f) > 0f) Color(0xFFFA5252) else Color(0xFFFCC419)
                val l2 = if (sin(t * 6f) > 0f) Color(0xFF4DABF7) else Color(0xFFFA5252)
                drawCircle(l1, 2.8f, Offset(-6f, hh - 6f))
                drawCircle(l2, 2.8f, Offset(6f, hh - 6f))
                drawHeroFace(lookX * 0.7f, 2f + eyeY, blink, hasSmile = false)
            }

            "renul_rudolf" -> {
                // Antlers
                val antlers = Path().apply {
                    moveTo(-7f, -hh + 1f); lineTo(-12f, -hh - 10f); lineTo(-8f, -hh - 6f)
                    moveTo(7f, -hh + 1f); lineTo(12f, -hh - 10f); lineTo(8f, -hh - 6f)
                }
                drawPath(antlers, Color(0xFF7C4D00), style = Stroke(3.2f))
                drawRoundRect(Color(0xFFA4632A), Offset(-hw, -hh + 1f), Size(hw * 2, hh * 2 - 1f), CornerRadius(10f, 10f))
                drawRoundRect(InkColor, Offset(-hw, -hh + 1f), Size(hw * 2, hh * 2 - 1f), CornerRadius(10f, 10f), style = Stroke(strokeW))
                drawHeroFace(lookX, -1f + eyeY, blink, hasSmile = false)
                // Glowing red nose
                val noseGlow = 3.5f + sin(t * 8f) * 0.8f
                drawCircle(Color(0xFFFF2A2A), noseGlow, Offset(lookX * 1.1f, 5f + eyeY))
                drawCircle(InkColor, noseGlow, Offset(lookX * 1.1f, 5f + eyeY), style = Stroke(1.8f))
            }

            "spiridus_jucaus" -> {
                // Green elf hat
                val hat = Path().apply {
                    moveTo(-hw, -hh + 2f)
                    lineTo(0f, -hh - 11f)
                    lineTo(hw, -hh + 2f)
                    close()
                }
                drawPath(hat, Color(0xFF2FB344))
                drawPath(hat, InkColor, style = Stroke(2.6f))
                drawCircle(Color(0xFFFCC419), 3.4f, Offset(0f, -hh - 11f))
                drawRoundRect(Color(0xFF51CF66), Offset(-hw, -hh + 1f), Size(hw * 2, hh * 2 - 1f), CornerRadius(9f, 9f))
                drawRoundRect(InkColor, Offset(-hw, -hh + 1f), Size(hw * 2, hh * 2 - 1f), CornerRadius(9f, 9f), style = Stroke(strokeW))
                drawHeroFace(lookX, 1f + eyeY, blink, hasSmile = true)
            }

            "acadea_ninja" -> {
                drawRoundRect(Color(0xFFF8FCFF), Offset(-hw, -hh), Size(hw * 2, hh * 2), CornerRadius(9f, 9f))
                drawRoundRect(InkColor, Offset(-hw, -hh), Size(hw * 2, hh * 2), CornerRadius(9f, 9f), style = Stroke(strokeW))
                drawRect(Color(0xFFE03131), Offset(-hw + 1f, -hh + 4f), Size((hw - 1f) * 2, 6f))
                drawRect(InkColor, Offset(-hw + 1f, -hh + 4f), Size((hw - 1f) * 2, 6f), style = Stroke(2.2f))
                drawHeroFace(lookX, 2f + eyeY, blink, hasSmile = false)
            }

            "pinguin_polar" -> {
                drawRoundRect(Color(0xFF1E293B), Offset(-hw, -hh), Size(hw * 2, hh * 2), CornerRadius(12f, 12f))
                drawRoundRect(InkColor, Offset(-hw, -hh), Size(hw * 2, hh * 2), CornerRadius(12f, 12f), style = Stroke(strokeW))
                drawOval(Color.White, Offset(-hw + 4f, -2f), Size((hw - 4f) * 2, hh + 1f))
                drawRect(Color(0xFFFA5252), Offset(-hw + 1f, hh - 7f), Size((hw - 1f) * 2, 4.5f))
                drawHeroFace(lookX, -3f + eyeY, blink, hasSmile = false)
                val beak = Path().apply {
                    moveTo(-3f + lookX, 2f + eyeY); lineTo(3f + lookX, 2f + eyeY); lineTo(lookX, 7f + eyeY); close()
                }
                drawPath(beak, Color(0xFFFF922B))
            }

            "clopotel_magic" -> {
                // Golden Sleigh Bell body + red velvet ribbon & holly
                val bellPath = Path().apply {
                    moveTo(-hw * 0.65f, -hh + 2f)
                    lineTo(hw * 0.65f, -hh + 2f)
                    lineTo(hw + 2f, hh - 2f)
                    lineTo(-hw - 2f, hh - 2f)
                    close()
                }
                drawCircle(Color(0xFFFCC419), 4.5f, Offset(0f, hh + 1f))
                drawCircle(InkColor, 4.5f, Offset(0f, hh + 1f), style = Stroke(2f))
                drawPath(bellPath, Color(0xFFFFD43B))
                drawPath(bellPath, InkColor, style = Stroke(strokeW))
                // Velvet red bow on top + green holly
                drawCircle(Color(0xFFE03131), 4.5f, Offset(-5.5f, -hh - 2f))
                drawCircle(Color(0xFFE03131), 4.5f, Offset(5.5f, -hh - 2f))
                drawCircle(Color(0xFF2FB344), 3.2f, Offset(0f, -hh - 2f))
                drawCircle(InkColor, 3.2f, Offset(0f, -hh - 2f), style = Stroke(1.8f))
                drawHeroFace(lookX, 1f + eyeY, blink, hasSmile = true)
            }

            "vrajitoarea_ghetii" -> {
                val crown = Path().apply {
                    moveTo(-10f, -hh + 2f); lineTo(-6f, -hh - 8f); lineTo(0f, -hh - 4f)
                    lineTo(6f, -hh - 8f); lineTo(10f, -hh + 2f); close()
                }
                drawPath(crown, Color(0xFF74C0FC))
                drawPath(crown, InkColor, style = Stroke(2.4f))
                drawRoundRect(Color(0xFF339AF0), Offset(-hw, -hh + 1f), Size(hw * 2, hh * 2 - 1f), CornerRadius(10f, 10f))
                drawRoundRect(InkColor, Offset(-hw, -hh + 1f), Size(hw * 2, hh * 2 - 1f), CornerRadius(10f, 10f), style = Stroke(strokeW))
                drawHeroFace(lookX, 1f + eyeY, blink, hasSmile = true)
            }

            "urs_polar_festiv" -> {
                drawCircle(Color(0xFFF8FCFF), 5f, Offset(-hw + 3f, -hh + 2f))
                drawCircle(InkColor, 5f, Offset(-hw + 3f, -hh + 2f), style = Stroke(2.5f))
                drawCircle(Color(0xFFF8FCFF), 5f, Offset(hw - 3f, -hh + 2f))
                drawCircle(InkColor, 5f, Offset(hw - 3f, -hh + 2f), style = Stroke(2.5f))
                drawRoundRect(Color(0xFFF8FCFF), Offset(-hw, -hh + 2f), Size(hw * 2, hh * 2 - 2f), CornerRadius(12f, 12f))
                drawRoundRect(InkColor, Offset(-hw, -hh + 2f), Size(hw * 2, hh * 2 - 2f), CornerRadius(12f, 12f), style = Stroke(strokeW))
                drawHeroFace(lookX, eyeY, blink, hasSmile = true)
            }

            "regele_ghetii" -> {
                val crown = Path().apply {
                    moveTo(-9f, -hh + 1f); lineTo(-9f, -hh - 8f); lineTo(0f, -hh - 10f)
                    lineTo(9f, -hh - 8f); lineTo(9f, -hh + 1f); close()
                }
                drawPath(crown, Color(0xFF66D9E8))
                drawPath(crown, InkColor, style = Stroke(2.6f))
                drawRoundRect(Color(0xFF1C7ED6), Offset(-hw, -hh + 1f), Size(hw * 2, hh * 2 - 1f), CornerRadius(9f, 9f))
                drawRoundRect(InkColor, Offset(-hw, -hh + 1f), Size(hw * 2, hh * 2 - 1f), CornerRadius(9f, 9f), style = Stroke(strokeW))
                drawHeroFace(lookX, 1f + eyeY, blink, hasSmile = false)
            }

            "steaua_craciunului" -> {
                drawRoundRect(Color(0xFFFCC419), Offset(-hw, -hh), Size(hw * 2, hh * 2), CornerRadius(12f, 12f))
                drawRoundRect(InkColor, Offset(-hw, -hh), Size(hw * 2, hh * 2), CornerRadius(12f, 12f), style = Stroke(strokeW))
                drawHeroFace(lookX, eyeY, blink, hasSmile = true)
            }

            "dragon_de_gheata" -> {
                val horns = Path().apply {
                    moveTo(-10f, -hh + 2f); lineTo(-14f, -hh - 9f); lineTo(-3f, -hh + 1f)
                    moveTo(10f, -hh + 2f); lineTo(14f, -hh - 9f); lineTo(3f, -hh + 1f)
                }
                drawPath(horns, Color(0xFF74C0FC))
                drawPath(horns, InkColor, style = Stroke(2.6f))
                drawRoundRect(Color(0xFF3BC9DB), Offset(-hw, -hh), Size(hw * 2, hh * 2), CornerRadius(9f, 9f))
                drawRoundRect(InkColor, Offset(-hw, -hh), Size(hw * 2, hh * 2), CornerRadius(9f, 9f), style = Stroke(strokeW))
                drawHeroFace(lookX, eyeY, blink, hasSmile = true)
            }

            "vulpea_polara" -> {
                // Fluffy snow-white arctic fox tail curling behind
                drawOval(Color(0xFFE7F5FF), Offset(-hw - 8f, 1f), Size(14f, 10f))
                drawOval(InkColor, Offset(-hw - 8f, 1f), Size(14f, 10f), style = Stroke(2f))
                // Pointed arctic fox ears
                val ears = Path().apply {
                    moveTo(-hw + 1f, -hh + 3f); lineTo(-hw + 4f, -hh - 9f); lineTo(-2f, -hh + 1f); close()
                    moveTo(hw - 1f, -hh + 3f); lineTo(hw - 4f, -hh - 9f); lineTo(2f, -hh + 1f); close()
                }
                drawPath(ears, Color(0xFFF8FCFF))
                drawPath(ears, InkColor, style = Stroke(2.5f))
                drawRoundRect(Color(0xFFF8FCFF), Offset(-hw, -hh + 1f), Size(hw * 2, hh * 2 - 1f), CornerRadius(11f, 11f))
                drawRoundRect(InkColor, Offset(-hw, -hh + 1f), Size(hw * 2, hh * 2 - 1f), CornerRadius(11f, 11f), style = Stroke(strokeW))
                // Icy teal scarf
                drawRoundRect(Color(0xFF38D9A9), Offset(-hw + 1f, hh - 7f), Size((hw - 1f) * 2, 5f), CornerRadius(2.5f, 2.5f))
                drawHeroFace(lookX, -1f + eyeY, blink, hasSmile = true)
                drawCircle(InkColor, 2.3f, Offset(lookX, 3.5f + eyeY))
            }

            "spargatorul_de_nuci" -> {
                drawRect(Color(0xFF1E293B), Offset(-hw + 2f, -hh - 7f), Size((hw - 2f) * 2, 8f))
                drawRect(InkColor, Offset(-hw + 2f, -hh - 7f), Size((hw - 2f) * 2, 8f), style = Stroke(2.4f))
                drawRoundRect(Color(0xFFE03131), Offset(-hw, -hh), Size(hw * 2, hh * 2), CornerRadius(7f, 7f))
                drawRoundRect(InkColor, Offset(-hw, -hh), Size(hw * 2, hh * 2), CornerRadius(7f, 7f), style = Stroke(strokeW))
                drawHeroFace(lookX, -1f + eyeY, blink, hasSmile = false)
                drawRect(Color.White, Offset(-7f + lookX, 5f), Size(14f, 4f))
            }

            "mos_craciun_turbo" -> {
                val hat = Path().apply {
                    moveTo(-hw, -hh + 2f); lineTo(0f, -hh - 11f); lineTo(hw, -hh + 2f); close()
                }
                drawPath(hat, Color(0xFFE03131))
                drawPath(hat, InkColor, style = Stroke(2.5f))
                drawCircle(Color.White, 3.6f, Offset(0f, -hh - 11f))
                drawRoundRect(Color(0xFFE03131), Offset(-hw, -hh + 1f), Size(hw * 2, hh * 2 - 1f), CornerRadius(10f, 10f))
                drawRoundRect(InkColor, Offset(-hw, -hh + 1f), Size(hw * 2, hh * 2 - 1f), CornerRadius(10f, 10f), style = Stroke(strokeW))
                // Fluffy white beard
                drawRoundRect(Color.White, Offset(-hw + 2f, 4f), Size((hw - 2f) * 2, hh - 3f), CornerRadius(6f, 6f))
                drawHeroFace(lookX, -2f + eyeY, blink, hasSmile = false)
            }

            "jack_frost_divin" -> {
                // Spiky ice crystal hair crown
                val iceCrown = Path().apply {
                    moveTo(-hw, -hh + 2f)
                    lineTo(-hw + 3f, -hh - 9f)
                    lineTo(-3f, -hh - 2f)
                    lineTo(0f, -hh - 11f)
                    lineTo(3f, -hh - 2f)
                    lineTo(hw - 3f, -hh - 9f)
                    lineTo(hw, -hh + 2f)
                    close()
                }
                drawPath(iceCrown, Color(0xFFD0EBFF))
                drawPath(iceCrown, InkColor, style = Stroke(2.4f))
                drawRoundRect(Color(0xFF0B7285), Offset(-hw, -hh + 1f), Size(hw * 2, hh * 2 - 1f), CornerRadius(10f, 10f))
                drawRoundRect(InkColor, Offset(-hw, -hh + 1f), Size(hw * 2, hh * 2 - 1f), CornerRadius(10f, 10f), style = Stroke(strokeW))
                drawRoundRect(Color(0xFF74C0FC), Offset(-hw + 1f, hh - 7f), Size((hw - 1f) * 2, 5.5f), CornerRadius(3f, 3f))
                drawHeroFace(lookX, -1f + eyeY, blink, hasSmile = true)
            }

            "aurora_boreala_divin" -> {
                // Celestial northern lights crest
                drawArc(
                    color = Color(0xFF38D9A9),
                    startAngle = 190f,
                    sweepAngle = 160f,
                    useCenter = false,
                    topLeft = Offset(-hw - 2f, -hh - 10f),
                    size = Size((hw + 2f) * 2f, 16f),
                    style = Stroke(3.6f)
                )
                drawRoundRect(Color(0xFF5F3DC4), Offset(-hw, -hh), Size(hw * 2, hh * 2), CornerRadius(12f, 12f))
                drawRoundRect(Color(0xFF38D9A9).copy(alpha = 0.55f), Offset(-hw + 2f, -hh + 2f), Size((hw - 2f) * 2f, (hh - 2f) * 2f), CornerRadius(10f, 10f))
                drawRoundRect(InkColor, Offset(-hw, -hh), Size(hw * 2, hh * 2), CornerRadius(12f, 12f), style = Stroke(strokeW))
                drawHeroFace(lookX, eyeY, blink, hasSmile = true)
            }

            "ingerul_zapezii" -> {
                drawOval(Color(0xFFFCC419), Offset(-10f, -hh - 9f), Size(20f, 6f), style = Stroke(2.8f))
                drawRoundRect(Color(0xFFE7F5FF), Offset(-hw, -hh), Size(hw * 2, hh * 2), CornerRadius(11f, 11f))
                drawRoundRect(InkColor, Offset(-hw, -hh), Size(hw * 2, hh * 2), CornerRadius(11f, 11f), style = Stroke(strokeW))
                drawHeroFace(lookX, eyeY, blink, hasSmile = true)
            }

            "craciunita_stelara" -> {
                // Festive fur-trimmed crimson hat + golden sash + rosy look
                val hat = Path().apply {
                    moveTo(-hw, -hh + 2f); lineTo(2f, -hh - 11f); lineTo(hw, -hh + 2f); close()
                }
                drawPath(hat, Color(0xFFC92A2A))
                drawPath(hat, InkColor, style = Stroke(2.4f))
                drawCircle(Color.White, 3.6f, Offset(2f, -hh - 11f))
                drawRoundRect(Color(0xFFF06595), Offset(-hw, -hh + 1f), Size(hw * 2, hh * 2 - 1f), CornerRadius(11f, 11f))
                drawRoundRect(Color.White, Offset(-hw + 1f, -hh + 1f), Size((hw - 1f) * 2, 5f), CornerRadius(4f, 4f))
                drawRect(Color(0xFFFCC419), Offset(-hw + 1f, hh - 7f), Size((hw - 1f) * 2, 5f))
                drawRoundRect(InkColor, Offset(-hw, -hh + 1f), Size(hw * 2, hh * 2 - 1f), CornerRadius(11f, 11f), style = Stroke(strokeW))
                drawHeroFace(lookX, eyeY, blink, hasSmile = true)
            }

            "mos_nicolae_astral" -> {
                // Royal Bishop Mitre hat + golden cross/star + silver beard
                val mitre = Path().apply {
                    moveTo(-hw + 2f, -hh + 2f)
                    lineTo(-5f, -hh - 10f)
                    lineTo(0f, -hh - 13f)
                    lineTo(5f, -hh - 10f)
                    lineTo(hw - 2f, -hh + 2f)
                    close()
                }
                drawPath(mitre, Color(0xFFB91C1C))
                drawPath(mitre, InkColor, style = Stroke(2.4f))
                drawCircle(Color(0xFFFCC419), 3.2f, Offset(0f, -hh - 5f))
                drawRoundRect(Color(0xFF991B1B), Offset(-hw, -hh + 1f), Size(hw * 2, hh * 2 - 1f), CornerRadius(10f, 10f))
                drawRoundRect(InkColor, Offset(-hw, -hh + 1f), Size(hw * 2, hh * 2 - 1f), CornerRadius(10f, 10f), style = Stroke(strokeW))
                drawRoundRect(Color(0xFFE2E8F0), Offset(-hw + 2f, 4f), Size((hw - 2f) * 2, hh - 3f), CornerRadius(6f, 6f))
                drawHeroFace(lookX, -2f + eyeY, blink, hasSmile = true)
            }

            "phoenix_festiv" -> {
                drawRoundRect(Color(0xFFE03131), Offset(-hw, -hh), Size(hw * 2, hh * 2), CornerRadius(10f, 10f))
                drawRoundRect(InkColor, Offset(-hw, -hh), Size(hw * 2, hh * 2), CornerRadius(10f, 10f), style = Stroke(strokeW))
                drawHeroFace(lookX, eyeY, blink, hasSmile = true)
            }

            else -> {
                // spiritul_craciunului
                drawRoundRect(Color(0xFF15803D), Offset(-hw, -hh), Size(hw * 2, hh * 2), CornerRadius(11f, 11f))
                drawRoundRect(InkColor, Offset(-hw, -hh), Size(hw * 2, hh * 2), CornerRadius(11f, 11f), style = Stroke(strokeW))
                drawHeroFace(lookX, eyeY, blink, hasSmile = true)
            }
        }
    }
}

private fun DrawScope.drawHeroAura(
    heroId: String,
    w: Float,
    h: Float,
    radius: Float,
    t: Float
) {
    when (heroId) {
        "om_de_zapada" -> {
            // Cool Polar Snow Aura: pulsing frost glow, rotating dashed ice ring & 6-arm snow crystals!
            val pulse = 1f + sin(t * 3.8f) * 0.10f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF74C0FC).copy(alpha = 0.42f),
                        Color(0xFF63E6BE).copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    center = Offset.Zero,
                    radius = radius * 1.48f * pulse
                ),
                radius = radius * 1.48f * pulse,
                center = Offset.Zero
            )
            rotate(degrees = (t * 48f) % 360f, pivot = Offset.Zero) {
                drawCircle(
                    color = Color(0xFFBAE6FD).copy(alpha = 0.72f),
                    radius = radius * 1.12f,
                    center = Offset.Zero,
                    style = Stroke(2.0f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f)))
                )
            }
            // Orbiting 6-arm crystalline snowflakes + shimmering frost motes
            for (i in 0 until 6) {
                val a = t * 2.5f + i * (PI.toFloat() / 3f)
                val sx = cos(a) * radius * (1.16f + (i % 2) * 0.12f)
                val sy = sin(a) * radius * (0.94f + (i % 2) * 0.08f)
                val flakeR = if (i % 2 == 0) 4.4f else 3.2f
                val flakeCol = if (i % 2 == 0) Color.White else Color(0xFF74C0FC)
                for (arm in 0 until 3) {
                    val armA = t * 3.2f + arm * (PI.toFloat() / 3f)
                    drawLine(
                        color = flakeCol,
                        start = Offset(sx - cos(armA) * flakeR, sy - sin(armA) * flakeR),
                        end = Offset(sx + cos(armA) * flakeR, sy + sin(armA) * flakeR),
                        strokeWidth = 1.6f,
                        cap = StrokeCap.Round
                    )
                }
                drawCircle(Color(0xFF63E6BE), 1.4f, Offset(sx, sy))
            }
        }
        "turta_dulce" -> {
            for (i in 0 until 5) {
                val a = t * 2.6f + i * 1.25f
                val py = 12f - ((t * 20f + i * 9f) % 26f)
                drawCircle(if (i % 2 == 0) Color(0xFFFFD43B) else Color(0xFFFF922B), 2.4f, Offset(cos(a) * 16f, py))
            }
        }
        "cadou_magic" -> {
            val pulse = 1f + sin(t * 5.5f) * 0.14f
            drawCircle(Color(0xFFFCC419).copy(alpha = 0.65f), radius * pulse, Offset.Zero, style = Stroke(2.4f))
        }
        "bradut_vesel" -> {
            val pulse = 1f + sin(t * 5f) * 0.16f
            drawCircle(Color(0xFF51CF66).copy(alpha = 0.6f), radius * pulse, Offset.Zero, style = Stroke(2.6f))
        }
        "renul_rudolf" -> {
            for (i in 0 until 4) {
                val ang = t * 3.8f + i * 1.57f
                drawCircle(Color(0xFFFFD43B), 2.8f, Offset(cos(ang) * radius * 1.05f, sin(ang) * radius * 0.8f))
            }
        }
        "spiridus_jucaus" -> {
            rotate(degrees = (t * 2.5f * 180f / PI.toFloat()), pivot = Offset.Zero) {
                drawCircle(
                    color = Color(0xFF51CF66).copy(alpha = 0.78f),
                    radius = radius * 1.05f,
                    center = Offset.Zero,
                    style = Stroke(2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4f)))
                )
            }
        }
        "acadea_ninja" -> {
            for (i in 0 until 3) {
                val ang = t * 4.6f + (i * 2f * PI.toFloat()) / 3f
                val sx = cos(ang) * (radius * 1.06f)
                val sy = sin(ang) * (radius * 0.85f)
                rotate(degrees = (t * 9f * 180f / PI.toFloat()), pivot = Offset(sx, sy)) {
                    drawRect(Color(0xFFFA5252), Offset(sx - 4f, sy - 1.5f), Size(8f, 3f))
                    drawRect(Color.White, Offset(sx - 1.5f, sy - 4f), Size(3f, 8f))
                }
            }
        }
        "pinguin_polar" -> {
            val wave = (t * 18f) % 16f
            drawCircle(Color(0xFF4DABF7).copy(alpha = max(0f, 0.8f - wave / 16f)), radius * 0.75f + wave, Offset.Zero, style = Stroke(2.2f))
        }
        "clopotel_magic" -> {
            // Golden echoing sleigh-bell soundwaves & shimmering chime sparks
            val ringCycle = (t * 1.2f) % 1f
            drawCircle(
                color = Color(0xFFFCC419).copy(alpha = (1f - ringCycle) * 0.85f),
                radius = radius * (0.78f + ringCycle * 0.52f),
                center = Offset.Zero,
                style = Stroke(2.5f)
            )
            for (i in 0 until 3) {
                val ang = t * 3.2f + i * 2.09f
                drawCircle(Color(0xFFFFE066), 2.8f, Offset(cos(ang) * radius * 1.08f, sin(ang) * radius * 0.9f))
            }
        }
        "vrajitoarea_ghetii" -> {
            for (i in 0 until 4) {
                val ang = -t * 2.4f + (i * PI.toFloat()) / 2f
                drawCircle(Color(0xFFBAE6FD), 3.3f, Offset(cos(ang) * radius * 1.08f, sin(ang) * radius * 0.82f))
            }
        }
        "urs_polar_festiv" -> {
            val pulse = 1f + sin(t * 4.5f) * 0.14f
            drawCircle(Color(0xFF74C0FC).copy(alpha = 0.65f), radius * pulse, Offset.Zero, style = Stroke(2.6f))
        }
        "regele_ghetii" -> {
            for (i in 0 until 4) {
                val ang = t * 2.8f + (i * PI.toFloat()) / 2f
                val cx = cos(ang) * radius * 1.08f
                val cy = sin(ang) * radius * 0.75f
                val shard = Path().apply {
                    moveTo(cx, cy - 5f); lineTo(cx + 4f, cy); lineTo(cx, cy + 5f); lineTo(cx - 4f, cy); close()
                }
                drawPath(shard, Color(0xFF66D9E8))
                drawPath(shard, InkColor, style = Stroke(1.6f))
            }
        }
        "steaua_craciunului" -> {
            rotate(degrees = (sin(t) * 0.35f) * 180f / PI.toFloat(), pivot = Offset.Zero) {
                drawOval(Color(0xFFFFD43B), Offset(-radius * 1.25f, -radius * 0.42f), Size(radius * 2.5f, radius * 0.84f), style = Stroke(2.5f))
            }
        }
        "dragon_de_gheata" -> {
            for (i in 0 until 6) {
                val ang = t * 4.2f + (i * PI.toFloat()) / 3f
                val col = if (i % 2 == 0) Color(0xFF3BC9DB) else Color.White
                drawCircle(col, 3.8f, Offset(cos(ang) * radius * 1.12f, sin(ang) * radius * 1.12f))
            }
        }
        "vulpea_polara" -> {
            for (i in 0 until 5) {
                val ang = t * 3.4f + (i * 2f * PI.toFloat()) / 5f
                val col = if (i % 2 == 0) Color(0xFF99E9F2) else Color(0xFF38D9A9)
                val px = cos(ang) * radius * 1.1f
                val py = sin(ang) * radius * 0.88f
                drawCircle(col.copy(alpha = 0.85f), 3.3f, Offset(px, py))
            }
        }
        "spargatorul_de_nuci" -> {
            val startDeg = (t * 5f * 180f / PI.toFloat()) % 360f
            val arcSize = Size(radius * 2.24f, radius * 2.24f)
            val arcTL = Offset(-radius * 1.12f, -radius * 1.12f)
            drawArc(Color(0xFFFCC419), startDeg, 80f, false, arcTL, arcSize, style = Stroke(2.8f))
            drawArc(Color(0xFFFA5252), startDeg + 180f, 80f, false, arcTL, arcSize, style = Stroke(2.8f))
        }
        "mos_craciun_turbo" -> {
            for (i in 0 until 4) {
                val a = t * 4.5f + i * 1.57f
                val col = if (i % 2 == 0) Color(0xFFFFD43B) else Color(0xFF51CF66)
                drawCircle(col, 3.4f, Offset(cos(a) * radius * 1.12f, sin(a) * radius * 0.9f))
            }
        }
        "jack_frost_divin" -> {
            // Swirling blizzard frost halo + 6 orbiting crystal diamonds
            drawCircle(Color(0xFF3BC9DB).copy(alpha = 0.45f), radius * 1.25f, Offset.Zero, style = Stroke(2.4f))
            for (i in 0 until 6) {
                val ang = t * 4.4f + (i * PI.toFloat()) / 3f
                val fx = cos(ang) * radius * 1.18f
                val fy = sin(ang) * radius * 0.95f
                drawCircle(Color(0xFFD0EBFF), 3.2f, Offset(fx, fy))
                drawCircle(Color(0xFF22B8CF), 1.6f, Offset(fx, fy))
            }
        }
        "aurora_boreala_divin" -> {
            // Multi-colored rotating Aurora Borealis ribbons & prismatic stars
            val startDeg = (t * 95f) % 360f
            val arcSize = Size(radius * 2.36f, radius * 2.36f)
            val arcTL = Offset(-radius * 1.18f, -radius * 1.18f)
            drawArc(Color(0xFF38D9A9), startDeg, 95f, false, arcTL, arcSize, style = Stroke(3.2f))
            drawArc(Color(0xFF9775FA), startDeg + 120f, 95f, false, arcTL, arcSize, style = Stroke(3.2f))
            drawArc(Color(0xFFFCC419), startDeg + 240f, 85f, false, arcTL, arcSize, style = Stroke(3.0f))
        }
        "craciunita_stelara" -> {
            // Orbiting mini wrapped gifts + golden confetti halo
            for (i in 0 until 4) {
                val ang = t * 3.6f + i * 1.57f
                val gx = cos(ang) * radius * 1.16f
                val gy = sin(ang) * radius * 0.92f
                val boxCol = if (i % 2 == 0) Color(0xFFFA5252) else Color(0xFF51CF66)
                drawRect(boxCol, Offset(gx - 3.5f, gy - 3.5f), Size(7f, 7f))
                drawRect(Color(0xFFFCC419), Offset(gx - 1f, gy - 3.5f), Size(2f, 7f))
            }
        }
        "mos_nicolae_astral" -> {
            // Royal candy-cane turbo comets + golden starlight ring
            drawCircle(Color(0xFFFCC419).copy(alpha = 0.65f), radius * 1.18f, Offset.Zero, style = Stroke(2.4f))
            for (i in 0 until 4) {
                val ang = -t * 4.8f + i * 1.57f
                val cx = cos(ang) * radius * 1.22f
                val cy = sin(ang) * radius * 0.92f
                drawCircle(if (i % 2 == 0) Color(0xFFE03131) else Color.White, 3.8f, Offset(cx, cy))
                drawCircle(Color(0xFFFCC419), 1.8f, Offset(cx, cy))
            }
        }
        "ingerul_zapezii" -> {
            val flap = sin(t * 9f) * 4.5f
            val wings = Path().apply {
                moveTo(-w * 0.4f, 0f); lineTo(-w * 0.85f, -9f + flap); lineTo(-w * 0.4f, 8f); close()
                moveTo(w * 0.4f, 0f); lineTo(w * 0.85f, -9f + flap); lineTo(w * 0.4f, 8f); close()
            }
            drawPath(wings, Color.White)
            drawPath(wings, InkColor, style = Stroke(2.2f))
        }
        "phoenix_festiv" -> {
            val flap = sin(t * 11f) * 5f
            val wings = Path().apply {
                moveTo(-w * 0.4f, 0f); lineTo(-w * 0.85f, -10f + flap); lineTo(-w * 0.4f, 9f); close()
                moveTo(w * 0.4f, 0f); lineTo(w * 0.85f, -10f + flap); lineTo(w * 0.4f, 9f); close()
            }
            drawPath(wings, Color(0xFFFCC419))
            drawPath(wings, InkColor, style = Stroke(2.4f))
        }
        "spiritul_craciunului" -> {
            for (i in 0 until 3) {
                val ang = t * 3.5f + (i * 2f * PI.toFloat()) / 3f
                val col = when (i) {
                    0 -> Color(0xFFFFD43B)
                    1 -> Color(0xFF51CF66)
                    else -> Color(0xFFFA5252)
                }
                drawCircle(col, 3.8f, Offset(cos(ang) * radius * 1.15f, sin(ang) * radius * 0.65f))
            }
        }
    }
}

private fun DrawScope.drawHeroFace(
    ox: Float,
    oy: Float,
    blink: Boolean,
    hasSmile: Boolean
) {
    val ry = if (blink) 1.2f else 4.6f
    val rx = 4.2f

    drawOval(Color.White, Offset(-6f + ox - rx, oy - ry), Size(rx * 2, ry * 2))
    drawOval(InkColor, Offset(-6f + ox - rx, oy - ry), Size(rx * 2, ry * 2), style = Stroke(2.0f))

    drawOval(Color.White, Offset(6f + ox - rx, oy - ry), Size(rx * 2, ry * 2))
    drawOval(InkColor, Offset(6f + ox - rx, oy - ry), Size(rx * 2, ry * 2), style = Stroke(2.0f))

    if (!blink) {
        drawCircle(InkColor, 2.0f, Offset(-5.5f + ox * 1.35f, oy))
        drawCircle(InkColor, 2.0f, Offset(6.5f + ox * 1.35f, oy))
    }

    if (hasSmile) {
        val smileRect = Rect(ox * 0.6f - 4.4f, oy + 6.5f - 4.4f, ox * 0.6f + 4.4f, oy + 6.5f + 4.4f)
        val p = Path().apply {
            arcTo(smileRect, 0f, 180f, false)
            close()
        }
        drawPath(p, Color(0xFFC92A2A))
        drawPath(p, InkColor, style = Stroke(2.0f))
    }
}
