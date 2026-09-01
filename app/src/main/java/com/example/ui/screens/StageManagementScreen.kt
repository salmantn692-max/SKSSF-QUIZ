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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.AdminNavTab
import com.example.ui.QuizAdminViewModel
import com.example.ui.theme.*

@Composable
fun StageManagementScreen(
    viewModel: QuizAdminViewModel,
    modifier: Modifier = Modifier
) {
    val stages by viewModel.stages.collectAsState()
    val questions by viewModel.questions.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var stageToEdit by remember { mutableStateOf<QuizStage?>(null) }
    var stageToDelete by remember { mutableStateOf<QuizStage?>(null) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("+ പുതിയ റൗണ്ട് ചേർക്കൂ", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "മത്സര റൗണ്ടുകൾ (Stages)",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = BentoOnSurface
                )
                Text(
                    text = "ക്വിസ് മത്സരത്തിന്റെ വിവിധ ഘട്ടങ്ങൾ ക്രമീകരിക്കുക: റൗണ്ട് 1 (MCQ), റൗണ്ട് 2 (ചിത്ര ക്ലൂകൾ), റൗണ്ട് 3 (ഓഡിയോ മിസ്റ്ററി).",
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoOnSurfaceVariant
                )
            }

            if (stages.isEmpty()) {
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
                                    Icons.Default.LayersClear,
                                    contentDescription = null,
                                    modifier = Modifier.size(30.dp),
                                    tint = BentoDeepPurple
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "റൗണ്ടുകളൊന്നും ചേർത്തിട്ടില്ല",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BentoOnSurface
                            )
                            Text(
                                text = "'+ പുതിയ റൗണ്ട് ചേർക്കൂ' ബട്ടൺ അമർത്തി ആദ്യ റൗണ്ട് തയ്യാറാക്കുക.",
                                style = MaterialTheme.typography.bodySmall,
                                color = BentoOnSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(stages, key = { it.id }) { stage ->
                    val stageQuestions = questions.filter { it.stageId == stage.id }
                    StageBentoCard(
                        stage = stage,
                        questionCount = stageQuestions.size,
                        onEdit = { stageToEdit = stage },
                        onDelete = { stageToDelete = stage },
                        onViewQuestions = {
                            viewModel.selectedStageFilter.value = stage.id
                            viewModel.selectTab(AdminNavTab.QUESTIONS)
                        },
                        onTestRun = { viewModel.startSimulator(stage) }
                    )
                }
            }
        }
    }

    // --- ADD STAGE DIALOG ---
    if (showAddDialog) {
        StageEditDialog(
            stage = null,
            onDismiss = { showAddDialog = false },
            onSave = { title, desc, type, timer ->
                viewModel.addStage(title, desc, type, timer)
                showAddDialog = false
            }
        )
    }

    // --- EDIT / RENAME STAGE DIALOG ---
    stageToEdit?.let { stage ->
        StageEditDialog(
            stage = stage,
            onDismiss = { stageToEdit = null },
            onSave = { title, desc, type, timer ->
                viewModel.updateStage(stage, title, desc, type, timer)
                stageToEdit = null
            }
        )
    }

    // --- DELETE CONFIRMATION DIALOG ---
    stageToDelete?.let { stage ->
        AlertDialog(
            onDismissRequest = { stageToDelete = null },
            shape = RoundedCornerShape(28.dp),
            title = { Text("റൗണ്ട് നീക്കം ചെയ്യണോ?", fontWeight = FontWeight.Bold) },
            text = {
                Text("'${stage.title}' നീക്കം ചെയ്താൽ ഇതിലുള്ള എല്ലാ ചോദ്യങ്ങളും റദ്ദാക്കപ്പെടും. മുന്നോട്ട് പോകണോ?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteStage(stage)
                        stageToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("ഡിലീറ്റ് ചെയ്യുക", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { stageToDelete = null }) {
                    Text("റദ്ദാക്കുക", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun StageBentoCard(
    stage: QuizStage,
    questionCount: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewQuestions: () -> Unit,
    onTestRun: () -> Unit
) {
    val typeBg = when (stage.stageType) {
        StageType.MCQ -> BentoPastelPurple
        StageType.IMAGE_BASED -> BentoPastelPink
        StageType.AUDIO_BASED -> BentoPastelViolet
    }
    val typeTextColor = when (stage.stageType) {
        StageType.MCQ -> BentoDeepPurple
        StageType.IMAGE_BASED -> BentoDeepPink
        StageType.AUDIO_BASED -> BentoDeepViolet
    }
    val typeIcon = when (stage.stageType) {
        StageType.MCQ -> Icons.Default.FormatListBulleted
        StageType.IMAGE_BASED -> Icons.Default.Image
        StageType.AUDIO_BASED -> Icons.Default.GraphicEq
    }
    val typeLabel = when (stage.stageType) {
        StageType.MCQ -> "റൗണ്ട് 1: MCQ ചോദ്യങ്ങൾ"
        StageType.IMAGE_BASED -> "റൗണ്ട് 2: ചിത്ര ക്ലൂകൾ (Image)"
        StageType.AUDIO_BASED -> "റൗണ്ട് 3: ശബ്ദരേഖ (Audio)"
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = typeBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = typeIcon,
                            contentDescription = null,
                            tint = typeTextColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = typeLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = typeTextColor
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(BentoSurfaceContainer)
                            .clickable { onEdit() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "റൗണ്ട് തിരുത്തുക",
                            tint = BentoOnSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFEBEE))
                            .clickable { onDelete() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "ഡിലീറ്റ് ചെയ്യുക",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stage.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BentoOnSurface
            )

            if (stage.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stage.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoOnSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stage Info Bento Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BentoSurfaceVariant,
                    border = BorderStroke(1.dp, BentoPastelPurple)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(14.dp), tint = BentoPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("$questionCount ചോദ്യങ്ങൾ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoOnSurface)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BentoSurfaceVariant,
                    border = BorderStroke(1.dp, BentoPastelPurple)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(14.dp), tint = BentoPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${stage.timerSeconds} സെക്കൻഡ് പരിധി", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoOnSurface)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = BentoOutline.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = BentoSurfaceContainerHigh,
                    border = BorderStroke(1.dp, BentoOutline),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clickable { onViewQuestions() }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.FormatListNumbered, contentDescription = null, modifier = Modifier.size(16.dp), tint = BentoPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ചോദ്യ ശേഖരം", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoTextDark)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = BentoPastelPurple,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clickable { onTestRun() }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.SportsEsports, contentDescription = null, modifier = Modifier.size(16.dp), tint = BentoDeepPurple)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("റൗണ്ട് ടെസ്റ്റ് റൺ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoDeepPurple)
                    }
                }
            }
        }
    }
}

@Composable
private fun StageEditDialog(
    stage: QuizStage?,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, stageType: StageType, timer: Int) -> Unit
) {
    var title by remember { mutableStateOf(stage?.title ?: "") }
    var description by remember { mutableStateOf(stage?.description ?: "") }
    var selectedType by remember { mutableStateOf(stage?.stageType ?: StageType.MCQ) }
    var timerSeconds by remember { mutableStateOf(stage?.timerSeconds ?: 30) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                if (stage == null) "പുതിയ റൗണ്ട് ചേർക്കുക" else "റൗണ്ട് വിവരങ്ങൾ മാറ്റുക",
                fontWeight = FontWeight.Bold,
                color = BentoOnSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("റൗണ്ടിന്റെ പേര് / ശീർഷകം *") },
                    placeholder = { Text("ഉദാ: റൗണ്ട് 1: ചരിത്ര സ്മൃതി (MCQ)") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("നിർദ്ദേശങ്ങൾ / വിവരണം") },
                    placeholder = { Text("ഈ റൗണ്ടിന്റെ ലഘു വിവരണം നൽകുക") },
                    maxLines = 3,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "ചോദ്യ ഫോർമാറ്റ് / റൗണ്ട് ടൈപ്പ്",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = BentoOnSurface
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StageTypeOptionRow(
                        type = StageType.MCQ,
                        title = "റൗണ്ട് 1: മൾട്ടിപ്പിൾ ചോയ്സ് (MCQ)",
                        subtitle = "4 ഓപ്ഷനുകളുള്ള സാധാരണ അറിവ് പരിശോധനാ ചോദ്യങ്ങൾ",
                        icon = Icons.Default.FormatListBulleted,
                        isSelected = selectedType == StageType.MCQ,
                        onSelect = { selectedType = StageType.MCQ }
                    )

                    StageTypeOptionRow(
                        type = StageType.IMAGE_BASED,
                        title = "റൗണ്ട് 2: ചിത്ര പസിലുകൾ (Image Clues)",
                        subtitle = "ഫോട്ടോയോ ചിത്രമോ കണ്ട് തിരിച്ചറിയാനുള്ള ചോദ്യങ്ങൾ",
                        icon = Icons.Default.Image,
                        isSelected = selectedType == StageType.IMAGE_BASED,
                        onSelect = { selectedType = StageType.IMAGE_BASED }
                    )

                    StageTypeOptionRow(
                        type = StageType.AUDIO_BASED,
                        title = "റൗണ്ട് 3: ശബ്ദരേഖ & ഓഡിയോ (Audio Clues)",
                        subtitle = "ശബ്ദശകലങ്ങൾ, മോഴ്സ് കോഡ്, സംഗീതം കേട്ട് ഉത്തരം കണ്ടെത്തൽ",
                        icon = Icons.Default.GraphicEq,
                        isSelected = selectedType == StageType.AUDIO_BASED,
                        onSelect = { selectedType = StageType.AUDIO_BASED }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ചോദ്യ സമയ പരിധി",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BentoOnSurface
                    )
                    Text(
                        text = "$timerSeconds സെക്കൻഡ്",
                        fontWeight = FontWeight.Bold,
                        color = BentoPrimary
                    )
                }

                Slider(
                    value = timerSeconds.toFloat(),
                    onValueChange = { timerSeconds = it.toInt() },
                    valueRange = 10f..90f,
                    steps = 7,
                    colors = SliderDefaults.colors(
                        thumbColor = BentoPrimary,
                        activeTrackColor = BentoPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title, description, selectedType, timerSeconds)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
            ) {
                Text(if (stage == null) "റൗണ്ട് ചേർക്കുക" else "മാറ്റങ്ങൾ സേവ് ചെയ്യുക", fontWeight = FontWeight.Bold)
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
private fun StageTypeOptionRow(
    type: StageType,
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) BentoPastelPurple else BentoSurfaceVariant,
        border = BorderStroke(
            1.dp,
            if (isSelected) BentoPrimary else BentoOutline.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) BentoDeepPurple else BentoOnSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) BentoDeepPurple else BentoOnSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) BentoDeepPurple.copy(alpha = 0.8f) else BentoOnSurfaceVariant
                )
            }
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(selectedColor = BentoPrimary)
            )
        }
    }
}
