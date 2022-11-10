package nova.committee.levelup.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.UUID;

/**
 * Project: levelup
 * Author: cnlimiter
 * Date: 2022/11/10 1:38
 * Description:
 */
@AutoRegisterCapability
public interface IProcessor extends INBTSerializable<CompoundTag> {
    void extraProcessing(Player player);

    void setUUID(UUID placer);

    Player getPlayerFromUUID();

}
