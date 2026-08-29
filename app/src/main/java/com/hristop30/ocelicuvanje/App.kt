package com.hristop30.ocelicuvanje

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.room.Room
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "ocelicuvanje.db")
            .build()

        setContent {
            AppTheme {
                AppScreen(db)
            }
        }
    }
}

@Composable
fun AppScreen(db: AppDatabase) {
    val people by db.personDao().getAll().collectAsState(initial = emptyList())
    var selectedPerson by remember { mutableStateOf<Person?>(null) }
    val scope = rememberCoroutineScope()

    if (selectedPerson == null) {
        PeopleScreen(
            people = people,
            onAdd = { name ->
                scope.launch { db.personDao().insert(Person(name = name)) }
            },
            onSelect = { person ->
                selectedPerson = person
            }
        )
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Plan",
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}
