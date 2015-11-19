package totemic_commons.pokefenn.ceremony;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.world.World;
import totemic_commons.pokefenn.api.ceremony.Ceremony;
import totemic_commons.pokefenn.api.ceremony.CeremonyTime;
import totemic_commons.pokefenn.api.music.MusicInstrument;
import totemic_commons.pokefenn.entity.animal.EntityBuffalo;
import totemic_commons.pokefenn.util.EntityUtil;

/**
 * Created by Pokefenn.
 * Licensed under MIT (If this is one of my Mods)
 */
public class CeremonyBuffaloDance extends Ceremony
{
    public CeremonyBuffaloDance(String modid, String name, int musicNeeded, CeremonyTime maxStartupTime, CeremonyTime effectTime,
            int musicPer5, MusicInstrument... instruments)
    {
        super(modid, name, musicNeeded, maxStartupTime, effectTime, musicPer5, instruments);
    }

    @Override
    public void effect(World world, int x, int y, int z)
    {
        int i = 0;

        for(Entity entity : EntityUtil.getEntitiesInRange(world, x, y, z, 8, 8))
        {
            if(i < 2)
            {
                if(entity instanceof EntityCow && !(entity instanceof EntityBuffalo))
                {
                    i++;
                    EntityBuffalo buffalo = new EntityBuffalo(world);
                    float health = ((EntityLivingBase)entity).getHealth() / ((EntityLivingBase)entity).getMaxHealth() * buffalo.getMaxHealth();
                    buffalo.setHealth(health);
                    EntityUtil.spawnEntity(world, entity.posX, entity.posY, entity.posZ, buffalo);
                    entity.setDead();
                }
            }
        }
    }
}
