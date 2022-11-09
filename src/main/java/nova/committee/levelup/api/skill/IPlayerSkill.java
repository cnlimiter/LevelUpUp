package nova.committee.levelup.api.skill;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Project: levelup
 * Author: cnlimiter
 * Date: 2022/11/10 1:26
 * Description:
 */
public interface IPlayerSkill {
    ResourceLocation getSkillName();

    void setSkillName(ResourceLocation skill);

    int getLevelCost(int currentLevel);

    void setLevelCosts(int[] levels);

    /**
     * Which skill tab this skill appears in.
     */
    ResourceLocation getSkillType();

    void setSkillType(ResourceLocation type);

    ResourceLocation[] getPrerequisites();

    void setPrerequisites(ResourceLocation[] prereqs);

    int getSkillColumn();

    void setSkillColumn(int column);

    int getSkillRow();

    void setSkillRow(int row);

    /**
     * The ItemStack that renders in the GUI
     * @return A stack with the skill level.
     */
    ItemStack getRepresentativeStack();

    void setRepresentativeStack(ItemStack stack);

    boolean isMaxLevel(int level);

    int getMaxLevel();

    boolean isEnabled();

    void setEnabled(boolean enabled);

    boolean isActive();

    void setActive(boolean active);
}
