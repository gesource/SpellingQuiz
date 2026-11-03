package jp.gesource.sample.spellingquiz.android

import android.content.Context
import org.json.JSONArray

data class WordCard(val meaning: String, val spelling: String)

fun loadWordCards(context: Context): List<WordCard> {
  val json = context.assets.open("word_cards.json").bufferedReader().use { it.readText() }
  val array = JSONArray(json)
  return List(array.length()) { i ->
    val item = array.getJSONObject(i)
    WordCard(meaning = item.getString("meaning"), spelling = item.getString("spelling"))
  }
}
