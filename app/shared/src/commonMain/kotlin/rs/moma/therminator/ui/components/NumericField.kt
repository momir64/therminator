package rs.moma.therminator.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier

@Composable
fun NumericField(label: String, value: Float?, modifier: Modifier = Modifier.fillMaxWidth(), onChange: (Float?) -> Unit) {
    OutlinedTextField(
        value = value?.toString().orEmpty(),
        modifier = modifier,
        onValueChange = {
            when {
                it.isEmpty() -> onChange(null)
                '.' !in it && value != null -> {}
                value == 0f -> {
                    val parts = it.split('.')
                    val integerPart = parts[0].dropLast(1).ifEmpty { "0" }
                    val decimalPart = parts.getOrNull(1)?.let { decimal -> ".$decimal" }.orEmpty()
                    onChange((integerPart + decimalPart).toFloatOrNull() ?: value)
                }

                it.toFloatOrNull()?.toString()?.contains("e", true)?.not() ?: false -> onChange(it.toFloatOrNull() ?: value)
            }
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@Composable
fun NumericField(label: String, value: Int?, modifier: Modifier = Modifier.fillMaxWidth(), onChange: (Int?) -> Unit) {
    OutlinedTextField(
        value = value?.toString().orEmpty(),
        modifier = modifier,
        onValueChange = {
            when {
                it.isEmpty() -> onChange(null)
                else -> onChange(it.toIntOrNull() ?: value)
            }
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}