package nova.committee.levelup.common.cap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import nova.committee.levelup.api.IProcessor;
import nova.committee.levelup.utils.UserUtil;

import java.util.UUID;

/**
 * Project: levelup
 * Author: cnlimiter
 * Date: 2022/11/10 20:05
 * Description:
 */
public class DefaultProcessor implements IProcessor {
    private Player player;
    protected UUID playerUUID;
    protected BlockEntity tile;

    public DefaultProcessor(BlockEntity entity){
        this.tile = entity;
    }

    @Override
    public void extraProcessing(Player player) {

    }

    @Override
    public void setUUID(UUID placer) {
        this.playerUUID = placer;

    }

    @Override
    public Player getPlayerFromUUID() {
        if (playerUUID == null) {
            if (player != null) player = null;
            return null;
        }
        if (player == null) {
            player = UserUtil.getPlayer(playerUUID);
        }
        return player;
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        if (playerUUID != null)
            tag.putString("player_uuid", playerUUID.toString());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("player_uuid")) {
            playerUUID = UUID.fromString(nbt.getString("player_uuid"));
            player = UserUtil.getPlayer(playerUUID);
        }
    }
}
