package nova.committee.levelup.init.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import nova.committee.levelup.api.IProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Project: levelup
 * Author: cnlimiter
 * Date: 2022/11/10 13:51
 * Description:
 */
public class CraftingSkillHandler {
    public static final CraftingSkillHandler INSTANCE = new CraftingSkillHandler();

    private CraftingSkillHandler() {}

    private Random rand = new Random();
    private static final ResourceLocation FURNACEMODS = new ResourceLocation("levelup", "furnacemods");
    private static final ResourceLocation FISHBONUS = new ResourceLocation("levelup", "fishbonus");
    private static final ResourceLocation CROPGROWTH = new ResourceLocation("levelup", "cropgrowth");
    private static final ResourceLocation HARVESTBONUS = new ResourceLocation("levelup", "harvestbonus");
    private static final ResourceLocation CRAFTBONUS = new ResourceLocation("levelup", "craft_bonus");

    @SubscribeEvent
    public void onCrafting(PlayerEvent.ItemCraftedEvent evt) {
        if (SkillRegistry.getSkillLevel(evt.getEntity(), CRAFTBONUS) > 0 && !isBlacklistedOutput(evt.getCrafting())) {
            if (isNotOneItemCrafting(evt.getInventory())) {
                int craftingChances = getCraftingItems(evt.craftMatrix);
                if (craftingChances > 0) {
                    int experienceGain = 0;
                    for (int i = 0; i < craftingChances; i++) {
                        if (rand.nextFloat() < 0.55F) {
                            experienceGain++;
                        }
                    }
                    if (experienceGain > 0)
                        SkillRegistry.addExperience(evt.player, experienceGain * (int)CapabilityEventHandler.getDivisor(CRAFTBONUS));
                }
            }
        }
    }

    private boolean isBlacklistedOutput(ItemStack stack) {
        if (!LevelUpConfig.blacklistOutputs.isEmpty()) {
            for (Ingredient ing : LevelUpConfig.blacklistOutputs) {
                if (ing.test(stack))
                    return true;
            }
        }
        return false;
    }

