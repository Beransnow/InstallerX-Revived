package com.rosan.installer.ui.page.main.settings.config.all

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.rosan.installer.data.settings.model.datastore.AppDataStore
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity
import com.rosan.installer.data.settings.repo.ConfigRepo
import com.rosan.installer.data.settings.util.ConfigOrder
import com.rosan.installer.ui.page.main.settings.SettingsScreen
import com.rosan.installer.ui.page.miuix.settings.MiuixSettingsScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AllViewModel(
    var navController: NavController,
    private val repo: ConfigRepo,
    private val appDataStore: AppDataStore
) : ViewModel(), KoinComponent {
    val context by inject<Context>()

    var state by mutableStateOf(AllViewState())
        private set

    private val _eventFlow = MutableSharedFlow<AllViewEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun dispatch(action: AllViewAction) {
        when (action) {
            is AllViewAction.Init -> init()
            is AllViewAction.LoadData -> loadData()
            is AllViewAction.UserReadScopeTips -> userReadTips()
            is AllViewAction.ChangeDataConfigOrder -> changeDataConfigOrder(action.configOrder)
            is AllViewAction.DeleteDataConfig -> deleteDataConfig(action.configEntity)
            is AllViewAction.RestoreDataConfig -> restoreDataConfig(action.configEntity)
            is AllViewAction.EditDataConfig -> editDataConfig(action.configEntity)
            is AllViewAction.MiuixEditDataConfig -> editMiuixDataConfig(action.configEntity)
            is AllViewAction.ApplyConfig -> applyConfig(action.configEntity)
        }
    }

    private var isInit = false

    private fun init() {
        synchronized(this) {
            if (isInit) return
            isInit = true
            loadData()
        }
    }

    private var loadDataJob: Job? = null

    private fun loadData() {
        loadDataJob?.cancel()
        state = state.copy(
            data = state.data.copy(
                progress = AllViewState.Data.Progress.Loading
            )
        )
        loadDataJob = viewModelScope.launch(Dispatchers.IO) {
            val initialState = AllViewState(
                userReadScopeTips = appDataStore.getBoolean(AppDataStore.USER_READ_SCOPE_TIPS, default = false).first(),
            )
            repo.flowAll(state.data.configOrder).collect {
                state = state.copy(
                    userReadScopeTips = initialState.userReadScopeTips,
                    data = state.data.copy(
                        configs = it,
                        progress = AllViewState.Data.Progress.Loaded
                    )
                )
            }
        }
    }

    private fun userReadTips() {
        state = state.copy(userReadScopeTips = true)
        viewModelScope.launch {
            appDataStore.putBoolean(AppDataStore.USER_READ_SCOPE_TIPS, true)
        }
    }

    private fun editDataConfig(configEntity: ConfigEntity) {
        viewModelScope.launch {
            navController.navigate(
                SettingsScreen.Builder.EditConfig(
                    configEntity.id
                ).route
            )
        }
    }

    private fun editMiuixDataConfig(configEntity: ConfigEntity) {
        viewModelScope.launch {
            navController.navigate(
                MiuixSettingsScreen.Builder.MiuixEditConfig(
                    configEntity.id
                ).route
            )
        }
    }

    private fun changeDataConfigOrder(configOrder: ConfigOrder) {
        state = state.copy(
            data = state.data.copy(
                configOrder = configOrder
            )
        )
    }

    private fun deleteDataConfig(configEntity: ConfigEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.delete(configEntity)
            _eventFlow.emit(
                AllViewEvent.DeletedConfig(
                    configEntity = configEntity
                )
            )
        }
    }

    private fun restoreDataConfig(configEntity: ConfigEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.insert(configEntity)
        }
    }

    private fun applyConfig(configEntity: ConfigEntity) {
        viewModelScope.launch {
            navController.navigate(
                SettingsScreen.Builder.ApplyConfig(
                    configEntity.id
                ).route
            )
        }
    }
}