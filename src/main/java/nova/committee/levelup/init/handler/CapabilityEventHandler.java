package nova.committee.levelup.init.handler;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import nova.committee.levelup.Static;
import nova.committee.levelup.api.player.PlayerExtension;
import nova.committee.levelup.api.role.IRole;
import nova.committee.levelup.api.skill.IPlayerSkill;
import nova.committee.levelup.common.cap.PlayerRoleProvider;
import nova.committee.levelup.init.registry.SkillRegistry;

/**
 * Project: levelup
 * Author: cnlimiter
 * Date: 2022/11/10 19:55
 * Description:
 */
public class CapabilityEventHandler {


    @SubscribeEvent
    public static void attachCap(final AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(new ResourceLocation(Static.MOD_ID, "player_roles"), new PlayerRoleProvider());
        }
    }


    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone evt) {
        if (!evt.isWasDeath() || !LevelUpConfig.resetClassOnDeath) {
            var data = new CompoundTag();
            SkillRegistry.getPlayer(evt.getOriginal()).orElse(new PlayerExtension()).deserializeNBT(data);
            SkillRegistry.getPlayer(evt.getEntity()).orElse(new PlayerExtension()).serializeNBT();
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent evt) {
        SkillRegistry.loadPlayer(evt.getEntity());
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent evt) {
        SkillRegistry.loadPlayer(evt.getEntity());
    }

    private static final String BOOK_TAG = "levelup:bookspawn";

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent evt) {
        if (evt.getEntity() instanceof ServerPlayer player) {
            spawnBook(evt.getEntity());
            SkillRegistry.loadPlayer(evt.getEntity());
            SkillPacketHandler.configChannel.sendTo(SkillPacketHandler.getConfigPacket(LevelUpConfig.getServerProperties()), player);
            for (ResourceLocation loc : SkillRegistry.getSkills().keySet()) {
                IPlayerSkill skill = SkillRegistry.getSkillFromName(loc);
                SkillPacketHandler.propertyChannel.sendTo(SkillPacketHandler.getPropertyPackets(skill), player);
            }
            for (ResourceLocation loc : SkillRegistry.getClasses().keySet()) {
                IRole cl = SkillRegistry.getClassFromName(loc);
                SkillPacketHandler.classChannel.sendTo(SkillPacketHandler.getClassPackets(cl), player);
            }
            SkillPacketHandler.refreshChannel.sendTo(SkillPacketHandler.getRefreshPacket(), player);
        }
    }

    private void spawnBook(Player player) {
        if (LevelUpConfig.giveSkillBook) {
            CompoundTag playerData = player.getPersistentData();
            CompoundTag data = getTag(playerData, Player.PERSISTED_NBT_TAG);
            if (!data.getBoolean(BOOK_TAG)) {
                ItemStack book = new ItemStack(SkillRegistry.skillBook);
                if (!player.addItem(book)) {
                    player.drop(book, true);
                }
                data.putBoolean(BOOK_TAG, true);
                playerData.put(Player.PERSISTED_NBT_TAG, data);
            }
        }
    }

    private CompoundTag getTag(CompoundTag base, String tag) {
        if (base == null)
            return new CompoundTag();
        return base.getCompound(tag);
    }

    public static double getDivisor(ResourceLocation skill) {
        IPlayerSkill sk = SkillRegistry.getSkillFromName(skill);
        if (sk != null) {
            return SkillRegistry.getProperty(sk).getDivisor();
        }
        return 1;
    }
}
