package au.edu.jcu.assessment.utilityapp.ui

import au.edu.jcu.assessment.utilityapp.data.model.Place

/** What the place search in Settings should show under the text field. */
sealed interface PlaceSearchState {
    /** Nothing typed yet, or too little to search for. */
    data object Idle : PlaceSearchState
    data object Loading : PlaceSearchState
    data class Results(val places: List<Place>) : PlaceSearchState
    data object Empty : PlaceSearchState
    data object Error : PlaceSearchState
}
