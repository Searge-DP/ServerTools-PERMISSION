package com.matthewprenger.servertools.permission.perms;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.permissions.api.PermBuilder;
import net.minecraftforge.permissions.api.context.IContext;

public class Builder implements PermBuilder<Builder> {

    String node;
    EntityPlayer player;

    @Override
    public boolean check() {

        return PermissionManager.checkPerm(node, player.getPersistentID());
    }

    @Override
    public Builder setUser(EntityPlayer player) {
        this.player = player;
        return this;
    }

    @Override
    public Builder setPermNode(String node) {
        this.node = node;
        return this;
    }

    @Override
    public Builder setTargetContext(IContext context) {
        return this;
    }

    @Override
    public Builder setUserContext(IContext context) {
        return this;
    }
}
