package com.rosan.installer.ui.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import com.rosan.installer.R
import com.rosan.installer.data.installer.model.entity.ProgressEntity
import com.rosan.installer.data.installer.repo.InstallerRepo
import com.rosan.installer.ui.page.main.installer.InstallerPage
import com.rosan.installer.ui.theme.InstallerMaterialExpressiveTheme
import com.rosan.installer.ui.util.PermissionDenialReason
import com.rosan.installer.ui.util.PermissionManager
import com.rosan.installer.util.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import timber.log.Timber

class UninstallerActivity : ComponentActivity(), KoinComponent {

    companion object {
        const val KEY_ID = "uninstaller_id"
        const val EXTRA_PACKAGE_NAME = "package_name"
    }

    private var installer: InstallerRepo? = null
    private var job: Job? = null
    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        // Compat Navigation Bar color for Xiaomi Devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            window.isNavigationBarContrastEnforced = false
        super.onCreate(savedInstanceState)
        Timber.d("UninstallerActivity onCreate.")

        permissionManager = PermissionManager(this)
        val installerId = savedInstanceState?.getString(KEY_ID)
        installer = get { parametersOf(installerId) }

        // Start the process only if it's a fresh launch, not a configuration change
        if (savedInstanceState == null) {
            var packageName: String?
            // First, try to get it from our custom extra (for internal calls)
            packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)

            // If not found, try to get it from the intent data (for system calls)
            if (packageName.isNullOrBlank()) {
                val action = intent.action
                if (action == Intent.ACTION_UNINSTALL_PACKAGE || action == Intent.ACTION_DELETE) {
                    intent.data?.schemeSpecificPart?.let {
                        packageName = it
                    }
                }
            }

            if (packageName.isNullOrBlank()) {
                Timber.e("UninstallerActivity started without a package name.")
                finish()
                return
            }

            Timber.d("Target package to uninstall: $packageName")
            // Trigger the uninstall resolution process
            requestPermissionsAndProceed(packageName)
        }

        startCollectors()
        showContent()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val currentId = installer?.id
        outState.putString(KEY_ID, currentId)
        Timber.d("UninstallerActivity onSaveInstanceState: Saving id: $currentId")
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        job?.cancel()
        job = null
        // Do not call installer.close() here if you want the process to continue in the background
        Timber.d("UninstallerActivity is being destroyed.")
        super.onDestroy()
    }

    private fun requestPermissionsAndProceed(packageName: String) {
        permissionManager.requestEssentialPermissions(
            onGranted = {
                Timber.d("Permissions granted. Proceeding with uninstall for $packageName")
                installer?.resolveUninstall(this@UninstallerActivity, packageName)
            },
            onDenied = { reason ->
                when (reason) {
                    PermissionDenialReason.NOTIFICATION -> {
                        Timber.w("Notification permission was denied.")
                        this.toast(R.string.enable_notification_hint)
                    }

                    PermissionDenialReason.STORAGE -> {
                        Timber.w("Storage permission was denied.")
                        this.toast(R.string.enable_storage_permission_hint)
                    }
                }
                finish()
            }
        )
    }

    private fun startCollectors() {
        job?.cancel()
        val scope = CoroutineScope(Dispatchers.Main.immediate)
        job = scope.launch {
            installer?.progress?.collect { progress ->
                Timber.d("[id=${installer?.id}] Activity collected progress: ${progress::class.simpleName}")
                // Finish the activity on final states
                if (progress is ProgressEntity.Finish) {
                    if (!this@UninstallerActivity.isFinishing) this@UninstallerActivity.finish()
                }
            }
        }
    }

    private fun showContent() {
        setContent {
            val currentInstaller = installer
            if (currentInstaller == null) {
                // If repo is null, we can't proceed.
                LaunchedEffect(Unit) {
                    finish()
                }
                return@setContent
            }

            InstallerMaterialExpressiveTheme {
                // The UninstallerPage composable will be created later.
                // It will be responsible for creating the ViewModel and displaying the UI.
                InstallerPage(installer = currentInstaller)
            }
        }
    }
}