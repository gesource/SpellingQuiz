package jp.gesource.sample.spellingquiz.android

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchesSpellingTest {
  @Test
  fun firstCandidateMatches() {
    assertTrue(matchesSpelling(listOf("apple", "appk"), "apple"))
  }

  @Test
  fun fifthCandidateMatches() {
    assertTrue(matchesSpelling(listOf("a", "b", "c", "d", "apple"), "apple"))
  }

  @Test
  fun onlySixthCandidateMatchesIsIncorrect() {
    assertFalse(matchesSpelling(listOf("a", "b", "c", "d", "e", "apple"), "apple"))
  }

  @Test
  fun ignoresCaseAndSurroundingWhitespace() {
    assertTrue(matchesSpelling(listOf(" Apple "), "apple"))
    assertTrue(matchesSpelling(listOf("apple"), " APPLE "))
  }

  @Test
  fun emptyCandidatesIsIncorrect() {
    assertFalse(matchesSpelling(emptyList(), "apple"))
  }
}
