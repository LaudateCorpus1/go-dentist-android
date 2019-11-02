package com.davidmiguel.godentist.manageclinics.add

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidmiguel.godentist.core.data.clinics.ClinicsRepository
import com.davidmiguel.godentist.core.model.Clinic
import com.davidmiguel.godentist.core.utils.Event
import com.davidmiguel.godentist.core.utils.ScreenState
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import com.davidmiguel.godentist.core.R as RC

class AddClinicViewModelImpl(
    private val firebaseAuth: FirebaseAuth,
    private val clinicsRepository: ClinicsRepository
) : ViewModel(), AddClinicViewModel {

    private val _screenState = MutableLiveData(ScreenState.INITIAL)
    override val screenState: LiveData<ScreenState>
        get() = _screenState

    private val _snackbarEvent = MutableLiveData<Event<Int>>()
    override val snackbarEvent: LiveData<Event<Int>>
        get() = _snackbarEvent

    private val _clinicUpdatedEvent = MutableLiveData<Event<Unit>>()
    override val clinicUpdatedEvent: LiveData<Event<Unit>>
        get() = _clinicUpdatedEvent

    private var clinicId: String? = null

    override val name = MutableLiveData<String>()
    private val _nameError = MutableLiveData(false)
    override val nameError: LiveData<Boolean>
        get() = _nameError

    override val percentage = MutableLiveData<String>()
    private val _percentageError = MutableLiveData(false)
    override val percentageError: LiveData<Boolean>
        get() = _percentageError

    override fun start(clinicId: String?) {
        if (_screenState.value != ScreenState.INITIAL) return
        if (clinicId != null) {
            this.clinicId = clinicId
            loadClinic(clinicId)
        } else {
            _screenState.value = ScreenState.DATA_LOADED
        }
    }

    private fun loadClinic(clinicId: String) {
        _screenState.value = ScreenState.LOADING_DATA
        clinicsRepository.get(firebaseAuth.uid!!, clinicId)
        viewModelScope.launch {

        }
        clinicsRepository.get(firebaseAuth.uid!!, clinicId)
            .addOnCompleteListener { task ->
                task.result?.run { onClinicLoaded(this) } ?: onErrorLoadingClinic()
            }
    }

    private fun onClinicLoaded(clinic: Clinic) {
        name.value = clinic.name
        _screenState.value = ScreenState.DATA_LOADED
    }

    private fun onErrorLoadingClinic() {
        _screenState.value = ScreenState.ERROR
    }

    override fun saveClinic() {
        _nameError.value = false
        _percentageError.value = false

        val currentId = clinicId ?: ""

        val currentName = name.value
        if (currentName.isNullOrBlank()) {
            _nameError.value = true
            return
        }

        val currentPercentage = percentage.value?.toIntOrNull()
        if (currentPercentage == null) {
            _percentageError.value = true
            return
        }

        clinicsRepository.update(
            firebaseAuth.uid!!, Clinic(currentId, currentName, currentPercentage)
        ).addOnSuccessListener {
            _clinicUpdatedEvent.value = Event(Unit)
            _snackbarEvent.value = Event(RC.string.all_dataSaved)
        }.addOnFailureListener {
            _snackbarEvent.value = Event(RC.string.all_errSavingData)
        }
    }
}