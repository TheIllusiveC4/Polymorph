/*
 * Copyright (c) 2020 C4
 *
 * This file is part of Polymorph, a mod made for Minecraft.
 *
 * Polymorph is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * Polymorph is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with Polymorph.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package top.theillusivec4.polymorph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.polymorph.client.ClientEventHandler;
import top.theillusivec4.polymorph.common.CommonEventHandler;
import top.theillusivec4.polymorph.common.capability.SelectorCapability;
import top.theillusivec4.polymorph.common.integrations.CompatibilityModule;
import top.theillusivec4.polymorph.common.integrations.craftingcraft.CraftingCraftModule;
import top.theillusivec4.polymorph.common.integrations.ironfurnaces.IronFurnacesModule;
import top.theillusivec4.polymorph.common.integrations.prettypipes.PrettyPipesModule;
import top.theillusivec4.polymorph.common.integrations.refinedstorage.RefinedStorageModule;
import top.theillusivec4.polymorph.common.network.NetworkManager;
import top.theillusivec4.polymorph.server.PolymorphCommand;

@Mod(Polymorph.MODID)
public class Polymorph {

  public static final String MODID = "polymorph";
  public static final Logger LOGGER = LogManager.getLogger();

  private static final Map<String, Supplier<CompatibilityModule>> INTEGRATIONS = new HashMap<>();
  private static final List<CompatibilityModule> ACTIVE_INTEGRATIONS = new ArrayList<>();

  static {
    INTEGRATIONS.put("craftingcraft", CraftingCraftModule::new);
    INTEGRATIONS.put("refinedstorage", RefinedStorageModule::new);
    INTEGRATIONS.put("prettypipes", PrettyPipesModule::new);
    INTEGRATIONS.put("ironfurnaces", IronFurnacesModule::new);
  }

  public static boolean isFastFurnaceLoaded = false;

  public Polymorph() {
    IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
    eventBus.addListener(this::setup);
    eventBus.addListener(this::clientSetup);
    MinecraftForge.EVENT_BUS.addListener(this::registerCommand);
    ModList modList = ModList.get();
    isFastFurnaceLoaded = modList.isLoaded("fastfurnace");
    INTEGRATIONS.forEach((modid, supplier) -> {

      if (modList.isLoaded(modid)) {
        ACTIVE_INTEGRATIONS.add(supplier.get());
      }
    });
  }

  private void setup(final FMLCommonSetupEvent evt) {
    NetworkManager.register();
    SelectorCapability.register();
    MinecraftForge.EVENT_BUS.register(new CommonEventHandler());
    ACTIVE_INTEGRATIONS.forEach(CompatibilityModule::setup);
  }

  private void clientSetup(final FMLClientSetupEvent evt) {
    MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
    ACTIVE_INTEGRATIONS.forEach(CompatibilityModule::clientSetup);
  }

  private void registerCommand(final RegisterCommandsEvent evt) {
    PolymorphCommand.register(evt.getDispatcher());
  }
}
