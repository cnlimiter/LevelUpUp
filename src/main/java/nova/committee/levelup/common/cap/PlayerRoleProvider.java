package nova.committee.levelup.common.cap;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import nova.committee.levelup.api.player.IPlayerRole;
import nova.committee.levelup.api.player.PlayerExtension;
import nova.committee.levelup.init.registry.ModCapabilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Project: levelup
 * Author: cnlimiter
 * Date: 2022/11/10 20:10
 * Description:
 */
public class PlayerRoleProvider implements NonNullSupplier<IPlayerRole>, ICapabilitySerializable<CompoundTag> {
    private final LazyOptional<IPlayerRole> optional;
    private final IPlayerRole data;

    public PlayerRoleProvider() {
        this.data = new PlayerExtension();
        this.optional = LazyOptional.of(() -> data);
    }
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return ModCapabilities.PLAYER_ROLE.orEmpty(cap, optional);
    }

    @Override
    public CompoundTag serializeNBT() {
        return this.data.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.data.deserializeNBT(nbt);
    }

    @Override
    public @NotNull IPlayerRole get() {
        return this.data;
    }
}
