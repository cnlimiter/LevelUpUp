package nova.committee.levelup.api.role;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import nova.committee.levelup.api.skill.PlayerSkillStorage;

import java.util.List;

/**
 * Project: levelup
 * Author: cnlimiter
 * Date: 2022/11/10 1:31
 * Description:
 */
public class RoleProperties {
    private ResourceLocation className;
    private ResourceLocation specSkill;
    private List<BonusSkill> bonusSkills;
    private String localizedName;
    private String description;

    public static RoleProperties fromJson(JsonObject obj) {
        ResourceLocation name = new ResourceLocation(GsonHelper.getAsString(obj, "name"));
        ResourceLocation bonus = new ResourceLocation(GsonHelper.getAsString(obj, "bonus"));
        List<BonusSkill> skills = Lists.newArrayList();
        if (obj.has("skills")) {
            for (JsonElement json : GsonHelper.getAsJsonArray(obj, "skills")) {
                JsonObject o = json.getAsJsonObject();
                skills.add(new BonusSkill(new ResourceLocation(GsonHelper.getAsString(o, "name")), GsonHelper.getAsInt(o, "level", 1)));
            }
        }
        String locName = GsonHelper.getAsString(obj, "localized_name", "");
        String description = GsonHelper.getAsString(obj, "description", "");
        return new RoleProperties(name, bonus, skills, locName, description);
    }

    public RoleProperties(ResourceLocation name, ResourceLocation spec, List<BonusSkill> skills, String localizedName, String description) {
        className = name;
        specSkill = spec;
        bonusSkills = skills;
        this.localizedName = localizedName;
        this.description = description;
    }

    public ResourceLocation getClassName() {
        return className;
    }

    public ResourceLocation getSpecSkill() {
        return specSkill;
    }

    public List<PlayerSkillStorage> getBonusSkills() {
        List<PlayerSkillStorage> storage = Lists.newArrayList();
        for (BonusSkill skill : bonusSkills) {
            IPlayerSkill sk = SkillRegistry.getSkillFromName(skill.getSkill());
            if (sk != null) {
                storage.add(new PlayerSkillStorage(sk, skill.getPoints()));
            }
        }
        return storage;
    }

    public String getLocalizedName() {
        return localizedName;
    }

    public String getDescription() {
        return description;
    }

    public void writeToBytes(FriendlyByteBuf buf) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Class", className.toString());
        tag.putString("Spec", specSkill.toString());
        if (!bonusSkills.isEmpty()) {
            CompoundTag bonus = new CompoundTag();
            bonus.putInt("Size", bonusSkills.size());
            for (int i = 0; i < bonusSkills.size(); i++) {
                BonusSkill b = bonusSkills.get(i);
                bonus.putString("name_" + i, b.getSkill().toString());
                bonus.putInt("level_" + i, b.getPoints());
            }
            tag.put("Bonus", bonus);
        }
        tag.putString("LocName", localizedName);
        tag.putString("Desc", description);
        buf.writeNbt(tag);
    }

    public static RoleProperties fromNBT(CompoundTag tag) {
        ResourceLocation name = new ResourceLocation(tag.getString("Class"));
        ResourceLocation spec = new ResourceLocation(tag.getString("Spec"));
        List<BonusSkill> bSkills = Lists.newArrayList();
        if (tag.contains("Bonus")) {
            CompoundTag bonus = tag.getCompound("Bonus");
            for (int i = 0; i < bonus.getInt("Size"); i++) {
                ResourceLocation bName = new ResourceLocation(bonus.getString("name_" + i));
                int level = bonus.getInt("level_" + i);
                bSkills.add(new BonusSkill(bName, level));
            }
        }
        String locName = tag.getString("LocName");
        String desc = tag.getString("Desc");
        return new RoleProperties(name, spec, bSkills, locName, desc);
    }

    private static class BonusSkill {
        private ResourceLocation skill;
        private int points;

        public BonusSkill(ResourceLocation skill, int points) {
            this.skill = skill;
            this.points = points;
        }

        public ResourceLocation getSkill() {
            return skill;
        }

        public int getPoints() {
            return points;
        }
    }
}
