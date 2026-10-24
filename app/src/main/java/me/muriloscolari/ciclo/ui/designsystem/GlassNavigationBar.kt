package me.muriloscolari.ciclo.ui.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.NightlightRound
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState

enum class NavigationTab(val route: String) {
    CALCULATE("calculate"),
    ALARMS("alarms"),
    HISTORY("history"),
    SETTINGS("settings")
}

@Composable
fun GlassNavigationBar(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    armedAlarmsCount: Int = 0
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassSurface(
            shape = RoundedCornerShape(999.dp),
            hazeState = hazeState,
            backgroundColor = Color(0x33141420),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavigationItem(
                    icon = Icons.Rounded.NightlightRound,
                    isSelected = currentTab == NavigationTab.CALCULATE,
                    onClick = { onTabSelected(NavigationTab.CALCULATE) }
                )
                NavigationItem(
                    icon = Icons.Rounded.Alarm,
                    isSelected = currentTab == NavigationTab.ALARMS,
                    hasBadge = armedAlarmsCount > 0,
                    onClick = { onTabSelected(NavigationTab.ALARMS) }
                )
                NavigationItem(
                    icon = Icons.Rounded.History,
                    isSelected = currentTab == NavigationTab.HISTORY,
                    onClick = { onTabSelected(NavigationTab.HISTORY) }
                )
                NavigationItem(
                    icon = Icons.Rounded.Settings,
                    isSelected = currentTab == NavigationTab.SETTINGS,
                    onClick = { onTabSelected(NavigationTab.SETTINGS) }
                )
            }
        }
    }
}

@Composable
private fun NavigationItem(
    icon: ImageVector,
    isSelected: Boolean,
    hasBadge: Boolean = false,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Color(0x33A78BFA) else Color.Transparent,
        animationSpec = tween(durationMillis = 250),
        label = "navItemBg"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryLavender else TextSecondary,
        animationSpec = tween(durationMillis = 250),
        label = "navItemIcon"
    )

    Box(
        modifier = Modifier
            .size(52.dp, 44.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )

        if (hasBadge) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .align(Alignment.TopEnd)
                    .padding(end = 10.dp, top = 6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF34D399))
            )
        }
    }
}
