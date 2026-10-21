package com.lodkin.dartstrainer.ui

import androidx.compose.runtime.Composable
import com.lodkin.dartstrainer.data.training.NormativesStateStorage
import com.lodkin.dartstrainer.data.training.TrainingStateStorage
import com.lodkin.dartstrainer.ui.training.BullDrawDialog
import com.lodkin.dartstrainer.ui.training.ExitTrainingDialog
import com.lodkin.dartstrainer.ui.training.FreeNewRoundDialog
import com.lodkin.dartstrainer.ui.training.NormativesResumeDialog
import com.lodkin.dartstrainer.ui.training.ResumeTrainingDialog
import com.lodkin.dartstrainer.ui.training.TimeUpDialog

/**
 * Маршрутизатор для всех всплывающих диалогов.
 */
@Composable
fun DialogsRoutes(
    context: android.content.Context,

    // ── Bull Draw ──
    showBullDraw: Boolean,
    bullDrawExerciseId: String?,
    playerName: String,
    botName: String,
    botLevel: Int,
    onBullWinnerChosen: (Int) -> Unit,
    onBullCancel: () -> Unit,

    // ── Exit Training ──
    showExitTrainingDialog: Boolean,
    onExitFinish: () -> Unit,
    onExitPostpone: () -> Unit,

    // ── Time Up ──
    showTimeUpDialog: Boolean,
    trainingMinutes: Int,
    onTimeUpFinish: () -> Unit,
    onTimeUpContinue: () -> Unit,

    // ── Free New Round ──
    showFreeNewRoundDialog: Boolean,
    onNewRound: () -> Unit,
    onFinishTraining: () -> Unit,

    // ── Resume Training ──
    showResumeDialog: Boolean,
    pendingResume: TrainingStateStorage.Snapshot?,
    onResumeContinue: () -> Unit,
    onResumeRestart: () -> Unit,

    // ── Resume Normatives ──
    showResumeNormativesDialog: Boolean,
    pendingNormativesResume: NormativesStateStorage.Snapshot?,
    onNormativesResumeContinue: () -> Unit,
    onNormativesResumeRestart: () -> Unit
) {
    if (showBullDraw && bullDrawExerciseId != null) {
        BullDrawDialog(
            playerName = playerName,
            botName = botName,
            botLevel = botLevel,
            onWinnerChosen = onBullWinnerChosen,
            onCancel = onBullCancel
        )
    }

    if (showExitTrainingDialog) {
        ExitTrainingDialog(
            onFinish = onExitFinish,
            onPostpone = onExitPostpone
        )
    }

    if (showTimeUpDialog) {
        TimeUpDialog(
            trainingMinutes = trainingMinutes,
            onFinish = onTimeUpFinish,
            onContinuePlus20 = onTimeUpContinue
        )
    }

    if (showFreeNewRoundDialog) {
        FreeNewRoundDialog(
            onNewRound = onNewRound,
            onFinish = onFinishTraining
        )
    }

    if (showResumeDialog && pendingResume != null) {
        val minutesAgo = TrainingStateStorage.minutesSinceLastUpdate(context)
        ResumeTrainingDialog(
            minutesAgo = minutesAgo,
            onContinue = onResumeContinue,
            onRestart = onResumeRestart
        )
    }

    if (showResumeNormativesDialog && pendingNormativesResume != null) {
        val minutesAgo = NormativesStateStorage.minutesSinceLastUpdate(context)
        NormativesResumeDialog(
            minutesAgo = minutesAgo,
            onContinue = onNormativesResumeContinue,
            onRestart = onNormativesResumeRestart
        )
    }
}
