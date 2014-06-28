package com.matthewprenger.servertools.permission.perms;

import com.matthewprenger.servertools.permission.Group;
import com.matthewprenger.servertools.permission.Reference;
import gnu.trove.set.hash.THashSet;
import net.minecraft.dispenser.ILocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.permissions.api.IGroup;
import net.minecraftforge.permissions.api.PermBuilderFactory;
import net.minecraftforge.permissions.api.context.*;

import java.util.Collection;
import java.util.List;

public class STPPermBuilderFactory implements PermBuilderFactory<Builder> {

    public static STPPermBuilderFactory INSTANCE = new STPPermBuilderFactory(); //TODO Remove when ForgePermAPI is pulled

    private static final IContext BLANK = new IContext() {
    };

    @Override
    public String getName() {
        return Reference.MOD_NAME;
    }

    @Override
    public Builder builder() {
        return new Builder();
    }

    @Override
    public Builder builder(EntityPlayer player, String permNode) {
        return new Builder().setUser(player).setPermNode(permNode);
    }

    @Override
    public IContext getDefaultContext(EntityPlayer player) {
        return new PlayerContext(player);
    }

    @Override
    public IContext getDefaultContext(TileEntity te) {
        return new TileEntityContext(te);
    }

    @Override
    public IContext getDefaultContext(ILocation loc) {
        return null;
    }

    @Override
    public IContext getDefaultContext(Entity entity) {
        return new EntityContext(entity);
    }

    @Override
    public IContext getDefaultContext(World world) {
        return new WorldContext(world);
    }

    @Override
    public IContext getGlobalContext() {
        return BLANK;
    }

    @Override
    public IContext getDefaultContext(Object whoKnows) {
        return BLANK;
    }

    @Override
    public void registerPermissions(List<PermReg> perms) {

    }

    @Override
    public void initialize() {

    }

    @Override
    public Collection<IGroup> getGroup(EntityPlayer player) {

        Collection<IGroup> groups = new THashSet<>();

        for (Group group : PermissionManager.getGroups(player.getPersistentID()))
            groups.add(group);

        return groups;
    }

    @Override
    public IGroup getGroup(String name) {
        return PermissionManager.getGroup(name);
    }

    @Override
    public Collection<IGroup> getAllGroups() {

        Collection<IGroup> groups = new THashSet<>();

        for (Group group : PermissionManager.getAllGroups())
            groups.add(group);

        return groups;
    }
}
