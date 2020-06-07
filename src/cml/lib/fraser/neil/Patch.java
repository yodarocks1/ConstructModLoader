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
package cml.lib.fraser.neil;

import cml.lib.fraser.neil.DiffMatchPatch.Operation;
import static cml.lib.fraser.neil.DiffMatchPatch.unescapeForEncodeUriCompatability;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author benne
 */
public class Patch {

    public LinkedList<Diff> diffs;
    public int start1;
    public int start2;
    public int length1;
    public int length2;

    /**
     * Constructor. Initializes with an empty list of diffs.
     */
    public Patch() {
        this.diffs = new LinkedList();
    }

    /**
     * Emulate GNU diff's format. Header: @@ -382,8 +481,9 @@ Indices are
     * printed as 1-based, not 0-based.
     *
     * @return The GNU diff string.
     */
    @Override
    public String toString() {
        String coords1, coords2;
        switch (this.length1) {
            case 0:
                coords1 = this.start1 + ",0";
                break;
            case 1:
                coords1 = Integer.toString(this.start1 + 1);
                break;
            default:
                coords1 = (this.start1 + 1) + "," + this.length1;
                break;
        }
        switch (this.length2) {
            case 0:
                coords2 = this.start2 + ",0";
                break;
            case 1:
                coords2 = Integer.toString(this.start2 + 1);
                break;
            default:
                coords2 = (this.start2 + 1) + "," + this.length2;
                break;
        }
        StringBuilder text = new StringBuilder();
        text.append("@@ -").append(coords1).append(" +").append(coords2)
                .append(" @@\n");
        // Escape the body of the patch with %xx notation.
        for (Diff aDiff : this.diffs) {
            switch (aDiff.operation) {
                case INSERT:
                    text.append('+');
                    break;
                case DELETE:
                    text.append('-');
                    break;
                case EQUAL:
                    text.append(' ');
                    break;
            }
            try {
                text.append(URLEncoder.encode(aDiff.text, "UTF-8").replace('+', ' '))
                        .append("\n");
            } catch (UnsupportedEncodingException e) {
                // Not likely on modern system.
                throw new Error("This system does not support UTF-8.", e);
            }
        }
        return unescapeForEncodeUriCompatability(text.toString());
    }

    /**
     * When deleting a large block of text (over ~64 characters), how close do
     * the contents have to be to match the expected contents. (0.0 =
     * perfection, 1.0 = very loose). Note that Match.Threshold controls how
     * closely the end points of a delete need to match.
     */
    public static float DeleteThreshold = 0.5f;
    /**
     * Chunk size for context length.
     */
    public static short Margin = 4;

    /**
     * Increase the context until it is unique, but don't let the pattern expand
     * beyond Match.MaxBits.
     *
     * @param patch The patch to grow.
     * @param text Source text.
     */
    private static void addContext(Patch patch, String text) {
        if (text.length() == 0) {
            return;
        }
        String pattern = text.substring(patch.start2, patch.start2 + patch.length1);
        int padding = 0;

        // Look for the first and last matches of pattern in text.  If two different
        // matches are found, increase the pattern length.
        while (text.indexOf(pattern) != text.lastIndexOf(pattern)
                && pattern.length() < Match.MaxBits - Margin - Margin) {
            padding += Margin;
            pattern = text.substring(Math.max(0, patch.start2 - padding),
                    Math.min(text.length(), patch.start2 + patch.length1 + padding));
        }
        // Add one chunk for good luck.
        padding += Margin;

        // Add the prefix.
        String prefix = text.substring(Math.max(0, patch.start2 - padding),
                patch.start2);
        if (prefix.length() != 0) {
            patch.diffs.addFirst(new Diff(Operation.EQUAL, prefix));
        }
        // Add the suffix.
        String suffix = text.substring(patch.start2 + patch.length1,
                Math.min(text.length(), patch.start2 + patch.length1 + padding));
        if (suffix.length() != 0) {
            patch.diffs.addLast(new Diff(Operation.EQUAL, suffix));
        }

        // Roll back the start points.
        patch.start1 -= prefix.length();
        patch.start2 -= prefix.length();
        // Extend the lengths.
        patch.length1 += prefix.length() + suffix.length();
        patch.length2 += prefix.length() + suffix.length();
    }

