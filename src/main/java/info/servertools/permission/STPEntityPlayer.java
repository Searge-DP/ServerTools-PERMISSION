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

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.world.WorldServer;

import com.mojang.authlib.GameProfile;

public abstract class STPEntityPlayer extends EntityPlayerMP {

    // This is never used
    public STPEntityPlayer(MinecraftServer p_i45285_1_, WorldServer p_i45285_2_, GameProfile p_i45285_3_,
                           ItemInWorldManager p_i45285_4_) {
        super(p_i45285_1_, p_i45285_2_, p_i45285_3_, p_i45285_4_);
    }

    @Override
    public boolean canCommandSenderUseCommand(int permissionLevel, String command) {
        return info.servertools.permission.PermissionManager.checkPerm("cmd." + command, super.getPersistentID());
    }
}
