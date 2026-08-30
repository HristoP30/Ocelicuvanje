package com.hristop30.ocelicuvanje

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getDatabase(applicationContext)

        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppScreen(db)
                }
            }
        }
    }
}

@Composable
fun AppScreen(db: AppDatabase) {
    val people by db.personDao().getAll().collectAsState(initial = emptyList())
    var selectedPerson by remember { mutableStateOf<Person?>(null) }
    val scope = rememberCoroutineScope()

    val person = selectedPerson

    if (person == null) {
        PeopleScreen(
            people = people,
            onAdd = { name ->
                scope.launch {
                    db.personDao().insert(Person(name = name))
                }
            },
            onSelect = { selectedPerson = it }
        )
    } else {
        PlanScreen(
            db = db,
            person = person,
            onBack = { selectedPerson = null }
        )
    }
}
