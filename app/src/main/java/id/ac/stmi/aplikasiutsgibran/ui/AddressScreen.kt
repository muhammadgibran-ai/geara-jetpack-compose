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
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaDarkBrown
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaOrange
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaYellow

data class DeliveryAddress(
    val id: String,
    val label: String,
    val recipientName: String,
    val phone: String,
    val fullAddress: String,
    val cityPostal: String,
    val isPrimary: Boolean
)

@Composable
fun AddressScreen(navController: NavController) {
    var addresses by remember {
        mutableStateOf(
            listOf(
                DeliveryAddress(
                    id = "addr_1",
                    label = "Rumah",
                    recipientName = "Gibran Rakabuming",
                    phone = "0812-3456-7890",
                    fullAddress = "Jl. Sudirman Kav. 45, Apartemen Senayan Tower B No. 12A",
                    cityPostal = "Jakarta Selatan, DKI Jakarta 12190",
                    isPrimary = true
                ),
                DeliveryAddress(
                    id = "addr_2",
                    label = "Kantor & Bengkel Geara",
                    recipientName = "Gibran (Office)",
                    phone = "0811-9988-7766",
                    fullAddress = "Menara Geara Lantai 8, Jl. Gatot Subroto No. 18",
                    cityPostal = "Jakarta Selatan, DKI Jakarta 12930",
                    isPrimary = false
                )
            )
        )
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var newLabel by remember { mutableStateOf("") }
    var newName by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }
    var newAddress by remember { mutableStateOf("") }
    var newCity by remember { mutableStateOf("") }

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
            // Top Bar
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
                    text = "Alamat Pengiriman",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Main White Container
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
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Text(
                        text = "Alamat Tersimpan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GearaDarkBrown
                    )
                    Text(
                        text = "Pilih alamat utama atau simpan alamat baru untuk pengiriman.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(addresses, key = { it.id }) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // Set as primary on click
                                        addresses = addresses.map {
                                            it.copy(isPrimary = it.id == item.id)
                                        }
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (item.isPrimary) Color(0xFFFFF7ED) else Color.White
                                ),
                                border = if (item.isPrimary) {
                                    androidx.compose.foundation.BorderStroke(1.5.dp, GearaOrange)
                                } else {
                                    androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E5E5))
                                },
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(if (item.isPrimary) GearaOrange else Color(0xFFE5E5E5)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.LocationOn,
                                                    contentDescription = null,
                                                    tint = if (item.isPrimary) Color.White else Color.DarkGray,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = item.label,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = GearaDarkBrown
                                            )
                                        }

                                        if (item.isPrimary) {
                                            Surface(
                                                color = GearaOrange,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "Alamat Utama",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = "${item.recipientName} • ${item.phone}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = GearaDarkBrown
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.fullAddress,
                                        fontSize = 12.sp,
                                        color = Color(0xFF555555),
                                        lineHeight = 16.sp
                                    )
                                    Text(
                                        text = item.cityPostal,
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        if (!item.isPrimary) {
                                            TextButton(onClick = {
                                                addresses = addresses.map { it.copy(isPrimary = it.id == item.id) }
                                            }) {
                                                Text("Jadikan Utama", color = GearaOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        TextButton(onClick = {
                                            addresses = addresses.filter { it.id != item.id }
                                        }) {
                                            Text("Hapus", color = Color.Red, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tombol Tambah Alamat Baru
                    Button(
                        onClick = { showAddDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GearaOrange)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tambah Alamat Baru",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "Tambah Alamat Baru",
                    fontWeight = FontWeight.Bold,
                    color = GearaDarkBrown
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newLabel,
                        onValueChange = { newLabel = it },
                        label = { Text("Label alamat (misal: Rumah, Kantor)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Nama Penerima") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("Nomor Ponsel") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newAddress,
                        onValueChange = { newAddress = it },
                        label = { Text("Alamat Lengkap & Patokan Jalan") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newCity,
                        onValueChange = { newCity = it },
                        label = { Text("Kota & Kode Pos") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank() && newAddress.isNotBlank()) {
                            val newAddr = DeliveryAddress(
                                id = "addr_${System.currentTimeMillis()}",
                                label = if (newLabel.isBlank()) "Alamat Baru" else newLabel,
                                recipientName = newName,
                                phone = if (newPhone.isBlank()) "0812-3456-7890" else newPhone,
                                fullAddress = newAddress,
                                cityPostal = if (newCity.isBlank()) "DKI Jakarta" else newCity,
                                isPrimary = addresses.isEmpty()
                            )
                            addresses = addresses + newAddr
                            showAddDialog = false
                            newLabel = ""
                            newName = ""
                            newPhone = ""
                            newAddress = ""
                            newCity = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GearaOrange)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

