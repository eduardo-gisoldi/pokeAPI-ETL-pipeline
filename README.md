# PokéAPI ETL Pipeline

This is my first fully structured Kotlin project! I created this tool specifically to facilitate the data management for a larger Android application I am developing called **Who's That Pokeapp**.

A lightweight, automated ETL (Extract, Transform, Load) pipeline built in Kotlin. This tool fetches raw Pokémon data across multiple [PokéAPI](https://pokeapi.co/) endpoints, scans a local directory of sprite assets, and transforms the complex nested JSON into a simplified, clean, and game-ready format. The output is a localized `.json` database.

## Features
* **Extract:**
    * Fetches data from three separate PokéAPI endpoints (`/pokemon/`, `/pokemon-species/`, and `/evolution-chain/`).
    * Reads a local directory of image assets to verify which sprites (and alternate forms/shinies) actually exist on the disk.
* **Transform:**
    * Parses complex, nested JSON responses using `Gson`.
    * Dynamically maps Pokémon IDs to their respective regions (Kanto up to Gen 10).
    * Extracts extended species data, including Habitats, Colors, Egg Groups, EV Yields, and calculates the specific integer-based Evolution Stage (0, 1, 2) using a recursive tree search.
    * Employs an in-memory caching system for Evolution Chains to drastically reduce API calls and prevent rate-limiting.
    * Uses Regex to dynamically match local file names (e.g., `p1.png`, `p1shiny.png`) to their respective Pokémon and stores them in lists to support multiple forms.
* **Load:**
    * Compiles the formatted data into a single `pokemon_data.json` file.

## Technologies Used
* **Kotlin** (JVM)
* **Gson** (JSON Parsing)
* **Gradle** (Build System)

## How to Run
1. Clone this repository.
2. Ensure you have a local folder containing your named Pokémon sprites (e.g., `p1.png`, `p1shiny.png`).
3. Open `Main.kt` in IntelliJ IDEA or your preferred IDE and update the `spritesFolder` variable to point to your local images directory.
4. Run `Main.kt`.
5. The script will sequentially fetch the data. Because it hits multiple endpoints per Pokémon, it uses an in-memory cache and a 100ms delay to respect API limits. The total build time is roughly 3–4 minutes.
6. The generated `pokemon_data.json` will be saved in the `output/` folder.

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
      "base": [
        "p1"
      ],
      "shiny": [
        "p1shiny"
      ]
    },
    "habitat": "Grassland",
    "color": "Green",
    "egg_groups": [
      "Monster",
      "Plant"
    ],
    "evolution_stage": 0,
    "ev_yield": [
      "1 Special Attack"
    ],
    "additional": []
  }
