package com.hristop30.ocelicuvanje

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                AppScreen()
            }
        }
    }
}

@Composable
fun AppScreen() {
    val people = remember {
        mutableStateListOf(
            Person("Person 1"),
            Person("Person 2")
        )
    }

    var selected = remember { androidx.compose.runtime.mutableStateOf<Person?>(null) }

    if (selected.value == null) {
        PeopleScreen(
            people = people,
            onAdd = { name ->
                people.add(Person(name))
            },
            onSelect = { person ->
                selected.value = person
            }
        )
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Plan goes here: ${selected.value!!.name}",
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}
