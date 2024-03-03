package lib.asyncimage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = "first"
            ) {
                composable("first") { FirstScreen(navController) }
                composable("second") { SecondScreen(navController) }
            }
        }
    }

    @Composable
    fun FirstScreen(navController: NavHostController) {
        Column(modifier = Modifier
            .fillMaxSize()
            .background(color = androidx.compose.ui.graphics.Color.DarkGray),
            Arrangement.Center, Alignment.Start) {
            Greeting(
                "Android", modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.1f)
                    .background(color = androidx.compose.ui.graphics.Color.Green)
                    .clickable {
                        // testUrl()
                    })
            AsyncImage("https://i.ytimg.com/vi/6ZfuNTqbHE8/default.jpg",
                modifier = Modifier.width(100.dp).height(70.dp), scaleFactor = 2f)
            Button(onClick = { navController.navigate("second") }) {
                Text("second screen")
            }
        }
    }

    @Composable
    fun SecondScreen(navController: NavHostController) {
        Column(modifier = Modifier
            .fillMaxSize()
            .background(color = androidx.compose.ui.graphics.Color.DarkGray),
            Arrangement.Center, Alignment.Start) {
            Greeting(
                "Android", modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.1f)
                    .background(color = androidx.compose.ui.graphics.Color.Green))

            Button(onClick = { navController.navigate("first") }) {
                Text("first screen")
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}