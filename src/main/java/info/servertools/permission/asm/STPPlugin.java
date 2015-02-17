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
package info.servertools.permission.asm;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.IFMLCallHook;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import info.servertools.permission.Reference;

import java.util.Map;

@IFMLLoadingPlugin.SortingIndex(Integer.MAX_VALUE)
@IFMLLoadingPlugin.MCVersion(Reference.MC_VERSION)
@IFMLLoadingPlugin.TransformerExclusions({"matthewprenger.servertools.permission.asm"})
public class STPPlugin implements IFMLLoadingPlugin, IFMLCallHook {

    @Override
    public Void call() throws Exception {
        if (!Reference.MC_VERSION.equals(Loader.MC_VERSION)) {
            throw new RuntimeException(String.format("This version of servertools was built for minecraft %s, but %s was found", Reference.MC_VERSION, Loader.MC_VERSION));
        }
        return null;
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{STPClassTransformer.class.getName()};
    }

    @Override
    public String getModContainerClass() {
        return ModContainer.class.getName();
    }

    @Override
    public String getSetupClass() {
        return this.getClass().getName();
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
