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

import com.matthewprenger.servertools.core.STVersion;
import com.matthewprenger.servertools.core.ServerTools;
import com.matthewprenger.servertools.core.command.CommandManager;
import com.matthewprenger.servertools.permission.command.*;
import com.matthewprenger.servertools.permission.config.PermissionConfig;
import com.matthewprenger.servertools.permission.handlers.EventHandler;
import com.matthewprenger.servertools.permission.perms.PermissionManager;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, dependencies = Reference.DEPENDENCIES, acceptableRemoteVersions = "*")
public class ServerToolsPermission {

    @Mod.Instance
    public static ServerToolsPermission instance;

    public static File permissionDir;

    public static final Logger log = LogManager.getLogger(Reference.MOD_ID);

    public static EventHandler eventHandler;

    @Mod.EventHandler
    public void invalidCert(FMLFingerprintViolationEvent event) {

        log.warn("Invalid ServerTools Permission fingerprint detected: {}", event.fingerprints.toString());
        log.warn("Expected: {}", event.expectedFingerprint);
        log.warn("Unpredictable results my occur");
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {

        STVersion.checkVersion("@MIN_CORE@");

        permissionDir = new File(ServerTools.serverToolsDir, "permission");
        if (permissionDir.mkdirs())
            ServerToolsPermission.log.trace("Creating Permission Directory at: " + permissionDir.getAbsolutePath());

        /* Initialize the Permission Configuration */
        PermissionConfig.init(new File(permissionDir, "permission.cfg"));
    }

    @Mod.EventHandler
    public void serverAboutToStart(FMLServerAboutToStartEvent event) {

        CommandManager.registerSTCommand(new CommandAddPlayer("addplayer"));
        CommandManager.registerSTCommand(new CommandRemovePlayer("removeplayer"));
        CommandManager.registerSTCommand(new CommandCreateGroup("creategroup"));
        CommandManager.registerSTCommand(new CommandRemoveGroup("removegroup"));
        CommandManager.registerSTCommand(new CommandAddPerm("addperm"));
        CommandManager.registerSTCommand(new CommandRemovePerm("removeperm"));
        CommandManager.registerSTCommand(new CommandSetGroupColor("setgroupcolor"));
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
