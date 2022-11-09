package nova.committee.levelup.api.skill;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Project: levelup
 * Author: cnlimiter
 * Date: 2022/11/10 1:35
 * Description:
 */
public class SkillProperties {
    private ResourceLocation skillName;
    private ResourceLocation skillType;
    private int[] levels;
    private ResourceLocation[] prerequisites;
    private int column;
    private int row;
    private boolean enabled;
    private boolean active;
    private double divisor;
    private ItemStack repStack = ItemStack.EMPTY;

    public static SkillProperties fromJson(JsonObject obj) {
        int[] levels = getLevels(obj);
        ResourceLocation skillName = new ResourceLocation(GsonHelper.getAsString(obj, "name"));
        ResourceLocation[] prerequisites = getPrerequisites(obj);
        int column = GsonHelper.getAsInt(obj, "column", 0);
        int row = GsonHelper.getAsInt(obj, "row", 0);
        boolean enabled = GsonHelper.getAsBoolean(obj, "enabled", true);
        boolean active = GsonHelper.getAsBoolean(obj, "active", true);
        ResourceLocation type = new ResourceLocation(GsonHelper.getAsString(obj, "type"));
        double divisor = GsonHelper.getAsInt(obj, "divisor", 1);
        ItemStack rep = ShapedRecipes.deserializeItem(GsonHelper.getAsJsonObject(obj, "stack"), false);
        return new SkillProperties(skillName, type, levels, prerequisites, column, row, enabled, active, divisor, rep);
    }

    public SkillProperties(ResourceLocation skillName, ResourceLocation skillType, int[] levelCosts, ResourceLocation[] prerequisites, int column, int row, boolean enabled, boolean active, double divisor, ItemStack rep) {
        this.skillName = skillName;
        this.skillType = skillType;
        this.levels = levelCosts;
        this.prerequisites = prerequisites;
        this.column = column;
        this.row = row;
        this.enabled = enabled;
        this.active = active;
        this.repStack = rep;
        this.divisor = divisor;
    }

    public ResourceLocation getName() {
        return skillName;
    }

    public ResourceLocation getType() {
        return skillType;
    }

    public int[] getLevels() {
        return levels;
    }

    public ResourceLocation[] getPrerequisites() {
        return prerequisites;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isActive() {
        return active;
    }

    public double getDivisor() {
        return divisor;
    }

    public ItemStack getRepStack() {
        return repStack;
    }

    public void writeToBytes(FriendlyByteBuf buf) {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", skillName.toString());
        tag.putString("type", skillType.toString());
        if (getLevels() != null && getLevels().length > 0)
            tag.putIntArray("levels", getLevels());
        if (getPrerequisites() != null && getPrerequisites().length > 0) {
            CompoundTag prereqs = new CompoundTag();
            prereqs.putByte("Size", (byte)getPrerequisites().length);
            for (int i = 0; i < getPrerequisites().length; i++) {
                ResourceLocation prereq = getPrerequisites()[i];
                prereqs.putString("prereq_" + i, prereq.toString());
            }
            tag.put("prereqs", prereqs);
        }
        tag.putInt("column", getColumn());
        tag.putInt("row", getRow());
        tag.putBoolean("enabled", isEnabled());
        tag.putBoolean("active", isActive());
        tag.putDouble("divisor", getDivisor());
        if (!repStack.isEmpty()) {
            tag.put("item", repStack.serializeNBT());
        }
        buf.writeNbt(tag);
    }

    public static SkillProperties fromNBT(CompoundTag tag) {
        ResourceLocation name = new ResourceLocation(tag.getString("name"));
        ResourceLocation type = new ResourceLocation(tag.getString("type"));
        int[] levels = tag.contains("levels") ? tag.getIntArray("levels") : new int[0];
        ResourceLocation[] prereqs = new ResourceLocation[0];
        if (tag.contains("prereqs")) {
            CompoundTag prereq = tag.getCompound("prereqs");
            byte size = prereq.getByte("Size");
            prereqs = new ResourceLocation[size];
            for (int i = 0; i < size; i++) {
                prereqs[i] = new ResourceLocation(prereq.getString("prereq_" + i));
            }
        }
        int column = tag.getInt("column");
        int row = tag.getInt("row");
        boolean enabled = tag.getBoolean("enabled");
        boolean active = tag.getBoolean("active");
        double divisor = tag.getDouble("divisor");
        ItemStack stack = ItemStack.EMPTY;
        if (tag.contains("item")) {
            stack = ItemStack.of(tag.getCompound("item"));
        }
        return new SkillProperties(name, type, levels, prereqs, column, row, enabled, active, divisor, stack);
    }

    private static int[] getLevels(JsonObject obj) {
        JsonArray levels = GsonHelper.getAsJsonArray(obj, "levels", null);
        if (levels != null) {
            List<Integer> lvl = new ArrayList<>();
            for (JsonElement element : levels) {
                lvl.add(element.getAsInt());
            }
            int[] lvls = new int[lvl.size()];
            for (int i = 0; i < lvls.length; i++) {
                lvls[i] = lvl.get(i);
            }
            return lvls;
        }
        return new int[0];
    }

    private static ResourceLocation[] getPrerequisites(JsonObject obj) {
        JsonArray prereqs = GsonHelper.getAsJsonArray(obj, "prerequisites", null);
        if (prereqs != null) {
            List<String> prereq = new ArrayList<>();
            for (JsonElement element : prereqs) {
                prereq.add(element.getAsString());
            }
            ResourceLocation[] str = new ResourceLocation[prereq.size()];
            for (int i = 0; i < str.length; i++)
                str[i] = new ResourceLocation(prereq.get(i));
            return str;
        }
        return new ResourceLocation[0];
    }
}
