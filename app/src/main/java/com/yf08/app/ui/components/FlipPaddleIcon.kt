package com.yf08.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 翻转拍产品图标：顶部圆形转盘（四孔镜片十字排列 + 中心旋钮）+ 下方长方手柄
 */
@Composable
fun FlipPaddleIcon(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    color: Color = Color(0xFF0F172A),
    strokeWidth: Dp = 3.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = size.toPx()
        val cx = w / 2
        val discR = w * 0.32f
        val discCy = w * 0.38f
        val stroke = strokeWidth.toPx()

        // 转盘外圈
        drawCircle(color = color, radius = discR, center = Offset(cx, discCy), style = Stroke(stroke))
        // 中心旋钮
        drawCircle(color = color, radius = discR * 0.18f, center = Offset(cx, discCy), style = Stroke(stroke))
        // 四个镜片孔（十字排列）
        val holeR = discR * 0.22f
        val offset = discR * 0.55f
        drawCircle(color = color, radius = holeR, center = Offset(cx, discCy - offset), style = Stroke(stroke))
        drawCircle(color = color, radius = holeR, center = Offset(cx, discCy + offset), style = Stroke(stroke))
        drawCircle(color = color, radius = holeR, center = Offset(cx - offset, discCy), style = Stroke(stroke))
        drawCircle(color = color, radius = holeR, center = Offset(cx + offset, discCy), style = Stroke(stroke))

        // 手柄
        val handleW = w * 0.16f
        val handleTop = discCy + discR + stroke * 0.5f
        val handleBottom = w * 0.92f
        val handlePath = Path().apply {
            moveTo(cx - handleW / 2, handleTop)
            lineTo(cx - handleW / 2, handleBottom)
            lineTo(cx + handleW / 2, handleBottom)
            lineTo(cx + handleW / 2, handleTop)
        }
        drawPath(handlePath, color = color, style = Stroke(stroke, cap = StrokeCap.Round))
        // 手柄底部圆角
        drawLine(
            color = color,
            start = Offset(cx - handleW / 2, handleBottom),
            end = Offset(cx + handleW / 2, handleBottom),
            strokeWidth = stroke, cap = StrokeCap.Round
        )
    }
}
