/*
 * Copyright 2014 Matthew Prenger
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

package com.matthewprenger.servertools.permission;

import com.matthewprenger.servertools.permission.elements.Group;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PermissionTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testPermissions() {

        Group admins = new Group("Admins");
        Group moderators = new Group("Moderators");
        Group players = new Group("Players");

        admins.addMember("adminUser");
        moderators.addMember("moderatorUser");
        players.addMember("playerUser");

        admins.addAllowedCommand("ban");
        moderators.addAllowedCommand("kick");
        players.addAllowedCommand("help");

        admins.addChildGroup(moderators.groupName);
        moderators.addChildGroup(players.groupName);

        try {
            Field field = GroupManager.class.getDeclaredField("groups");
            field.setAccessible(true);
            Map<String, Group> groups = (Map<String, Group>) field.get(null);

            groups.put(admins.groupName, admins);
            groups.put(moderators.groupName, moderators);
            groups.put(players.groupName, players);

            assertTrue(GroupManager.canUseCommand("adminUser", "ban"));
            assertTrue(GroupManager.canUseCommand("adminUser", "kick"));
            assertTrue(GroupManager.canUseCommand("adminUser", "help"));

            assertTrue(GroupManager.canUseCommand("moderatorUser", "kick"));
            assertTrue(GroupManager.canUseCommand("moderatorUser", "help"));

            assertTrue(GroupManager.canUseCommand("playerUser", "help"));

            assertFalse(GroupManager.canUseCommand("moderatorUser", "ban"));

            assertFalse(GroupManager.canUseCommand("playerUser", "ban"));
            assertFalse(GroupManager.canUseCommand("playerUser", "kick"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
