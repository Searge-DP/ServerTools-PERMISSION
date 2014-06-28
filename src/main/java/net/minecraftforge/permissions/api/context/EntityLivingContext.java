package net.minecraftforge.permissions.api.context;

import net.minecraft.entity.EntityLiving;
import net.minecraftforge.permissions.api.context.IContext.IHealthContext;

public class EntityLivingContext extends EntityContext implements IHealthContext
{
    private final float max, current;

    public EntityLivingContext(EntityLiving entity)
    {
        super(entity);
        max = entity.getMaxHealth();
        current = entity.getHealth();
    }

    public float getMaxHealth()
    {
        return max;
    }

    public float getCurrentHealth()
    {
        return current;
    }
}