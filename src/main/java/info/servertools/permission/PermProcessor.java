/*
 * Copyright 2014 ServerTools
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.servertools.permission;

public final class PermProcessor {

    public static final char WILDCARD = '*';

    /**
     * Checks weather a given perm node matches another. The proposed perm node may contain the wildcard character: *
     *
     * @param proposed
     *         the perm node that is being questioned
     * @param toMatch
     *         the perm node that must be matched
     *
     * @return {@code true} if the perm nodes match, {@code false} otherwise
     */
    public static boolean matches(final String proposed, final String toMatch) {
        if (proposed.equals(toMatch)) return true;
        if (!proposed.contains(String.valueOf(WILDCARD))) return false;
        if (proposed.length() > toMatch.length()) return false;

        boolean matches = true;
        for (int i = 0; i < proposed.length(); i++) {
            if (proposed.charAt(i) == WILDCARD) continue;
            if (proposed.charAt(i) != toMatch.charAt(i)) {
                matches = false;
                break;
            }
        }

        return matches;
    }

    private PermProcessor() {}
}
