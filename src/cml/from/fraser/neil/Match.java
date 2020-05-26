/*
 * Diff Match and Patch
 * Copyright 2018 The diff-match-patch Authors.
 * https://github.com/google/diff-match-patch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cml.from.fraser.neil;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author benne
 */
public class Match {

    /**
     * At what point is no match declared (0.0 = perfection, 1.0 = very loose).
     */
    public static float Threshold = 0.5f;
    /**
     * How far to search for a match (0 = exact location, 1000+ = broad match).
     * A match this many characters away from the expected location will add 1.0
     * to the score (0.0 is a perfect match).
     */
    public static int Distance = 1000;
    /**
     * The number of bits in an int.
     */
    public static short MaxBits = 32;

    /**
     * Locate the best instance of 'pattern' in 'text' near 'loc'. Returns -1 if
     * no match found.
     *
     * @param text The text to search.
     * @param pattern The pattern to search for.
     * @param loc The location to search around.
     * @return Best match index or -1.
     */
    public static int main(String text, String pattern, int loc) {
        // Check for null inputs.
        if (text == null || pattern == null) {
            throw new IllegalArgumentException("Null inputs. (main)");
        }

        loc = Math.max(0, Math.min(loc, text.length()));
        if (text.equals(pattern)) {
            // Shortcut (potentially not guaranteed by the algorithm)
            return 0;
        } else if (text.length() == 0) {
            // Nothing to match.
            return -1;
        } else if (loc + pattern.length() <= text.length()
                && text.substring(loc, loc + pattern.length()).equals(pattern)) {
            // Perfect match at the perfect spot!  (Includes case of null pattern)
            return loc;
        } else {
            // Do a fuzzy compare.
            return bitap(text, pattern, loc);
        }
    }

    /**
     * Locate the best instance of 'pattern' in 'text' near 'loc' using the
     * Bitap algorithm. Returns -1 if no match found.
     *
     * @param text The text to search.
     * @param pattern The pattern to search for.
     * @param loc The location to search around.
     * @return Best match index or -1.
     */
    protected static int bitap(String text, String pattern, int loc) {
        assert (MaxBits == 0 || pattern.length() <= MaxBits) : "Pattern too long for this application.";

        // Initialise the alphabet.
        Map<Character, Integer> s = alphabet(pattern);

        // Highest score beyond which we give up.
        double score_threshold = Threshold;
        // Is there a nearby exact match? (speedup)
        int best_loc = text.indexOf(pattern, loc);
        if (best_loc != -1) {
            score_threshold = Math.min(bitapScore(0, best_loc, loc, pattern),
                    score_threshold);
            // What about in the other direction? (speedup)
            best_loc = text.lastIndexOf(pattern, loc + pattern.length());
            if (best_loc != -1) {
                score_threshold = Math.min(bitapScore(0, best_loc, loc, pattern),
                        score_threshold);
            }
        }

        // Initialise the bit arrays.
        int matchmask = 1 << (pattern.length() - 1);
        best_loc = -1;

        int bin_min, bin_mid;
        int bin_max = pattern.length() + text.length();
        // Empty initialization added to appease Java compiler.
        int[] last_rd = new int[0];
        for (int d = 0; d < pattern.length(); d++) {
            // Scan for the best match; each iteration allows for one more error.
            // Run a binary search to determine how far from 'loc' we can stray at
            // this error level.
            bin_min = 0;
            bin_mid = bin_max;
            while (bin_min < bin_mid) {
                if (bitapScore(d, loc + bin_mid, loc, pattern)
                        <= score_threshold) {
                    bin_min = bin_mid;
                } else {
                    bin_max = bin_mid;
                }
                bin_mid = (bin_max - bin_min) / 2 + bin_min;
            }
            // Use the result from this iteration as the maximum for the next.
            bin_max = bin_mid;
            int start = Math.max(1, loc - bin_mid + 1);
            int finish = Math.min(loc + bin_mid, text.length()) + pattern.length();

            int[] rd = new int[finish + 2];
            rd[finish + 1] = (1 << d) - 1;
            for (int j = finish; j >= start; j--) {
                int charMatch;
                if (text.length() <= j - 1 || !s.containsKey(text.charAt(j - 1))) {
                    // Out of range.
                    charMatch = 0;
                } else {
                    charMatch = s.get(text.charAt(j - 1));
                }
                if (d == 0) {
                    // First pass: exact match.
                    rd[j] = ((rd[j + 1] << 1) | 1) & charMatch;
                } else {
                    // Subsequent passes: fuzzy match.
                    rd[j] = (((rd[j + 1] << 1) | 1) & charMatch)
                            | (((last_rd[j + 1] | last_rd[j]) << 1) | 1) | last_rd[j + 1];
                }
                if ((rd[j] & matchmask) != 0) {
                    double score = bitapScore(d, j - 1, loc, pattern);
                    // This match will almost certainly be better than any existing
                    // match.  But check anyway.
                    if (score <= score_threshold) {
                        // Told you so.
                        score_threshold = score;
                        best_loc = j - 1;
                        if (best_loc > loc) {
                            // When passing loc, don't exceed our current distance from loc.
                            start = Math.max(1, 2 * loc - best_loc);
                        } else {
                            // Already passed loc, downhill from here on in.
                            break;
                        }
                    }
                }
            }
            if (bitapScore(d + 1, loc, loc, pattern) > score_threshold) {
                // No hope for a (better) match at greater error levels.
                break;
            }
            last_rd = rd;
        }
        return best_loc;
    }

    /**
     * Compute and return the score for a match with e errors and x location.
     *
     * @param e Number of errors in match.
     * @param x Location of match.
     * @param loc Expected location of match.
     * @param pattern Pattern being sought.
     * @return Overall score for match (0.0 = good, 1.0 = bad).
     */
    private static double bitapScore(int e, int x, int loc, String pattern) {
        float accuracy = (float) e / pattern.length();
        int proximity = Math.abs(loc - x);
        if (Distance == 0) {
            // Dodge divide by zero error.
            return proximity == 0 ? accuracy : 1.0;
        }
        return accuracy + (proximity / (float) Distance);
    }

    /**
     * Initialise the alphabet for the Bitap algorithm.
     *
     * @param pattern The text to encode.
     * @return Hash of character locations.
     */
    protected static Map<Character, Integer> alphabet(String pattern) {
        Map<Character, Integer> s = new HashMap<Character, Integer>();
        char[] char_pattern = pattern.toCharArray();
        for (char c : char_pattern) {
            s.put(c, 0);
        }
        int i = 0;
        for (char c : char_pattern) {
            s.put(c, s.get(c) | (1 << (pattern.length() - i - 1)));
            i++;
        }
        return s;
    }
}
