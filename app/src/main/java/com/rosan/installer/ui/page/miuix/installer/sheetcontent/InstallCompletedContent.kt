package com.rosan.installer.ui.page.miuix.installer.sheetcontent

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rosan.installer.R
import com.rosan.installer.data.app.model.entity.AppEntity
import com.rosan.installer.data.installer.model.entity.InstallResult
import com.rosan.installer.util.help
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardColors
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.useful.Cancel
import top.yukonga.miuix.kmp.icon.icons.useful.Confirm
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Composable
fun InstallCompletedContent(
    results: List<InstallResult>,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.weight(1f, fill = false)) {
            LazyColumn(
                modifier = Modifier
                    .wrapContentSize()
                    .scrollEndHaptic()
                    .overScrollVertical(),
                overscrollEffect = null,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(results, key = { it.entity.app.packageName + it.entity.app.name }) { result ->
                    MiuixResultItemCard(result = result)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
        ) {
            TextButton(
                onClick = onClose,
                text = stringResource(R.string.finish),
                colors = ButtonDefaults.textButtonColorsPrimary(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MiuixResultItemCard(result: InstallResult) {
    val app = result.entity.app
    val appLabel = (app as? AppEntity.BaseEntity)?.label ?: app.packageName
    val cardColor = if (isSystemInDarkTheme()) Color(0xFF434343) else Color.White

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardColors(
            color = cardColor,
            contentColor = MiuixTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column {
                Text(
                    text = appLabel,
                    style = MiuixTheme.textStyles.headline1
                )
                Text(
                    text = app.packageName,
                    style = MiuixTheme.textStyles.subtitle,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            }

            if (result.success)
                MiuixSuccessRow()
            else if (result.error != null)
                MiuixCompletedErrorCardContent(error = result.error)
        }
    }
}

@Composable
private fun MiuixSuccessRow() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = MiuixIcons.Useful.Confirm,
            contentDescription = "Success",
            tint = MiuixTheme.colorScheme.primary
        )
        Text(
            text = stringResource(R.string.installer_install_success),
            style = MiuixTheme.textStyles.headline1,
            color = MiuixTheme.colorScheme.primary
        )
    }
}

@Composable
private fun MiuixCompletedErrorCardContent(
    error: Throwable,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val contentColor = if (isDark) MiuixTheme.colorScheme.onSurface else Color(0xFF601A15)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = MiuixIcons.Useful.Cancel,
                contentDescription = stringResource(R.string.installer_install_failed),
                tint = contentColor
            )
            Text(
                text = error.help(),
                fontWeight = FontWeight.Bold,
                style = MiuixTheme.textStyles.body1,
                color = contentColor
            )
        }

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp),
            color = MaterialTheme.colorScheme.outline
        )

        Text(
            text = (error.message ?: "An unknown error occurred.").trim(),
            style = MiuixTheme.textStyles.subtitle,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 12.dp)
        )
    }
}