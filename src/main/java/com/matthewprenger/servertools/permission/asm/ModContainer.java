package com.matthewprenger.servertools.permission.asm;

import com.google.common.collect.ImmutableList;
import com.matthewprenger.servertools.permission.Reference;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.ModMetadata;

public class ModContainer extends DummyModContainer {

    public ModContainer() {
        super(new ModMetadata());
        ModMetadata meta = super.getMetadata();
        meta.modId = "STPPlugin";
        meta.name = "ServerTools|Permission Plugin";
        meta.description = "Coremod for ServerTools|Permission";
        meta.authorList = ImmutableList.of("matthewprenger");
        meta.parent = Reference.MOD_ID;
    }
}
