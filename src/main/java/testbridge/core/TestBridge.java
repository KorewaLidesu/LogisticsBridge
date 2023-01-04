package testbridge.core;

import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Getter;

import net.minecraft.block.Block;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.SidedProxy;

import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.blocks.LogisticsProgramCompilerTileEntity;
import logisticspipes.blocks.LogisticsProgramCompilerTileEntity.ProgrammCategories;
import logisticspipes.LPItems;
import logisticspipes.items.ItemUpgrade;
import logisticspipes.recipes.NBTIngredient;
import logisticspipes.recipes.RecipeManager;

import testbridge.helpers.datafixer.TBDataFixer;
import testbridge.integration.IntegrationRegistry;
import testbridge.integration.IntegrationType;
import testbridge.network.GuiHandler;
import testbridge.part.PartSatelliteBus;
import testbridge.pipes.PipeCraftingManager;
import testbridge.pipes.ResultPipe;
import testbridge.pipes.upgrades.BufferCMUpgrade;
import testbridge.proxy.CommonProxy;
import testbridge.client.TB_Textures;

@Mod(modid = TestBridge.MODID, name = TestBridge.NAME, version = TestBridge.VERSION, dependencies = TestBridge.DEPS, acceptedMinecraftVersions = "1.12.2")
public class TestBridge {

  public static final String MODID = "testbridge";
  public static final String NAME = "Test Bridge";
  public static final String VERSION = "@VERSION@";
  public static final String DEPS = "after:appliedenergistics2;after:refinedstorage@[1.6.15,);required-after:mixinbooter@[4.2,);required-after:logisticspipes@[0.10.4.,);";

  @Getter
  private static final boolean debug = Boolean.getBoolean("tb.debugging");

  public TestBridge() {
    MinecraftForge.EVENT_BUS.register(this);
  }

  @Mod.Instance(TestBridge.MODID)
  public static TestBridge INSTANCE;

  @SidedProxy(
      clientSide = "testbridge.proxy.ClientProxy",
      serverSide = "testbridge.proxy.CommonProxy")
  public static CommonProxy proxy;

  public static final Logger log = LogManager.getLogger(NAME);

  public static TB_Textures TBTextures = new TB_Textures();

  @Getter
  private static boolean AELoaded;
  @Getter
  private static boolean RSLoaded;
  @Getter
  private static boolean TOPLoaded;

  @Mod.EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    log.info("==================================================================================");
    log.info("Test Bridge: Start Pre Initialization");
    long tM = System.currentTimeMillis();
    AELoaded = Loader.isModLoaded("appliedenergistics2");
    RSLoaded = Loader.isModLoaded("refinedstorage");
    TOPLoaded = Loader.isModLoaded("theoneprobe");

    //TODO: preInit

    proxy.preInit(event);

    for (final IntegrationType type : IntegrationType.values()) {
      IntegrationRegistry.INSTANCE.add(type);
    }

    if (AELoaded) {
      AE2Plugin.preInit();
    }

    if (RSLoaded) {
      // TODO
    }

    IntegrationRegistry.INSTANCE.preInit();

    MinecraftForge.EVENT_BUS.register(TB_EventHandlers.class);
    proxy.registerRenderers();

