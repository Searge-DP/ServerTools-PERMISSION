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

import com.matthewprenger.servertools.permission.perms.PermissionManager;
import gnu.trove.set.hash.THashSet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.permissions.api.IGroup;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public class Group implements IGroup {

    public final String groupName;
    private final Set<UUID> members = new THashSet<>();
    private final Set<String> perms = new THashSet<>();
    private String parentGroup = null;
    private String chatColor = "white";

    public Group(String groupName) {

        this.groupName = groupName;
    }

    public String getChatColor() {

        return chatColor;
    }

    public void setChatColor(String chatColor) {

        this.chatColor = chatColor;
        PermissionManager.saveGroup(groupName);
    }

    @Override
    public void addPlayerToGroup(EntityPlayer player) {

        members.add(player.getPersistentID());
        PermissionManager.saveGroup(groupName);
    }

    @Override
    public boolean removePlayerFromGroup(EntityPlayer player) {

        if ( members.remove(player.getPersistentID())) {
            PermissionManager.saveGroup(groupName);
            return true;
        }

        return false;
    }

    @Override
    public Collection<UUID> getAllPlayers() {
        return members;
    }

    @Override
    public boolean isPlayerInGroup(EntityPlayer player) {

        return members.contains(player.getPersistentID());
    }

    public boolean isMember(UUID uuid) {

        return members.contains(uuid);
    }

    public void addPerm(String perm) {

        perms.add(perm);
        PermissionManager.saveGroup(groupName);
    }

    public boolean removePerm(String perm) {

        if (perms.remove(perm)) {
            PermissionManager.saveGroup(groupName);
            return true;
        }

        return false;
    }

    public boolean hasPerm(String perm) {

        return perms.contains(perm) || PermissionManager.getGroup(parentGroup).hasPerm(perm);
    }

    public Collection<String> getPerms() {

        return perms;
    }

    @Override
    public IGroup getParent() {
        return PermissionManager.getGroup(parentGroup);
    }

    @Override
    public void setParent(IGroup parent) {

        this.parentGroup = parent.getName();
        PermissionManager.saveGroup(groupName);
    }

    @Override
    public String getName() {
        return groupName;
    }

    @Override
    public void setName(String name) {
        // NO OP for now
    }
}
