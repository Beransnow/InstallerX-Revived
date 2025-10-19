package com.rosan.installer.ui.page.main.installer.dialog

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import com.rosan.installer.data.installer.repo.InstallerRepo
import com.rosan.installer.ui.page.main.widget.dialog.PositionDialog
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun DialogPage(
    installer: InstallerRepo, viewModel: InstallerViewModel = koinViewModel {
        parametersOf(installer)
    }
) {
    LaunchedEffect(installer.id) {
        viewModel.dispatch(InstallerViewAction.CollectRepo(installer))
    }
    val params = dialogGenerateParams(installer, viewModel)
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "dialog_alpha"
    )

    Box(
        modifier = Modifier.alpha(alpha)
    ) {
        PositionDialog(
            onDismissRequest = {
                if (viewModel.isDismissible) {
                    // Only allow dismiss if the current state is dismissible
                    // If the setting is enabled, close the dialog directly.
                    // Otherwise, send it to the background (which shows a notification).
                    if (viewModel.disableNotificationOnDismiss) {
                        viewModel.dispatch(InstallerViewAction.Close)
                    } else {
                        viewModel.dispatch(InstallerViewAction.Background)
                    }
                }
            },
            centerIcon = dialogInnerWidget(installer, params.icon),
            centerTitle = dialogInnerWidget(installer, params.title),
            centerSubtitle = dialogInnerWidget(installer, params.subtitle),
            centerText = dialogInnerWidget(installer, params.text),
            centerContent = dialogInnerWidget(installer, params.content),
            centerButton = dialogInnerWidget(installer, params.buttons)
        )
    }
}