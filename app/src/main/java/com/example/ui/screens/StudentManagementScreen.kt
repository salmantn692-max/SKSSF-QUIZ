package com.example.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Student
import com.example.data.model.StudentStatus
import com.example.data.model.StudentSubmission
import com.example.ui.QuizAdminViewModel
import com.example.ui.theme.*

@Composable
fun StudentManagementScreen(
    viewModel: QuizAdminViewModel,
    modifier: Modifier = Modifier
) {
    val students by viewModel.students.collectAsState()
    val submissions by viewModel.submissions.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var studentToDelete by remember { mutableStateOf<Student?>(null) }
    var studentForScorecard by remember { mutableStateOf<Student?>(null) }

    val filteredStudents = remember(students, searchQuery) {
        if (searchQuery.isBlank()) {
            students
        } else {
            students.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.registerNumber.contains(searchQuery, ignoreCase = true) ||
                        it.departmentOrGrade.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("+ പുതിയ വിദ്യാർത്ഥി", fontWeight = FontWeight.Bold) },
                containerColor = BentoPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(28.dp)
            )
        },
        containerColor = BentoBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "വിദ്യാർത്ഥി വിവരങ്ങളും രജിസ്ട്രേഷനും",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = BentoOnSurface
                )
                Text(
                    text = "മത്സരാർത്ഥികളെ ചേർക്കുക, പരീക്ഷാ സ്റ്റാറ്റസ് നിരീക്ഷിക്കുക, വ്യക്തിഗത സ്കോർകാർഡുകൾ പരിശോധിക്കുക.",
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoOnSurfaceVariant
                )
            }

            // Quick Batch Seed & Clear Bar in Bento Style
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = BentoPastelPurple,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable { viewModel.seedBatchSampleStudents() }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(18.dp), tint = BentoDeepPurple)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("മാതൃകാ വിദ്യാർത്ഥികളെ ചേർക്കൂ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoDeepPurple)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFFFEBEE),
                        border = BorderStroke(1.dp, Color(0xFFFFCDD2)),
                        modifier = Modifier
                            .height(44.dp)
                            .clickable { viewModel.clearAllStudents() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFFDC2626))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("എല്ലാം നീക്കുക", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                        }
                    }
                }
            }

            // Search Box
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("പേര്, രജിസ്റ്റർ നമ്പർ, സ്കൂൾ തിരയുക...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BentoPrimary) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Student Count Summary Bento Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "രജിസ്റ്റർ ചെയ്തവർ (${filteredStudents.size})",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoOnSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BentoPastelPurple
                    ) {
                        Text(
                            text = "${students.count { it.status == StudentStatus.COMPLETED }} പേർ പൂർത്തിയാക്കി",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoDeepPurple,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (filteredStudents.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = BentoSurface,
                        border = BorderStroke(1.dp, BentoOutline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(BentoPastelPurple),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.PersonOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(30.dp),
                                    tint = BentoDeepPurple
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "വിദ്യാർത്ഥികളൊന്നും ലിസ്റ്റിൽ ഇല്ല",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BentoOnSurface
                            )
                            Text(
                                text = "'+ പുതിയ വിദ്യാർത്ഥി' വഴിയോ 'മാതൃകാ വിദ്യാർത്ഥികളെ ചേർക്കൂ' വഴിയോ ലിസ്റ്റ് തയ്യാറാക്കുക.",
                                style = MaterialTheme.typography.bodySmall,
                                color = BentoOnSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredStudents, key = { it.id }) { student ->
                    val studentSubmissions = submissions.filter { it.studentId == student.id }
                    StudentBentoCard(
                        student = student,
                        submissionCount = studentSubmissions.size,
                        onViewScorecard = { studentForScorecard = student },
                        onDelete = { studentToDelete = student }
                    )
                }
            }
        }
    }

    // --- ADD STUDENT DIALOG ---
    if (showAddDialog) {
        AddStudentDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, regNo, dept ->
                viewModel.addStudent(name, regNo, dept)
                showAddDialog = false
            }
        )
    }

    // --- DELETE STUDENT CONFIRMATION ---
    studentToDelete?.let { st ->
        AlertDialog(
            onDismissRequest = { studentToDelete = null },
            shape = RoundedCornerShape(28.dp),
            title = { Text("വിദ്യാർത്ഥിയെ നീക്കം ചെയ്യണോ?", fontWeight = FontWeight.Bold) },
            text = { Text("'${st.name}' (${st.registerNumber}) എന്ന മത്സരാർത്ഥിയെ ലിസ്റ്റിൽ നിന്ന് നീക്കണോ?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteStudent(st)
                        studentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("നീക്കം ചെയ്യുക", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToDelete = null }) {
                    Text("റദ്ദാക്കുക", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // --- STUDENT SCORECARD DIALOG ---
    studentForScorecard?.let { st ->
        val stSubmissions = submissions.filter { it.studentId == st.id }
        StudentScorecardDialog(
            student = st,
            submissions = stSubmissions,
            onDismiss = { studentForScorecard = null }
        )
    }
}

@Composable
private fun StudentBentoCard(
    student: Student,
    submissionCount: Int,
    onViewScorecard: () -> Unit,
    onDelete: () -> Unit
) {
    val (statusColor, statusBg, statusText) = when (student.status) {
        StudentStatus.REGISTERED -> Triple(BentoOnSurfaceVariant, BentoSurfaceVariant, "രജിസ്റ്റർ ചെയ്തു")
        StudentStatus.IN_PROGRESS -> Triple(BentoPrimary, BentoPastelPurple, "പരീക്ഷ എഴുതുന്നു")
        StudentStatus.COMPLETED -> Triple(Color(0xFF16A34A), Color(0xFFDCFCE7), "പൂർത്തിയായി")
    }

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = BentoSurface,
        border = BorderStroke(1.dp, BentoOutline),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Candidate Avatar Initial in Bento Pastel Purple
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(BentoPastelPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = student.name.take(1).uppercase(),
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = BentoDeepPurple
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoOnSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BentoSurfaceVariant
                        ) {
                            Text(
                                text = student.registerNumber,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoDeepPurple,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "• ${student.departmentOrGrade}",
                            style = MaterialTheme.typography.bodySmall,
                            color = BentoOnSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = statusBg
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.4.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BentoOutline.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))

            // Score & Submissions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text("ആകെ മാർക്ക്", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BentoOnSurfaceVariant)
                        Text("${student.totalScore} പോയിന്റ്", fontWeight = FontWeight.Black, fontSize = 15.sp, color = BentoPrimary)
                    }
                    Column {
                        Text("എടുത്ത സമയം", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BentoOnSurfaceVariant)
                        Text("${student.timeSpentSeconds} സെക്കൻഡ്", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BentoOnSurface)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = onViewScorecard) {
                        Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp), tint = BentoPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("സ്കോർകാർഡ്", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoPrimary)
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddStudentDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, regNo: String, department: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var regNo by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = { Text("പുതിയ മത്സരാർത്ഥിയെ ചേർക്കുക", fontWeight = FontWeight.Bold, color = BentoOnSurface) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("പൂർണ്ണ നാമം *") },
                    placeholder = { Text("ഉദാ: ആദിത്യ കൃഷ്ണൻ") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = regNo,
                    onValueChange = { regNo = it },
                    label = { Text("രജിസ്റ്റർ നമ്പർ / റോൾ നമ്പർ *") },
                    placeholder = { Text("ഉദാ: REG2026-101") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("സ്കൂൾ / ക്ലാസ് / ഡിപ്പാർട്ട്മെന്റ്") },
                    placeholder = { Text("ഉദാ: ജി.എച്ച്.എസ്.എസ് കോഴിക്കോട് - പത്താം ക്ലാസ്") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && regNo.isNotBlank()) {
                        onAdd(name, regNo, department)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
            ) {
                Text("രജിസ്റ്റർ ചെയ്യുക", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("റദ്ദാക്കുക", fontWeight = FontWeight.Bold, color = BentoOnSurfaceVariant)
            }
        }
    )
}

@Composable
private fun StudentScorecardDialog(
    student: Student,
    submissions: List<StudentSubmission>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = {
            Column {
                Text("മത്സരാർത്ഥിയുടെ സ്കോർകാർഡ്", fontWeight = FontWeight.Bold, color = BentoOnSurface)
                Text(
                    text = "${student.name} • ${student.registerNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoOnSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Summary bar in Bento Pastel Purple
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = BentoPastelPurple,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("നേടിയ ആകെ പോയിന്റ്", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BentoDeepPurple)
                            Text("${student.totalScore} മാർക്ക്", fontWeight = FontWeight.Black, fontSize = 18.sp, color = BentoDeepPurple)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("എടുത്ത സമയം", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BentoDeepPurple)
                            Text("${student.timeSpentSeconds} സെക്കൻഡ്", fontWeight = FontWeight.Bold, color = BentoDeepPurple)
                        }
                    }
                }

                Text("ഓരോ ചോദ്യത്തിന്റെയും ഫലം", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = BentoOnSurface)

                if (submissions.isEmpty()) {
                    Text(
                        text = "ഈ വിദ്യാർത്ഥി ഇതുവരെ ഉത്തരങ്ങളൊന്നും സമർപ്പിച്ചിട്ടില്ല.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoOnSurfaceVariant
                    )
                } else {
                    submissions.forEach { sub ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (sub.isCorrect) Color(0xFFDCFCE7) else Color(0xFFFFEBEE),
                            border = BorderStroke(1.dp, if (sub.isCorrect) Color(0xFF86EFAC) else Color(0xFFFFCDD2)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = sub.stageTitle,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (sub.isCorrect) Color(0xFF166534) else Color(0xFF991B1B)
                                    )
                                    Text(
                                        text = if (sub.isCorrect) "+${sub.pointsEarned} മാർക്ക്" else "0 മാർക്ക്",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        color = if (sub.isCorrect) Color(0xFF166534) else Color(0xFF991B1B)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = sub.questionText,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = BentoTextDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "നൽകിയ ഉത്തരം: ${sub.selectedAnswer} (ശരിയായത്: ${sub.correctAnswer})",
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
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("ശരി", fontWeight = FontWeight.Bold, color = BentoPrimary)
            }
        }
    )
}
