package com.example.topoclimb.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.topoclimb.data.Contest
import com.example.topoclimb.data.ContestRankEntry
import com.example.topoclimb.data.ContestStep
import com.example.topoclimb.repository.FederatedTopoClimbRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

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
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
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
            
            // Load contest steps
            repository.getContestSteps(backendId, contestId)
                .onSuccess { steps ->
                    _uiState.value = _uiState.value.copy(steps = steps)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(steps = emptyList())
                }
            
            // Load global ranking
            repository.getContestRanking(backendId, contestId)
                .onSuccess { ranking ->
                    _uiState.value = _uiState.value.copy(globalRanking = ranking)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(globalRanking = emptyList())
                }
            
            _uiState.value = _uiState.value.copy(isLoading = false, isRefreshing = false)
        }
    }
    
    fun refreshContestDetails() {
        val backendId = _uiState.value.backendId ?: return
        val contestId = _uiState.value.contestId ?: return
        val contest = _uiState.value.contest ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            
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
            
            // Load global ranking with force refresh
            repository.getContestRanking(backendId, contestId, forceRefresh = true)
                .onSuccess { ranking ->
                    _uiState.value = _uiState.value.copy(globalRanking = ranking)
                }
                .onFailure { exception ->
                    // Don't fail the whole refresh if ranking fails
                    _uiState.value = _uiState.value.copy(globalRanking = emptyList())
                }
            
            // If a step is selected, refresh its ranking too
            _uiState.value.selectedStepId?.let { stepId ->
                repository.getStepRanking(backendId, contestId, stepId, forceRefresh = true)
                    .onSuccess { ranking ->
                        _uiState.value = _uiState.value.copy(selectedStepRanking = ranking)
                    }
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copy(selectedStepRanking = emptyList())
                    }
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
            repository.getStepRanking(backendId, contestId, stepId)
                .onSuccess { ranking ->
                    _uiState.value = _uiState.value.copy(selectedStepRanking = ranking)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(selectedStepRanking = emptyList())
                }
        }
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
