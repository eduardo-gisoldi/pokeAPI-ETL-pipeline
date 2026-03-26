# PokéAPI ETL Pipeline

This is my first fully structured Kotlin project! I created this tool specifically to facilitate the data management for a larger Android application I am developing called **Who's That Pokeapp**. 

A lightweight, automated ETL (Extract, Transform, Load) pipeline built in Kotlin. This tool fetches raw Pokémon data from the [PokéAPI](https://pokeapi.co/), transforms the complex nested JSON into a simplified, clean and game-ready format. The output is a localized `.json` database.

## Features
* **Extract:** Fetches the National Pokédex directly from the PokéAPI endpoints.
* **Transform:** * Parses complex, nested JSON responses using `Gson`.
    * Dynamically maps Pokémon IDs to their respective regions (Kanto up to Gen 10).
    * Formats ID and name strings (e.g., `1` becomes `"001"`, "bulbasaur" becomes "Bulbasaur").
    * Auto-generates correct file names for base and shiny sprites. 
      * *(Expecting a modified version of the numbered and named sprites found at [Dragonflycave](https://www.dragonflycave.com/resources/sprites/))*
* **Load:** Compiles the formatted data into a single `pokemon_data.json` file.

## Technologies Used
* **Kotlin** (JVM)
* **Gson** (JSON Parsing)
* **Gradle** (Build System)

## How to Run
1. Clone this repository.
2. Open the project in IntelliJ IDEA or your preferred IDE.
3. Run `Main.kt`.
4. The script will sequentially fetch the data (with a built-in 50ms delay to respect API limits) and generate `pokemon_data.json` in the `output/` folder.

## Sample Output
The pipeline generates a clean, flattened JSON array. Here is an example of a single transformed entry (`output/pokemon_data.json`):

```json
  {
    "id": 1,
    "national_number": "001",
    "name": "Bulbasaur",
    "region": "Kanto",
    "types": [
      "Grass",
      "Poison"
    ],
    "height": 7,
    "weight": 69,
    "abilities": [
      "Overgrow",
      "Chlorophyll"
    ],
    "base_stats": {
      "hp": 45,
      "attack": 49,
      "defense": 49,
      "special_attack": 65,
      "special_defense": 65,
      "speed": 45
    },
    "sprites": {
      "base_name": "kan001Bulbasaur",
      "shiny_name": "kan001Bulbasaur_shiny"
    },
    "additional": []
  }
