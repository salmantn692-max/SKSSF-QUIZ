package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AdminNavTab
import com.example.ui.theme.*

data class NavItem(
    val tab: AdminNavTab,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val NAV_ITEMS = listOf(
    NavItem(AdminNavTab.DASHBOARD, "ഹോം", Icons.Filled.Home, Icons.Outlined.Home),
    NavItem(AdminNavTab.STAGES, "റൗണ്ടുകൾ", Icons.Filled.ViewCarousel, Icons.Outlined.ViewCarousel),
    NavItem(AdminNavTab.QUESTIONS, "ചോദ്യങ്ങൾ", Icons.Filled.Quiz, Icons.Outlined.Quiz),
    NavItem(AdminNavTab.STUDENTS, "മത്സരാർത്ഥികൾ", Icons.Filled.People, Icons.Outlined.People),
    NavItem(AdminNavTab.RESULTS, "ഫലങ്ങൾ", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents),
    NavItem(AdminNavTab.BRANDING, "തീം", Icons.Filled.Palette, Icons.Outlined.Palette),
    NavItem(AdminNavTab.SIMULATOR, "സിമുലേറ്റർ", Icons.Filled.PlayCircle, Icons.Outlined.PlayCircleOutline)
)

@Composable
fun AdminBottomNavigationBar(
    currentTab: AdminNavTab,
    onTabSelected: (AdminNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = BentoSurfaceContainer,
        tonalElevation = 0.dp
    ) {
        NAV_ITEMS.forEach { item ->
            val isSelected = currentTab == item.tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(item.tab) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = BentoPastelPurple,
                    selectedIconColor = BentoDeepPurple,
                    selectedTextColor = BentoDeepPurple,
                    unselectedIconColor = BentoOnSurfaceVariant.copy(alpha = 0.7f),
                    unselectedTextColor = BentoOnSurfaceVariant.copy(alpha = 0.7f)
                ),
                alwaysShowLabel = true
            )
        }
    }
}

@Composable
fun AdminNavigationRail(
    currentTab: AdminNavTab,
    onTabSelected: (AdminNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier.fillMaxHeight(),
        containerColor = BentoSurfaceContainer,
        header = {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BentoPrimary,
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .size(44.dp)
            ) {
                Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text(
                        text = "ക്വി",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    ) {
        NAV_ITEMS.forEach { item ->
            val isSelected = currentTab == item.tab
            NavigationRailItem(
                selected = isSelected,
                onClick = { onTabSelected(item.tab) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    indicatorColor = BentoPastelPurple,
                    selectedIconColor = BentoDeepPurple,
                    selectedTextColor = BentoDeepPurple,
                    unselectedIconColor = BentoOnSurfaceVariant.copy(alpha = 0.7f),
                    unselectedTextColor = BentoOnSurfaceVariant.copy(alpha = 0.7f)
                )
            )
        }
    }
}
