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

package com.matthewprenger.servertools.permission.asm;

import com.matthewprenger.servertools.core.asm.STClassTransformer;

public class STPClassTransformer extends STClassTransformer {

    static {
        PatchNote chPatch = new STClassTransformer.PatchNote("net.minecraft.command.CommandHandler", "com.matthewprenger.servertools.permission.STPCommandHandler");
        chPatch.addMethodToPatch(new STClassTransformer.MethodNote("executeCommand", "func_71556_a", "(Lnet/minecraft/command/ICommandSender;Ljava/lang/String;)I"));
        chPatch.addMethodToPatch(new STClassTransformer.MethodNote("getPossibleCommands", "func_71558_b", "(Lnet/minecraft/command/ICommandSender;Ljava/lang/String;)Ljava/util/List;"));
        chPatch.addMethodToPatch(new STClassTransformer.MethodNote("getPossibleCommands", "func_71557_a", "(Lnet/minecraft/command/ICommandSender;)Ljava/util/List;"));
        addPatch(chPatch);
    }
}
