package com.rosan.installer.ui.page.main.installer.dialog.inner

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rosan.installer.R
import com.rosan.installer.data.installer.repo.InstallerRepo
import com.rosan.installer.ui.page.main.installer.dialog.DialogInnerParams
import com.rosan.installer.ui.page.main.installer.dialog.DialogParams
import com.rosan.installer.ui.page.main.installer.dialog.DialogParamsType
import com.rosan.installer.ui.page.main.installer.dialog.InstallerViewAction
import com.rosan.installer.ui.page.main.installer.dialog.InstallerViewModel
import com.rosan.installer.ui.page.main.installer.dialog.InstallerViewState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun preparingDialog(
    installer: InstallerRepo, viewModel: InstallerViewModel
): DialogParams {
    val currentState = viewModel.state
    val progress = if (currentState is InstallerViewState.Preparing) {
        currentState.progress
    } else {
        -1f // Default to indeterminate if state is wrong
    }

    return DialogParams(
        icon = DialogInnerParams(
            DialogParamsType.IconWorking.id, workingIcon
        ),
        title = DialogInnerParams(
            DialogParamsType.InstallerPreparing.id,
        ) {
            Text(stringResource(R.string.installer_preparing))
        },
        text = DialogInnerParams(
            DialogParamsType.InstallerPreparing.id,
        ) {
            Column {
                Text(
                    text = stringResource(R.string.installer_preparing_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                if (progress < 0f) {
                    // Indeterminate progress
                    LinearWavyProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        amplitude = 0f // not wavy
                    )
                } else {
                    // Determinate progress with smooth animation
                    val animatedProgress by animateFloatAsState(
                        targetValue = progress,
                        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
                        label = "PreparingProgressAnimation"
                    )
                    LinearWavyProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxWidth(),
                        amplitude = { 0f }
                    )
                }
            }
        },
        buttons = DialogButtons(
            DialogParamsType.ButtonsCancel.id
        ) {
            listOf(DialogButton(stringResource(R.string.cancel)) {
                viewModel.dispatch(InstallerViewAction.Close)
            })
        }
    )
}