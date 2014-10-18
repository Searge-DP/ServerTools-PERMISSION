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

import org.junit.Test;

import static info.servertools.permission.PermProcessor.matches;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PermProcessorTest {

    @Test
    public void testMatches() {
        assertTrue(matches("cmd.foo.bar", "cmd.foo.bar"));
        assertTrue(matches("cmd.foo.*", "cmd.foo.bar"));
        assertTrue(matches("cmd.*", "cmd.foo.bar"));
        assertTrue(matches("*", "cmd.foo.bar"));

        assertFalse(matches("cmd.foo.bar", "cmd.foo.tar"));
        assertFalse(matches("cmd.foo.*", "cmd.boo.bar"));
        assertFalse(matches("cmd.*", "nocmd.foo.bar"));
    }
}
