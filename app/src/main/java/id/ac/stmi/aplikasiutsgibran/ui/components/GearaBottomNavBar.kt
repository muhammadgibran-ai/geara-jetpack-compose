package id.ac.stmi.aplikasiutsgibran.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaOrange

@Composable
fun GearaBottomNavBar(
    navController: NavController,
    currentRoute: String
) {
    NavigationBar(
        containerColor = GearaOrange,
        contentColor = Color.White,
        tonalElevation = 0.dp,
        modifier = Modifier
            .height(68.dp)
            .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
    ) {
        val items = listOf(
            Triple("home", "Beranda", Icons.Default.Home),
            Triple("catalog", "Menu", Icons.Default.GridView),
            Triple("favorites", "Favorit", Icons.Default.FavoriteBorder),
            Triple("orders", "Pesanan", Icons.Default.Assignment),
            Triple("support", "Dukungan", Icons.Default.HeadsetMic)
        )

        items.forEach { (route, label, icon) ->
            val isSelected = currentRoute == route
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (route == "home") {
                        if (currentRoute != "home") {
                            val popped = navController.popBackStack("home", inclusive = false)
                            if (!popped) {
                                navController.navigate("home") {
                                    popUpTo(0) { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        }
                    } else if (currentRoute != route) {
                        navController.navigate(route) {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (isSelected && route == "favorites") Icons.Default.Favorite else icon,
                        contentDescription = label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    unselectedIconColor = Color.White.copy(alpha = 0.8f),
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