    /**
     * Compute a list of patches to turn text1 into text2. A set of diffs will
     * be computed.
     *
     * @param text1 Old text.
     * @param text2 New text.
     * @return LinkedList of Patch objects.
     */
    public static LinkedList<Patch> make(String text1, String text2) {
        if (text1 == null || text2 == null) {
            throw new IllegalArgumentException("Null inputs. (make)");
        }
        // No diffs provided, compute our own.
        LinkedList<Diff> diffs = Diff.main(text1, text2, true);
        if (diffs.size() > 2) {
            Diff.cleanupSemantic(diffs);
            Diff.cleanupEfficiency(diffs);
        }
        return make(text1, diffs);
    }

    /**
     * Compute a list of patches to turn text1 into text2. text1 will be derived
     * from the provided diffs.
     *
     * @param diffs Array of Diff objects for text1 to text2.
     * @return LinkedList of Patch objects.
     */
    public static LinkedList<Patch> make(LinkedList<Diff> diffs) {
        if (diffs == null) {
            throw new IllegalArgumentException("Null inputs. (make)");
        }
        // No origin string provided, compute our own.
        String text1 = Diff.text1(diffs);
        return make(text1, diffs);
    }


    /**
     * Compute a list of patches to turn text1 into text2. text2 is not
     * provided, diffs are the delta between text1 and text2.
     *
     * @param text1 Old text.
     * @param diffs Array of Diff objects for text1 to text2.
     * @return LinkedList of Patch objects.
     */
    public static LinkedList<Patch> make(String text1, LinkedList<Diff> diffs) {
        if (text1 == null || diffs == null) {
            throw new IllegalArgumentException("Null inputs. (make)");
        }

        LinkedList<Patch> patches = new LinkedList();
        if (diffs.isEmpty()) {
            return patches;  // Get rid of the null case.
        }
        Patch patch = new Patch();
        int char_count1 = 0;  // Number of characters into the text1 string.
        int char_count2 = 0;  // Number of characters into the text2 string.
        // Start with text1 (pretext) and apply the diffs until we arrive at
        // text2 (posttext). We recreate the patches one by one to determine
        // context info.
        String pretext = text1;
        String posttext = text1;
        for (Diff aDiff : diffs) {
            if (patch.diffs.isEmpty() && aDiff.operation != Operation.EQUAL) {
                // A new patch starts here.
                patch.start1 = char_count1;
                patch.start2 = char_count2;
            }

            switch (aDiff.operation) {
                case INSERT:
                    patch.diffs.add(aDiff);
                    patch.length2 += aDiff.text.length();
                    posttext = posttext.substring(0, char_count2)
                            + aDiff.text + posttext.substring(char_count2);
                    break;
                case DELETE:
                    patch.length1 += aDiff.text.length();
                    patch.diffs.add(aDiff);
                    posttext = posttext.substring(0, char_count2)
                            + posttext.substring(char_count2 + aDiff.text.length());
                    break;
                case EQUAL:
                    if (aDiff.text.length() <= 2 * Margin
                            && !patch.diffs.isEmpty() && aDiff != diffs.getLast()) {
                        // Small equality inside a patch.
                        patch.diffs.add(aDiff);
                        patch.length1 += aDiff.text.length();
                        patch.length2 += aDiff.text.length();
                    }

                    if (aDiff.text.length() >= 2 * Margin && !patch.diffs.isEmpty()) {
                        // Time for a new patch.
                        if (!patch.diffs.isEmpty()) {
                            addContext(patch, pretext);
                            patches.add(patch);
                            patch = new Patch();
                            // Unlike Unidiff, our patch lists have a rolling context.
                            // https://github.com/google/diff-match-patch/wiki/Unidiff
                            // Update prepatch text & pos to reflect the application of the
                            // just completed patch.
                            pretext = posttext;
                            char_count1 = char_count2;
                        }
                    }
                    break;
            }

            // Update the current character count.
            if (aDiff.operation != Operation.INSERT) {
                char_count1 += aDiff.text.length();
            }
            if (aDiff.operation != Operation.DELETE) {
                char_count2 += aDiff.text.length();
            }
        }
        // Pick up the leftover patch if not empty.
        if (!patch.diffs.isEmpty()) {
            addContext(patch, pretext);
            patches.add(patch);
        }

        return patches;
    }

