package com.hristop30.ocelicuvanje

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class Person(
    val name: String
)

@Composable
fun PeopleScreen(
    people: List<Person>,
    onAdd: (String) -> Unit,
    onSelect: (Person) -> Unit
) {
    var name = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Who is training?",
            style = MaterialTheme.typography.headlineSmall
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = name.value,
                onValueChange = { name.value = it },
                modifier = Modifier.weight(1f),
                label = { Text("Name") }
            )

            Button(
                onClick = {
                    if (name.value.isNotBlank()) {
                        onAdd(name.value.trim())
                        name.value = ""
                    }
                }
            ) {
                Text("Add")
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(people) { person ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(person) }
                ) {
                    Text(
                        text = person.name,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
