package nova.committee.levelup.api.player;

import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import nova.committee.levelup.api.skill.PlayerSkillStorage;
import nova.committee.levelup.api.role.IRole;
import nova.committee.levelup.api.skill.IPlayerSkill;
import nova.committee.levelup.init.registry.SkillRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Project: levelup
 * Author: cnlimiter
 * Date: 2022/11/10 19:48
 * Description:
 */
public class PlayerExtension implements IPlayerRole {
    private Map<ResourceLocation, Integer> skillMap = new HashMap<>();
    private ResourceLocation playerClass;
    private int levels = 0;
    private boolean isActive = true;

    public PlayerExtension() {
        for (ResourceLocation loc : SkillRegistry.getSkills().keySet()) {
            skillMap.put(loc, 0);
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        if (playerClass != null)
            tag.putString("Class", playerClass.toString());
        if (levels > 0)
            tag.putInt("Levels", levels);
        Map<String, CompoundTag> types = Maps.newHashMap();
        for (ResourceLocation skillName : skillMap.keySet()) {
            IPlayerSkill skill = getSkillFromName(skillName);
            ResourceLocation skillType = skill.getSkillType();
            String type = skillType.getPath();
            if (type.endsWith("_bonus"))
                type = type.replace("_bonus", "");
            if (!types.containsKey(type)) {
                types.put(type, new CompoundTag());
            }
            types.get(type).putInt(skill.getSkillName().toString(), skillMap.get(skillName));
        }
        for (String type : types.keySet()) {
            tag.put(type, types.get(type));
        }
        tag.putByte("Version", (byte)2);
        tag.putBoolean("Active", isActive);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (!nbt.contains("Version")) {
            for (ResourceLocation skill : skillMap.keySet()) {
                IPlayerSkill sk = getSkillFromName(skill);
                ResourceLocation type = sk.getSkillType();
                String skillName = type.getPath().startsWith("m") ? "Mining" : type.getPath().startsWith("cr") ? "Crafting" : "Combat";
                CompoundTag skillTag = nbt.getCompound(skillName);
                setSkillLevel(skill, skillTag.getInt(sk.getSkillName().toString()));
            }
        }
        else {
            for (ResourceLocation skill : skillMap.keySet()) {
                IPlayerSkill playerSkill = getSkillFromName(skill);
                ResourceLocation skillType = playerSkill.getSkillType();
                String skillName = skillType.getPath();
                if (skillName.endsWith("_bonus")) {
                    skillName = skillName.replace("_bonus", "");
                }
                CompoundTag skillTag = nbt.getCompound(skillName);
                setSkillLevel(skill, skillTag.getInt(playerSkill.getSkillName().toString()));
            }
        }
        if (nbt.contains("Class")) {
            playerClass = new ResourceLocation(nbt.getString("Class"));
        } else {
            playerClass = getClassFromSpecialization();
        }
        if (nbt.contains("Levels")) {
            levels = nbt.getInt("Levels");
        }
        if (nbt.contains("Active")) {
            isActive = nbt.getBoolean("Active");
        }
    }
    @Override
    public IPlayerSkill getSkillFromName(ResourceLocation name) {
        return SkillRegistry.getSkillFromName(name);
    }

    @Override
    public void addToSkill(ResourceLocation name, int increase) {
        setSkillLevel(name, skillMap.get(name) + increase);
    }

    @Override
    public int getSkillLevel(ResourceLocation name, boolean checkActive) {
        if (getSkillFromName(name) == null || !getSkillFromName(name).isActive() || (checkActive && !isActive())) return 0;
        return skillMap.get(name);
    }

    @Override
    public void setSkillLevel(ResourceLocation name, int level) {
        IPlayerSkill skill = getSkillFromName(name);
        if (level > skill.getMaxLevel()) level = skill.getMaxLevel();
        skillMap.put(name, level);
    }

    @Override
    public void setPlayerData(ResourceLocation[] names, int[] data) {
        for (int i = 0; i < names.length && i < data.length; i++) {
            setSkillLevel(names[i], data[i]);
        }
    }

    @Override
    public boolean hasClass() {
        return playerClass != null;
    }

    @Override
    public ResourceLocation getPlayerClass() {
        return playerClass;
    }

    @Override
    public int getLevelBank() {
        return levels;
    }

    @Override
    public boolean addLevelFromExperience(Player player) {
        if (player.experienceLevel >= LevelUpConfig.levelCost) {
            levels++;
            player.giveExperienceLevels(-LevelUpConfig.levelCost);
            return true;
        }
        return false;
    }

    @Override
    public void changeLevelBank(int levels) {
        this.levels = Math.max(0, levels);
    }

    @Override
    public ResourceLocation getSpecialization() {
        if (playerClass != null) {
            return Objects.requireNonNull(SkillRegistry.getClassFromName(playerClass)).getSpecializationSkill().getSkillName();
        }
        return null;
    }

    private ResourceLocation getClassFromSpecialization() {
        if (getSpec() > -1) {
            switch(getSpec()) {
                case 1: return new ResourceLocation("levelup", "artisan");
                case 2: return new ResourceLocation("levelup", "warrior");
                default: return new ResourceLocation("levelup", "miner");
            }
        }
        return null;
    }

    private byte getSpec() {
        if (getSkillLevel(new ResourceLocation("levelup:mining_bonus"), false) > 0) {
            return 0;
        }
        else if (getSkillLevel(new ResourceLocation("levelup:craft_bonus"), false) > 0) {
            return 1;
        }
        else if (getSkillLevel(new ResourceLocation("levelup:combat_bonus"), false) > 0) {
            return 2;
        }
        return -1;
    }

    @Override
    public void resetClass() {
        for (ResourceLocation loc : SkillRegistry.getSkills().keySet()) {
            skillMap.put(loc, 0);
        }
        playerClass = null;
    }

    @Override
    public Map<ResourceLocation, Integer> getSkills() {
        return skillMap;
    }

    @Override
    public void toggleActive() {
        isActive = !isActive;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public void setPlayerClass(ResourceLocation location) {
        IRole cl = SkillRegistry.getClassFromName(location);
        if (this.playerClass != null && cl != null) {
            IRole oldClass = SkillRegistry.getClassFromName(this.playerClass);
            if (oldClass != null && !oldClass.getSkillBonuses().isEmpty()) {
                skillMap.put(oldClass.getSpecializationSkill().getSkillName(), skillMap.get(oldClass.getSpecializationSkill().getSkillName()) - 1);
                for (PlayerSkillStorage sk : oldClass.getSkillBonuses()) {
                    int level = skillMap.get(sk.getSkill().getSkillName());
                    skillMap.put(sk.getSkill().getSkillName(), Math.max(0, level - sk.getLevel()));
                }
            }
        }
        if (cl != null) {
            if (skillMap.get(cl.getSpecializationSkill().getSkillName()) < 1)
                skillMap.put(cl.getSpecializationSkill().getSkillName(), 1);
            if (!cl.getSkillBonuses().isEmpty()) {
                for (PlayerSkillStorage sk : cl.getSkillBonuses()) {
                    if (skillMap.get(sk.getSkill().getSkillName()) < sk.getLevel())
                        skillMap.put(sk.getSkill().getSkillName(), sk.getLevel());
                }
            }
        }
        this.playerClass = location;
    }


}
