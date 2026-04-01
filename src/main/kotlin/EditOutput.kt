import java.io.File

fun main() {

    // read the original output file and our additional data file
    var originalOutput = File("output/pokemon_data.json")
    var jsonString = originalOutput.readText()
    var additionalData = File("src/main/resources/assets/PokeArchetypes.txt")
    var archetype = "isStarter"

    additionalData.forEachLine { line ->
        if (line.isBlank()) return@forEachLine // Skip empty lines

        if (line.startsWith("is")) {
            archetype = line
        } else {
            // Split the line by commas and trim whitespace
            val pokemonNames = line.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            for (pokemon in pokemonNames) {
                // find the pokemon
                val regex = Regex("""("name"\s*:\s*"$pokemon"[\s\S]*?)"additional"\s*:\s*\[\s*\]""")
                // create reoplacement string with the archetype
                val replacement = """$1"additional": ["$archetype"]"""

                // insert the archetype into the json string
                if (regex.containsMatchIn(jsonString)) {
                    jsonString = jsonString.replaceFirst(regex, replacement)
                    println("Success: Added $archetype to $pokemon's additional list")
                } else {
                    println("Warning: Could not find $pokemon or its empty 'additional' array.")
                }
            }
        }
    }

    // save changes
    originalOutput.writeText(jsonString)
    println("\nFinished updating pokemon_data.json!")
}