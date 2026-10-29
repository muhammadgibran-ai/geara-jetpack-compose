package id.ac.stmi.aplikasiutsgibran

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import id.ac.stmi.aplikasiutsgibran.data.GearaViewModel
import id.ac.stmi.aplikasiutsgibran.ui.*
import id.ac.stmi.aplikasiutsgibran.ui.components.OfflineBanner
import android.os.Build
import android.content.pm.PackageManager
import id.ac.stmi.aplikasiutsgibran.ui.theme.AplikasiUTSGibranTheme

import androidx.fragment.app.FragmentActivity

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set window navigation bar and status bar colors to prevent black boundary lines
        window.navigationBarColor = android.graphics.Color.WHITE
        window.statusBarColor = android.graphics.Color.parseColor("#FFBF00") // GearaYellow
        val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightNavigationBars = true
        insetsController.isAppearanceLightStatusBars = true

        // Minta izin notifikasi runtime untuk Android 13+ (API 33)
        requestNotificationPermission()

        setContent {
            AplikasiUTSGibranTheme {
                val navController = rememberNavController()
                val gearaViewModel: GearaViewModel = hiltViewModel()
                val isOffline by gearaViewModel.isOffline.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        AnimatedVisibility(
                            visible = isOffline,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Box(modifier = Modifier.statusBarsPadding()) {
                                OfflineBanner()
                            }
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            NavHost(navController = navController, startDestination = "launch") {
                        // 1. Splash / Launch
                        composable("launch") {
                            LaunchScreen(navController)
                        }

                        // 2. Onboarding
                        composable("onboarding") {
                            OnboardingScreen(navController)
                        }

                        // 3. Auth
                        composable("login") {
                            LoginScreen(navController)
                        }
                        composable("signup") {
                            SignUpScreen(navController)
                        }

                        // 4. Main Screens
                        composable("home") {
                            HomeScreen(navController, gearaViewModel)
                        }
                        composable("catalog") {
                            CatalogScreen(navController, gearaViewModel)
                        }
                        composable("favorites") {
                            FavoritesScreen(navController, gearaViewModel)
                        }
                        composable("support") {
                            SupportScreen(navController)
                        }
                        composable("messages") {
                            SupportScreen(navController)
                        }
                        composable("orders") {
                            OrdersScreen(navController, gearaViewModel)
                        }
                        composable("profile") {
                            ProfileScreen(navController, gearaViewModel)
                        }

                        // 5. Product Detail
                        composable(
                            route = "product_detail/{productId}",
                            arguments = listOf(navArgument("productId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val productId = backStackEntry.arguments?.getString("productId") ?: ""
                            ProductDetailScreen(productId, navController, gearaViewModel)
                        }

                        // 6. Cart & Checkout Flow
                        composable("cart") {
                            CartScreen(navController, gearaViewModel)
                        }
                        composable("checkout") {
                            CheckoutScreen(navController, gearaViewModel)
                        }
                        composable("payment") {
                            PaymentScreen(navController)
                        }
                        composable(
                            route = "payment_gateway/{orderId}",
                            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                            PaymentGatewayScreen(orderId, navController, gearaViewModel)
                        }
                        composable(
                            route = "order_confirmed/{orderId}",
                            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                            OrderConfirmedScreen(orderId, navController)
                        }

                        // 7. Live Tracking
                        composable(
                            route = "live_tracking/{orderId}",
                            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                            LiveTrackingScreen(orderId, navController, gearaViewModel)
                        }

                        // 8. Notifications & Search
                        composable("notifications") {
                            NotificationScreen(navController, gearaViewModel)
                        }
                        composable("search") {
                            SearchScreen(navController, gearaViewModel)
                        }

                        // 9. Profile Drawer Pages
                        composable("addresses") {
                            AddressScreen(navController)
                        }
                        composable("payment_methods") {
                            PaymentMethodsScreen(navController)
                        }
                        composable("contact_us") {
                            ContactUsScreen(navController)
                        }
                        composable("faq") {
                            FaqScreen(navController)
                        }
                        composable("settings") {
                            SettingsScreen(navController)
                        }
                    }
                }
            }
        }
    }
}
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }
}
