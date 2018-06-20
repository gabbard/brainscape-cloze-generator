package gabbard.cloze

import java.nio.file.Files
import java.nio.file.Paths


fun csvEscape(field: String): String {
    return "\"" + field.replace("\"", "\"\"") + "\""
}

if (args.size != 2) {
    print("""usage: kotlinc --script generate-cloze [input_file] [output_file]
            |    input_file: one card template per line. For each string in [[double-brackets]]
            |                a cloze deletion card will be generated with that string blanked out.
            |    output_file: output will be written in Brainscape's CSV import format"""
            .trimMargin("|"))
    System.exit(1)
}

val inputFile = Paths.get(args[0])
val outputFile = Paths.get(args[1])

val clozeBlankRegex = """\[\[.*?]]""".toRegex()

val removeBrackets: (MatchResult) -> String = {
    it.value.removePrefix("[[").removeSuffix("]]")
}

fun removeAllBrackets(template: String): String {
    return template.replace(clozeBlankRegex, removeBrackets)
}

val blank = "_______"

Files.newBufferedWriter(outputFile, Charsets.UTF_8).use {
    val outStream = it
    for (template in Files.readAllLines(inputFile, Charsets.UTF_8)) {
        val answer = removeAllBrackets(template)
        clozeBlankRegex.findAll(template).forEach {
            val question = removeAllBrackets(template.replaceRange(it.range, blank))
            // we could use https://github.com/holgerbrandl/kscript
            // to use a proper CSV library
            outStream.write("${csvEscape(question)}\t${csvEscape(answer)}\n")
        }
    }
}
