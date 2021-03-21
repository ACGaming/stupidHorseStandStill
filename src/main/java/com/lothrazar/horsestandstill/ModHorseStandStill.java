package com.lothrazar.horsestandstill;

import org.apache.logging.log4j.Logger;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = ModHorseStandStill.MODID, certificateFingerprint = "@FINGERPRINT@", updateJSON = "")
public class ModHorseStandStill
{
    public static final String MODID = "horsestandstill";
    public static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new EventHorseStandStill());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {}
}