package nova.committee.levelup.api.role;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import nova.committee.levelup.api.skill.IPlayerSkill;
import nova.committee.levelup.api.PlayerSkillStorage;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Project: levelup
 * Author: cnlimiter
 * Date: 2022/11/10 1:30
 * Description:
 */
public class BaseRole implements IRole {
    private ResourceLocation className;
    private IPlayerSkill bonusSkill;
    private List<PlayerSkillStorage> applicableSkills;
    private String localizedName;
    private String description;

    public BaseRole(ResourceLocation location, IPlayerSkill spec, List<PlayerSkillStorage> skillBonuses, String locName, String desc) {
        className = location;
        bonusSkill = spec;
        applicableSkills = skillBonuses;
        localizedName = locName;
        description = desc;
    }

    public static BaseRole fromProperties(RoleProperties props) {
        ResourceLocation location = props.getClassName();
        IPlayerSkill skill = SkillRegistry.getSkillFromName(props.getSpecSkill());
        List<PlayerSkillStorage> skills = props.getBonusSkills();
        String locName = props.getLocalizedName();
        String desc = props.getDescription();
        return new BaseRole(location, skill, skills, locName, desc);
    }

    public BaseRole fromJson(JsonObject json) {
        ResourceLocation location = new ResourceLocation(GsonHelper.getAsString(json, "name"));
        IPlayerSkill bonus = SkillRegistry.getSkillFromName(new ResourceLocation(GsonHelper.getAsString(json, "bonus")));
        List<PlayerSkillStorage> skillBonuses = Lists.newArrayList();
        for (JsonElement obj : GsonHelper.getAsJsonArray(json, "skills")) {
            skillBonuses.add(PlayerSkillStorage.fromJson(obj.getAsJsonObject()));
        }
        return new BaseRole(location, bonus, skillBonuses, "", "");
    }

    @Override
    @Nonnull
    public ResourceLocation getRoleName() {
        return className;
    }

    @Override
    @Nonnull
    public IPlayerSkill getSpecializationSkill() {
        return bonusSkill;
    }

    @Override
    public List<PlayerSkillStorage> getSkillBonuses() {
        return applicableSkills;
    }

    @Override
    public String getLocalizedName() {
        return localizedName;
    }

    @Override
    public String getLocalizedDescription() {
        return description;
    }
}
