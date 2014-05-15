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

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class PermissionConfig {

    public static String defaultGroup;
    public static boolean prefixChatGroupName;

    public static void init(File file) {

        Configuration configuration = new Configuration(file);

        try {

            configuration.load();

            defaultGroup = configuration.get(Configuration.CATEGORY_GENERAL, "Default Group", "Player", "The group players will be added to if they aren't in any groups").getString();
            prefixChatGroupName = configuration.get(Configuration.CATEGORY_GENERAL, "Enable Group Chat Prefix", true, "Prefix players names in chat with their group names").getBoolean(true);

            if (configuration.hasChanged()) {
                configuration.save();
            }

        } catch (Exception e) {
            e.printStackTrace();
            ServerToolsPermission.log.fatal("Failed to load permission configuration");
        }
    }
}
