package nova.committee.levelup.api;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import nova.committee.levelup.api.skill.IPlayerSkill;

/**
 * Project: levelup
 * Author: cnlimiter
 * Date: 2022/11/10 1:27
 * Description:
 */
public class PlayerSkillStorage {
    private IPlayerSkill skill;
    private int level;

    public PlayerSkillStorage(IPlayerSkill skill, int level) {
        this.skill = skill;
        this.level = level;
    }

    private PlayerSkillStorage() {}

    public static PlayerSkillStorage fromJson(JsonObject json) {
        String skill = GsonHelper.getAsString(json, "name");
        if (!skill.equals("")) {
            IPlayerSkill sk = SkillRegistry.getSkillFromName(new ResourceLocation(skill));
            if (sk != null) {
                return new PlayerSkillStorage(sk, GsonHelper.getAsInt(json, "level", 1));
            }
        }
        return null;
    }

    public IPlayerSkill getSkill() {
        return skill;
    }

    public int getLevel() {
        return level;
    }
}
