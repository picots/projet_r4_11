package but.info.projet.data

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClubViewModel : ViewModel() {

    private val repository = ClubRepository()

    var clubs = mutableStateOf<List<Club>>(emptyList())
    var error : String? = null

    fun loadClubs() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.e("clubs", repository.getClubs().toString())
                clubs.value = repository.getClubs()
            } catch (e : Exception) {
                error = e.message!!
            }
        }
    }

    fun deactivateClub(club: Club) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deactivateClub(club.id)

            clubs.value = clubs.value.map {
                if (it.id == club.id)
                    it.copy(active = 0)
                else it
            }
        }
    }
}