    /**
     * Given an array of patches, return another array that is identical.
     *
     * @param patches Array of Patch objects.
     * @return Array of Patch objects.
     */
    public static LinkedList<Patch> deepCopy(LinkedList<Patch> patches) {
        LinkedList<Patch> patchesCopy = new LinkedList();
        patches.stream().map((aPatch) -> {
            Patch patchCopy = new Patch();
            aPatch.diffs.stream().map((aDiff) -> new Diff(aDiff.operation, aDiff.text)).forEachOrdered((diffCopy) -> {
                patchCopy.diffs.add(diffCopy);
            });
            patchCopy.start1 = aPatch.start1;
            patchCopy.start2 = aPatch.start2;
            patchCopy.length1 = aPatch.length1;
            patchCopy.length2 = aPatch.length2;
            return patchCopy;
        }).forEachOrdered((patchCopy) -> {
            patchesCopy.add(patchCopy);
        });
        return patchesCopy;
    }

    /**
     * Merge a set of patches onto the text. Return a patched text, as well as
     * an array of true/false values indicating which patches were applied.
     *
     * @param patches Array of Patch objects
     * @param text Old text.
     * @return Two element Object array, containing the new text and an array of
     * boolean values.
     */
    public static Object[] apply(LinkedList<Patch> patches, String text) {
        if (patches == null || patches.isEmpty()) {
            return new Object[]{text, new boolean[0]};
        }

        // Deep copy the patches so that no changes are made to originals.
        patches = deepCopy(patches);

        String nullPadding = addPadding(patches);
        text = nullPadding + text + nullPadding;
        splitMax(patches);

        int x = 0;
        // delta keeps track of the offset between the expected and actual location
        // of the previous patch.  If there are patches expected at positions 10 and
        // 20, but the first patch was found at 12, delta is 2 and the second patch
        // has an effective expected position of 22.
        int delta = 0;
        boolean[] results = new boolean[patches.size()];
        for (Patch aPatch : patches) {
            int expected_loc = aPatch.start2 + delta;
            String text1 = Diff.text1(aPatch.diffs);
            int start_loc;
            int end_loc = -1;
            if (text1.length() > Match.MaxBits) {
                // splitMax will only provide an oversized pattern in the case of
                // a monster delete.
                start_loc = Match.main(text,
                        text1.substring(0, Match.MaxBits), expected_loc);
                if (start_loc != -1) {
                    end_loc = Match.main(text,
                            text1.substring(text1.length() - Match.MaxBits),
                            expected_loc + text1.length() - Match.MaxBits);
                    if (end_loc == -1 || start_loc >= end_loc) {
                        // Can't find valid trailing context.  Drop this patch.
                        start_loc = -1;
                    }
                }
            } else {
                start_loc = Match.main(text, text1, expected_loc);
            }
            if (start_loc == -1) {
                // No match found.  :(
                results[x] = false;
                // Subtract the delta for this failed patch from subsequent patches.
                delta -= aPatch.length2 - aPatch.length1;
            } else {
                // Found a match.  :)
                results[x] = true;
                delta = start_loc - expected_loc;
                String text2;
                if (end_loc == -1) {
                    text2 = text.substring(start_loc,
                            Math.min(start_loc + text1.length(), text.length()));
                } else {
                    text2 = text.substring(start_loc,
                            Math.min(end_loc + Match.MaxBits, text.length()));
                }
                if (text1.equals(text2)) {
                    // Perfect match, just shove the replacement text in.
                    text = text.substring(0, start_loc) + Diff.text2(aPatch.diffs)
                            + text.substring(start_loc + text1.length());
                } else {
                    // Imperfect match.  Run a diff to get a framework of equivalent
                    // indices.
                    LinkedList<Diff> diffs = Diff.main(text1, text2, false);
                    if (text1.length() > Match.MaxBits
                            && Diff.levenshtein(diffs) / (float) text1.length()
                            > DeleteThreshold) {
                        // The end points match, but the content is unacceptably bad.
                        results[x] = false;
                    } else {
                        Diff.cleanupSemanticLossless(diffs);
                        int index1 = 0;
                        for (Diff aDiff : aPatch.diffs) {
                            if (aDiff.operation != Operation.EQUAL) {
                                int index2 = Diff.xIndex(diffs, index1);
                                if (aDiff.operation == Operation.INSERT) {
                                    // Insertion
                                    text = text.substring(0, start_loc + index2) + aDiff.text
                                            + text.substring(start_loc + index2);
                                } else if (aDiff.operation == Operation.DELETE) {
                                    // Deletion
                                    text = text.substring(0, start_loc + index2)
                                            + text.substring(start_loc + Diff.xIndex(diffs,
                                                    index1 + aDiff.text.length()));
                                }
                            }
                            if (aDiff.operation != Operation.DELETE) {
                                index1 += aDiff.text.length();
                            }
                        }
                    }
                }
            }
            x++;
        }
        // Strip the padding off.
        text = text.substring(nullPadding.length(), text.length()
                - nullPadding.length());
        return new Object[]{text, results};
    }

