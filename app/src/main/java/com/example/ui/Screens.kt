package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.graphicsLayer
import com.example.R
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import coil.compose.SubcomposeAsyncImage
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.ErrorRed
import com.example.data.AuditLogEntity
import com.example.data.BookEntity
import com.example.data.UserEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Intent

@Composable
fun WelcomeDashboardScreen(
    viewModel: AppViewModel,
    onStartApp: () -> Unit,
    onLoginWithCard: () -> Unit = onStartApp
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFEEF2FF),
                        Color(0xFFF8FAFC),
                        Color.White
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // School Logo Wrapper
            Surface(
                modifier = Modifier
                    .size(90.dp)
                    .shadow(8.dp, CircleShape),
                color = Color.White,
                shape = CircleShape,
                border = BorderStroke(2.dp, Color(0xFFEFF6FF))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_alkhairaat),
                    contentDescription = "Logo MTs Al-Khairaat Bunyu",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title Header
            Text(
                text = "Sistem Pembelajaran Terpadu MTs Al-Khairaat Bunyu",
                color = Color(0xFF1E3A8A),
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                lineHeight = 26.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Perpustakaan Digital Terintegrasi Sekolah",
                color = Color(0xFF64748B),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Official Badge Pill
            Surface(
                color = Color(0xFFEFF6FF),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFFBFDBFE))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Color(0xFF1E3A8A),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Akses Resmi Akun Sekolah",
                        color = Color(0xFF1E3A8A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Pilih Peran Login Anda",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // CARD 1: SISWA
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_student_access"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.5.dp, Color(0xFF1E3A8A).copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            color = Color(0xFFDBEAFE)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = Color(0xFF1E3A8A),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Surface(
                                color = Color(0xFFDBEAFE),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "SISWA KELAS VII, VIII, IX",
                                    color = Color(0xFF1E3A8A),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Kartu Akses Siswa",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B),
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Masuk menggunakan Nama Siswa dan Sandi Kelas dari Kartu Login madrasah untuk mengakses buku pelajaran kelas Anda.",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onLoginWithCard,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_masuk_akun_siswa"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Login,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "MASUK AKUN SISWA",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CARD 2: GURU & MAJELIS PENGAJAR
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_teacher_access"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.5.dp, Color(0xFFD97706).copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            color = Color(0xFFFFEDD5)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = null,
                                    tint = Color(0xFFEA580C),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Surface(
                                color = Color(0xFFFFF7ED),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "GURU & MAJELIS PENGAJAR",
                                    color = Color(0xFFC2410C),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Kartu Akses Guru & Pengajar",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B),
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Masuk menggunakan Akun Guru / Pengajar untuk membuka koleksi pegangan guru & modul pengajaran mata pelajaran Anda.",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onLoginWithCard,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_masuk_akun_guru"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Article,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "MASUK AKUN GURU",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CARD 3: PORTAL GURU & OPERATOR
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_portal_operator"),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFEF3C7).copy(alpha = 0.5f),
                border = BorderStroke(1.5.dp, Color(0xFFF59E0B))
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = "⚙️ Portal Guru & Operator",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B),
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Kelola/Unggah Buku & Impor Akun Excel.",
                                color = Color(0xFF64748B),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onLoginWithCard,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Buka Portal",
                            color = Color(0xFF78350F),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "MTs Al-Khairaat Bunyu • Sistem E-Book Resmi",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DashboardNotificationsHeader(
    viewModel: AppViewModel,
    booksCount: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val updateInfo = viewModel.appUpdateInfo
    val isCheckingUpdate = viewModel.isCheckingUpdate
    val isSyncing = viewModel.isSyncingCatalog

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Notifikasi Pembaharuan Aplikasi (Awal Dashboard)
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
            ),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth().testTag("top_app_update_card")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = "Update App",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (updateInfo?.hasUpdate == true) 
                                "🎉 Versi Baru Aplikasi Sekolah (v${updateInfo.latestVersionName})" 
                            else 
                                "⚡ Notifikasi Pembaharuan Aplikasi",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = if (updateInfo?.hasUpdate == true)
                                updateInfo.releaseNotes.ifBlank { "Tersedia pembaruan sistem aplikasi sekolah." }
                            else
                                "Sistem v1.0.0 siap & terhubung dengan Cloudflare.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (updateInfo?.hasUpdate == true && updateInfo.apkUrl.isNotBlank()) {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.apkUrl))
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                android.widget.Toast.makeText(context, "Membuka halaman unduh update...", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            viewModel.checkAppUpdate()
                            android.widget.Toast.makeText(context, "Memeriksa pembaruan aplikasi ke Cloudflare...", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp).testTag("btn_top_app_update")
                ) {
                    if (isCheckingUpdate) {
                        CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = if (updateInfo?.hasUpdate == true) "Perbarui" else "Cek Update",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 2. Notifikasi Buku & Katalog Terbaru (Awal Dashboard)
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.95f)
            ),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth().testTag("top_latest_books_card")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Buku Terbaru",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "📚 Notifikasi Buku & Katalog Terbaru",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = if (isSyncing) "Menyinkronkan buku dari Portal Admin..." else "$booksCount buku pelajaran siap dibaca.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        viewModel.syncCatalogFromCloudflare()
                        android.widget.Toast.makeText(context, "Menyinkronkan katalog buku terbaru...", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp).testTag("btn_top_sync_books")
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text("Muat Buku", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DashboardNotificationsFooter(
    viewModel: AppViewModel,
    booksCount: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val updateInfo = viewModel.appUpdateInfo
    val isCheckingUpdate = viewModel.isCheckingUpdate
    val isSyncing = viewModel.isSyncingCatalog

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Notifikasi Pembaharuan Aplikasi (Bagian Paling Bawah)
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
            ),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth().testTag("bottom_app_update_card")
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = "Update App Bottom",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "⚡ Notifikasi Pembaharuan Aplikasi",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = if (updateInfo?.hasUpdate == true)
                                "Versi ${updateInfo.latestVersionName} tersedia untuk diunduh."
                            else
                                "Status: Versi aplikasi v1.0.0 aktif & terhubung ke server.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (updateInfo?.hasUpdate == true && updateInfo.apkUrl.isNotBlank()) {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.apkUrl))
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                android.widget.Toast.makeText(context, "Membuka link download APK...", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            viewModel.checkAppUpdate()
                            android.widget.Toast.makeText(context, "Pemeriksaan pembaruan aplikasi dijalankan.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp).testTag("btn_bottom_check_update")
                ) {
                    if (isCheckingUpdate) {
                        CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = if (updateInfo?.hasUpdate == true) "Unduh Update" else "Cek Update",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Notifikasi Buku & Katalog Terbaru (Bagian Paling Bawah)
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f)
            ),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth().testTag("bottom_latest_books_card")
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Latest Books Bottom",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "📚 Notifikasi Buku & Katalog Terbaru",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Katalog saat ini: $booksCount buku terdaftar di Cloudflare.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        viewModel.syncCatalogFromCloudflare()
                        android.widget.Toast.makeText(context, "Sinkronisasi buku terbaru diproses...", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp).testTag("btn_bottom_sync_books")
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text("Refresh Buku", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BookDashboard(
    viewModel: AppViewModel,
    user: UserEntity,
    onBookSelected: (BookEntity) -> Unit,
    onOpenLogin: () -> Unit = {}
) {
    val books by viewModel.filteredBooks.collectAsState()
    var showUploadDialog by remember { mutableStateOf(false) }
    var bookToDelete by remember { mutableStateOf<BookEntity?>(null) }

    val isTeacher = user.role == "TEACHER" || user.role == "ADMIN"
    val isNamedStudent = !isTeacher && user.id != "usr_student_default"

    Scaffold(
        floatingActionButton = {
            if (isTeacher) {
                ExtendedFloatingActionButton(
                    onClick = { showUploadDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.testTag("add_book_fab"),
                    icon = { Icon(Icons.Default.CloudUpload, contentDescription = "Unggah Buku") },
                    text = { Text("Unggah Buku Baru", fontWeight = FontWeight.Bold) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Elegant Welcome Header Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isTeacher) "Mode Guru (Pengajar)" else if (isNamedStudent) "Assalamu'alaikum, ${user.name}" else "Perpustakaan Digital Siswa",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isTeacher) MaterialTheme.colorScheme.secondary else Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier.testTag("user_role_tag")
                                ) {
                                    Text(
                                        text = if (isTeacher) "PENGELOLA KURIKULUM & MATERI" else "SISWA ${user.gradeClass.uppercase()}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isTeacher) MaterialTheme.colorScheme.onSecondary else Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                // Quick button to open Login / Switch Account
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.White.copy(alpha = 0.28f),
                                    modifier = Modifier
                                        .clickable { onOpenLogin() }
                                        .testTag("btn_quick_user_login")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isNamedStudent) "Ganti Akun" else "Masuk Kartu",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        Icon(
                            imageVector = if (isTeacher) Icons.Default.School else Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = "Role Icon",
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(36.dp).padding(start = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Searching text input field
                    OutlinedTextField(
                        value = viewModel.searchQuery,
                        onValueChange = { viewModel.searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_books_input"),
                        placeholder = { Text("Cari Judul, Pengarang, Mapel...", color = Color.LightGray) },
                        leadingIcon = { Icon(Icons.Default.Search, "Search Icon", tint = Color.LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            focusedContainerColor = Color.White.copy(alpha = 0.12f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.08f)
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Notifikasi pada awal dashboard aplikasi
            DashboardNotificationsHeader(
                viewModel = viewModel,
                booksCount = books.size,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Filters selector header row (matching Ruang Baca Guru in Admin Portal)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Saring Jenjang Kelas & Kategori",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isSubjectLockedTeacher = user.role == "TEACHER" && user.gradeClass.startsWith("Mapel:")
                    val isStudentLocked = !isTeacher && user.gradeClass.isNotBlank() && user.gradeClass != "Semua Kelas"

                    if (isStudentLocked || isSubjectLockedTeacher) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isSubjectLockedTeacher) "Katalog Dikunci Khusus ${user.gradeClass}" else "Katalog Dikunci Khusus ${user.gradeClass}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    } else {
                        val grades = listOf("Semua Kelas", "Kelas VII", "Kelas VIII", "Kelas IX")
                        grades.forEach { grade ->
                            val isSelected = if (grade == "Semua Kelas") {
                                viewModel.selectedGradeFilter.isEmpty() || viewModel.selectedGradeFilter == "Semua Kelas"
                            } else {
                                viewModel.selectedGradeFilter == grade
                            }
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    viewModel.selectedGradeFilter = if (grade == "Semua Kelas") "" else grade
                                },
                                label = { Text(grade, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    // Kategori Filter chip picker
                    FilterChip(
                        selected = viewModel.selectedCategoryFilter.isNotEmpty(),
                        onClick = {
                            viewModel.selectedCategoryFilter = when (viewModel.selectedCategoryFilter) {
                                "" -> "Mata Pelajaran"
                                "Mata Pelajaran" -> "Referensi Tambahan"
                                else -> ""
                            }
                        },
                        label = {
                            Text(
                                if (viewModel.selectedCategoryFilter.isEmpty()) "Semua Kategori" else viewModel.selectedCategoryFilter,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            // Reassuring guidance banner for students
            if (!isTeacher) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Katalog terhubung dengan Portal Admin & Guru. Buku otomatis diperbarui dan dapat dibaca langsung.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Books items collection list or grid based on container screen
            if (books.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = "Empty",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Belum Ada Buku Tersedia",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (viewModel.selectedGradeFilter.isNotEmpty())
                                "Tidak ada buku untuk ${viewModel.selectedGradeFilter}. Coba pilih 'Semua Kelas' atau sinkronkan dengan Portal Admin."
                            else
                                "Buku bawaan dikosongkan. Buku yang ditambahkan di Portal Admin & Guru akan otomatis muncul di sini.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (viewModel.selectedGradeFilter.isNotEmpty()) {
                                OutlinedButton(
                                    onClick = { viewModel.selectedGradeFilter = "" }
                                ) {
                                    Text("Lihat Semua Kelas")
                                }
                            }
                            Button(
                                onClick = { viewModel.syncCatalogFromCloudflare() },
                                modifier = Modifier.testTag("btn_sync_catalog")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sinkronkan Katalog")
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "MTs Al-Khairaat Bunyu • Sistem E-Book Resmi",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        DashboardNotificationsFooter(
                            viewModel = viewModel,
                            booksCount = books.size
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(160.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(books) { book ->
                        BookGridCard(
                            book = book,
                            activeDownloads = viewModel.activeDownloads.collectAsState().value,
                            onBookSelected = onBookSelected,
                            onUnduh = { viewModel.unduhBuku(book.id) },
                            onDelete = if (isTeacher) { { bookToDelete = book } } else null
                        )
                    }

                    // Notifikasi & Footer pada bagian paling bawah dashboard
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp, bottom = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "MTs Al-Khairaat Bunyu • Sistem E-Book Resmi",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            DashboardNotificationsFooter(
                                viewModel = viewModel,
                                booksCount = books.size
                            )
                        }
                    }
                }
            }
        }

        // Upload custom Book Dialog Form
        if (showUploadDialog) {
            UploadBookDialog(
                viewModel = viewModel,
                onDismiss = { showUploadDialog = false }
            )
        }

        bookToDelete?.let { book ->
            AlertDialog(
                onDismissRequest = { bookToDelete = null },
                title = { Text(text = "Hapus Buku", fontWeight = FontWeight.Bold) },
                text = { Text(text = "Apakah Anda yakin ingin menghapus buku '${book.title}'? Tindakan ini tidak dapat diurungkan.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteBook(book.id)
                        bookToDelete = null
                    }) {
                        Text("Hapus", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { bookToDelete = null }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}

@Composable
fun FilterChipButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        ),
        modifier = Modifier.height(36.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp)
        ) {
            Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = if (isSelected) Icons.Default.FilterListOff else Icons.Default.ArrowDropDown,
                contentDescription = "Drop",
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun BookGridCard(
    book: BookEntity,
    activeDownloads: List<com.example.data.DownloadTaskEntity>,
    onBookSelected: (BookEntity) -> Unit,
    onUnduh: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val downloadTask = activeDownloads.find { it.bookId == book.id }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("book_item_${book.id}")
            .clickable { onBookSelected(book) },
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Elegant Cover representation matching Ruang Baca Guru in admin.html
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1E3A8A), // deep education blue
                                Color(0xFF2563EB)
                            )
                        )
                    )
                    .padding(8.dp)
            ) {
                // Top-Start Grade Class badge (Warning Yellow)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = book.gradeClass,
                        color = Color.Black,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Top-End Subject badge (White pill)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White.copy(alpha = 0.92f),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = book.subject,
                        color = Color(0xFF1E3A8A),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (onDelete != null) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Buku",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 18.dp, start = 4.dp, end = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = "Book",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = book.title,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = book.author,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Details section representation matching Ruang Baca Guru
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = book.gradeClass,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "${book.pageCount} Hal",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Penulis: ${book.author}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (book.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = book.description,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress indicators for active download or 'Pratinjau & Baca Buku' Button
                when {
                    downloadTask != null && downloadTask.status == "DOWNLOADING" -> {
                        Column {
                            LinearProgressIndicator(
                                progress = { downloadTask.progress / 100f },
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.fillMaxWidth().height(4.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Mengunduh ${downloadTask.progress}%",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    else -> {
                        Button(
                            onClick = { onBookSelected(book) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E3A8A),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (book.isDownloaded) "Buka Buku" else "Pratinjau & Baca",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MyBooksScreen(
    viewModel: AppViewModel,
    user: UserEntity,
    onBookSelected: (BookEntity) -> Unit
) {
    val books by viewModel.filteredBooks.collectAsState()
    val offlineBooks = books.filter { it.isDownloaded }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Koleksi Offline Saya",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Baca buku sekolah Anda kapan saja tanpa memerlukan kouta internet.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (offlineBooks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = "Empty",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Belum Ada Buku Offline",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Silahkan unduh buku pelajaran Anda lewat saringan utama di laman Dashboard.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(offlineBooks) { book ->
                    OfflineBookRow(
                        book = book,
                        onOpen = { onBookSelected(book) },
                        onDelete = { viewModel.hapusOfflineCache(book.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun OfflineBookRow(
    book: BookEntity,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.LibraryBooks, contentDescription = "Book", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${book.subject} • Kelas ${book.gradeClass}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(onClick = onOpen) {
                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Baca", tint = MaterialTheme.colorScheme.primary)
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus Offline", tint = ErrorRed)
            }
        }
    }
}

@Composable
fun AuditLogsScreen(
    viewModel: AppViewModel
) {
    val logs by viewModel.auditLogs.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Audit Log Perpustakaan",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Laporan log audit keamanan, login, unggah berkas, dan aktivitas sistem.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(logs) { log ->
                AuditLogRow(log = log, formatter = dateFormat)
            }
        }
    }
}

@Composable
fun AuditLogRow(log: AuditLogEntity, formatter: SimpleDateFormat) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = when (log.action) {
                        "LOGIN" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        "UPLOAD_SUKSES" -> SuccessGreen.copy(alpha = 0.15f)
                        "UNDUH_SUKSES" -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    }
                ) {
                    Text(
                        text = log.action,
                        color = when (log.action) {
                            "LOGIN" -> MaterialTheme.colorScheme.primary
                            "UPLOAD_SUKSES" -> SuccessGreen
                            "UNDUH_SUKSES" -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = formatter.format(Date(log.timestamp)),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = log.details,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Oleh: ${log.userName} (${log.userId})",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun UploadBookDialog(
    viewModel: AppViewModel,
    onDismiss: () -> Unit
) {
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedUri = uri
        if (uri != null) {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    selectedFileName = cursor.getString(nameIndex)
                    if (viewModel.uploadTitle.isEmpty()) {
                        viewModel.uploadTitle = selectedFileName?.substringBeforeLast(".") ?: ""
                    }
                    if (selectedFileName?.endsWith(".docx", ignoreCase = true) == true) {
                        viewModel.uploadFileExt = "docx"
                    } else {
                        viewModel.uploadFileExt = "pdf"
                    }
                }
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Unggah & Konversi Buku Baru",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Operator sekolah dapat menyisipkan file PDF atau file DOCX untuk dikonversi otomatis.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (viewModel.isUploading) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(progress = { viewModel.uploadProgress / 100f })
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Memproses Berkas: ${viewModel.uploadProgress}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (viewModel.uploadFileExt == "docx")
                                "Menjalankan LibreOffice Headless & Menstabilkan Struktur..."
                            else "Memvalidasi Metadata & Menyimpan File...",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = { launcher.launch("*/*") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = "Pilih File")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(selectedFileName ?: "Pilih Berkas PDF dari Perangkat")
                    }

                    OutlinedTextField(
                        value = viewModel.uploadSubject,
                        onValueChange = { viewModel.uploadSubject = it },
                        label = { Text("Mata Pelajaran (cth: IPA)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )

                    var expanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        OutlinedTextField(
                            value = if (viewModel.uploadGrade.isEmpty()) "Pilih Kelas" else viewModel.uploadGrade,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Jenjang Kelas") },
                            trailingIcon = {
                                IconButton(onClick = { expanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Pilih Kelas")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                                .clickable { expanded = true }
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            listOf("Kelas VII", "Kelas VIII", "Kelas IX", "Semua Kelas").forEach { grade ->
                                DropdownMenuItem(
                                    text = { Text(grade) },
                                    onClick = {
                                        viewModel.uploadGrade = grade
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (errorMsg != null) {
                        Text(
                            text = errorMsg ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Batal")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.performUpload(
                                    fileUri = selectedUri,
                                    onSuccess = { onDismiss() },
                                    onError = { errorMsg = it }
                                )
                            }
                        ) {
                            Text("Unggah & Proses")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    book: BookEntity,
    viewModel: AppViewModel,
    onRead: () -> Unit,
    onBack: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val currentUser by viewModel.loggedInUser.collectAsState()
    val isTeacher = currentUser?.role == "TEACHER" || currentUser?.role == "ADMIN"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Buku Pelajaran", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_detail")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    if (isTeacher) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus Buku", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text(text = "Hapus Buku", fontWeight = FontWeight.Bold) },
                text = { Text(text = "Apakah Anda yakin ingin menghapus buku '${book.title}'? Tindakan ini tidak dapat diurungkan.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteBook(book.id)
                        showDeleteConfirm = false
                        onBack()
                    }) {
                        Text("Hapus", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("Batal")
                    }
                }
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Simulated cover
            Surface(
                modifier = Modifier
                    .size(120.dp, 170.dp)
                    .shadow(4.dp, RoundedCornerShape(8.dp)),
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(16.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Book, contentDescription = "Cover", tint = Color.White, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = book.title,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            maxLines = 4
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = book.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Karya: ${book.author}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Metadata card grid list parameters
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Deskripsi Buku:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = book.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetadataPair("Kelas", book.gradeClass)
                        MetadataPair("Kategori", book.category)
                        MetadataPair("Tahun", book.year)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetadataPair("Mata Pelajaran", book.subject)
                        MetadataPair("Operator Pengampu", book.teacherOwner)
                        MetadataPair("Halaman", "${book.pageCount} Hal")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Large CTA Read button
            Button(
                onClick = onRead,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("read_book_detail_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Read")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "BACA SEKARANG",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            val context = LocalContext.current
            OutlinedButton(
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Buku Pelajaran: ${book.title}")
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "📖 *Buku Pelajaran MTs Al-Khairaat Bunyu*\n\n" +
                            "Judul: *${book.title}*\n" +
                            "Mata Pelajaran: ${book.subject} (${book.gradeClass})\n" +
                            "Pengampu: ${book.teacherOwner}\n" +
                            "Halaman: ${book.pageCount} Hal\n\n" +
                            "Buka buku ini di Aplikasi E-Book Madrasah Anda:\n${viewModel.appShareUrl}"
                        )
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Bagikan ke Siswa / WhatsApp"))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = "Bagikan ke WhatsApp", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Bagikan ke WhatsApp Siswa",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun MetadataPair(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StudentListScreen(viewModel: AppViewModel) {
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val currentUser by viewModel.loggedInUser.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(0) } // 0: Siswa, 1: Guru, 2: Admin
    var userToDelete by remember { mutableStateOf<UserEntity?>(null) }
    var userToResetPassword by remember { mutableStateOf<UserEntity?>(null) }
    var showTeacherTemplateDialog by remember { mutableStateOf(false) }
    var showStudentTemplateDialog by remember { mutableStateOf(false) }

    val filteredUsers = when (selectedTab) {
        0 -> allUsers.filter { it.role == "STUDENT" }
        1 -> allUsers.filter { it.role == "TEACHER" }
        else -> allUsers.filter { it.role == "ADMIN" }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Kelola Akun & Pengguna",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Segmented tabs to selection
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.padding(bottom = 16.dp),
            containerColor = Color.Transparent
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Siswa (${allUsers.count { it.role == "STUDENT" }})", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Guru (${allUsers.count { it.role == "TEACHER" }})", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Admin (${allUsers.count { it.role == "ADMIN" }})", fontSize = 12.sp) }
            )
        }

        // Action row for Student Template Generator
        if (selectedTab == 0 && (currentUser?.role == "ADMIN" || currentUser?.role == "TEACHER")) {
            OutlinedButton(
                onClick = { showStudentTemplateDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "📄 Template & Import Siswa (Format: nomor, nama siswa, kelas)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Action row for Teacher Template Generator
        if (selectedTab == 1 && (currentUser?.role == "ADMIN" || currentUser?.role == "TEACHER")) {
            OutlinedButton(
                onClick = { showTeacherTemplateDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "📄 Template & Import Guru (Format: nomor, nama guru)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (filteredUsers.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Belum ada akun di kategori ini.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredUsers, key = { it.id }) { user ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Icon(
                                imageVector = when (user.role) {
                                    "ADMIN" -> Icons.Default.Settings
                                    "TEACHER" -> Icons.Default.School
                                    else -> Icons.Default.Person
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (user.role == "STUDENT") {
                                        "Kelas: ${user.gradeClass.ifEmpty { "-" }} | Sandi: ${user.password}"
                                    } else {
                                        "${user.email} | ${user.gradeClass}"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Show Reset Password for students and Delete button for admin/teacher
                            if (currentUser?.role == "ADMIN" || currentUser?.role == "TEACHER") {
                                if (user.role == "STUDENT") {
                                    IconButton(
                                        onClick = { userToResetPassword = user },
                                        modifier = Modifier.testTag("reset_password_user_${user.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Key,
                                            contentDescription = "Reset Password Siswa",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                if (currentUser?.id != user.id) {
                                    IconButton(
                                        onClick = { userToDelete = user },
                                        modifier = Modifier.testTag("delete_user_${user.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Hapus Akun",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (userToDelete != null) {
        val u = userToDelete!!
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Hapus Akun?") },
            text = { Text("Apakah Anda yakin ingin menghapus akun ${u.name} (${u.role})? Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteUser(u.id)
                        userToDelete = null
                    }
                ) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }

    if (userToResetPassword != null) {
        ResetStudentPasswordDialog(
            studentUser = userToResetPassword!!,
            viewModel = viewModel,
            onDismiss = { userToResetPassword = null },
            onSuccess = {
                userToResetPassword = null
            }
        )
    }

    if (showTeacherTemplateDialog) {
        TeacherBatchTemplateDialog(
            viewModel = viewModel,
            onDismiss = { showTeacherTemplateDialog = false }
        )
    }

    if (showStudentTemplateDialog) {
        StudentBatchTemplateDialog(
            viewModel = viewModel,
            onDismiss = { showStudentTemplateDialog = false }
        )
    }
}

@Composable
fun TeacherBatchTemplateDialog(
    viewModel: AppViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var inputExcelText by remember {
        mutableStateOf(
            "nomor\tnama guru\n" +
            "1\tGURU BAHASA ARAB\n" +
            "2\tGURU INFORMATIKA\n" +
            "3\tGURU AL-QURAN HADIS\n" +
            "4\tGURU AKIDAH AKHLAK\n" +
            "5\tGURU FIQIH\n" +
            "6\tGURU SKI"
        )
    }
    var selectedXlsxFileName by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isImporting by remember { mutableStateOf(false) }

    val createTeacherTemplateLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val csvContent = "nomor,nama guru\n1,GURU BAHASA ARAB\n2,GURU INFORMATIKA\n3,GURU AL-QURAN HADIS\n4,GURU AKIDAH AKHLAK\n5,GURU FIQIH\n6,GURU SKI\n"
                    outputStream.write(csvContent.toByteArray(Charsets.UTF_8))
                }
                statusMessage = "🎉 File Template Excel Guru berhasil disimpan ke HP Anda!"
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = "Gagal menyimpan file template: ${e.localizedMessage}"
            }
        }
    }

    val xlsxPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        selectedXlsxFileName = cursor.getString(nameIndex)
                    }
                }
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    isImporting = true
                    viewModel.importTeacherBatchFromXlsx(
                        inputStream = inputStream,
                        onSuccess = { count ->
                            isImporting = false
                            statusMessage = "🎉 Berhasil mengimpor $count akun guru dari file Excel '${selectedXlsxFileName ?: "XLSX"}'!"
                            errorMessage = null
                        },
                        onError = { err ->
                            isImporting = false
                            errorMessage = err
                        }
                    )
                }
            } catch (e: Exception) {
                isImporting = false
                errorMessage = "Gagal membaca file Excel: ${e.localizedMessage}"
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Badge,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Template Excel (.xlsx) Akun Guru", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "📊 Format Kolom File Excel (.xlsx):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Kolom A: nomor",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Kolom B: nama guru",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• Baris 1: Header 'nomor' (Kolom A) dan 'nama guru' (Kolom B).\n• Baris 2 dst: Data nomor & nama guru (gelar bersifat opsional).\n• Password awal otomatis disetel ke '2026'.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Button 0: Download Excel/CSV Template File
                OutlinedButton(
                    onClick = { createTeacherTemplateLauncher.launch("Template_Guru_MTs_AlKhairaat.csv") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("📥 Unduh Template Excel Guru (.xlsx / .csv)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Button 1: File picker for real .xlsx file
                Button(
                    onClick = { xlsxPickerLauncher.launch("*/*") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("📂 Unggah File Excel (.xlsx)", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Button 2: Copy Excel format
                OutlinedButton(
                    onClick = {
                        val excelFormat = "nomor\tnama guru\n1\tGURU BAHASA ARAB\n2\tGURU INFORMATIKA\n3\tGURU AL-QURAN HADIS\n4\tGURU AKIDAH AKHLAK\n5\tGURU FIQIH\n6\tGURU SKI"
                        clipboardManager.setText(AnnotatedString(excelFormat))
                        statusMessage = "✅ Format Tabel Excel 'nomor | nama guru' berhasil disalin!"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Salin Format Tabel Excel (nomor, nama guru)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Atau Tempel (Paste) Sel dari Microsoft Excel:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = inputExcelText,
                    onValueChange = {
                        inputExcelText = it
                        statusMessage = null
                        errorMessage = null
                    },
                    minLines = 5,
                    maxLines = 8,
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (statusMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(statusMessage!!, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (inputExcelText.isBlank()) {
                        errorMessage = "Data teks Excel tidak boleh kosong!"
                        return@Button
                    }
                    isImporting = true
                    viewModel.importTeacherBatch(
                        csvText = inputExcelText,
                        onSuccess = { count ->
                            isImporting = false
                            statusMessage = "🎉 Berhasil mengimpor $count akun guru dari data Excel!"
                        },
                        onError = { err ->
                            isImporting = false
                            errorMessage = err
                        }
                    )
                },
                enabled = !isImporting
            ) {
                if (isImporting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mengimpor...")
                } else {
                    Text("Import Teks Excel")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
fun StudentBatchTemplateDialog(
    viewModel: AppViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var inputExcelText by remember {
        mutableStateOf(
            "nomor\tnama siswa\tkelas\n" +
            "1\tAHMAD FAUZI\tKelas VII\n" +
            "2\tAQEELA ZAHRATUSSYIFA\tKelas VIII\n" +
            "3\tALGEZI ACHMAD GIFAHRI\tKelas IX"
        )
    }
    var selectedXlsxFileName by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isImporting by remember { mutableStateOf(false) }

    val createStudentTemplateLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val csvContent = "nomor,nama siswa,kelas\n1,AHMAD FAUZI,Kelas VII\n2,AQEELA ZAHRATUSSYIFA,Kelas VIII\n3,ALGEZI ACHMAD GIFAHRI,Kelas IX\n"
                    outputStream.write(csvContent.toByteArray(Charsets.UTF_8))
                }
                statusMessage = "🎉 File Template Excel Siswa berhasil disimpan ke HP Anda!"
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = "Gagal menyimpan file template: ${e.localizedMessage}"
            }
        }
    }

    val xlsxPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        selectedXlsxFileName = cursor.getString(nameIndex)
                    }
                }
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    isImporting = true
                    viewModel.importStudentBatchFromXlsx(
                        inputStream = inputStream,
                        onSuccess = { count ->
                            isImporting = false
                            statusMessage = "🎉 Berhasil mengimpor $count akun siswa dari file Excel '${selectedXlsxFileName ?: "XLSX"}'!"
                            errorMessage = null
                        },
                        onError = { err ->
                            isImporting = false
                            errorMessage = err
                        }
                    )
                }
            } catch (e: Exception) {
                isImporting = false
                errorMessage = "Gagal membaca file Excel: ${e.localizedMessage}"
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Template Excel (.xlsx) Akun Siswa", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "📊 Format Kolom File Excel (.xlsx):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Kolom A: nomor",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Kolom B: nama siswa",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Kolom C: kelas",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• Baris 1: Header 'nomor' (A), 'nama siswa' (B), dan 'kelas' (C).\n• Baris 2 dst: Data siswa.\n• Password otomatis: Kelas VII ('2027'), Kelas VIII ('2026'), Kelas IX ('2025').",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Button 0: Download Excel/CSV Template File
                OutlinedButton(
                    onClick = { createStudentTemplateLauncher.launch("Template_Siswa_MTs_AlKhairaat.csv") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("📥 Unduh Template Excel Siswa (.xlsx / .csv)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Button 1: Upload Excel file
                Button(
                    onClick = { xlsxPickerLauncher.launch("*/*") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("📂 Unggah File Excel (.xlsx) Siswa", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        val excelFormat = "nomor\tnama siswa\tkelas\n1\tAHMAD FAUZI\tKelas VII\n2\tAQEELA ZAHRATUSSYIFA\tKelas VIII\n3\tALGEZI ACHMAD GIFAHRI\tKelas IX"
                        clipboardManager.setText(AnnotatedString(excelFormat))
                        statusMessage = "✅ Format Tabel Excel 'nomor | nama siswa | kelas' berhasil disalin!"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Salin Format Tabel Excel (nomor, nama, kelas)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Atau Tempel (Paste) Sel dari Microsoft Excel:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = inputExcelText,
                    onValueChange = {
                        inputExcelText = it
                        statusMessage = null
                        errorMessage = null
                    },
                    minLines = 5,
                    maxLines = 8,
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (statusMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(statusMessage!!, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (inputExcelText.isBlank()) {
                        errorMessage = "Data teks Excel tidak boleh kosong!"
                        return@Button
                    }
                    isImporting = true
                    viewModel.importStudentBatch(
                        csvText = inputExcelText,
                        onSuccess = { count ->
                            isImporting = false
                            statusMessage = "🎉 Berhasil mengimpor $count akun siswa dari data Excel!"
                        },
                        onError = { err ->
                            isImporting = false
                            errorMessage = err
                        }
                    )
                },
                enabled = !isImporting
            ) {
                if (isImporting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mengimpor...")
                } else {
                    Text("Import Teks Excel")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: AppViewModel,
    user: com.example.data.UserEntity,
    onBookSelected: (BookEntity) -> Unit
) {
    val gradeOptions = listOf("Kelas VII", "Kelas VIII", "Kelas IX")
    
    var selectedGradeTab by remember {
        mutableStateOf(
            if (user.gradeClass in gradeOptions) user.gradeClass else "Kelas VII"
        )
    }

    var showAddNoteDialog by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<com.example.data.NoteEntity?>(null) }
    var noteToDelete by remember { mutableStateOf<com.example.data.NoteEntity?>(null) }

    val notesForGrade by viewModel.getNotesForGrade(selectedGradeTab)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddNoteDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_note_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Catatan")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tambah Catatan", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Catatan Belajar Per Kelas",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = "Catatan yang dibuat hanya akan muncul pada kelas yang ditentukan.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            SecondaryTabRow(
                selectedTabIndex = gradeOptions.indexOf(selectedGradeTab).coerceAtLeast(0),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                containerColor = Color.Transparent
            ) {
                gradeOptions.forEach { grade ->
                    Tab(
                        selected = selectedGradeTab == grade,
                        onClick = { selectedGradeTab = grade },
                        text = {
                            Text(
                                text = grade,
                                fontWeight = if (selectedGradeTab == grade) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                    )
                }
            }

            if (notesForGrade.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Empty Notes",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Belum Ada Catatan di $selectedGradeTab",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Catatan yang Anda masukkan untuk $selectedGradeTab akan tampil di sini.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(notesForGrade, key = { it.id }) { note ->
                        Card(
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = note.bookTitle,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = note.gradeClass,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { noteToEdit = note },
                                            modifier = Modifier.testTag("edit_note_${note.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Catatan",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        IconButton(
                                            onClick = { noteToDelete = note },
                                            modifier = Modifier.testTag("delete_note_${note.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Hapus Catatan",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                                if (note.pageNumber > 0) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Halaman ${note.pageNumber}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = note.noteContent,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddNoteDialog) {
        var bookTitleDraft by remember { mutableStateOf("") }
        var pageNumDraft by remember { mutableStateOf("1") }
        var targetGradeDraft by remember { mutableStateOf(selectedGradeTab) }
        var contentDraft by remember { mutableStateOf("") }
        var expandedGradeDropdown by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text("Tambah Catatan Baru", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = targetGradeDraft,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Target Kelas") },
                            trailingIcon = {
                                IconButton(onClick = { expandedGradeDropdown = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Pilih Kelas")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedGradeDropdown = true }
                        )
                        DropdownMenu(
                            expanded = expandedGradeDropdown,
                            onDismissRequest = { expandedGradeDropdown = false }
                        ) {
                            gradeOptions.forEach { grade ->
                                DropdownMenuItem(
                                    text = { Text(grade) },
                                    onClick = {
                                        targetGradeDraft = grade
                                        expandedGradeDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = bookTitleDraft,
                        onValueChange = { bookTitleDraft = it },
                        label = { Text("Judul Buku / Topik Pelajaran") },
                        placeholder = { Text("contoh: Bahasa Arab - Bab 2") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = pageNumDraft,
                        onValueChange = { pageNumDraft = it.filter { char -> char.isDigit() } },
                        label = { Text("Halaman") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = contentDraft,
                        onValueChange = { contentDraft = it },
                        label = { Text("Isi Catatan") },
                        placeholder = { Text("Tulis rangkuman atau poin penting...") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (contentDraft.isNotBlank()) {
                            val titleToUse = if (bookTitleDraft.isBlank()) "Catatan Pelajaran" else bookTitleDraft
                            val pageToUse = pageNumDraft.toIntOrNull() ?: 1
                            viewModel.insertNote(
                                userId = user.id,
                                bookId = "general_note",
                                bookTitle = titleToUse,
                                pageNumber = pageToUse,
                                noteContent = contentDraft,
                                gradeClass = targetGradeDraft
                            )
                        }
                        showAddNoteDialog = false
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (noteToEdit != null) {
        val note = noteToEdit!!
        var editBookTitle by remember(note.id) { mutableStateOf(note.bookTitle) }
        var editPageNum by remember(note.id) { mutableStateOf(if (note.pageNumber > 0) note.pageNumber.toString() else "") }
        var editTargetGrade by remember(note.id) { mutableStateOf(note.gradeClass) }
        var editContent by remember(note.id) { mutableStateOf(note.noteContent) }
        var editExpandedGradeDropdown by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { noteToEdit = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Catatan", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = editTargetGrade,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Target Kelas") },
                            trailingIcon = {
                                IconButton(onClick = { editExpandedGradeDropdown = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Pilih Kelas")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { editExpandedGradeDropdown = true }
                        )
                        DropdownMenu(
                            expanded = editExpandedGradeDropdown,
                            onDismissRequest = { editExpandedGradeDropdown = false }
                        ) {
                            gradeOptions.forEach { grade ->
                                DropdownMenuItem(
                                    text = { Text(grade) },
                                    onClick = {
                                        editTargetGrade = grade
                                        editExpandedGradeDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = editBookTitle,
                        onValueChange = { editBookTitle = it },
                        label = { Text("Judul Buku / Topik Pelajaran") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editPageNum,
                        onValueChange = { editPageNum = it.filter { char -> char.isDigit() } },
                        label = { Text("Halaman") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editContent,
                        onValueChange = { editContent = it },
                        label = { Text("Isi Catatan") },
                        placeholder = { Text("Tulis rangkuman atau poin penting...") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editContent.isNotBlank()) {
                            val titleToUse = if (editBookTitle.isBlank()) "Catatan Pelajaran" else editBookTitle
                            val pageToUse = editPageNum.toIntOrNull() ?: 1
                            viewModel.updateNote(
                                noteId = note.id,
                                userId = note.userId,
                                bookId = note.bookId,
                                bookTitle = titleToUse,
                                pageNumber = pageToUse,
                                noteContent = editContent,
                                gradeClass = editTargetGrade,
                                createdAt = note.createdAt
                            )
                        }
                        noteToEdit = null
                    }
                ) {
                    Text("Simpan Perubahan")
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToEdit = null }) {
                    Text("Batal")
                }
            }
        )
    }

    if (noteToDelete != null) {
        val note = noteToDelete!!
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text("Hapus Catatan", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus catatan '${note.bookTitle}'? Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteNote(note.id)
                        noteToDelete = null
                    }
                ) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun TeacherPinDialog(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    onVerifyPin: (String) -> Boolean
) {
    var pinText by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    var showPin by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Akses Mode Guru", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    text = "Masukkan PIN Pengajar untuk membuka akses pengelolaan kurikulum, menambah buku, dan mengkalibrasi penomoran.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = pinText,
                    onValueChange = {
                        if (it.length <= 10) {
                            pinText = it
                            errorText = null
                        }
                    },
                    label = { Text("PIN Pengajar") },
                    singleLine = true,
                    isError = errorText != null,
                    supportingText = {
                        if (errorText != null) {
                            Text(errorText!!, color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("PIN Bawaan Madrasah: 1926", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    visualTransformation = if (showPin) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPin = !showPin }) {
                            Icon(
                                imageVector = if (showPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showPin) "Sembunyikan" else "Tampilkan"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("input_teacher_pin")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (onVerifyPin(pinText)) {
                        onSuccess()
                        onDismiss()
                    } else {
                        errorText = "PIN salah! Masukkan PIN yang benar (Bawaan: 1926)"
                    }
                },
                modifier = Modifier.testTag("btn_confirm_teacher_pin")
            ) {
                Text("Buka Mode Guru")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun ChangeTeacherPinDialog(
    currentPin: String,
    onDismiss: () -> Unit,
    onPinChanged: (String) -> Unit
) {
    var oldPinInput by remember { mutableStateOf("") }
    var newPinInput by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ubah PIN Guru", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = oldPinInput,
                    onValueChange = { oldPinInput = it; errorText = null },
                    label = { Text("PIN Lama") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPinInput,
                    onValueChange = { newPinInput = it; errorText = null },
                    label = { Text("PIN Baru") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPinInput,
                    onValueChange = { confirmPinInput = it; errorText = null },
                    label = { Text("Konfirmasi PIN Baru") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorText != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(errorText!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (oldPinInput.trim() != currentPin.trim()) {
                        errorText = "PIN lama tidak sesuai."
                    } else if (newPinInput.length < 4) {
                        errorText = "PIN minimal 4 digit/karakter."
                    } else if (newPinInput != confirmPinInput) {
                        errorText = "Konfirmasi PIN tidak cocok."
                    } else {
                        onPinChanged(newPinInput.trim())
                        onDismiss()
                    }
                }
            ) {
                Text("Simpan PIN")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun StudentLoginDialog(
    viewModel: AppViewModel,
    onDismiss: () -> Unit,
    onLoginSuccess: () -> Unit = {}
) {
    var nameInput by remember { mutableStateOf(viewModel.loginEmail) }
    var passwordInput by remember { mutableStateOf(viewModel.loginPassword) }
    var showPassword by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }
    val isAuthenticating = viewModel.isAuthenticating
    val authError = viewModel.authError

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Masuk Kartu Siswa / Guru", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "Gunakan Nama Siswa dan Sandi Kelas yang tertera pada Kartu Login madrasah.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = {
                        nameInput = it
                        localError = null
                        viewModel.loginEmail = it
                    },
                    label = { Text("Nama Siswa (atau 'guru')") },
                    placeholder = { Text("Nama sesuai kartu") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("input_student_login_name")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        localError = null
                        viewModel.loginPassword = it
                    },
                    label = { Text("Kata Sandi") },
                    placeholder = { Text("Sandi kelas (cth: 2026)") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null)
                    },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showPassword) "Sembunyikan" else "Tampilkan"
                            )
                        }
                    },
                    visualTransformation = if (showPassword) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().testTag("input_student_login_password")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Info card for Class Passwords & Admin Portal Assignment
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "🔒 Hak Akses Login Siswa:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("• Username & Sandi hanya dibuat oleh Guru melalui Portal Admin.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("• Kelas VII: Sandi '2027' | Kelas VIII: Sandi '2026' | Kelas IX: Sandi '2025'", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("• Akun di luar data Guru/Portal Admin akan ditolak sistem.", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    }
                }

                val displayError = localError ?: authError
                if (displayError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = displayError,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameInput.isBlank()) {
                        localError = "Nama siswa tidak boleh kosong"
                        return@Button
                    }
                    if (passwordInput.isBlank()) {
                        localError = "Kata sandi wajib diisi"
                        return@Button
                    }
                    viewModel.loginEmail = nameInput
                    viewModel.loginPassword = passwordInput
                    viewModel.performLogin {
                        onLoginSuccess()
                        onDismiss()
                    }
                },
                enabled = !isAuthenticating,
                modifier = Modifier.testTag("btn_confirm_student_login")
            ) {
                if (isAuthenticating) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Memeriksa...")
                } else {
                    Text("Masuk Sekarang")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun StudentChangePasswordDialog(
    user: UserEntity,
    viewModel: AppViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit = {}
) {
    var oldPassInput by remember { mutableStateOf("") }
    var newPassInput by remember { mutableStateOf("") }
    var confirmPassInput by remember { mutableStateOf("") }
    var showPasswords by remember { mutableStateOf(false) }
    var localErrorText by remember { mutableStateOf<String?>(null) }
    var successToastMessage by remember { mutableStateOf<String?>(null) }

    val isChanging = viewModel.isChangingPassword

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ubah Kata Sandi Siswa", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "Akun: ${user.name} (${user.gradeClass})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Ubah kata sandi login Anda sesuai dengan yang diinginkan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = oldPassInput,
                    onValueChange = {
                        oldPassInput = it
                        localErrorText = null
                    },
                    label = { Text("Kata Sandi Saat Ini") },
                    singleLine = true,
                    visualTransformation = if (showPasswords) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { showPasswords = !showPasswords }) {
                            Icon(
                                imageVector = if (showPasswords) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showPasswords) "Sembunyikan" else "Tampilkan"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("input_student_old_password")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newPassInput,
                    onValueChange = {
                        newPassInput = it
                        localErrorText = null
                    },
                    label = { Text("Kata Sandi Baru") },
                    singleLine = true,
                    visualTransformation = if (showPasswords) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = { Icon(Icons.Default.Password, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("input_student_new_password")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPassInput,
                    onValueChange = {
                        confirmPassInput = it
                        localErrorText = null
                    },
                    label = { Text("Konfirmasi Kata Sandi Baru") },
                    singleLine = true,
                    visualTransformation = if (showPasswords) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("input_student_confirm_password")
                )

                val displayErr = localErrorText ?: viewModel.changePasswordError
                if (displayErr != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = displayErr,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (successToastMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = successToastMessage!!,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (oldPassInput.isBlank()) {
                        localErrorText = "Masukkan kata sandi Anda yang sekarang."
                        return@Button
                    }
                    if (newPassInput.isBlank()) {
                        localErrorText = "Masukkan kata sandi baru."
                        return@Button
                    }
                    if (newPassInput.trim().length < 3) {
                        localErrorText = "Kata sandi baru minimal 3 karakter."
                        return@Button
                    }
                    if (newPassInput.trim() != confirmPassInput.trim()) {
                        localErrorText = "Konfirmasi kata sandi baru tidak cocok!"
                        return@Button
                    }

                    viewModel.changeStudentPassword(
                        userId = user.id,
                        oldPass = oldPassInput,
                        newPass = newPassInput,
                        onSuccess = {
                            successToastMessage = "✅ Kata sandi berhasil diperbarui!"
                            onSuccess()
                            onDismiss()
                        },
                        onError = { err ->
                            localErrorText = err
                        }
                    )
                },
                enabled = !isChanging,
                modifier = Modifier.testTag("btn_save_student_password")
            ) {
                if (isChanging) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Menyimpan...")
                } else {
                    Text("Simpan Kata Sandi Baru")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun ResetStudentPasswordDialog(
    studentUser: UserEntity,
    viewModel: AppViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit = {}
) {
    var newPassInput by remember { mutableStateOf(studentUser.password) }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset Password Siswa", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    text = "Siswa: ${studentUser.name} (${studentUser.gradeClass})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Setel kata sandi baru jika siswa lupa kata sandinya sendiri.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = newPassInput,
                    onValueChange = {
                        newPassInput = it
                        errorText = null
                    },
                    label = { Text("Kata Sandi Baru Siswa") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_reset_student_password")
                )

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(errorText!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newPassInput.isBlank()) {
                        errorText = "Kata sandi tidak boleh kosong"
                        return@Button
                    }
                    viewModel.resetStudentPassword(
                        userId = studentUser.id,
                        newPass = newPassInput,
                        onSuccess = {
                            onSuccess()
                            onDismiss()
                        },
                        onError = { err ->
                            errorText = err
                        }
                    )
                },
                modifier = Modifier.testTag("btn_confirm_reset_student_password")
            ) {
                Text("Simpan Reset Password")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}



