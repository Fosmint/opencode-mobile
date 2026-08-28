package dev.opencode.mobile.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Custom, minimalist icon set built as Compose ImageVectors (a vector path
 * format, not a raster/bitmap or emoji glyph — satisfies the "SVG-only,
 * no PNG/JPG/emoji" requirement while being natively renderable without a
 * separate asset pipeline). Each icon uses a plain 24x24 viewport, single
 * outline stroke, no fills where possible, matching the grayscale/minimal
 * visual language.
 */
object OpenCodeIcons {

    private fun stroke(builder: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit) = builder

    val Send: ImageVector
        get() = ImageVector.Builder(
            name = "Send", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f,
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(3f, 20f); lineTo(21f, 12f); lineTo(3f, 4f); lineTo(3f, 10.5f); lineTo(14f, 12f); lineTo(3f, 13.5f); close()
        }.build()

    val Stop: ImageVector
        get() = ImageVector.Builder(
            name = "Stop", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f,
        ).path(fill = SolidColor(Color.Black)) {
            moveTo(7f, 7f); lineTo(17f, 7f); lineTo(17f, 17f); lineTo(7f, 17f); close()
        }.build()

    val Attach: ImageVector
        get() = ImageVector.Builder(
            name = "Attach", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f,
        ).path(
            fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(16.5f, 6.5f)
            lineTo(8.5f, 14.5f)
            curveTo(7.4f, 15.6f, 7.4f, 17.4f, 8.5f, 18.5f)
            curveTo(9.6f, 19.6f, 11.4f, 19.6f, 12.5f, 18.5f)
            lineTo(19.5f, 11.5f)
            curveTo(21.2f, 9.8f, 21.2f, 7.2f, 19.5f, 5.5f)
            curveTo(17.8f, 3.8f, 15.2f, 3.8f, 13.5f, 5.5f)
            lineTo(6.5f, 12.5f)
        }.build()

    val Search: ImageVector
        get() = ImageVector.Builder(
            name = "Search", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f,
        ).path(
            fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(15.5f, 15.5f); lineTo(21f, 21f)
        }.build()

    val Back: ImageVector
        get() = ImageVector.Builder(
            name = "Back", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f,
        ).path(
            fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(15f, 5f); lineTo(8f, 12f); lineTo(15f, 19f)
        }.build()

    val More: ImageVector
        get() = ImageVector.Builder(
            name = "More", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f,
        ).path(fill = SolidColor(Color.Black)) {
            moveTo(6f, 12f); arcToRelative(1.5f, 1.5f, 0f, true, true, -3f, 0f); arcToRelative(1.5f, 1.5f, 0f, true, true, 3f, 0f); close()
            moveTo(13.5f, 12f); arcToRelative(1.5f, 1.5f, 0f, true, true, -3f, 0f); arcToRelative(1.5f, 1.5f, 0f, true, true, 3f, 0f); close()
            moveTo(21f, 12f); arcToRelative(1.5f, 1.5f, 0f, true, true, -3f, 0f); arcToRelative(1.5f, 1.5f, 0f, true, true, 3f, 0f); close()
        }.build()

    val Terminal: ImageVector
        get() = ImageVector.Builder(
            name = "Terminal", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f,
        ).path(
            fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(3f, 4f); lineTo(21f, 4f); lineTo(21f, 20f); lineTo(3f, 20f); close()
            moveTo(6f, 9f); lineTo(10f, 12f); lineTo(6f, 15f)
            moveTo(12f, 15f); lineTo(17f, 15f)
        }.build()

    val Code: ImageVector
        get() = ImageVector.Builder(
            name = "Code", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f,
        ).path(
            fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(9f, 6f); lineTo(3f, 12f); lineTo(9f, 18f)
            moveTo(15f, 6f); lineTo(21f, 12f); lineTo(15f, 18f)
        }.build()

    val Git: ImageVector
        get() = ImageVector.Builder(
            name = "Git", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f,
        ).path(
            fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(6f, 4f); lineTo(6f, 14f)
            moveTo(6f, 14f); arcToRelative(3f, 3f, 0f, true, false, 0.001f, 0f)
            moveTo(6f, 4f); arcToRelative(2f, 2f, 0f, true, false, 0.001f, 0f)
            moveTo(18f, 10f); arcToRelative(2f, 2f, 0f, true, false, 0.001f, 0f)
            moveTo(9f, 6f); curveTo(13f, 6f, 18f, 6f, 18f, 10f)
        }.build()

    val AgentBot: ImageVector
        get() = ImageVector.Builder(
            name = "AgentBot", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f,
        ).path(
            fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(12f, 3f); lineTo(12f, 6f)
            moveTo(5f, 8f); lineTo(19f, 8f); lineTo(19f, 19f); lineTo(5f, 19f); close()
            moveTo(9f, 12.5f); lineTo(9f, 14f)
            moveTo(15f, 12.5f); lineTo(15f, 14f)
        }.build()

