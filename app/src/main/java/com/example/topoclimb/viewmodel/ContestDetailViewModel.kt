package com.example.topoclimb.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.topoclimb.data.Contest
import com.example.topoclimb.data.ContestCategory
import com.example.topoclimb.data.ContestRankEntry
import com.example.topoclimb.data.ContestStep
import com.example.topoclimb.repository.FederatedTopoClimbRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

enum class StepState {
    UPCOMING,
    ACTIVE,
    ENDED
}

data class ContestDetailUiState(
    val contest: Contest? = null,
    val steps: List<ContestStep> = emptyList(),
    val globalRanking: List<ContestRankEntry> = emptyList(),
    val selectedStepId: Int? = null,
    val selectedStepRanking: List<ContestRankEntry> = emptyList(),
    val categories: List<ContestCategory> = emptyList(),
    val userCategoryIds: List<Int> = emptyList(),
    val selectedCategoryId: Int? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val snackbarMessage: String? = null,
    val backendId: String? = null,
    val contestId: Int? = null
)

class ContestDetailViewModel(
    application: Application
) : AndroidViewModel(application) {
    
    private val repository = FederatedTopoClimbRepository(application)
    
    private val _uiState = MutableStateFlow(ContestDetailUiState())
    val uiState: StateFlow<ContestDetailUiState> = _uiState.asStateFlow()
    
    fun loadContestDetails(backendId: String, contestId: Int, contest: Contest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                backendId = backendId,
                contestId = contestId,
                contest = contest
            )
            
            // Load contest categories
            repository.getContestCategories(backendId, contestId)
                .onSuccess { categories ->
                    _uiState.value = _uiState.value.copy(categories = categories)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(categories = emptyList())
                }
            
            // Load user's categories
            repository.getUserCategories(backendId, contestId)
                .onSuccess { userCategoryIds ->
                    _uiState.value = _uiState.value.copy(userCategoryIds = userCategoryIds)
                }
                .onFailure { exception ->
                    // User not authenticated or no categories - that's ok
                    _uiState.value = _uiState.value.copy(userCategoryIds = emptyList())
                }
            
            // Load contest steps
            repository.getContestSteps(backendId, contestId)
                .onSuccess { steps ->
                    _uiState.value = _uiState.value.copy(steps = steps)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(steps = emptyList())
                }
            
            // Load global ranking (or category ranking if a category is selected)
            loadRankingData(backendId, contestId)
            
            _uiState.value = _uiState.value.copy(isLoading = false, isRefreshing = false)
        }
    }
    
    private suspend fun loadRankingData(backendId: String, contestId: Int) {
        val categoryId = _uiState.value.selectedCategoryId
        
        if (categoryId != null) {
            repository.getCategoryRanking(backendId, contestId, categoryId)
                .onSuccess { ranking ->
                    _uiState.value = _uiState.value.copy(globalRanking = ranking)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(globalRanking = emptyList())
                }
        } else {
            repository.getContestRanking(backendId, contestId)
                .onSuccess { ranking ->
                    _uiState.value = _uiState.value.copy(globalRanking = ranking)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(globalRanking = emptyList())
                }
        }
    }
    
    fun refreshContestDetails() {
        val backendId = _uiState.value.backendId ?: return
        val contestId = _uiState.value.contestId ?: return
        val contest = _uiState.value.contest ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            
            // Refresh categories
            repository.getContestCategories(backendId, contestId)
                .onSuccess { categories ->
                    _uiState.value = _uiState.value.copy(categories = categories)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(categories = emptyList())
                }
            
            // Refresh user's categories
            repository.getUserCategories(backendId, contestId)
                .onSuccess { userCategoryIds ->
                    _uiState.value = _uiState.value.copy(userCategoryIds = userCategoryIds)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(userCategoryIds = emptyList())
                }
            
            // Load contest steps with force refresh
            repository.getContestSteps(backendId, contestId, forceRefresh = true)
                .onSuccess { steps ->
                    _uiState.value = _uiState.value.copy(steps = steps)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Failed to refresh contest steps",
                        isRefreshing = false
                    )
                    return@launch
                }
            
            // Refresh global or category ranking
            loadRankingData(backendId, contestId)
            
            // If a step is selected, refresh its ranking too
            _uiState.value.selectedStepId?.let { stepId ->
                loadStepRankingData(backendId, contestId, stepId)
            }
            
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }
    
    fun selectStep(stepId: Int?) {
        val backendId = _uiState.value.backendId ?: return
        val contestId = _uiState.value.contestId ?: return
        
        _uiState.value = _uiState.value.copy(selectedStepId = stepId)
        
        if (stepId == null) {
            _uiState.value = _uiState.value.copy(selectedStepRanking = emptyList())
            return
        }
        
        viewModelScope.launch {
            loadStepRankingData(backendId, contestId, stepId)
        }
    }
    
    private suspend fun loadStepRankingData(backendId: String, contestId: Int, stepId: Int) {
        val categoryId = _uiState.value.selectedCategoryId
        
        if (categoryId != null) {
            repository.getCategoryStepRanking(backendId, contestId, categoryId, stepId)
                .onSuccess { ranking ->
                    _uiState.value = _uiState.value.copy(selectedStepRanking = ranking)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(selectedStepRanking = emptyList())
                }
        } else {
            repository.getStepRanking(backendId, contestId, stepId)
                .onSuccess { ranking ->
                    _uiState.value = _uiState.value.copy(selectedStepRanking = ranking)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(selectedStepRanking = emptyList())
                }
        }
    }
    
    fun selectCategory(categoryId: Int?) {
        val backendId = _uiState.value.backendId ?: return
        val contestId = _uiState.value.contestId ?: return
        
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
        
        viewModelScope.launch {
            // Reload global ranking with selected category
            loadRankingData(backendId, contestId)
            
            // If a step is selected, reload its ranking too
            _uiState.value.selectedStepId?.let { stepId ->
                loadStepRankingData(backendId, contestId, stepId)
            }
        }
    }
    
    fun toggleCategoryRegistration(categoryId: Int) {
        val backendId = _uiState.value.backendId ?: return
        val contestId = _uiState.value.contestId ?: return
        
        viewModelScope.launch {
            val isRegistered = _uiState.value.userCategoryIds.contains(categoryId)
            
            val result = if (isRegistered) {
                repository.unregisterFromCategory(backendId, contestId, categoryId)
            } else {
                repository.registerToCategory(backendId, contestId, categoryId)
            }
            
            result.onSuccess {
                // Refresh user categories
                repository.getUserCategories(backendId, contestId)
                    .onSuccess { userCategoryIds ->
                        _uiState.value = _uiState.value.copy(userCategoryIds = userCategoryIds)
                    }
            }.onFailure { exception ->
                // Check if it's an authentication error
                val message = exception.message ?: "Failed to update category registration"
                if (message.contains("not authenticated", ignoreCase = true) || 
                    message.contains("User not authenticated", ignoreCase = true)) {
                    _uiState.value = _uiState.value.copy(
                        snackbarMessage = "You must be logged in to register for categories"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        snackbarMessage = message
                    )
                }
            }
        }
    }
    
    fun clearSnackbarMessage() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }
    
    fun getStepState(step: ContestStep): StepState {
        return try {
            val now = Instant.now()
            val startTime = Instant.parse(step.startTime)
            val endTime = Instant.parse(step.endTime)
            
            when {
                now.isBefore(startTime) -> StepState.UPCOMING
                now.isAfter(endTime) -> StepState.ENDED
                else -> StepState.ACTIVE
            }
        } catch (e: Exception) {
            StepState.ENDED
        }
    }
}
