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

package com.matthewprenger.servertools.permission.handlers;

import com.matthewprenger.servertools.permission.Group;
import com.matthewprenger.servertools.permission.config.PermissionConfig;
import com.matthewprenger.servertools.permission.perms.PermissionManager;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;

import java.util.Collection;

import static net.minecraft.util.EnumChatFormatting.RESET;
import static net.minecraft.util.EnumChatFormatting.WHITE;

public class EventHandler {

    public EventHandler() {

        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void nameFormat(net.minecraftforge.event.entity.player.PlayerEvent.NameFormat event) {

        if (!PermissionConfig.prefixChatGroupName)
            return;

        Collection<Group> groups = PermissionManager.getGroups(event.entityPlayer.getPersistentID());

        if (groups.isEmpty())
            return;

        String prefix = "";

        for (Group group : groups) {
            if (group.groupName.equalsIgnoreCase(PermissionConfig.defaultGroup))
                continue;

            EnumChatFormatting color = EnumChatFormatting.getValueByName(group.getChatColor());
            if (color == null)
                color = WHITE;

            prefix += color + "[" + group.groupName + "]" + RESET;
        }
        prefix += " ";

        event.displayname = prefix + event.displayname;
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {

        if (PermissionManager.getGroups(event.player.getPersistentID()).isEmpty()) {

            PermissionManager.assignDefaultGroup(event.player);
            ChatComponentText componentText = new ChatComponentText(String.format("You have been added to the default group %s", PermissionConfig.defaultGroup));
            componentText.getChatStyle().setItalic(true).setColor(EnumChatFormatting.GOLD);
            event.player.addChatMessage(componentText);
        }
    }
}
