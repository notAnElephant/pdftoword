import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.DialogWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberDialogState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import java.io.File

@Composable
@Preview
fun App(viewModel: MainViewModel = viewModel { MainViewModel() }) {
	val uiState by viewModel.uiState.collectAsState()
	var fileSavedDialogState by remember { mutableStateOf(false) }

	Scaffold(
		modifier = Modifier.padding(8.dp),
		bottomBar = {
			Box(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
				Button(
					modifier = Modifier.fillMaxWidth(),
					enabled = uiState.canSave,
					onClick = {
						viewModel.saveDataToWord()
						fileSavedDialogState = true
					}
				) {
					Text("Adatok mentése Wordbe")
				}

				if (!uiState.canSave) {
					var expanded by remember { mutableStateOf(false) }

					Icon(
						imageVector = Icons.AutoMirrored.Filled.Help,
						contentDescription = "Reasons",
						modifier = Modifier
							.padding(8.dp)
							.size(24.dp)
							.align(Alignment.CenterEnd)
							.clickable { expanded = true }
					)

					DropdownMenu(
						expanded = expanded,
						onDismissRequest = { expanded = false },
					) {
						Text(
							"Az alábbi feltételek nem teljesülnek a mentéshez:",
							modifier = Modifier.padding(12.dp)
						)
						viewModel.getSaveReasons().forEach { reason ->
							//TODO ezek ne legyenek kattinthatóak, de disable-olva se legyenek (szürkék)
							DropdownMenuItem(onClick = { expanded = false }) {
								Text("- $reason")
							}
						}
					}
				}
			}
		},
	) {
		Row()
		{
			if (fileSavedDialogState) {
				Dialog(
					onDismissRequest = { fileSavedDialogState = false },
				){
					Surface(
						shape = MaterialTheme.shapes.medium,
						elevation = 8.dp,
					) {
						Column(
							modifier = Modifier.padding(16.dp),
							horizontalAlignment = Alignment.CenterHorizontally
						) {
							Text("Word-fájl mentve. Juhhúúú!")
							Spacer(modifier = Modifier.height(8.dp))
							Button(onClick = { fileSavedDialogState = false }) {
								Text("Ok")
							}
						}
					}
				}
			}
			Column(modifier = Modifier.weight(1f).padding(8.dp)) {

				FilePickerLoader(
					"PDF", listOf("pdf"),
					uiState.inputPdfPath,
					viewModel::setInputPdfPath,
					viewModel::readPdfForm
				)
				Card(modifier = Modifier.padding(top = 8.dp).fillMaxWidth().height(200.dp)) {
					LazyColumn {
						item { Text("Kulcsok a PDF-ben") }
						items(viewModel.pdfFormData.keys.toList()) { key ->
							Text("$key: ${viewModel.pdfFormData[key]}")
						}
					}
				}
			}
			Column(modifier = Modifier.weight(1f).padding(8.dp)) {
				FilePickerLoader(
					"Word", listOf("docx", "doc"), uiState.inputWordPath,
					viewModel::setInputWordPath,
					viewModel::readWordKeys
				)
				TextField(
					modifier = Modifier.fillMaxWidth(),
					value = uiState.outputWordPath,
					onValueChange = { viewModel.setOutputWordPath(it) },
					label = { Text("A kimeneti Word elérési útja") },
					trailingIcon = {
						if(File(uiState.outputWordPath).exists())
							Icon(Icons.Default.Warning, contentDescription = "File already exists")
						//TODO tooltippel
					},
					leadingIcon = {
						IconButton(
							enabled = uiState.inputWordPath.isNotBlank(),
							onClick = { viewModel.copyOutputWordPath() }) {
							Icon(Icons.Default.FileCopy, contentDescription = "Copy input path")
						}
					}
				)
				Card(modifier = Modifier.padding(top = 8.dp).fillMaxWidth().height(200.dp)) {
					if (uiState.areWordKeysLoading) {
						Box(
							modifier = Modifier.fillMaxSize(),
							contentAlignment = Alignment.Center
						) {
							CircularProgressIndicator(
								modifier = Modifier.size(48.dp),
								color = MaterialTheme.colors.primary,
								backgroundColor = MaterialTheme.colors.surface,
							)
						}
					}
					else {
						LazyColumn {
							item { Text("Kulcsok a Wordben") }
							items(uiState.wordKeys) { key ->
								Text(key)
							}
						}
					}
				}
			}
		}
	}
}

@Composable
private fun FilePickerLoader(
	fileType: String,
	extensions: List<String>,
	filePath: String,
	setFilePath: (String) -> Unit,
	onFilePicked: () -> Unit = {},
) {
	var showFilePicker by remember { mutableStateOf(false) }
	Button(onClick = { showFilePicker = true }) {
		Text("$fileType helyének kiválasztása")
	}
	FilePicker(show = showFilePicker, fileExtensions = extensions) { platformFile ->
		showFilePicker = false
		platformFile?.path?.let {
			setFilePath(it)
			onFilePicked()
		}
	}
	TextField(
		value = filePath,
		onValueChange = { setFilePath(it) },
		label = { Text("$fileType elérési útja") },
		modifier = Modifier.fillMaxWidth()
	)
}


fun main() = application {
	Window(onCloseRequest = ::exitApplication) {
		App()
	}
}
