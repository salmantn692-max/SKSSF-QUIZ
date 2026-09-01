package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuizEvent
import com.example.ui.AdminNavTab
import com.example.ui.AppMode
import com.example.ui.theme.BentoDeepPurple
import com.example.ui.theme.BentoPastelPurple
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoSurfaceContainer

@Composable
fun AppHeader(
    quizEvent: QuizEvent?,
    currentTab: AdminNavTab,
    onTabSelected: (AdminNavTab) -> Unit,
    appMode: AppMode = AppMode.ADMIN,
    onModeToggle: (AppMode) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val eventTitle = quizEvent?.title ?: "ക്വിസ് അഡ്മിൻ"
    val organizer = quizEvent?.organizer ?: "കൺട്രോൾ സെന്റർ"

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Bento Brand Avatar & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BentoPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ക്വി",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ക്വിസ് കൺട്രോളർ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "അഡ്മിൻ പാനൽ • $eventTitle",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Bento Action Pills / Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Student App Switcher Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFDCFCE7),
                    border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                    modifier = Modifier
                        .height(36.dp)
                        .clickable { onModeToggle(AppMode.STUDENT) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "സ്റ്റുഡന്റ് ആപ്പ്",
                            tint = Color(0xFF15803D),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "സ്റ്റുഡന്റ് ആപ്പ്",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D)
                        )
                    }
                }

                // Settings / Branding Icon Button in Bento pastel circle
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BentoPastelPurple)
                        .clickable { onTabSelected(AdminNavTab.BRANDING) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "ഇവന്റ് ബ്രാൻഡിംഗ് & ക്രമീകരണങ്ങൾ",
                        tint = BentoDeepPurple,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