    log.info("Pre Initialization took in {} milliseconds", (System.currentTimeMillis() - tM));
    log.info("==================================================================================");
  }

  @Mod.EventHandler
  public void init(FMLInitializationEvent evt) {
    log.info("==================================================================================");
    log.info("Start Initialization");
    long tM = System.currentTimeMillis();

    //TODO: init

    NetworkRegistry.INSTANCE.registerGuiHandler(TestBridge.INSTANCE, new GuiHandler());
    TBDataFixer.INSTANCE.init();

    if (evt.getSide() == Side.SERVER) {
      TestBridge.TBTextures.registerBlockIcons(null);
    }

    loadRecipes();
    proxy.init(evt);

    IntegrationRegistry.INSTANCE.init();

    log.info("Initialization took in {} milliseconds", (System.currentTimeMillis() - tM));
    log.info("==================================================================================");
  }

  @Mod.EventHandler
  public void postInit(FMLPostInitializationEvent event) {
    log.info("==================================================================================");
    log.info("Start Post Initialization");
    long tM = System.currentTimeMillis();

    //TODO: onPostInit

    IntegrationRegistry.INSTANCE.postInit();

    log.info("Post Initialization took in {} milliseconds", (System.currentTimeMillis() - tM));
    log.info("==================================================================================");
  }

  @SubscribeEvent
  public void initItems(RegistryEvent.Register<Item> event) {
    IForgeRegistry<Item> registry = event.getRegistry();
    //Items
    if (AELoaded){
//      registry.register(TB_ItemHandlers.itemHolder);
      registry.register(TB_ItemHandlers.itemPackage);
      registry.register(TB_ItemHandlers.virtualPattern);
    }
    // Pipe
    LogisticsBlockGenericPipe.registerPipe(registry, "result", ResultPipe::new);
    LogisticsBlockGenericPipe.registerPipe(registry, "crafting_manager", PipeCraftingManager::new);
    // Upgrade
    ItemUpgrade.registerUpgrade(registry, BufferCMUpgrade.getName(), BufferCMUpgrade::new);
  }

  @SubscribeEvent
  public void initBlocks(RegistryEvent.Register<Block> event) {
    IForgeRegistry<Block> registry = event.getRegistry();
    // TODO Block
  }

  @Mod.EventHandler
  public void cleanup(FMLServerStoppingEvent event) {
    ResultPipe.cleanup();
    if (AELoaded) {
      PartSatelliteBus.cleanup();
    }
  }

  @Mod.EventHandler
  public void onServerLoad(FMLServerStartingEvent event) {
    // TODO
  }

  private static void loadRecipes() {
    ResourceLocation resultPipe = TB_ItemHandlers.pipeResult.getRegistryName();
    ResourceLocation craftingMgrPipe = TB_ItemHandlers.pipeCraftingManager.getRegistryName();
    ResourceLocation bufferUpgrage = TB_ItemHandlers.upgradeBuffer.getRegistryName();

    LogisticsProgramCompilerTileEntity.programByCategory.get(ProgrammCategories.MODDED).add(resultPipe);
    LogisticsProgramCompilerTileEntity.programByCategory.get(ProgrammCategories.MODDED).add(craftingMgrPipe);
    LogisticsProgramCompilerTileEntity.programByCategory.get(ProgrammCategories.MODDED).add(bufferUpgrage);
    ResourceLocation group = new ResourceLocation(MODID, "recipes");

    if (isAELoaded())
      AE2Plugin.loadRecipes(group);

    //  Result Pipe
    RecipeManager.craftingManager.addRecipe(new ItemStack(TB_ItemHandlers.pipeResult),
        new RecipeManager.RecipeLayout(
            " p ",
            "rfr",
            " s "
        ),
        new RecipeManager.RecipeIndex('p', getIngredientForProgrammer(TB_ItemHandlers.pipeResult)),
        new RecipeManager.RecipeIndex('r', "dustRedstone"),
        new RecipeManager.RecipeIndex('f', LPItems.chipFPGA),
        new RecipeManager.RecipeIndex('s', LPItems.pipeBasic)
    );

    // Crafting Manager pipe
    RecipeManager.craftingManager.addRecipe(new ItemStack(TB_ItemHandlers.pipeCraftingManager),
        new RecipeManager.RecipeLayout(
            "fpf",
            "bsb",
            "fdf"
        ),
        new RecipeManager.RecipeIndex('b', LPItems.chipAdvanced),
        new RecipeManager.RecipeIndex('p', getIngredientForProgrammer(TB_ItemHandlers.pipeCraftingManager)),
        new RecipeManager.RecipeIndex('s', LPItems.pipeCrafting),
        new RecipeManager.RecipeIndex('d', "gemDiamond"),
        new RecipeManager.RecipeIndex('f', LPItems.chipFPGA)
    );

    // Buffer Upgrade
    RecipeManager.craftingManager.addRecipe(new ItemStack(TB_ItemHandlers.upgradeBuffer, 1),
        new RecipeManager.RecipeLayout(
            "rpr",
            "gag",
            "qnq"
        ),
        new RecipeManager.RecipeIndex('r', "dustRedstone"),
        new RecipeManager.RecipeIndex('p', getIngredientForProgrammer(TB_ItemHandlers.upgradeBuffer)),
        new RecipeManager.RecipeIndex('g', "ingotGold"),
        new RecipeManager.RecipeIndex('a', LPItems.chipAdvanced),
        new RecipeManager.RecipeIndex('q', "paper"),
        new RecipeManager.RecipeIndex('n', "nuggetGold")
    );
  }

  @SuppressWarnings("null")
  private static Ingredient getIngredientForProgrammer(Item targetPipe) {
    ItemStack programmerStack = new ItemStack(LPItems.logisticsProgrammer);
    programmerStack.setTagCompound(new NBTTagCompound());

    // Suppress NPE warning
    assert programmerStack.getTagCompound() != null;
    assert targetPipe.getRegistryName() != null;

    programmerStack.getTagCompound().setString("LogisticsRecipeTarget", targetPipe.getRegistryName().toString());
    return NBTIngredient.fromStacks(programmerStack);
  }
}
