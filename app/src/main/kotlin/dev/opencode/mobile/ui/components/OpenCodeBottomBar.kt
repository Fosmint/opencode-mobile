package dev.opencode.mobile.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import dev.opencode.mobile.ui.icons.OpenCodeIcons
import dev.opencode.mobile.ui.theme.NavRoute

private data class BottomDestination(val route: NavRoute, val label: String)

private val bottomDestinations = listOf(
    BottomDestination(NavRoute.Home, "Home"),
    BottomDestination(NavRoute.Projects, "Projects"),
    BottomDestination(NavRoute.Settings, "Settings"),
)

/**
 * Top-level bottom navigation: Home / Projects / Settings. "Sessions" from
 * the original nav sketch is reached by drilling into a project rather than
 * being its own top-level tab, since a session list is meaningless without
 * a project selected first.
 */
@Composable
fun OpenCodeBottomBar(currentRoute: String?, onNavigate: (NavRoute) -> Unit) {
    NavigationBar {
        bottomDestinations.forEach { destination ->
            val icon = when (destination.route) {
                NavRoute.Home -> OpenCodeIcons.Home
                NavRoute.Projects -> OpenCodeIcons.Folder
                else -> OpenCodeIcons.Settings
            }
            NavigationBarItem(
                selected = currentRoute == destination.route.route,
                onClick = { onNavigate(destination.route) },
                icon = { Icon(icon, contentDescription = destination.label) },
                label = { Text(destination.label) },
            )
        }
    }
}
