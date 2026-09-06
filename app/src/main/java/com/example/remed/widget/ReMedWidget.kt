package com.example.remed.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.remed.RemedApplication
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReMedWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as RemedApplication
        val authRepo = app.authRepository
        val userId = authRepo.currentUser?.uid ?: "GUEST_USER"
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val medList = app.database.medicationDao().getMedicationsList(userId)
        val pendingMed = medList.firstOrNull { !it.isTaken }
        val timeStr = if (pendingMed != null) {
            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(pendingMed.scheduledTime))
        } else ""

        val waterLog = app.database.waterDao().getLog(userId, today)
        val waterSettings = app.database.waterDao().getSettingsSync(userId)
        val waterIntake = waterLog?.amount ?: 0
        val waterGoal = waterSettings?.dailyGoal ?: 2000

        val stepLog = app.database.stepDao().getLog(userId, today)
        val stepSettings = app.database.stepDao().getSettingsSync(userId)
        val stepCount = stepLog?.count ?: 0
        val stepGoal = stepSettings?.dailyGoal ?: 5000

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A))
                    .padding(12.dp)
                    .cornerRadius(16.dp)
            ) {
                // Header
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ReMed Health",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                // Card 1: Next Medication
                Column(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B))
                        .padding(10.dp)
                        .cornerRadius(12.dp)
                ) {
                    Text(
                        text = "💊 Medication",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF38BDF8)),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))

                    if (pendingMed != null) {
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = GlanceModifier.defaultWeight()) {
                                Text(
                                    text = pendingMed.name,
                                    style = TextStyle(
                                        color = ColorProvider(Color.White),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "${pendingMed.dosage} • $timeStr",
                                    style = TextStyle(
                                        color = ColorProvider(Color(0xFF94A3B8)),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                            Button(
                                text = "✓ Taken",
                                onClick = actionRunCallback<MarkMedicationTakenAction>(
                                    actionParametersOf(MarkMedicationTakenAction.medIdKey to pendingMed.id)
                                )
                            )
                        }
                    } else {
                        Text(
                            text = "✓ All medications taken today",
                            style = TextStyle(
                                color = ColorProvider(Color(0xFF4ADE80)),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                // Card 2: Hydration & Steps
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Water
                    Column(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .background(Color(0xFF1E293B))
                            .padding(8.dp)
                            .cornerRadius(10.dp)
                    ) {
                        Text(
                            text = "💧 Water",
                            style = TextStyle(color = ColorProvider(Color(0xFF38BDF8)), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "$waterIntake / $waterGoal ml",
                            style = TextStyle(color = ColorProvider(Color.White), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        Button(
                            text = "+250 ml",
                            onClick = actionRunCallback<AddWaterAction>()
                        )
                    }

                    Spacer(modifier = GlanceModifier.width(8.dp))

                    // Steps
                    Column(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .background(Color(0xFF1E293B))
                            .padding(8.dp)
                            .cornerRadius(10.dp)
                    ) {
                        Text(
                            text = "👟 Steps",
                            style = TextStyle(color = ColorProvider(Color(0xFF4ADE80)), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "$stepCount / $stepGoal",
                            style = TextStyle(color = ColorProvider(Color.White), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
