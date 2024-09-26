import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.docx4j.Docx4J
import org.docx4j.model.datastorage.migration.VariablePrepare
import java.io.File


object WordDocumentHandling {
	fun openAndReplaceWord(filePath: String, outputPath: String, mappings: Map<String, String>) {
		Logger.d("Opening Word document")
		val wordMLPackage = Docx4J.load(File(filePath))

		// this currently only works with ${key} format, and removes the ${} from the key,
		// even when the key is not found in the mappings. This is not necessarily a problem though,
		// because we only let the user generate the output when all the keys in the word doc are provided
		VariablePrepare.prepare(wordMLPackage)
		wordMLPackage.mainDocumentPart.variableReplace(mappings)

		Docx4J.save(wordMLPackage, File(outputPath))
		Logger.d("Saved Word document")
	}

	suspend fun readWordKeysAsync(filePath: String, prefix: String, postfix: String): Set<String> {
		Logger.d("Getting keys from Word document")

		val regex = Regex(Regex.escape(prefix) + "(.*?)" + Regex.escape(postfix))
		return withContext(Dispatchers.IO) {
			val wordMLPackage = Docx4J.load(File(filePath))
			VariablePrepare.prepare(wordMLPackage)
			val text = wordMLPackage.mainDocumentPart.content.toString()

			return@withContext regex.findAll(text).map { it.groupValues[1] }.toSet()
		}
	}
}