    val ErrorIcon: ImageVector
        get() = ImageVector.Builder(
            name = "ErrorIcon", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f,
        ).path(
            fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(12f, 3f); lineTo(21f, 19f); lineTo(3f, 19f); close()
            moveTo(12f, 9.5f); lineTo(12f, 13.5f)
            moveTo(12f, 16f); lineTo(12f, 16.2f)
        }.build()

    val Success: ImageVector
        get() = ImageVector.Builder(
            name = "Success", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f,
        ).path(
            fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(4f, 12.5f); lineTo(9.5f, 18f); lineTo(20f, 6f)
        }.build()

    val Home: ImageVector
        get() = ImageVector.Builder(
            name = "Home", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f,
        ).path(
            fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(4f, 11f); lineTo(12f, 4f); lineTo(20f, 11f)
            moveTo(6f, 10f); lineTo(6f, 20f); lineTo(18f, 20f); lineTo(18f, 10f)
        }.build()

    val Folder: ImageVector
        get() = ImageVector.Builder(
            name = "Folder", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f,
        ).path(
            fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(3f, 6f); lineTo(9f, 6f); lineTo(11f, 8.5f); lineTo(21f, 8.5f); lineTo(21f, 18f); lineTo(3f, 18f); close()
        }.build()

    val Chat: ImageVector
        get() = ImageVector.Builder(
            name = "Chat", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f,
        ).path(
            fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(4f, 5f); lineTo(20f, 5f); lineTo(20f, 16f); lineTo(9f, 16f); lineTo(5f, 20f); lineTo(5f, 16f); lineTo(4f, 16f); close()
        }.build()

    val Settings: ImageVector
        get() = ImageVector.Builder(
            name = "Settings", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f,
        ).path(
            fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(12f, 8.5f); arcToRelative(3.5f, 3.5f, 0f, true, false, 0.001f, 0f)
            moveTo(12f, 2.5f); lineTo(12f, 5f)
            moveTo(12f, 19f); lineTo(12f, 21.5f)
            moveTo(4.2f, 6.8f); lineTo(6f, 8.6f)
            moveTo(18f, 15.4f); lineTo(19.8f, 17.2f)
            moveTo(2.5f, 12f); lineTo(5f, 12f)
            moveTo(19f, 12f); lineTo(21.5f, 12f)
            moveTo(4.2f, 17.2f); lineTo(6f, 15.4f)
            moveTo(18f, 8.6f); lineTo(19.8f, 6.8f)
        }.build()

    val Copy: ImageVector
        get() = ImageVector.Builder(
            name = "Copy", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f,
        ).path(
            fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(8f, 8f); lineTo(19f, 8f); lineTo(19f, 19f); lineTo(8f, 19f); close()
            moveTo(5f, 16f); lineTo(5f, 5f); lineTo(16f, 5f); lineTo(16f, 8f)
        }.build()

    val Retry: ImageVector
        get() = ImageVector.Builder(
            name = "Retry", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f,
        ).path(
            fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(4f, 12f); curveTo(4f, 7.6f, 7.6f, 4f, 12f, 4f); curveTo(15.3f, 4f, 18.1f, 6f, 19.3f, 8.8f)
            moveTo(20f, 12f); curveTo(20f, 16.4f, 16.4f, 20f, 12f, 20f); curveTo(8.7f, 20f, 5.9f, 18f, 4.7f, 15.2f)
            moveTo(19.5f, 5f); lineTo(19.3f, 8.8f); lineTo(15.5f, 8.6f)
            moveTo(4.5f, 19f); lineTo(4.7f, 15.2f); lineTo(8.5f, 15.4f)
        }.build()

    val ChevronDown: ImageVector
        get() = ImageVector.Builder(
            name = "ChevronDown", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f,
        ).path(
            fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(6f, 9f); lineTo(12f, 15f); lineTo(18f, 9f)
        }.build()

    val Plus: ImageVector
        get() = ImageVector.Builder(
            name = "Plus", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f,
        ).path(
            fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(12f, 5f); lineTo(12f, 19f)
            moveTo(5f, 12f); lineTo(19f, 12f)
        }.build()

    val File: ImageVector
        get() = ImageVector.Builder(
            name = "File", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f,
        ).path(
            fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(7f, 3f); lineTo(14f, 3f); lineTo(19f, 8f); lineTo(19f, 21f); lineTo(7f, 21f); close()
            moveTo(14f, 3f); lineTo(14f, 8f); lineTo(19f, 8f)
        }.build()

    val Trash: ImageVector
        get() = ImageVector.Builder(
            name = "Trash", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f,
        ).path(
            fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(5f, 7f); lineTo(19f, 7f)
            moveTo(9f, 7f); lineTo(9f, 4.5f); lineTo(15f, 4.5f); lineTo(15f, 7f)
            moveTo(7f, 7f); lineTo(7.7f, 20f); lineTo(16.3f, 20f); lineTo(17f, 7f)
        }.build()
}
