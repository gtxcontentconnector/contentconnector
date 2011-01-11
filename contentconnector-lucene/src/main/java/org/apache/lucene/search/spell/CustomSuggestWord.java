package org.apache.lucene.search.spell;


/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 *  SuggestWord, used in suggestSimilar method in SpellChecker class.
 * 
 *
 */
final class CustomSuggestWord {
  /**
   * the score of the word.
   */
  private float score;
  
  /**
   * setScore.
   * @param s score
   */
  public void setScore(final float s) {
	  score = s;
  }
  
  /**
   * getScore.
   * @return score.
   */
  public float getScore() {
	  return score;
  }

  /**
   * The freq of the word.
   */
  private int freq;
  
  /**
   * setter for freq.
   * @param f freq
   */
  public void setFreq(final int f) {
	  freq = f;
  }
  /**
   * getter for freq.
   * @return freq
   */
  public int getFreq() {
	  return freq;
  }

  /**
   * the suggested word.
   */
  private String string;

  /**
   * getter for String.
   * @return string
   */
  public String getString() {
	  return string;
  }
  /**
   * Setter for string.
   * @param str string
   */
  public void setString(final String str) {
	  string = str;
  }
  /**
   * compare.
   * @param a compare to elem.
   * @return comp.
   */
  public int compareTo(final CustomSuggestWord a) {
    // first criteria: the edit distance
	
    if (score > a.score) {
      return 1;
    }
    if (score < a.score) {
      return -1;
    }

    // second criteria (if first criteria is equal): the popularity
    if (freq > a.freq) {
      return 1;
    }

    if (freq < a.freq) {
      return -1;
    }
    return 0;
  }
}
