package com.na.didi.hangerz.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.na.didi.hangerz.repo.UploadsRepo

class UploadsViewModel @ViewModelInject internal constructor(
uploadsRepository: UploadsRepo
) : ViewModel() {
    //val picsList: LiveData<List<PlantAndGardenPlantings>> =
    //    picsRepository.getPlantedGardens()
}