    /**
     * Add some padding on text start and end so that edges can match something.
     * Intended to be called only from within apply.
     *
     * @param patches Array of Patch objects.
     * @return The padding string added to each side.
     */
    public static String addPadding(LinkedList<Patch> patches) {
        short paddingLength = Margin;
        String nullPadding = "";
        for (short x = 1; x <= paddingLength; x++) {
            nullPadding += String.valueOf((char) x);
        }

        // Bump all the patches forward.
        for (Patch aPatch : patches) {
            aPatch.start1 += paddingLength;
            aPatch.start2 += paddingLength;
        }

        // Add some padding on start of first diff.
        Patch patch = patches.getFirst();
        LinkedList<Diff> diffs = patch.diffs;
        if (diffs.isEmpty() || diffs.getFirst().operation != Operation.EQUAL) {
            // Add nullPadding equality.
            diffs.addFirst(new Diff(Operation.EQUAL, nullPadding));
            patch.start1 -= paddingLength;  // Should be 0.
            patch.start2 -= paddingLength;  // Should be 0.
            patch.length1 += paddingLength;
            patch.length2 += paddingLength;
        } else if (paddingLength > diffs.getFirst().text.length()) {
            // Grow first equality.
            Diff firstDiff = diffs.getFirst();
            int extraLength = paddingLength - firstDiff.text.length();
            firstDiff.text = nullPadding.substring(firstDiff.text.length())
                    + firstDiff.text;
            patch.start1 -= extraLength;
            patch.start2 -= extraLength;
            patch.length1 += extraLength;
            patch.length2 += extraLength;
        }

        // Add some padding on end of last diff.
        patch = patches.getLast();
        diffs = patch.diffs;
        if (diffs.isEmpty() || diffs.getLast().operation != Operation.EQUAL) {
            // Add nullPadding equality.
            diffs.addLast(new Diff(Operation.EQUAL, nullPadding));
            patch.length1 += paddingLength;
            patch.length2 += paddingLength;
        } else if (paddingLength > diffs.getLast().text.length()) {
            // Grow last equality.
            Diff lastDiff = diffs.getLast();
            int extraLength = paddingLength - lastDiff.text.length();
            lastDiff.text += nullPadding.substring(0, extraLength);
            patch.length1 += extraLength;
            patch.length2 += extraLength;
        }

        return nullPadding;
    }

