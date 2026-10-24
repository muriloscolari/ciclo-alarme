package me.muriloscolari.ciclo.ui.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    hazeState: HazeState? = null,
    backgroundColor: Color = GlassFill,
    borderStroke: BorderStroke? = null,
    borderColor: Color? = null,
    borderBrush: Brush? = null,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val blurEnabled = LocalBlurEnabled.current

    var boxModifier = modifier.clip(shape)

    boxModifier = if (hazeState != null && blurEnabled) {
        boxModifier.hazeChild(
            state = hazeState,
            style = dev.chrisbanes.haze.HazeDefaults.style(
                backgroundColor = backgroundColor,
                blurRadius = 24.dp
            )
        )
    } else {
        boxModifier.background(
            if (blurEnabled) backgroundColor else DarkSurfaceElevated.copy(alpha = 0.85f)
        )
    }

    val effectiveBorderStroke = when {
        borderColor != null -> BorderStroke(borderWidth, borderColor)
        borderStroke != null -> borderStroke
        borderBrush != null -> null
        else -> BorderStroke(borderWidth, GlassBorder)
    }

    if (borderBrush != null) {
        boxModifier = boxModifier.border(borderWidth, borderBrush, shape)
    } else if (effectiveBorderStroke != null) {
        boxModifier = boxModifier.border(effectiveBorderStroke, shape)
    }

    if (onClick != null) {
        boxModifier = boxModifier.clickable { onClick() }
    }

    Box(
        modifier = boxModifier
    ) {
        content()
    }
}
