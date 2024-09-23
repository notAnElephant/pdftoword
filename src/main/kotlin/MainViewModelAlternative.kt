import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import co.touchlab.kermit.Logger
import composables.MessageLabelState
import composables.MessageType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.interactive.form.PDField
import org.apache.poi.xwpf.extractor.XWPFWordExtractor
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.FileInputStream
import java.util.*

data class MainUiStateAlternative(
	var inputWordPath: String = "",
	var inputPdfPath: String = "",
	var outputPdfPath: String = "",
	var messageList: List<MessageLabelState> = listOf(),
	var canSave: Boolean = false
)

class MainViewModelAlternative : ViewModel() {

	private val _uiState = MutableStateFlow(MainUiState())
	val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

	private val prefix = "["
	private val postfix = "]"
	private val wordKeyRegex = "\\$prefix.*?$postfix".toRegex()


	var formData = mutableStateMapOf<String, String>()
	var wordData = mutableStateMapOf<String, String>()
	fun readPdfForm(filePath: String) {
		PDDocument.load(File(filePath)).use {
			val form = it.documentCatalog.acroForm

			form?.fields?.forEach { field: PDField ->
				formData[field.partialName] = field.valueAsString.lowercase(Locale.getDefault())
			}
		}

		updateCanSave()

		Logger.d { "PDF form data: $formData" }
	}

	fun saveDataToWord() {
		Logger.d { "Saving data to new Word file" }

		FileInputStream(uiState.value.inputWordPath).use { fis ->
			val document = XWPFDocument(fis)
			val wordExtractor = XWPFWordExtractor(document)
			val wordText = wordExtractor.text
			val newWordText = wordKeyRegex.replace(wordText) { result ->
				val key = clearKey(result)
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

	fun readWordKeys(path: String) {
		Logger.d { "Reading Word keys" }
		FileInputStream(path).use { fis ->
			val document = XWPFDocument(fis)
			val wordExtractor = XWPFWordExtractor(document)
			wordExtractor.text.let { text ->
				wordKeyRegex.findAll(text).forEach { matchResult ->
					val key = clearKey(matchResult)
					wordData[key] = ""
				}
			}
		}
		if (wordData.isEmpty()) {
			addMessage("Nincsenek kulcsok a Wordben", MessageType.ERROR)
		}
		updateCanSave()
	}

	private fun clearKey(result: MatchResult) =
		result.value.removeSurrounding(prefix, postfix).lowercase(Locale.getDefault())

	private fun addMessage(message: String, type: MessageType) {
		val messageList = _uiState.value.messageList.toMutableList()
		messageList.add(MessageLabelState(message, type))
		_uiState.value = _uiState.value.copy(messageList = messageList)
	}

	private fun updateCanSave() {
		_uiState.value = _uiState.value.copy(
			canSave =
			_uiState.value.messageList.isEmpty()
					&& formData.isNotEmpty()
					&& wordData.isNotEmpty()
					&& wordData.keys.all { formData.keys.contains(it) }
		)
	}

	fun setInputPdfPath(path: String) {
		_uiState.value = _uiState.value.copy(inputPdfPath = path)
	}

	fun setInputWordPath(path: String) {
		_uiState.value = _uiState.value.copy(inputWordPath = path)
	}

}