    /**
     * Look through the patches and break up any which are longer than the
     * maximum limit of the match algorithm. Intended to be called only from
     * within apply.
     *
     * @param patches LinkedList of Patch objects.
     */
    public static void splitMax(LinkedList<Patch> patches) {
        short size = Match.MaxBits;
        String precontext, postcontext;
        Patch patch;
        int start1, start2;
        boolean empty;
        Operation type;
        String text;
        ListIterator<Patch> pointer = patches.listIterator();
        Patch bigpatch = pointer.hasNext() ? pointer.next() : null;
        while (bigpatch != null) {
            if (bigpatch.length1 <= Match.MaxBits) {
                bigpatch = pointer.hasNext() ? pointer.next() : null;
                continue;
            }
            // Remove the big old patch.
            pointer.remove();
            start1 = bigpatch.start1;
            start2 = bigpatch.start2;
            precontext = "";
            while (!bigpatch.diffs.isEmpty()) {
                // Create one of several smaller patches.
                patch = new Patch();
                empty = true;
                patch.start1 = start1 - precontext.length();
                patch.start2 = start2 - precontext.length();
                if (precontext.length() != 0) {
                    patch.length1 = patch.length2 = precontext.length();
                    patch.diffs.add(new Diff(Operation.EQUAL, precontext));
                }
                while (!bigpatch.diffs.isEmpty()
                        && patch.length1 < size - Margin) {
                    type = bigpatch.diffs.getFirst().operation;
                    text = bigpatch.diffs.getFirst().text;
                    if (type == Operation.INSERT) {
                        // Insertions are harmless.
                        patch.length2 += text.length();
                        start2 += text.length();
                        patch.diffs.addLast(bigpatch.diffs.removeFirst());
                        empty = false;
                    } else if (type == Operation.DELETE && patch.diffs.size() == 1
                            && patch.diffs.getFirst().operation == Operation.EQUAL
                            && text.length() > 2 * size) {
                        // This is a large deletion.  Let it pass in one chunk.
                        patch.length1 += text.length();
                        start1 += text.length();
                        empty = false;
                        patch.diffs.add(new Diff(type, text));
                        bigpatch.diffs.removeFirst();
                    } else {
                        // Deletion or equality.  Only take as much as we can stomach.
                        text = text.substring(0, Math.min(text.length(),
                                size - patch.length1 - Margin));
                        patch.length1 += text.length();
                        start1 += text.length();
                        if (type == Operation.EQUAL) {
                            patch.length2 += text.length();
                            start2 += text.length();
                        } else {
                            empty = false;
                        }
                        patch.diffs.add(new Diff(type, text));
                        if (text.equals(bigpatch.diffs.getFirst().text)) {
                            bigpatch.diffs.removeFirst();
                        } else {
                            bigpatch.diffs.getFirst().text = bigpatch.diffs.getFirst().text
                                    .substring(text.length());
                        }
                    }
                }
                // Compute the head context for the next patch.
                precontext = Diff.text2(patch.diffs);
                precontext = precontext.substring(Math.max(0, precontext.length()
                        - Margin));
                // Append the end context for this patch.
                if (Diff.text1(bigpatch.diffs).length() > Margin) {
                    postcontext = Diff.text1(bigpatch.diffs).substring(0, Margin);
                } else {
                    postcontext = Diff.text1(bigpatch.diffs);
                }
                if (postcontext.length() != 0) {
                    patch.length1 += postcontext.length();
                    patch.length2 += postcontext.length();
                    if (!patch.diffs.isEmpty()
                            && patch.diffs.getLast().operation == Operation.EQUAL) {
                        patch.diffs.getLast().text += postcontext;
                    } else {
                        patch.diffs.add(new Diff(Operation.EQUAL, postcontext));
                    }
                }
                if (!empty) {
                    pointer.add(patch);
                }
            }
            bigpatch = pointer.hasNext() ? pointer.next() : null;
        }
    }

