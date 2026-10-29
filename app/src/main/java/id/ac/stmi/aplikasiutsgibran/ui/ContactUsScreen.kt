package id.ac.stmi.aplikasiutsgibran.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaDarkBrown
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaOrange
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaYellow
import kotlinx.coroutines.launch

@Composable
fun ContactUsScreen(navController: NavController) {
    val context = LocalContext.current
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = GearaYellow
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Top Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = GearaOrange,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Text(
                    text = "Hubungi Kami",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // White Container
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                color = Color(0xFFF9F9F9),
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 22.dp)
                ) {
                    Text(
                        text = "Layanan Pelanggan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GearaDarkBrown
                    )
                    Text(
                        text = "Hubungi tim after-sales Geara untuk bantuan suku cadang dan servis kendaraan Anda.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Contact Card 1: WhatsApp
                    ContactChannelCard(
                        icon = Icons.Default.Chat,
                        title = "WhatsApp Customer Care",
                        detail = "+62 812-8888-4321",
                        badge = "Respon cepat",
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/6281288884321"))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Contact Card 2: Telepon Call Center
                    ContactChannelCard(
                        icon = Icons.Default.Phone,
                        title = "Call Center Bebas Pulsa",
                        detail = "1500-888 / (021) 5098-7777",
                        badge = "24 Jam",
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:1500888"))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Contact Card 3: Email
                    ContactChannelCard(
                        icon = Icons.Default.Email,
                        title = "Email Dukungan",
                        detail = "support@geara.co.id",
                        badge = "Email",
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@geara.co.id"))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Contact Card 4: Kantor Pusat
                    ContactChannelCard(
                        icon = Icons.Default.LocationOn,
                        title = "Kantor & Showroom",
                        detail = "Menara Geara, Jl. MH Thamrin No. 28, Jakarta Pusat",
                        badge = "Kunjungan",
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=Menara+Geara+Jakarta"))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Form Kirim Pesan Cepat
                    Text(
                        text = "Kirim Pesan",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = GearaDarkBrown
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Topik kendala (misal: Garansi suku cadang)") },
                        placeholder = { Text("misal: Garansi Aki Hybrid") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Isi Pesan") },
                        placeholder = { Text("Jelaskan kendala atau pertanyaan yang ingin disampaikan...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (subject.isNotBlank() && message.isNotBlank()) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Pesan terkirim. Tim After-Sales akan segera menghubungi Anda.")
                                }
                                subject = ""
                                message = ""
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Mohon isi topik dan pesan terlebih dahulu.")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GearaOrange)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kirim Pesan",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun ContactChannelCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    detail: String,
    badge: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF7ED)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = GearaOrange,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GearaDarkBrown
                    )
                    Text(
                        text = detail,
                        fontSize = 12.sp,
                        color = Color(0xFF555555)
                    )
                }
            }

            Surface(
                color = GearaOrange.copy(alpha = 0.12f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = badge,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = GearaOrange,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

