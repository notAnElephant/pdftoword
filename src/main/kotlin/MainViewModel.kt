import WordDocumentHandling.readWordKeysAsync
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.interactive.form.PDField
import java.io.File
import java.io.FileNotFoundException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale


data class MainUiState(
	var inputWordPath: String = "",
	var inputPdfPath: String = "",
	var outputWordPath: String = "",
	var wordKeys: List<String> = listOf(),
	var canSave: Boolean = true,
	var areWordKeysLoading: Boolean = false
)

class MainViewModel : ViewModel() {

	private val _uiState = MutableStateFlow(MainUiState())
	val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

	private val prefix = "\${"
	private val postfix = "}"
	private val specialKeys = mapOf(
		"dátum" to LocalDate.now()
			.format(DateTimeFormatter.ofPattern("yyyy. MMMM d.", Locale("hu")))
	)

	var pdfFormData = mutableStateMapOf<String, String>()
	fun readPdfForm() {

		if (!File(_uiState.value.inputPdfPath).exists()) {
			Logger.d { "Pdf does not exist" }
			throw FileNotFoundException("Pdf file does not exist")
		}

		pdfFormData.clear()

		val document = PDDocument.load(File(_uiState.value.inputPdfPath))
		val form = document.documentCatalog.acroForm

		form?.fields?.forEach { field: PDField ->
			pdfFormData[field.partialName.lowercase()] = field.valueAsString
		}

		document.close()

		updateCanSave()

		Logger.d { "PDF form data: $pdfFormData" }
	}

	/**
	 * Updates the canSave property of the UI state
	 * The user can save if all of the following conditions are met:
	 * - the pdf form's data is not empty
	 * - the word's data is not empty
	 * - all keys in wordData are present in formData
	 */
	//TODO make this in sync with getReasons
	private fun updateCanSave() {
		_uiState.value = _uiState.value.copy(
			canSave =
			pdfFormData.isNotEmpty()
					&& _uiState.value.wordKeys.isNotEmpty()
					&& _uiState.value.wordKeys.all { specialKeys.containsKey(it) || pdfFormData.containsKey(it) }
		)
	}

	fun saveDataToWord() {
		Logger.d { "Saving data to Word" }

		WordDocumentHandling.openAndReplaceWord(
			_uiState.value.inputWordPath,
			_uiState.value.outputWordPath,
			pdfFormData + specialKeys
		)
	}

	fun readWordKeys() {
		Logger.d { "Reading Word keys" }


		if (!File(_uiState.value.inputWordPath).exists()) {
			Logger.d { "Word does not exist" }
			throw FileNotFoundException("Word file does not exist")
		}

		_uiState.value = _uiState.value.copy(areWordKeysLoading = true)

		viewModelScope.launch {
			_uiState.value = _uiState.value.copy(
				wordKeys = readWordKeysAsync(
					_uiState.value.inputWordPath,
					prefix,
					postfix
				).toList()
			)

			if (_uiState.value.wordKeys.isEmpty()) {
				//TODO should we show a popup?
				Logger.d { "No keys found in Word" }
			} else {
				Logger.d { "Word keys: ${_uiState.value.wordKeys}" }
			}
			updateCanSave()
			_uiState.value = _uiState.value.copy(areWordKeysLoading = false)
		}
	}


	fun setInputPdfPath(s: String) {
		_uiState.value = _uiState.value.copy(inputPdfPath = s)
	}

	fun setInputWordPath(s: String) {
		_uiState.value = _uiState.value.copy(inputWordPath = s)
	}

	fun getSaveReasons(): List<String> {
		val reasons = mutableListOf<String>()
		if (pdfFormData.isEmpty()) {
			reasons.add("Nincsenek kulcsok a PDF-ben (vagy nem lett PDF beolvasva)")
		}
		if (_uiState.value.wordKeys.isEmpty()) {
			reasons.add("Nincsenek kulcsok a Wordben (vagy nem lett Word-fájl beolvasva)")
		}
		if (_uiState.value.wordKeys.any { !specialKeys.containsKey(it) && !pdfFormData.containsKey(it) }) {
			reasons.add("Van olyan kulcs a Wordben, ami a PDF-ben nincs")
		}
		return reasons
	}

	fun setOutputWordPath(it: String) {
		_uiState.value = _uiState.value.copy(outputWordPath = it)
	}

	fun copyOutputWordPath() {
		var path = _uiState.value.inputWordPath
		if (path.contains('.')) {
			path = path.substring(0, path.lastIndexOf('.'))
		}
		path += "_kimenet.docx"
		_uiState.value = _uiState.value.copy(outputWordPath = path)
	}
}