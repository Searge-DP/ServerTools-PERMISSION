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

package com.matthewprenger.servertools.permission.elements;

import com.google.common.collect.ImmutableSet;
import com.matthewprenger.servertools.permission.GroupManager;

import java.util.HashSet;
import java.util.Set;

public class Group {

    public final String groupName;
    private final Set<String> members;
    private final Set<String> allowedCommands;
    private final Set<String> childGroups;
    private String chatColor;

    public Group(String groupName) {

        this.groupName = groupName;
        members = new HashSet<>();
        allowedCommands = new HashSet<>();
        childGroups = new HashSet<>();
        chatColor = "white";
    }

    public void addMember(String username) {

        members.add(username);
    }

    public void addChildGroup(String groupName) {

        childGroups.add(groupName);
    }

    public boolean removeChildGroup(String groupName) {

        if (childGroups.contains(groupName)) {
            childGroups.remove(groupName);
            return true;
        }

        return false;
    }

    public boolean removeUser(String username) {

        if (members.contains(username)) {
            members.remove(username);
            return true;
        }

        return false;
    }

    public boolean isMember(String username) {

        return members.contains(username);
    }

    public void addAllowedCommand(String commandName) {

        allowedCommands.add(commandName);
    }

    public boolean removeAllowedCommand(String commandName) {

        if (allowedCommands.contains(commandName)) {
            allowedCommands.remove(commandName);
            return true;
        }

        return false;
    }

    public Set<String> getMembers() {

        return ImmutableSet.copyOf(members);
    }

    public Set<String> getAllowedCommands() {

        Set<String> set = new HashSet<>();

        set.addAll(allowedCommands);

        for (String child : childGroups) {
            if (GroupManager.getGroups().containsKey(child)) {
                set.addAll(GroupManager.getGroups().get(child).getAllowedCommands());
            }
        }

        return set;
    }

    public Set<String> getChildGroups() {

        return ImmutableSet.copyOf(childGroups);
    }

    public String getChatColor() {

        return chatColor;
    }

    public void setChatColor(String chatColor) {

        this.chatColor = chatColor;
    }
}
