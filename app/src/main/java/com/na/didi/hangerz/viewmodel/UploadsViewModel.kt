package com.na.didi.hangerz.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.na.didi.hangerz.data.repository.UploadsRepo
import com.na.didi.hangerz.util.onEachEvent
import com.na.didi.hangerz.view.viewcontract.UploadsViewContract
import com.na.didi.hangerz.view.viewintent.UploadsViewIntent
import com.na.didi.hangerz.view.viewstate.UploadsViewState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
class UploadsViewModel @ViewModelInject internal constructor(
        uploadsRepo: UploadsRepo
) : ViewModel() {

    private val uploadsRepository: UploadsRepo = uploadsRepo
    private val state = MutableStateFlow<UploadsViewState?>(null)

    fun bindIntents(viewContract: UploadsViewContract) {
        viewContract.initState().onEach {
            state.filterNotNull().collect {
                viewContract.render(it)
            }

            initUploads()
        }.launchIn(viewModelScope)


        viewContract.loadFromNetwork().onEach {
            loadFromNetwork()
        }.launchIn(viewModelScope)

        viewContract.selectContent().onEachEvent { selectContent ->
            selectContent(selectContent)
        }.launchIn(viewModelScope)

    }

    private suspend fun initUploads() {

        state.value = UploadsViewState.Loading()

        try {
            uploadsRepository.getUploads().collect { pagingData ->
                state.value = UploadsViewState.UploadsList(pagingData, null)
            }
        } catch (exception: Exception) {
            state.value = UploadsViewState.UploadsList(null, exception.localizedMessage!!)
        }

    }

     /*fun initFeed() = flow {
        emit(UploadsViewState.Feed(null, null))
        try {
            uploadsRepository.getUploads().collect { pagingData ->
                emit(UploadsViewState.Feed(pagingData, null))
            }
        } catch (exception: Exception) {
            emit(UploadsViewState.Feed(null, exception.localizedMessage!!))
        }
    }*/

   private fun selectContent(selectContent: UploadsViewIntent.SelectContent) {
      state.value = UploadsViewState.OpenContent(selectContent.position, selectContent.content)
   }

    private fun loadFromNetwork() {
        /*uploadsRepository.getMainFeedNetwork(...).onEach { resource ->
            when (resource.status) {
               LOADING -> repository.getMainFeedRoom(...
                    )
                    .onEach { pagedList ->
                    // Pass local data.
                    state.value = FeedViewState.Feed(...)
                }.launchIn(coroutineScope)
               SUCCESS -> resource.data?.collect { pagedList ->
                  // Pass network data.
                  state.value = FeedViewState.Feed(...)
               }
               ERROR -> repository.getMainFeedRoom(...
                    )
                    .onEach { pagedList ->
                    // Pass local data.
                    state.value = FeedViewState.Feed(...)
                }.launchIn(coroutineScope)
            }
        }.launchIn(coroutineScope)*/
    }




}
