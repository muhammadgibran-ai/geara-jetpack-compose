package id.ac.stmi.aplikasiutsgibran.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import id.ac.stmi.aplikasiutsgibran.ui.components.GearaBottomNavBar
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaDarkBrown
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaOrange
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaYellow

data class FigmaSupportMessage(
    val id: String,
    val text: String,
    val time: String,
    val isUser: Boolean,
    val optionsList: List<String> = emptyList(),
    val chips: List<String> = emptyList()
)

@Composable
fun SupportScreen(navController: NavController) {
    var inputText by remember { mutableStateOf("") }

    val messages = remember {
        mutableStateListOf(
            FigmaSupportMessage("1", "Halo tim Geara Motors!", "09:00", isUser = true),
            FigmaSupportMessage(
                "2",
                "Halo! Selamat datang di Layanan CRM After-Sales Geara Motors. Silakan pilih layanan yang Anda butuhkan:",
                "09:00",
                isUser = false
            ),
            FigmaSupportMessage(
                "3",
                "",
                "09:00",
                isUser = false,
                optionsList = listOf(
                    "1. Konsultasi Suku Cadang & Garansi Mobil Geara",
                    "2. Jadwal Servis Berkala (Periodic Maintenance)",
                    "3. Status Pengiriman Suku Cadang Resmi",
                    "4. Booking Servis Bengkel Resmi Geara",
                    "5. Bantuan Teknis Darurat (Emergency Assistance)"
                )
            ),
            FigmaSupportMessage("4", "1", "09:03", isUser = true),
            FigmaSupportMessage(
                "5",
                "Pesanan aktif suku cadang mobil Anda:\nOli Mesin Geara Fully Synthetic 0W-20 & Filter Oli Terra SUV\nNo. Pesanan: GEARA-ORD-88219\nEstimasi Tiba: Besok Pagi",
                "09:03",
                isUser = false,
                chips = listOf("Lacak Pengiriman", "Hubungi Teknisi Geara")
            )
        )
    }

    Scaffold(
        bottomBar = { GearaBottomNavBar(navController, "support") },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header: Yellow background with "<" and "Dukungan CRM Geara"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GearaYellow)
                    .padding(top = 12.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = GearaOrange
                        )
                    }
                    Text(
                        text = "Dukungan CRM Geara",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 48.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            // White Body Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(Color.White)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Chat messages list exactly matching Figma 9.5-B
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 20.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(messages) { msg ->
                            FigmaChatBubble(
                                message = msg,
                                onChipClick = { chip ->
                                    messages.add(
                                        FigmaSupportMessage(
                                            id = System.currentTimeMillis().toString(),
                                            text = chip,
                                            time = "09:05",
                                            isUser = true
                                        )
                                    )
                                }
                            )
                        }
                    }

                    // Bottom Bar on Peach Background (#FFE0D3)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFFE0D3),
                        shadowElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Attachment clip button
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AttachFile,
                                    contentDescription = "Lampiran",
                                    tint = GearaOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Text Field
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White)
                                    .padding(horizontal = 14.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                androidx.compose.foundation.text.BasicTextField(
                                    value = inputText,
                                    onValueChange = { inputText = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { innerTextField ->
                                        if (inputText.isEmpty()) {
                                            Text(
                                                text = "Tulis pesan...",
                                                color = Color(0xFFA8A8A8),
                                                fontSize = 13.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                            }

                            // Mic button
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice",
                                    tint = GearaOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Send button
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable {
                                        if (inputText.isNotBlank()) {
                                            messages.add(
                                                FigmaSupportMessage(
                                                    id = System.currentTimeMillis().toString(),
                                                    text = inputText,
                                                    time = "Sekarang",
                                                    isUser = true
                                                )
                                            )
                                            inputText = ""
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Kirim",
                                    tint = GearaOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FigmaChatBubble(
    message: FigmaSupportMessage,
    onChipClick: (String) -> Unit
) {
    val isUser = message.isUser

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (isUser) {
            // User Bubble: Soft cream/yellow background (#F7E5C8)
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 4.dp
                        )
                    )
                    .background(Color(0xFFF7E5C8))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.text,
                    fontSize = 14.sp,
                    color = GearaDarkBrown
                )
            }
        } else {
            // Bot Bubble: White background with Orange outline border (#E95322)
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = 4.dp,
                            bottomEnd = 16.dp
                        )
                    )
                    .border(1.5.dp, GearaOrange, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp))
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column {
                    if (message.text.isNotEmpty()) {
                        Text(
                            text = message.text,
                            fontSize = 13.sp,
                            color = GearaDarkBrown,
                            lineHeight = 18.sp
                        )
                    }

                    if (message.optionsList.isNotEmpty()) {
                        message.optionsList.forEach { opt ->
                            Text(
                                text = opt,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GearaDarkBrown,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    if (message.chips.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            message.chips.forEach { chip ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFFFE0D3))
                                        .clickable { onChipClick(chip) }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = chip,
                                        fontSize = 11.sp,
                                        color = GearaOrange,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = message.time,
            fontSize = 11.sp,
            color = Color(0xFFA8A8A8),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}
