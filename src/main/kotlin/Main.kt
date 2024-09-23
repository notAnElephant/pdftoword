import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.lifecycle.viewmodel.compose.viewModel
import com.darkrockstudios.libraries.mpfilepicker.FilePicker

@Composable
@Preview

fun App(viewModel: MainViewModel = viewModel { MainViewModel() }) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.padding(8.dp),
        bottomBar = {
            Button(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                enabled = viewModel.doKeysMatch,
                onClick = { viewModel.saveDataToWord() }) {
                Text("Adatok mentése Wordbe")
            }
        },
    ) {
        Row()
        {
            Column(modifier = Modifier.weight(1f).padding(8.dp)) {

                FilePickerLoader("PDF", listOf("pdf")) { 
                    viewModel.readPdfForm(it)
                }
                Card(modifier = Modifier.padding(top = 8.dp)) {
                    LazyColumn {
                        item{Text("Kulcsok a PDF-ben")}
                        items(viewModel.formData.keys.toList()) { key ->
                            Text("$key: ${viewModel.formData[key]}")
                        }
                    }
                }
            }
            Column(modifier = Modifier.weight(1f).padding(8.dp)) {
                FilePickerLoader("Word", listOf("docx", "doc")) {
                    viewModel.readWordKeys(it)
                }
                Card(modifier = Modifier.padding(top = 8.dp)) {
                    LazyColumn {
                        item{Text("Kulcsok a Wordben")}
                        items(viewModel.wordData.keys.toList()) { key ->
                            Text(key)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilePickerLoader(fileType: String, extensions: List<String>, onButtonClicked: (String) -> Unit){
    var showFilePicker by remember { mutableStateOf(false) }
    var filePath by remember { mutableStateOf("") }
    Button(onClick = { showFilePicker = true }) {
        Text("$fileType helyének kiválasztása")
    }
    FilePicker(show = showFilePicker, fileExtensions = extensions) { platformFile ->
        showFilePicker = false
        filePath = platformFile?.path.toString()
    }
    TextField(
        value = filePath,
        onValueChange = { filePath = it },
        label = { Text("$fileType elérési útja") },
        modifier = Modifier.fillMaxWidth()
    )
    Button(onClick = { onButtonClicked(filePath) }) {
        Text("Kulcsok betöltése $fileType-ból/-ből")
    }
}


fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
