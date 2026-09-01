package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.QuizAdminViewModel
import com.example.ui.theme.*

data class ThemeColorOption(
    val hex: String,
    val name: String,
    val color: Color
)

val THEME_COLORS = listOf(
    ThemeColorOption("#6750A4", "റോയൽ പർപ്പിൾ", Color(0xFF6750A4)),
    ThemeColorOption("#4F46E5", "ഇൻഡിഗോ ബ്ലൂ", Color(0xFF4F46E5)),
    ThemeColorOption("#EA580C", "ഓറഞ്ച് ഹെറിറ്റേജ്", Color(0xFFEA580C)),
    ThemeColorOption("#0284C7", "സയൻ ബ്ലൂ", Color(0xFF0284C7)),
    ThemeColorOption("#10B981", "എമറാൾഡ് ഗ്രീൻ", Color(0xFF10B981)),
    ThemeColorOption("#7C3AED", "ഡീപ് വയലറ്റ്", Color(0xFF7C3AED)),
    ThemeColorOption("#DC2626", "ക്രിംസൺ റെഡ്", Color(0xFFDC2626))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventBrandingScreen(
    viewModel: QuizAdminViewModel,
    modifier: Modifier = Modifier
) {
    val event by viewModel.quizEvent.collectAsState()

    var eventTitle by remember(event) { mutableStateOf(event?.title ?: "സംസ്ഥാന തല ക്വിസ് ചാമ്പ്യൻഷിപ്പ് 2026") }
    var eventSubtitle by remember(event) { mutableStateOf(event?.subtitle ?: "ഗാന്ധി സ്മൃതി & ശാസ്ത്രോത്സവം മെഗാ ഫിനാലെ") }
    var organizerName by remember(event) { mutableStateOf(event?.organizer ?: "സംസ്ഥാന ക്വിസ് കൗൺസിൽ") }
    var selectedThemeHex by remember(event) { mutableStateOf(event?.themeColorHex ?: "#6750A4") }
    var defaultTimer by remember(event) { mutableStateOf(event?.defaultTimerSeconds ?: 30) }
    var passingScore by remember(event) { mutableStateOf(event?.passingScorePercentage ?: 50) }

    val activeColor = remember(selectedThemeHex) {
        THEME_COLORS.find { it.hex.equals(selectedThemeHex, ignoreCase = true) }?.color ?: BentoPrimary
    }

    val currentMasterPassword by viewModel.adminMasterPassword.collectAsState()
    var newPasswordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var isNewPasswordVisible by remember { mutableStateOf(false) }
    var passwordChangeMessage by remember { mutableStateOf<String?>(null) }
    var isSuccessMessage by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BentoBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "ഇവന്റ് ബ്രാൻഡിംഗും ക്രമീകരണങ്ങളും",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = BentoOnSurface
            )
            Text(
                text = "ക്വിസ് മത്സരത്തിന്റെ പേര്, സംഘാടകർ, തീം നിറങ്ങൾ, റൗണ്ട് ടൈമറുകൾ എന്നിവ ഇവിടെ ക്രമീകരിക്കാം.",
                style = MaterialTheme.typography.bodySmall,
                color = BentoOnSurfaceVariant
            )
        }

        // --- 1. QUICK PRESETS BENTO BAR ---
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = BentoSurfaceVariant,
                border = BorderStroke(1.dp, BentoPastelPurple)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(BentoPastelPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = BentoDeepPurple,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "തയ്യാറാക്കിയ ക്വിസ് തീമുകൾ (Presets)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = BentoDeepPurple
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = BentoPastelPurple,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    viewModel.applyEventPreset("gandhi")
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "ഗാന്ധി ജയന്തി",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = BentoDeepPurple
                                )
                                Text(
                                    text = "പൈതൃക ക്വിസ്",
                                    fontSize = 10.sp,
                                    color = BentoDeepPurple.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = BentoPastelViolet,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    viewModel.applyEventPreset("science")
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "ശാസ്ത്രോത്സവം",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = BentoDeepPurple
                                )
                                Text(
                                    text = "സയൻസ് & സ്പേസ്",
                                    fontSize = 10.sp,
                                    color = BentoDeepPurple.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = BentoPastelPink,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    viewModel.applyEventPreset("kerala")
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "കേരളപ്പിറവി",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF31111D)
                                )
                                Text(
                                    text = "സാഹിത്യം & കല",
                                    fontSize = 10.sp,
                                    color = Color(0xFF31111D).copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 2. EVENT TITLE & DETAILS FORM ---
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = BentoSurface,
                border = BorderStroke(1.dp, BentoOutline)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "മത്സര വിവരങ്ങൾ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoOnSurface
                    )

                    OutlinedTextField(
                        value = eventTitle,
                        onValueChange = { eventTitle = it },
                        label = { Text("ക്വിസ് മത്സരത്തിന്റെ പേര്") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = eventSubtitle,
                        onValueChange = { eventSubtitle = it },
                        label = { Text("ഉപശീർഷകം / വിവരണം") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = organizerName,
                        onValueChange = { organizerName = it },
                        label = { Text("സംഘാടകർ / വിദ്യാഭ്യാസ സമിതി") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )
                }
            }
        }

        // --- 3. THEME COLOR SELECTION ---
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = BentoSurface,
                border = BorderStroke(1.dp, BentoOutline)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "തീം കളർ പാലറ്റ്",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoOnSurface
                    )
                    Text(
                        text = "വേദിയിലെ സ്ക്രീനുകൾക്കും അഡ്മിൻ പാനലിനും അനുയോജ്യമായ നിറം തെരഞ്ഞെടുക്കുക.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoOnSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        THEME_COLORS.forEach { option ->
                            val isSelected = selectedThemeHex.equals(option.hex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(option.color)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) BentoPrimary else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedThemeHex = option.hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 4. DEFAULT TIMER & PASSING CRITERIA ---
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = BentoSurface,
                border = BorderStroke(1.dp, BentoOutline)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "സമയ പരിധിയും മാർക്ക് മാനദണ്ഡങ്ങളും",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoOnSurface
                    )

                    // Timer Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "സാധാരണ ചോദ്യ സമയം",
                                style = MaterialTheme.typography.bodyMedium,
                                color = BentoOnSurface
                            )
                            Text(
                                text = "$defaultTimer സെക്കൻഡ്",
                                fontWeight = FontWeight.Bold,
                                color = BentoPrimary
                            )
                        }
                        Slider(
                            value = defaultTimer.toFloat(),
                            onValueChange = { defaultTimer = it.toInt() },
                            valueRange = 10f..60f,
                            steps = 9
                        )
                    }

                    // Passing Score Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "വിജയ ശതമാനം (Passing Score)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = BentoOnSurface
                            )
                            Text(
                                text = "$passingScore %",
                                fontWeight = FontWeight.Bold,
                                color = BentoPrimary
                            )
                        }
                        Slider(
                            value = passingScore.toFloat(),
                            onValueChange = { passingScore = it.toInt() },
                            valueRange = 30f..80f,
                            steps = 9
                        )
                    }
                }
            }
        }

        // --- 5. ADMIN SECURITY & PASSWORD MANAGEMENT ---
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = BentoSurface,
                border = BorderStroke(1.dp, BentoDeepPurple.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(BentoPastelPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = BentoDeepPurple,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "അഡ്മിൻ സുരക്ഷാ പാസ്‌വേഡ് (Admin PIN)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BentoOnSurface
                            )
                            Text(
                                text = "സ്റ്റുഡന്റ് മോഡിൽ നിന്ന് അഡ്മിൻ പാനലിലേക്ക് പ്രവേശിക്കാനുള്ള പാസ്‌വേഡ് ഇവിടെ മാറ്റാം.",
                                style = MaterialTheme.typography.bodySmall,
                                color = BentoOnSurfaceVariant
                            )
                        }
                    }

                    // Current Active Password display badge
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = BentoPastelPurple,
                        border = BorderStroke(1.dp, BentoDeepPurple.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Key,
                                    contentDescription = null,
                                    tint = BentoDeepPurple,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "നിലവിലെ പാസ്‌വേഡ്:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BentoDeepPurple
                                )
                            }
                            Text(
                                text = currentMasterPassword,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = BentoDeepPurple
                            )
                        }
                    }

                    // New Password Input Field
                    OutlinedTextField(
                        value = newPasswordInput,
                        onValueChange = {
                            newPasswordInput = it
                            passwordChangeMessage = null
                        },
                        label = { Text("പുതിയ പാസ്‌വേഡ് / PIN") },
                        placeholder = { Text("ഉദാഹരണത്തിന്: 5678 അല്ലെങ്കിൽ admin@2026") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        visualTransformation = if (isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { isNewPasswordVisible = !isNewPasswordVisible }) {
                                Icon(
                                    if (isNewPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = BentoOnSurfaceVariant
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BentoPrimary,
                            unfocusedBorderColor = BentoOutline
                        )
                    )

                    // Confirm Password Input Field
                    OutlinedTextField(
                        value = confirmPasswordInput,
                        onValueChange = {
                            confirmPasswordInput = it
                            passwordChangeMessage = null
                        },
                        label = { Text("പുതിയ പാസ്‌വേഡ് വീണ്ടും നൽകുക (Confirm)") },
                        placeholder = { Text("പുതിയ പാസ്‌വേഡ് ഒന്നുകൂടി ടൈപ്പ് ചെയ്യുക") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        visualTransformation = if (isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BentoPrimary,
                            unfocusedBorderColor = BentoOutline
                        )
                    )

                    // Feedback Alert Message if any
                    if (passwordChangeMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSuccessMessage) Color(0xFFDCFCE7) else Color(0xFFFFEBEE),
                            border = BorderStroke(1.dp, if (isSuccessMessage) Color(0xFF86EFAC) else Color(0xFFFCA5A5)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isSuccessMessage) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (isSuccessMessage) Color(0xFF15803D) else Color(0xFFDC2626),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = passwordChangeMessage!!,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSuccessMessage) Color(0xFF15803D) else Color(0xFFDC2626)
                                )
                            }
                        }
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val success = viewModel.updateAdminMasterPassword("1234")
                                if (success) {
                                    newPasswordInput = ""
                                    confirmPasswordInput = ""
                                    passwordChangeMessage = "പാസ്‌വേഡ് ഡിഫോൾട്ട് '1234' ലേക്ക് റീസെറ്റ് ചെയ്തു!"
                                    isSuccessMessage = true
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, BentoOutline),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = BentoOnSurfaceVariant)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("റീസെറ്റ് (1234)", fontSize = 12.sp, color = BentoOnSurfaceVariant)
                        }

                        Button(
                            onClick = {
                                val cleanNew = newPasswordInput.trim()
                                val cleanConfirm = confirmPasswordInput.trim()
                                if (cleanNew.isEmpty()) {
                                    passwordChangeMessage = "ദയവായി പുതിയ പാസ്‌വേഡ് നൽകുക!"
                                    isSuccessMessage = false
                                } else if (cleanNew.length < 3) {
                                    passwordChangeMessage = "പാസ്‌വേഡിൽ കുറഞ്ഞത് 3 അക്ഷരങ്ങൾ/അക്കങ്ങൾ ഉണ്ടായിരിക്കണം!"
                                    isSuccessMessage = false
                                } else if (cleanNew != cleanConfirm) {
                                    passwordChangeMessage = "നൽകിയ രണ്ട് പാസ്‌വേഡുകളും പൊരുത്തപ്പെടുന്നില്ല!"
                                    isSuccessMessage = false
                                } else {
                                    val success = viewModel.updateAdminMasterPassword(cleanNew)
                                    if (success) {
                                        newPasswordInput = ""
                                        confirmPasswordInput = ""
                                        passwordChangeMessage = "അഡ്മിൻ പാസ്‌വേഡ് വിജയകരമായി മാറ്റി: $cleanNew"
                                        isSuccessMessage = true
                                    }
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BentoDeepPurple),
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("പാസ്‌വേഡ് മാറ്റുക", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    // Lock immediately button
                    FilledTonalButton(
                        onClick = { viewModel.lockToStudentMode() },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ഇപ്പോൾ തന്നെ സ്റ്റുഡന്റ് മോഡിലേക്ക് ലോക്ക് ചെയ്യുക", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // --- 6. SAVE CHANGES BUTTON ---
        item {
            Button(
                onClick = {
                    viewModel.updateEventBranding(
                        title = eventTitle,
                        subtitle = eventSubtitle,
                        organizer = organizerName,
                        themeHex = selectedThemeHex,
                        defaultTimer = defaultTimer,
                        passingPercentage = passingScore
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ഇവന്റ് വിവരങ്ങൾ സേവ് ചെയ്യുക",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
