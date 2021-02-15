# skin-journey

Augmented Reality sample app for skincare, it is a work-in-progress. Purpose is to track the face by taking selfies (TODO), and track the products by snapping a photo of their packaging (identification & ingredients TODO).

This is a WIP, working use-case is the following:

-> open camera -> visually search for a product packaging (something with text) -> if text was found, simulate processing w delay, and display products in a bottom-sheet -> tap on a result  to confirm it -> product will be added to DB -> user navigated to MyProducts fragment. 

#TechStack:
Using CameraX -> lifecycle-aware camera, where preview and image-analysys use cases were bound.
Using MLKit -> Object-Detection and Text-Detection processors to identify detect objects and text in the  virtual environment.
Other -> Kotlin coroutines & Flows, Room, Paging3, NavigationComponent, Mockito, Android-Hilt(DI)


MVI  architecture is used. To enforce the architecture, and make it easier to implement, i created the base classes BaseViewModelMVI<S,E,I> and BaseFragmentMVI<...>, so every ViewModel and Fragment in the app should extends from these.


#Intents -> represent actions from the user or the app itself (ex. button clicks). Implemented as a Channel, (but consumed as a Flow, to avoid deprecation) - needed this to be a fire-and-forget kind of thing, so I used a Channel - it can't be a MutableStateFlow, because it is not stateful, and each event should be received by the ViewModel not just the changes. When the view is created, the Fragment calls .bindViewIntents, so the ViewModel will observe the user events.

#States -> represents the Model from MVI and it should be immutable. Upon receiving an Intent (event), the ViewModel could updtate the State (or fire an Effect). The state is  implemented as a MutableStateFlow in the ViewModel, and exposed to the View via a Flow.

#Effects -> represents one-time events that should be displayed in the UI as a result of an Intent received in the ViewModel (ex snackbar  or navigation). Implemented as a Channel, exposed as a Flow.

#Views -> The Fragment will render States and Effects, these should be the only UI modifications, nothing else should be able to modify the UI besides the renderState and renderEffect methods. The View will collect the State and Effect Flows, and will update the UI accordingly.

#ViewModels -> receives Intents from the View, and holds & updates the State (or fire effects). Generally it should only have one public  method, bindViewIntents, to implement the communication from the View to this ViewModel. The ViewModel receives Intents, and updates the State.




