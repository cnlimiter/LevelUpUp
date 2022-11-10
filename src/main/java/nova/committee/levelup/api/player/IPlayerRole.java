package nova.committee.levelup.api.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.util.INBTSerializable;
import nova.committee.levelup.api.skill.IPlayerSkill;

import java.util.Map;

/**
 * Project: levelup
 * Author: cnlimiter
 * Date: 2022/11/10 19:47
 * Description:
 */
@AutoRegisterCapability
public interface IPlayerRole extends INBTSerializable<CompoundTag> {

    IPlayerSkill getSkillFromName(ResourceLocation skill);

    default int getSkillLevel(ResourceLocation name) {
        return getSkillLevel(name, true);
    }

    int getSkillLevel(ResourceLocation name, boolean checkActive);

    void setSkillLevel(ResourceLocation name, int level);

    void setPlayerData(ResourceLocation[] skills, int[] data);

    void addToSkill(ResourceLocation name, int value);

    boolean hasClass();

    ResourceLocation getPlayerClass();

    boolean isActive();

    void toggleActive();

    void resetClass();

    void setPlayerClass(ResourceLocation location);

    int getLevelBank();

    boolean addLevelFromExperience(Player player);

    void changeLevelBank(int levels);

    ResourceLocation getSpecialization();

    Map<ResourceLocation, Integer> getSkills();
}
