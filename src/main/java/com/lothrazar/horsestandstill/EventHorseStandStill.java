package com.lothrazar.horsestandstill;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventHorseStandStill
{
    private static final int DISTANCE = 6;
    private static final String STATE_WAITING = ModHorseStandStill.MODID + ".waiting";
    private static final String STATE_RIDING = ModHorseStandStill.MODID + ".riding";
    private static final String NBT_RIDING = ModHorseStandStill.MODID + ".tracked";
    private static final String NBT_TRACKEDX = ModHorseStandStill.MODID + ".trackedx";
    private static final String NBT_TRACKEDY = ModHorseStandStill.MODID + ".trackedy";
    private static final String NBT_TRACKEDZ = ModHorseStandStill.MODID + ".trackedz";

    @SubscribeEvent
    public void onHit(LivingEvent.LivingUpdateEvent event)
    {
        EntityLivingBase entity = event.getEntityLiving();
        if (!(entity instanceof AbstractHorse))
        {
            return;
        }
        //find my horse
        AbstractHorse horse = (AbstractHorse) entity;
        boolean emptyState = !horse.getEntityData().hasKey(NBT_RIDING);
        boolean ridingState = STATE_RIDING.equals(horse.getEntityData().getString(NBT_RIDING));
        boolean isWaitingState = STATE_WAITING.equals(horse.getEntityData().getString(NBT_RIDING));
        boolean isPlayerRiding = this.isRiddenByPlayer(horse);
        boolean isSaddled = horse.isHorseSaddled();

        if (emptyState)
        {
            if (entity.world.isRemote)
            {
                //no client side data or tracking needed
                return;
            }
            //ModHorseStandStill.logger.info("CURRENT = empty");
            if (isPlayerRiding)
            {
                //from no state to RIDING.
                //ModHorseStandStill.logger.info("no state to riding");
                setRidingState(horse);
            }
            else
            {
                horse.setNoAI(false);
            }
        }
        else if (ridingState)
        {
            if (entity.world.isRemote)
            {
                //no client side data or tracking needed
                return;
            }
            //ModHorseStandStill.logger.info("CURRENT = riding");
            //player is riding
            //did it get off
            if (!isPlayerRiding)
            {
                //still saddled, player has gotten off though
                //but im in riding state
                //so move to waiting
                if (isSaddled)
                {
                    //ModHorseStandStill.logger.info("saddled but no player, riding -> waiting");
                    setWaitingStateAndPos(horse);
                }
                else
                {
                    //ModHorseStandStill.logger.info("NOT saddled and no player, riding -> no state");
                    clearState(horse);
                    horse.setNoAI(false);
                }
            }
            //still riding i guess
        }
        else if (isWaitingState)
        {
            if (entity.world.isRemote)
            {
                //no client side data or tracking needed
                return;
            }
            //ModHorseStandStill.logger.info("CURRENT = waiting");
            if (isSaddled && horse.isEntityAlive() && !horse.isInWater())
            {
                //still WAITING, ok do my thing
                //wait did a player jump on my back just now
                if (isPlayerRiding)
                {
                    //waiting to riding
                    //ModHorseStandStill.logger.info("waiting to riding");
                    setRidingState(horse);
                }
                else
                {
                    //stay waiting
                    //ModHorseStandStill.logger.info("waiting TICK");
                    onWaitingStateTick(horse);
                    //the only place we use true
                    horse.setNoAI(true);
                }
            }
            else
            {
                //ModHorseStandStill.logger.info("Clear state");
                //dead or not saddled, player must have removed, just clear me
                //set to no state
                clearState(horse);
                horse.setNoAI(false);
            }
        }
    }

    private boolean isRiddenByPlayer(AbstractHorse horse)
    {
        return horse.getControllingPassenger() instanceof EntityPlayer;
    }

    private void onWaitingStateTick(AbstractHorse horse)
    {
        horse.spawnRunningParticles();
        //player not riding AND its tagged with NBT_RIDING false
        int x = horse.getEntityData().getInteger(NBT_TRACKEDX);
        int y = horse.getEntityData().getInteger(NBT_TRACKEDY);
        int z = horse.getEntityData().getInteger(NBT_TRACKEDZ);
        //ok am i too far away
        BlockPos pos = new BlockPos(x, y, z);
        double distance = UtilWorld.distanceBetweenHorizontal(pos, horse.getPosition());
        if (distance > DISTANCE)
        {
            //so here we go
            //ModHorseStandStill.logger.info(horse.world.isRemote + " warped horse since distance was = " + distance);
            horse.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1, 1);
        }
    }

    private void clearState(AbstractHorse horse)
    {
        //clear all data
        horse.getEntityData().removeTag(NBT_RIDING);
        horse.getEntityData().removeTag(NBT_TRACKEDX);
        horse.getEntityData().removeTag(NBT_TRACKEDY);
        horse.getEntityData().removeTag(NBT_TRACKEDZ);
    }

    private void setWaitingStateAndPos(AbstractHorse horse)
    {
        horse.getEntityData().setString(NBT_RIDING, STATE_WAITING);
        BlockPos pos = horse.getPosition();
        horse.getEntityData().setInteger(NBT_TRACKEDX, pos.getX());
        horse.getEntityData().setInteger(NBT_TRACKEDY, pos.getY());
        horse.getEntityData().setInteger(NBT_TRACKEDZ, pos.getZ());
    }

    private void setRidingState(AbstractHorse horse)
    {
        horse.getEntityData().setString(NBT_RIDING, STATE_RIDING);
        horse.getEntityData().removeTag(NBT_TRACKEDX);
        horse.getEntityData().removeTag(NBT_TRACKEDY);
        horse.getEntityData().removeTag(NBT_TRACKEDZ);
    }
}