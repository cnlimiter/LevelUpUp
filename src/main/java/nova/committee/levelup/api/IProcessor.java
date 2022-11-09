package nova.committee.levelup.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/**
 * Project: levelup
 * Author: cnlimiter
 * Date: 2022/11/10 1:38
 * Description:
 */
public interface IProcessor {
    void extraProcessing(Player player);

    void setUUID(UUID placer);

    Player getPlayerFromUUID();

    CompoundTag writeToNBT(CompoundTag tag);

    void readFromNBT(CompoundTag tag);
}