    private boolean isNotOneItemCrafting(Container inv) {
        boolean notSame = false;
        ItemStack firstStack = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (!inv.getItem(i).isEmpty() && !inv.getItem(i).getItem().hasCraftingRemainingItem(inv.getItem(i))) {
                if (firstStack.isEmpty()) {
                    firstStack = inv.getItem(i).copy();
                }
                else if (!firstStack.is(inv.getItem(i).getItem())) {
                    notSame = true;
                }
            }
        }
        return notSame;
    }

    private int getCraftingItems(Container inv) {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (!inv.getItem(i).isEmpty() && !inv.getItem(i).getItem().hasCraftingRemainingItem(inv.getItem(i))) {
                ItemStack stack = inv.getItem(i).copy();
                stack.setCount(1);
                if (items.isEmpty())
                    items.add(stack);
                else if (!SkillRegistry.listContains(stack, items))
                    items.add(stack);
            }
        }
        return items.size();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBlockBroken(BlockEvent.BreakEvent evt) {
        if (!evt.getLevel().isClientSide() && evt.getPlayer() != null && SkillRegistry.getPlayer(evt.getPlayer()).isActive()) {
            if (evt.getState().getBlock() instanceof CropBlock cropBlock || evt.getState().getBlock() instanceof StemBlock) {
                if (!((IPlantable)evt.getState().getBlock()).canGrow(evt.getWorld(), evt.getPos(), evt.getState(), false)) {
                    doCropDrops(evt);
                }
            }
            else if (evt.getState().getBlock() instanceof MelonBlock) {
                doCropDrops(evt);
            }
        }
    }

    private void doCropDrops(BlockEvent.BreakEvent evt) {
        var rand = evt.getPlayer().getRandom();
        int skill = SkillRegistry.getSkillLevel(evt.getPlayer(), HARVESTBONUS);
        if (skill > 0) {
            if (rand.nextInt((int)CapabilityEventHandler.getDivisor(HARVESTBONUS)) < skill) {
                Item item = evt.getState().getBlock().getItemDropped(evt.getState(), rand, 0);
                if (item == Items.AIR || item == null) {
                    if (evt.getState().getBlock() == Blocks.PUMPKIN_STEM)
                        item = Items.PUMPKIN_SEEDS;
                    else if (evt.getState().getBlock() == Blocks.MELON_STEM)
                        item = Items.MELON_SEEDS;
                }
                if (item != Items.AIR && item != null) {
                    evt.getLevel().addFreshEntity(new ItemEntity(evt.getLevel(), evt.getPos().getX(), evt.getPos().getY(), evt.getPos().getZ(), new ItemStack(item, Math.max(1, evt.getState().getBlock().quantityDropped(evt.getState(), 0, rand)), evt.getState().getBlock().damageDropped(evt.getState()))));
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerUpdate(TickEvent.PlayerTickEvent evt) {
        if (evt.phase == TickEvent.Phase.START && SkillRegistry.getPlayer(evt.player).isActive()) {
            var player = evt.player;
            if (player != null) {
                int skillLevel = SkillRegistry.getSkillLevel(player, CROPGROWTH);
                if (!player.level.isClientSide && skillLevel > 0 && player.getRandom().nextFloat() <= skillLevel / 500F) {
                    growCropsAround(player.level, skillLevel, player);
                }
            }
        }
    }

    private void growCropsAround(Level world, int range, Player player) {
        int posX = (int)player.getX();
        int posY = (int)player.getY();
        int posZ = (int)player.getZ();
        int dist = range / 2 + 2;
        for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(posX - dist, posY - dist, posZ - dist), new BlockPos(posX + dist + 1, posY + dist + 1, posZ + dist + 1))) {
            Block block = world.getBlockState(pos).getBlock();
            if (block instanceof IPlantable && !SkillRegistry.getCropBlacklist().contains(block)) {
                world.scheduleTick(pos, block, block.tickRate(world));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onFishInteract(PlayerInteractEvent.RightClickItem evt) {
        if (evt.getResult() != Event.Result.DENY && SkillRegistry.getPlayer(evt.getEntity()).isActive()) {
            FishingHook hook = evt.getEntity().fishing;
            if (hook != null && hook.caughtEntity == null && hook.ticksCatchable > 0) {
                ItemStack loot = getFishingLoot(evt.getWorld(), evt.getPlayer());
                if (!loot.isEmpty()) {
                    ItemStack stack = evt.getPlayer().inventory.getCurrentItem();
                    int i = stack.getCount();
                    int j = stack.getItemDamage();
                    stack.damageItem(1, evt.getPlayer());
                    evt.getEntity().swingArm(evt.getHand());
                    evt.getEntity().inventory.setInventorySlotContents(evt.getPlayer().inventory.currentItem, stack);
                    if (evt.getPlayer().capabilities.isCreativeMode) {
                        stack.grow(i);
                        if (stack.isItemStackDamageable()) {
                            stack.setItemDamage(j);
                        }
                    }
                    if (stack.getCount() <= 0) {
                        evt.getEntity().inventory.setInventorySlotContents(evt.getPlayer().inventory.currentItem, ItemStack.EMPTY);
                        MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(evt.getPlayer(), stack, evt.getHand()));
                    }
                    if (!evt.getEntity().isHandActive() && evt.getPlayer() instanceof PlayerMP) {
                        ((PlayerMP) evt.getPlayer()).sendContainerToPlayer(evt.getPlayer().inventoryContainer);
                    }
                    evt.setResult(Event.Result.DENY);
                    if (!hook.level.isClientSide) {
                        var item = new ItemEntity(hook.level, hook.getX(), hook.getY(), hook.getZ(), loot);
                        double d5 = hook.getAngler().posX - hook.posX;
                        double d6 = hook.getAngler().posY - hook.posY;
                        double d7 = hook.getAngler().posZ - hook.posZ;
                        double d8 = MathHelper.sqrt(d5 * d5 + d6 * d6 + d7 * d7);
                        double d9 = 0.1D;
                        item.motionX = d5 * d9;
                        item.motionY = d6 * d9 + MathHelper.sqrt(d8) * 0.08D;
                        item.motionZ = d7 * d9;
                        hook.world.spawnEntity(item);
                        hook.getAngler().world.spawnEntity(new EntityXPOrb(hook.getAngler().world, hook.getAngler().posX, hook.getAngler().posY + 0.5D, hook.getAngler().posZ + 0.5D, evt.getPlayer().getRNG().nextInt(6) + 1));
                    }
                }
            }
        }
    }

    private ItemStack getFishingLoot(Level world, Player player) {
        if (!world.isClientSide) {
            double divisor = 1D / CapabilityEventHandler.getDivisor(FISHBONUS);
            if (player.getRandom().nextDouble() <= SkillRegistry.getSkillLevel(player, FISHBONUS) * divisor) {
                LootContext.Builder build = new LootContext.Builder((ServerLevel) world);
                build.withLuck((float) EnchantmentHelper.getEnchantmentLevel(Enchantment.getEnchantmentByLocation("luck_of_the_sea"), player) + player.getLuck());
                return Library.getLootManager().getLootTableFromLocation(new ResourceLocation("levelup", "fishing/fishing_loot")).generateLootForPools(player.getRNG(), build.build()).get(0).copy();
            }
        }
        return ItemStack.EMPTY;
    }

    @SubscribeEvent
    public void registerTileCapability(AttachCapabilitiesEvent<TileEntity> evt) {
        if (evt.getObject() instanceof TileEntityFurnace) {
            final TileEntityFurnace furnace = (TileEntityFurnace)evt.getObject();
            evt.addCapability(FURNACEMODS, new ICapabilitySerializable<NBTTagCompound>() {
                IProcessor instance = new CapabilityFurnace(furnace);

                @Override
                public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
                    return capability == PlayerCapability.MACHINE_PROCESSING;
                }

                @Override
                public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
                    return capability == PlayerCapability.MACHINE_PROCESSING ? PlayerCapability.MACHINE_PROCESSING.<T>cast(instance) : null;
                }

                @Override
                public NBTTagCompound serializeNBT() {
                    return ((NBTTagCompound)PlayerCapability.MACHINE_PROCESSING.getStorage().writeNBT(PlayerCapability.MACHINE_PROCESSING, instance, null));
                }

                @Override
                public void deserializeNBT(NBTTagCompound tag) {
                    PlayerCapability.MACHINE_PROCESSING.getStorage().readNBT(PlayerCapability.MACHINE_PROCESSING, instance, null, tag);
                }
            });
        }
        else if (evt.getObject() instanceof TileEntityBrewingStand) {
            final TileEntityBrewingStand stand = (TileEntityBrewingStand)evt.getObject();
            evt.addCapability(FURNACEMODS, new ICapabilitySerializable<NBTTagCompound>() {
                IProcessor instance = new CapabilityBrewingStand(stand);

                @Override
                public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
                    return capability == PlayerCapability.MACHINE_PROCESSING;
                }

                @Override
                public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
                    return capability == PlayerCapability.MACHINE_PROCESSING ? PlayerCapability.MACHINE_PROCESSING.<T>cast(instance) : null;
                }

                @Override
                public NBTTagCompound serializeNBT() {
                    return ((NBTTagCompound)PlayerCapability.MACHINE_PROCESSING.getStorage().writeNBT(PlayerCapability.MACHINE_PROCESSING, instance, null));
                }

                @Override
                public void deserializeNBT(NBTTagCompound tag) {
                    PlayerCapability.MACHINE_PROCESSING.getStorage().readNBT(PlayerCapability.MACHINE_PROCESSING, instance, null, tag);
                }
            });
        }
    }

    @SubscribeEvent
    public void onTileInteracted(PlayerInteractEvent.RightClickBlock evt) {
        if (!evt.getLevel().isClientSide && evt.getEntity() != null) {
            Player player = evt.getEntity();
            if (player instanceof FakePlayer || !player.isCrouching() || !evt.getItemStack().isEmpty())
                return;
            var tile = evt.getLevel().getBlockEntity(evt.getPos());
            if (tile != null) {
                if (tile.hasCapability(PlayerCapability.MACHINE_PROCESSING, Direction.UP)) {
                    IProcessor cap = tile.getCapability(PlayerCapability.MACHINE_PROCESSING, Direction.UP);
                    if (cap != null) {
                        String name = UsernameCache.getLastKnownUsername(player.getGameProfile().getId());
                        if (cap.getPlayerFromUUID() == null) {
                            player.displayClientMessage(Component.translatable("levelup.interact.register", name), true);
                            cap.setUUID(player.getGameProfile().getId());
                        } else if (cap.getPlayerFromUUID().getGameProfile().getId() == player.getGameProfile().getId()) {
                            player.displayClientMessage(Component.translatable("levelup.interact.unregister", name), true);
                            cap.setUUID(null);
                        } else {
                            name = UsernameCache.getLastKnownUsername(cap.getPlayerFromUUID().getGameProfile().getId());
                            player.displayClientMessage(Component.translatable("levelup.interact.notowned", name), true);
                        }
                    }
                }
            }
        }
    }
}
