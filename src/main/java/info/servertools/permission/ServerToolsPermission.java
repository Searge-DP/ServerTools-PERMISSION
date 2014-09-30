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

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import info.servertools.core.ServerTools;
import info.servertools.core.command.CommandManager;
import info.servertools.permission.command.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, dependencies = Reference.DEPENDENCIES, acceptableRemoteVersions = "*")
public class ServerToolsPermission {

    @Mod.Instance
    public static ServerToolsPermission instance;

    public static final File permissionDir = new File(ServerTools.serverToolsDir, "permission");

    static {
        permissionDir.mkdirs();
    }

    public static final Logger log = LogManager.getLogger(Reference.MOD_ID);

    public static EventHandler eventHandler;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        PermissionConfig.init(new File(permissionDir, "permission.cfg"));
    }

    @Mod.EventHandler
    public void serverAboutToStart(FMLServerAboutToStartEvent event) {

        CommandManager.registerSTCommand(new CommandAddPlayer("addplayer"));
        CommandManager.registerSTCommand(new CommandAddUUID("adduuid"));
        CommandManager.registerSTCommand(new CommandRemovePlayer("removeplayer"));
        CommandManager.registerSTCommand(new CommandRemoveUUID("removeuuid"));
        CommandManager.registerSTCommand(new CommandCreateGroup("creategroup"));
        CommandManager.registerSTCommand(new CommandRemoveGroup("removegroup"));
        CommandManager.registerSTCommand(new CommandAddPerm("addperm"));
        CommandManager.registerSTCommand(new CommandRemovePerm("removeperm"));
        CommandManager.registerSTCommand(new CommandSetGroupColor("setgroupcolor"));
        CommandManager.registerSTCommand(new CommandSetParent("setparent"));
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        if (eventHandler == null) eventHandler = new EventHandler();
    }

    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
        PermissionManager.init(new File(permissionDir, "groups"));
    }
}
