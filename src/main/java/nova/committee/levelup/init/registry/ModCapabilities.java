package nova.committee.levelup.init.registry;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityToken;
import nova.committee.levelup.api.IProcessor;
import nova.committee.levelup.api.player.IPlayerRole;
import static net.minecraftforge.common.capabilities.CapabilityManager.get;

/**
 * Project: levelup
 * Author: cnlimiter
 * Date: 2022/11/10 20:02
 * Description:
 */
public class ModCapabilities {
    public static final Capability<IPlayerRole> PLAYER_ROLE = get(new CapabilityToken<>(){});
    public static Capability<IProcessor> MACHINE_PROCESSING = get(new CapabilityToken<>(){});

}
