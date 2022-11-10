package nova.committee.levelup.api.role;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import nova.committee.levelup.api.skill.IPlayerSkill;
import nova.committee.levelup.api.skill.PlayerSkillStorage;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Project: levelup
 * Author: cnlimiter
 * Date: 2022/11/10 1:25
 * Description:
 */
public interface IRole {
    @Nonnull
    ResourceLocation getRoleName();

    default String getUnlocalizedName() {
        return "role." + getRoleName().toString() + ".name";
    }

    default String getUnlocalizedDescription() {
        return "role." + getRoleName().toString() + ".desc";
    }

    String getLocalizedName();

    String getLocalizedDescription();

    @Nonnull
    IPlayerSkill getSpecializationSkill();

    List<PlayerSkillStorage> getSkillBonuses();

    default int getBonusSkillLevel(IPlayerSkill skill) {
        if (getSkillBonuses() != null && !getSkillBonuses().isEmpty()) {
            for (PlayerSkillStorage stor : getSkillBonuses()) {
                if (stor.getSkill().getSkillName().equals(skill.getSkillName())) {
                    return stor.getLevel();
                }
            }
        }
        return 0;
    }

    default ItemStack getRepresentativeStack() {
        return getSpecializationSkill() != null ? getSpecializationSkill().getRepresentativeStack() : ItemStack.EMPTY;
    }

    default void applyBonus(Player player) {
        player.getCapability(PlayerCapability.PLAYER_CLASS, EnumFacing.UP).ifPresent(o -> {

        });
        if (pClass != null) {
            pClass.setSkillLevel(getSpecializationSkill().getSkillName(), 1);
            if (!getSkillBonuses().isEmpty()) {
                for (PlayerSkillStorage skill : getSkillBonuses()) {
                    pClass.setSkillLevel(skill.getSkill().getSkillName(), skill.getLevel());
                }
            }
        }
    }
}
