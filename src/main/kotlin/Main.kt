import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File

fun main() {
    // final list
    val finalPokedex = mutableListOf<PokemonData>()

    // setup gson
    val gson = GsonBuilder().setPrettyPrinting().create()

    // read folder once to save time
    val spritesFolder = File("/home/edu/Documents/Datasets/pkmn01/pokemon")
    val allLocalFiles = spritesFolder.listFiles()?.map { it.name } ?: emptyList()

    // setup cache for evolution chains
    val evoChainCache = mutableMapOf<String, ApiEvolutionChainWrapper>()

    // loop through the national dex
    for (i in 1..1025) {
        println("Extracting Pokémon #$i...")

        try {
            // extract pokemon data
            val rawData = java.net.URL("https://pokeapi.co/api/v2/pokemon/$i/").readText()
            val apiPoke = gson.fromJson(rawData, ApiPokemon::class.java)

            // extract species data
            val speciesRaw = java.net.URL("https://pokeapi.co/api/v2/pokemon-species/$i/").readText()
            val apiSpecies = gson.fromJson(speciesRaw, ApiSpecies::class.java)

            // extract evolution data
            val evoUrl = apiSpecies.evolution_chain.url
            val apiEvo = evoChainCache.getOrPut(evoUrl) {
                val evoRaw = java.net.URL(evoUrl).readText()
                gson.fromJson(evoRaw, ApiEvolutionChainWrapper::class.java)
            }

            // transform
            val formattedNumber = apiPoke.id.toString().padStart(3, '0')
            val capitalizedName = apiPoke.name.replaceFirstChar { it.uppercase() }

            val cleanTypes = apiPoke.types.map { it.type.name.replaceFirstChar { char -> char.uppercase() } }
            val cleanAbilities = apiPoke.abilities.map { it.ability.name.replaceFirstChar { char -> char.uppercase() } }

            // clean species data
            val cleanColor = apiSpecies.color.name.replaceFirstChar { it.uppercase() }
            // habitat can be null
            val cleanHabitat = apiSpecies.habitat?.name?.replaceFirstChar { it.uppercase() } ?: "Unknown"
            val cleanEggGroups = apiSpecies.egg_groups.map { it.name.replaceFirstChar { char -> char.uppercase() } }

            // ev yield logic
            val evYieldList = apiPoke.stats.filter { it.effort > 0 }.map {
                val statName = it.stat.name.replace("-", " ").replaceFirstChar { char -> char.uppercase() }
                "${it.effort} $statName"
            }

            // evolution stage logic (now returns an Int)
            val evoStage = getEvolutionStage(apiEvo.chain, apiPoke.name)

            val region = when (apiPoke.id) {
                in 1..151 -> "Kanto"
                in 152..251 -> "Johto"
                in 252..386 -> "Hoenn"
                in 387..493 -> "Sinnoh"
                in 494..649 -> "Unova"
                in 650..721 -> "Kalos"
                in 722..809 -> "Alola"
                in 810..898 -> "Galar"
                in 899..905 -> "Hisui"
                in 906..1025 -> "Paldea"
                in 1026..1200 -> "Generation 10"
                else -> "Unknown Region"
            }

            val stats = BaseStats(
                hp = apiPoke.stats[0].base_stat,
                attack = apiPoke.stats[1].base_stat,
                defense = apiPoke.stats[2].base_stat,
                special_attack = apiPoke.stats[3].base_stat,
                special_defense = apiPoke.stats[4].base_stat,
                speed = apiPoke.stats[5].base_stat
            )

            // base sprite name logic
            val idRegex = Regex("^p${apiPoke.id}(?![0-9])(.*)\\.png$")
            val matchingFiles = allLocalFiles.filter { it.matches(idRegex) }

            // separate them and drop the .png extension
            val baseList = matchingFiles.filter { !it.contains("shiny") }.map { it.removeSuffix(".png") }
            val shinyList = matchingFiles.filter { it.contains("shiny") }.map { it.removeSuffix(".png") }

            val mySprites = Sprites(
                base = baseList,
                shiny = shinyList
            )

            // load
            val pokemonData = PokemonData(
                id = apiPoke.id,
                national_number = formattedNumber,
                name = capitalizedName,
                region = region,
                types = cleanTypes,
                height = apiPoke.height,
                weight = apiPoke.weight,
                abilities = cleanAbilities,
                base_stats = stats,
                sprites = mySprites,
                habitat = cleanHabitat,
                color = cleanColor,
                egg_groups = cleanEggGroups,
                evolution_stage = evoStage,
                ev_yield = evYieldList,
                additional = emptyList()
            )

            // add to list
            finalPokedex.add(pokemonData)

        } catch (e: Exception) {
            println("Failed to fetch #$i. Error: ${e.message}")
        }

        // pause to avoid rate limits
        Thread.sleep(100)
    }

    // save file
    println("All Pokémon fetched! Preparing to save...")

    val outputFolder = File("output")
    if (!outputFolder.exists()) {
        outputFolder.mkdirs()
    }

    val finalJsonString = gson.toJson(finalPokedex)
    File(outputFolder, "pokemon_data.json").writeText(finalJsonString)

    println("Done! Check the /output folder for your database.")
}

// helper function for evolution stage
fun getEvolutionStage(chain: ApiEvolutionNode, targetName: String, currentStage: Int = 0): Int {
    // found the pokemon
    if (chain.species.name == targetName) {
        return currentStage
    }
    // check next evolution
    for (evolution in chain.evolves_to) {
        val stage = getEvolutionStage(evolution, targetName, currentStage + 1)
        if (stage != -1) return stage
    }
    return -1
}

// Source Blueprints
data class ApiPokemon(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val types: List<ApiTypeWrapper>,
    val stats: List<ApiStatWrapper>,
    val abilities: List<ApiAbilityWrapper>
)
data class ApiTypeWrapper(val type: ApiNameUrl)
data class ApiStatWrapper(val base_stat: Int, val effort: Int, val stat: ApiNameUrl)
data class ApiAbilityWrapper(val ability: ApiNameUrl)
data class ApiNameUrl(val name: String, val url: String)

// Species Blueprints
data class ApiSpecies(
    val color: ApiNameUrl,
    val egg_groups: List<ApiNameUrl>,
    val habitat: ApiNameUrl?,
    val evolution_chain: ApiUrl
)
data class ApiUrl(val url: String)

// Evolution Chain Blueprints
data class ApiEvolutionChainWrapper(val chain: ApiEvolutionNode)
data class ApiEvolutionNode(
    val species: ApiNameUrl,
    val evolves_to: List<ApiEvolutionNode>
)

// Destination Blueprints
data class Sprites(
    val base: List<String>,
    val shiny: List<String>
)
data class BaseStats(
    val hp: Int,
    val attack: Int,
    val defense: Int,
    val special_attack: Int,
    val special_defense: Int,
    val speed: Int
)
data class PokemonData(
    val id: Int,
    val national_number: String,
    val name: String,
    val region: String,
    val types: List<String>,
    val height: Int,
    val weight: Int,
    val abilities: List<String>,
    val base_stats: BaseStats,
    val sprites: Sprites,
    val habitat: String,
    val color: String,
    val egg_groups: List<String>,
    val evolution_stage: Int,
    val ev_yield: List<String>,
    val additional: List<String>
)