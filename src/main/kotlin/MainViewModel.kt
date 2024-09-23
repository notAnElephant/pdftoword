import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import co.touchlab.kermit.Logger
import composables.MessageLabelState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.interactive.form.PDField
import org.apache.poi.xwpf.extractor.XWPFWordExtractor
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.FileInputStream
import java.util.Locale

data class MainUiState(
	var inputWordPath: String = "",
	var inputPdfPath: String = "",
	var outputPdfPath: String = "",
	var messageList: List<MessageLabelState> = listOf(),
	var canSave: Boolean = false
)
class MainViewModel : ViewModel(){

	private val _uiState = MutableStateFlow(MainUiState())
	val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

	private val prefix = "["
	private val postfix = "]"
	private val wordKeyRegex = "\\$prefix.*?$postfix".toRegex()

	var formData = mutableStateMapOf<String, String>()
	var wordData = mutableStateMapOf<String, String>()
	var doKeysMatch by mutableStateOf(false)
	fun readPdfForm(filePath: String){
		val document = PDDocument.load(File(filePath))
		val form = document.documentCatalog.acroForm

		form?.fields?.forEach { field: PDField ->
			formData[field.partialName] = field.valueAsString
		}

		document.close()
		
		updateDoKeysMatch()
		
		Logger.d { "PDF form data: $formData" }
	}

	private fun updateDoKeysMatch() {
		doKeysMatch = formData.keys == wordData.keys
	}

	fun saveDataToWord() {
		Logger.d{"Saving data to Word"}
		FileInputStream(uiState.value.inputWordPath).use { fis ->
			val document = XWPFDocument(fis)
			val wordExtractor = XWPFWordExtractor(document)
			val wordText = wordExtractor.text
			val newWordText = wordKeyRegex.replace(wordText) { result ->
				val key = clearKeyFromPreAndPostfix(result)
				wordData[key] ?: result.value
			}
			document.createParagraph().createRun().setText(newWordText)
			wordExtractor.close()
			document.close()
			File(uiState.value.outputPdfPath).outputStream().use { fos ->
				document.write(fos)
			}
		}
	}

	private fun clearKeyFromPreAndPostfix(result: MatchResult) =
		result.value.removeSurrounding(prefix, postfix).lowercase(Locale.getDefault())


	fun readWordKeys(it: String) {
		Logger.d{"Reading Word keys"}
		TODO("Not yet implemented")
	}

}