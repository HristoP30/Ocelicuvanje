package com.hristop30.ocelicuvanje

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "days")
data class Day(
    val personId: Int,
    val name: String,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)

@Entity(tableName = "exercises")
data class Exercise(
    val dayId: Int,
    val name: String,
    val targetSets: Int,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)

@Entity(tableName = "sets")
data class SetEntry(
    val exerciseId: Int,
    val date: String,
    val setNumber: Int,
    val reps: Int,
    val weight: Double,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)

@Dao
interface DayDao {
    @Query("SELECT * FROM days WHERE personId = :personId ORDER BY id")
    fun getForPerson(personId: Int): Flow<List<Day>>

    @Insert
    suspend fun insert(day: Day)

    @Delete
    suspend fun delete(day: Day)
}

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises WHERE dayId = :dayId ORDER BY id")
    fun getForDay(dayId: Int): Flow<List<Exercise>>

    @Insert
    suspend fun insert(exercise: Exercise)

    @Delete
    suspend fun delete(exercise: Exercise)
}

@Dao
interface SetDao {
    @Query("SELECT * FROM sets WHERE exerciseId = :exerciseId ORDER BY date DESC, setNumber")
    fun getForExercise(exerciseId: Int): Flow<List<SetEntry>>

    @Insert
    suspend fun insert(set: SetEntry)
}

fun todayAsText(): String {
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
}

@Composable
private fun ScreenHeader(title: String, onBack: () -> Unit) {
    Column {
        TextButton(onClick = onBack) {
            Text("< Back")
        }
        Text(title, style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
fun PlanScreen(db: AppDatabase, person: Person, onBack: () -> Unit) {
    var selectedDay by remember { mutableStateOf<Day?>(null) }
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }

    val day = selectedDay
    val exercise = selectedExercise

    if (day == null) {
        DayListScreen(
            db = db,
            person = person,
            onBack = onBack,
            onSelectDay = { selectedDay = it }
        )
    } else if (exercise == null) {
        ExerciseListScreen(
            db = db,
            day = day,
            onBack = { selectedDay = null },
            onSelectExercise = { selectedExercise = it }
        )
    } else {
        SetEntryScreen(
            db = db,
            exercise = exercise,
            onBack = { selectedExercise = null }
        )
    }
}

@Composable
private fun DayListScreen(
    db: AppDatabase,
    person: Person,
    onBack: () -> Unit,
    onSelectDay: (Day) -> Unit
) {
    val days by db.dayDao().getForPerson(person.id).collectAsState(initial = emptyList())
    var dayName by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenHeader("${person.name}'s plan", onBack)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = dayName,
                onValueChange = { dayName = it },
                modifier = Modifier.weight(1f),
                label = { Text("Day name") }
            )

            Button(onClick = {
                val cleanName = dayName.trim()
                if (cleanName.isNotEmpty()) {
                    scope.launch {
                        db.dayDao().insert(Day(personId = person.id, name = cleanName))
                    }
                    dayName = ""
                }
            }) {
                Text("Add")
            }
        }

        days.forEach { day ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectDay(day) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(day.name, modifier = Modifier.padding(vertical = 16.dp))
                    TextButton(onClick = {
                        scope.launch { db.dayDao().delete(day) }
                    }) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseListScreen(
    db: AppDatabase,
    day: Day,
    onBack: () -> Unit,
    onSelectExercise: (Exercise) -> Unit
) {
    val exercises by db.exerciseDao().getForDay(day.id).collectAsState(initial = emptyList())
    var name by remember { mutableStateOf("") }
    var targetSets by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenHeader(day.name, onBack)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Exercise name") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = targetSets,
                onValueChange = { targetSets = it },
                label = { Text("Number of sets") },
                modifier = Modifier.weight(1f)
            )

            Button(onClick = {
                val sets = targetSets.toIntOrNull()
                val cleanName = name.trim()
                if (cleanName.isNotEmpty() && sets != null && sets > 0) {
                    scope.launch {
                        db.exerciseDao().insert(
                            Exercise(dayId = day.id, name = cleanName, targetSets = sets)
                        )
                    }
                    name = ""
                    targetSets = ""
                }
            }) {
                Text("Add")
            }
        }

        exercises.forEach { exercise ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectExercise(exercise) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${exercise.name} - ${exercise.targetSets} sets",
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    TextButton(onClick = {
                        scope.launch { db.exerciseDao().delete(exercise) }
                    }) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}

@Composable
private fun SetEntryScreen(
    db: AppDatabase,
    exercise: Exercise,
    onBack: () -> Unit
) {
    val allSets by db.setDao().getForExercise(exercise.id).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val reps = remember { mutableStateListOf(*Array(exercise.targetSets) { "" }) }
    val weights = remember { mutableStateListOf(*Array(exercise.targetSets) { "" }) }

    val history = allSets.groupBy { it.date }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenHeader(exercise.name, onBack)

        for (index in 0 until exercise.targetSets) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Set ${index + 1}")

                OutlinedTextField(
                    value = reps[index],
                    onValueChange = { reps[index] = it },
                    label = { Text("Reps") },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = weights[index],
                    onValueChange = { weights[index] = it },
                    label = { Text("Weight") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val valid = (0 until exercise.targetSets).all {
                    reps[it].toIntOrNull() != null && weights[it].toDoubleOrNull() != null
                }

                if (valid) {
                    scope.launch {
                        val date = todayAsText()
                        for (index in 0 until exercise.targetSets) {
                            db.setDao().insert(
                                SetEntry(
                                    exerciseId = exercise.id,
                                    date = date,
                                    setNumber = index + 1,
                                    reps = reps[index].toInt(),
                                    weight = weights[index].toDouble()
                                )
                            )
                            reps[index] = ""
                            weights[index] = ""
                        }
                    }
                }
            }
        ) {
            Text("Save sets")
        }

        Text("History", style = MaterialTheme.typography.titleMedium)

        history.forEach { (date, sets) ->
            Text(date, style = MaterialTheme.typography.titleSmall)
            sets.forEach { set ->
                Text("Set ${set.setNumber}: ${set.reps} reps at ${set.weight}")
            }
        }
    }
}
