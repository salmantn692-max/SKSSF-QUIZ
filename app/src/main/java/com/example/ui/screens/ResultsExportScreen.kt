package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Student
import com.example.data.model.StudentSubmission
import com.example.ui.QuizAdminViewModel
import com.example.ui.theme.*

@Composable
fun ResultsExportScreen(
    viewModel: QuizAdminViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val event by viewModel.quizEvent.collectAsState()
    val students by viewModel.students.collectAsState()
    val submissions by viewModel.submissions.collectAsState()

    var showCsvPreviewDialog by remember { mutableStateOf(false) }

    val rankedStudents = remember(students) {
        students.sortedWith(
            compareByDescending<Student> { it.totalScore }
                .thenBy { it.timeSpentSeconds }
        )
    }

    val totalPointsAwarded = remember(submissions) {
        submissions.sumOf { it.pointsEarned }
    }

    val correctCount = remember(submissions) {
        submissions.count { it.isCorrect }
    }

    val accuracyRate = remember(submissions) {
        if (submissions.isNotEmpty()) ((correctCount.toFloat() / submissions.size) * 100).toInt() else 0
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BentoBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "ഫലപ്രഖ്യാപനവും CSV എക്സ്പോർട്ടും",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = BentoOnSurface
            )
            Text(
                text = "മത്സരാർത്ഥികളുടെ റാങ്ക് ലിസ്റ്റ്, ചോദ്യം തിരിച്ചുള്ള വിശകലനം, ഔദ്യോഗിക CSV ഫയൽ ഷെയറിംഗ് എന്നിവ ഇവിടെ ലഭ്യമാണ്.",
                style = MaterialTheme.typography.bodySmall,
                color = BentoOnSurfaceVariant
            )
        }

        // --- 1. EXPORT HERO BENTO CARD ---
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = BentoSurfaceVariant,
                border = BorderStroke(1.dp, BentoPastelPurple),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(BentoPastelPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.FileDownload,
                                contentDescription = null,
                                tint = BentoDeepPurple,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ഔദ്യോഗിക ഫല റിപ്പോർട്ട്",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BentoOnSurface
                            )
                            Text(
                                text = "${students.size} മത്സരാർത്ഥികൾ • ${submissions.size} ഉത്തരങ്ങൾ വിലയിരുത്തി",
                                style = MaterialTheme.typography.bodySmall,
                                color = BentoOnSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = BentoPrimary,
                            modifier = Modifier
                                .weight(1.2f)
                                .height(44.dp)
                                .clickable { viewModel.exportAndShareCsv(context) }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("CSV എക്സ്പോർട്ട്", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = BentoSurfaceContainerHigh,
                            border = BorderStroke(1.dp, BentoOutline),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clickable { showCsvPreviewDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, tint = BentoPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("പ്രിവ്യൂ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BentoTextDark)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = BentoPastelPurple,
                            modifier = Modifier
                                .size(44.dp)
                                .clickable {
                                    val csv = viewModel.getCsvContent()
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Quiz CSV", csv)
                                    clipboard.setPrimaryClip(clip)
                                    viewModel.showToast("CSV ക്ലിപ്പ്ബോർഡിലേക്ക് കോപ്പി ചെയ്തു!")
                                }
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy CSV",
                                    tint = BentoDeepPurple,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 2. AGGREGATED STATS BENTO TILES ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = BentoSurface,
                    border = BorderStroke(1.dp, BentoOutline),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("ശരിയുത്തര ശതമാനം", style = MaterialTheme.typography.labelSmall, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoOnSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$accuracyRate%", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF16A34A))
                        Text("$correctCount / ${submissions.size} ശരിയായ ഉത്തരങ്ങൾ", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = BentoOnSurfaceVariant)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = BentoSurface,
                    border = BorderStroke(1.dp, BentoOutline),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("ആകെ നേടിയ പോയിന്റ്", style = MaterialTheme.typography.labelSmall, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoOnSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$totalPointsAwarded", fontSize = 24.sp, fontWeight = FontWeight.Black, color = BentoPrimary)
                        Text("നൽകപ്പെട്ട ആകെ മാർക്ക്", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = BentoOnSurfaceVariant)
                    }
                }
            }
        }

        // --- 3. FULL CANDIDATE LEADERBOARD BENTO SECTION ---
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = BentoSurface,
                border = BorderStroke(1.dp, BentoOutline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ഔദ്യോഗിക ലീഡർബോർഡ്",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoOnSurface
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BentoPastelPurple
                        ) {
                            Text(
                                text = "${rankedStudents.size} പേർ റാങ്ക് ചെയ്യപ്പെട്ടു",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoDeepPurple,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (rankedStudents.isEmpty()) {
                        Text(
                            text = "സ്കോറുകളൊന്നും രേഖപ്പെടുത്തിയിട്ടില്ല. വിദ്യാർത്ഥികളെ ചേർക്കുകയോ പരീക്ഷ നടത്തുകയോ ചെയ്യുക.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BentoOnSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            rankedStudents.take(15).forEachIndexed { rankIndex, student ->
                                val (rankBadgeBg, rankTextColor) = when (rankIndex) {
                                    0 -> Pair(Color(0xFFF59E0B), Color.White)
                                    1 -> Pair(Color(0xFF94A3B8), Color.White)
                                    2 -> Pair(Color(0xFFB45309), Color.White)
                                    else -> Pair(BentoSurfaceVariant, BentoOnSurfaceVariant)
                                }

                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (rankIndex < 3) BentoSurfaceVariant else BentoSurfaceContainer,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(rankBadgeBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "#${rankIndex + 1}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Black,
                                                color = rankTextColor
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = student.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = BentoOnSurface
                                            )
                                            Text(
                                                text = "${student.registerNumber} • ${student.departmentOrGrade}",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontSize = 11.sp,
                                                color = BentoOnSurfaceVariant
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "${student.totalScore} മാർക്ക്",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Black,
                                                color = BentoPrimary
                                            )
                                            Text(
                                                text = "${student.timeSpentSeconds} സെക്കൻഡ്",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = BentoOnSurfaceVariant
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

        // --- 4. DETAILED AUDIT TRAIL LOG ---
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = BentoSurface,
                border = BorderStroke(1.dp, BentoOutline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "സമർപ്പിച്ച ഉത്തരങ്ങളുടെ വിവരണം (${submissions.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoOnSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (submissions.isEmpty()) {
                        Text(
                            text = "ഉത്തരങ്ങളൊന്നും ഇതുവരെ സമർപ്പിച്ചിട്ടില്ല.",
                            style = MaterialTheme.typography.bodySmall,
                            color = BentoOnSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            submissions.take(10).forEach { sub ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = BentoSurfaceVariant,
                                    border = BorderStroke(1.dp, BentoOutline.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${sub.studentName} (${sub.registerNumber})",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = BentoOnSurface
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (sub.isCorrect) Color(0xFFDCFCE7) else Color(0xFFFFEBEE)
                                            ) {
                                                Text(
                                                    text = if (sub.isCorrect) "ശരിയുത്തരം (+${sub.pointsEarned} മാർക്ക്)" else "തെറ്റായ ഉത്തരം (0 മാർക്ക്)",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (sub.isCorrect) Color(0xFF166534) else Color(0xFF991B1B),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = sub.questionText,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = BentoTextDark
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "നൽകിയത്: ${sub.selectedAnswer} • ശരി: ${sub.correctAnswer}",
                                                fontSize = 10.sp,
                                                color = BentoOnSurfaceVariant
                                            )
                                            Text(
                                                text = "${sub.timeSpentSeconds} സെക്കൻഡ്",
                                                fontSize = 10.sp,
                                                color = BentoOnSurfaceVariant
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
    }

    // --- CSV PREVIEW DIALOG ---
    if (showCsvPreviewDialog) {
        val rawCsv = remember { viewModel.getCsvContent() }
        AlertDialog(
            onDismissRequest = { showCsvPreviewDialog = false },
            shape = RoundedCornerShape(28.dp),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("CSV ഫയൽ പ്രിവ്യൂ", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BentoOnSurface)
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Quiz CSV", rawCsv))
                            viewModel.showToast("CSV കോപ്പി ചെയ്തു!")
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = BentoPrimary)
                    }
                }
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0F172A))
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState())
                        .horizontalScroll(rememberScrollState())
                ) {
                    Text(
                        text = rawCsv,
                        color = Color(0xFF38BDF8),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCsvPreviewDialog = false
                        viewModel.exportAndShareCsv(context)
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
                ) {
                    Text("ഫയൽ ഷെയർ ചെയ്യുക", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCsvPreviewDialog = false }) {
                    Text("ശരി", fontWeight = FontWeight.Bold, color = BentoOnSurfaceVariant)
                }
            }
        )
    }
}