    /**
     * Take a list of patches and return a textual representation.
     *
     * @param patches List of Patch objects.
     * @return Text representation of patches.
     */
    public static String toText(List<Patch> patches) {
        if (patches == null) {
            return "";
        }
        StringBuilder text = new StringBuilder();
        patches.forEach((aPatch) -> {
            text.append(aPatch);
        });
        return text.toString();
    }

    /**
     * Parse a textual representation of patches and return a List of Patch
     * objects.
     *
     * @param textline Text representation of patches.
     * @return List of Patch objects.
     * @throws IllegalArgumentException If invalid input.
     */
    public static List<Patch> fromText(String textline)
            throws IllegalArgumentException {
        List<Patch> patches = new LinkedList();
        if (textline.length() == 0) {
            return patches;
        }
        List<String> textList = Arrays.asList(textline.split("\n"));
        LinkedList<String> text = new LinkedList(textList);
        Patch patch;
        Pattern patchHeader
                = Pattern.compile("^@@ -(\\d+),?(\\d*) \\+(\\d+),?(\\d*) @@$");
        Matcher m;
        char sign;
        String line;
        while (!text.isEmpty()) {
            m = patchHeader.matcher(text.getFirst());
            if (!m.matches()) {
                throw new IllegalArgumentException(
                        "Invalid patch string: " + text.getFirst());
            }
            patch = new Patch();
            patches.add(patch);
            patch.start1 = Integer.parseInt(m.group(1));
            if (m.group(2).length() == 0) {
                patch.start1--;
                patch.length1 = 1;
            } else if (m.group(2).equals("0")) {
                patch.length1 = 0;
            } else {
                patch.start1--;
                patch.length1 = Integer.parseInt(m.group(2));
            }

            patch.start2 = Integer.parseInt(m.group(3));
            if (m.group(4).length() == 0) {
                patch.start2--;
                patch.length2 = 1;
            } else if (m.group(4).equals("0")) {
                patch.length2 = 0;
            } else {
                patch.start2--;
                patch.length2 = Integer.parseInt(m.group(4));
            }
            text.removeFirst();

            OUTER:
            while (!text.isEmpty()) {
                try {
                    sign = text.getFirst().charAt(0);
                } catch (IndexOutOfBoundsException e) {
                    // Blank line?  Whatever.
                    text.removeFirst();
                    continue;
                }
                line = text.getFirst().substring(1);
                line = line.replace("+", "%2B");
                try {
                    line = URLDecoder.decode(line, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    // Not likely on modern system.
                    throw new Error("This system does not support UTF-8.", e);
                } catch (IllegalArgumentException e) {
                    // Malformed URI sequence.
                    throw new IllegalArgumentException(
                            "Illegal escape in fromText: " + line, e);
                }
                switch (sign) {
                    case '-':
                        // Deletion.
                        patch.diffs.add(new Diff(Operation.DELETE, line));
                        break;
                    case '+':
                        // Insertion.
                        patch.diffs.add(new Diff(Operation.INSERT, line));
                        break;
                    case ' ':
                        // Minor equality.
                        patch.diffs.add(new Diff(Operation.EQUAL, line));
                        break;
                    case '@':
                        // Start of next patch.
                        break OUTER;
                    default:
                        // WTF?
                        throw new IllegalArgumentException(
                                "Invalid patch mode '" + sign + "' in: " + line);
                }
                text.removeFirst();
            }
        }
        return patches;
    }
}
