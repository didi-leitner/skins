package com.na.didi.hangerz.viewmodel

import android.util.Log
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
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class UploadsViewModel @ViewModelInject internal constructor(
        uploadsRepo: UploadsRepo
) : ViewModel() {


    /*private val mutableState = MutableStateFlow(GreetingState(false, "", ""))
    val statet: StateFlow<GreetingState>
        get() = mutableState*/

    private val uploadsRepository: UploadsRepo = uploadsRepo

    // Backing property to avoid state updates from other classes
    private val _state = MutableStateFlow<UploadsViewState>(UploadsViewState.Loading())
    // The UI collects from this StateFlow to get its state updates
    val state: StateFlow<UploadsViewState> = _state

    init {
        viewModelScope.launch {
            initUploads()
        }
    }


    fun bindIntents(viewContract: UploadsViewContract) {
        viewContract.initState().onEach {
            Log.v("TAGGG","ieieieieieiei " + it)

            state.filterNotNull().collect {
                Log.v("TAGGG","collect state, render " + it)
                viewContract.render(it)
            }

            Log.v("TAGGG","nu ajunge aici?")


        }.launchIn(viewModelScope)


        viewContract.loadFromNetwork().onEach {
            loadFromNetwork()
        }.launchIn(viewModelScope)

        viewContract.selectContent().onEachEvent { selectContent ->
            selectContent(selectContent)
        }.launchIn(viewModelScope)

    }

    suspend fun initUploads() {

        Log.v("TAGGG","initUploads")

        _state.value = UploadsViewState.Error()

        try {
            uploadsRepository.getUploads().collect { pagingData ->
                Log.v("TAGGG","collected data " + pagingData)
                _state.value = UploadsViewState.UploadsList(pagingData, null)
            }

            Log.v("TAGGG","after repo collect")
        } catch (exception: Exception) {
            _state.value = UploadsViewState.UploadsList(null, exception.localizedMessage!!)
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
      _state.value = UploadsViewState.OpenContent(selectContent.position, selectContent.content)
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
