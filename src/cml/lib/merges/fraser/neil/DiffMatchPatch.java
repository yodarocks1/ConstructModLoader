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
package cml.lib.merges.fraser.neil;

import java.util.List;

/**
 *
 * @author benne
 */
public class DiffMatchPatch {

    /**
     * Internal class for returning results from diff_linesToChars(). Other less
     * paranoid languages just use a three-element array.
     */
    static class LinesToCharsResult {

        protected String chars1;
        protected String chars2;
        protected List<String> lineArray;

        protected LinesToCharsResult(String chars1, String chars2,
                List<String> lineArray) {
            this.chars1 = chars1;
            this.chars2 = chars2;
            this.lineArray = lineArray;
        }
    }

    //  DIFF FUNCTIONS
    /**
     * The data structure representing a diff is a Linked list of Diff objects:
     * {Diff(Operation.DELETE, "Hello"), Diff(Operation.INSERT, "Goodbye"),
     * Diff(Operation.EQUAL, " world.")} which means: delete "Hello", add
     * "Goodbye" and keep " world."
     */
    public enum Operation {
        DELETE, INSERT, EQUAL
    }

    /**
     * Unescape selected chars for compatability with JavaScript's encodeURI. In
     * speed critical applications this could be dropped since the receiving
     * application will certainly decode these fine. Note that this function is
     * case-sensitive. Thus "%3f" would not be unescaped. But this is ok because
     * it is only called with the output of URLEncoder.encode which returns
     * uppercase hex.
     *
     * Example: "%3F" -&gt; "?", "%24" -&gt; "$", etc.
     *
     * @param str The string to escape.
     * @return The escaped string.
     */
    public static String unescapeForEncodeUriCompatability(String str) {
        return str.replace("%21", "!").replace("%7E", "~")
                .replace("%27", "'").replace("%28", "(").replace("%29", ")")
                .replace("%3B", ";").replace("%2F", "/").replace("%3F", "?")
                .replace("%3A", ":").replace("%40", "@").replace("%26", "&")
                .replace("%3D", "=").replace("%2B", "+").replace("%24", "$")
                .replace("%2C", ",").replace("%23", "#");
    }
}
