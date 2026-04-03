package but.info.projet.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import but.info.projet.data.Club
import but.info.projet.data.ClubViewModel
import but.info.projet.ui.theme.ProjetTheme
import kotlin.getValue

class MainActivity : ComponentActivity() {

     private val clubViewModel: ClubViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProjetTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                   ClubScreen(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = clubViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun ClubScreen(modifier: Modifier, viewModel: ClubViewModel) {

    LaunchedEffect(Unit) {
        viewModel.loadClubs()
    }

    Log.e("clubs",  viewModel.clubs.toString())

    if(viewModel.error != null)
        Row(modifier = modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center){Text(viewModel.error!!)}
    else
        ClubList(viewModel.clubs.value) { club ->
            viewModel.deactivateClub(club)
        }
}

@Composable
fun ClubList(
    clubs: List<Club>,
    onDeactivate: (Club) -> Unit
) {
    LazyColumn {
        items(clubs) { club ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                ClubItem(
                    club = club,
                    onDeactivate = onDeactivate
                )
            }
        }
    }
}
@Composable
fun ClubItem(
    club: Club,
    onDeactivate: (Club) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {

        Text(text = club.name)
        Text(text = club.address)

        if (club.active == 0) {
            Text("Désactivé", color = Color.Red)
        } else {
            Button(onClick = { onDeactivate(club) }) {
                Text("Désactiver")
            }
        }
    }
}