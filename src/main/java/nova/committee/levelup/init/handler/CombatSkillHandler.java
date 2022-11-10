package nova.committee.levelup.init.handler;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import nova.committee.levelup.init.registry.SkillRegistry;

/**
 * Project: levelup
 * Author: cnlimiter
 * Date: 2022/11/10 1:39
 * Description:
 */
public class CombatSkillHandler {
    public static final CombatSkillHandler INSTANCE = new CombatSkillHandler();

    private CombatSkillHandler() {}

    private static final ResourceLocation ARROWSPEED = new ResourceLocation("levelup", "arrowspeed");
    private static final ResourceLocation ARROWDRAW = new ResourceLocation("levelup", "bowdraw");
    private static final ResourceLocation NATURALARMOR = new ResourceLocation("levelup", "naturalarmor");
    private static final ResourceLocation SHIELDBLOCK = new ResourceLocation("levelup", "shieldblock");
    private static final ResourceLocation STEALTHDAMAGE = new ResourceLocation("levelup", "stealthdamage");
    private static final ResourceLocation STEALTHSPEED = new ResourceLocation("levelup", "stealthspeed");
    private static final ResourceLocation SWORDCRIT = new ResourceLocation("levelup", "swordcrit");
    private static final ResourceLocation SWORDDAMAGE = new ResourceLocation("levelup", "sworddamage");
    private static final ResourceLocation COMBATBONUS = new ResourceLocation("levelup", "combat_bonus");
    private static final ResourceLocation FALLDAMAGE = new ResourceLocation("levelup", "fallprotect");
    private static final ResourceLocation SPRINTSPEED = new ResourceLocation("levelup", "sprintspeed");

    @SubscribeEvent
    public void onArrowLoose(EntityJoinLevelEvent evt) {
        if (evt.getEntity() instanceof Arrow arrow) {
            if (arrow.shootingEntity instanceof Player && SkillRegistry.getPlayer(((Player)arrow.shootingEntity)).isActive()) {
                int archer = SkillRegistry.getSkillLevel((Player)arrow.shootingEntity, ARROWSPEED);
                if (archer > 0) {
                    double divisor = CapabilityEventHandler.getDivisor(ARROWSPEED);
                    arrow.motionX *= 1.0F + archer / divisor;
                    arrow.motionY *= 1.0F + archer / divisor;
                    arrow.motionZ *= 1.0F + archer / divisor;
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onBowUse(ArrowNockEvent evt) {
        int archery = SkillRegistry.getSkillLevel(evt.getEntity(), ARROWDRAW);
        if (archery > 0 && SkillRegistry.getPlayer(evt.getEntity()).isActive()) {
            evt.getEntity().setItemInHand(evt.getHand(), evt.getBow());
            setItemUseCount(evt.getEntity(), (int)(archery / CapabilityEventHandler.getDivisor(ARROWDRAW)));
            evt.setAction(new InteractionResultHolder<>(InteractionResult.SUCCESS, evt.getBow()));
        }
    }

    private void setItemUseCount(Player player, int archery) {
        player.useItemRemaining -= archery;
    }

    @SubscribeEvent
    public void onDamageTaken(LivingHurtEvent evt) {
        if (evt.getEntity() instanceof Player player && SkillRegistry.getPlayer(player).isActive()) {
            int skill = SkillRegistry.getSkillLevel(player, NATURALARMOR);
            if (skill > 0) {
                if (!evt.getSource().isUnblockable()) {
                    float amount = evt.getAmount() * (float)(1.0F - skill / CapabilityEventHandler.getDivisor(NATURALARMOR));
                    evt.setAmount(amount);
                }
            }
            skill = SkillRegistry.getSkillLevel(player, SHIELDBLOCK);
            if (skill > 0) {
                if (isBlocking(player) && player.getRNG().nextFloat() < skill / CapabilityEventHandler.getDivisor(SHIELDBLOCK)) {
                    evt.setAmount(0F);
                }
            }
            skill = SkillRegistry.getSkillLevel(player, FALLDAMAGE);
            if (skill > 0 && evt.getSource() == DamageSource.FALL) {
                float divisor = (float)(1D / CapabilityEventHandler.getDivisor(FALLDAMAGE));
                float reduction = Math.min(skill * divisor, 0.9F);
                evt.setAmount(evt.getAmount() * (1.0F - reduction));
                return;
            }
        }
        DamageSource src = evt.getSource();
        float dmg = evt.getAmount();
        if (src.getEntity() instanceof Player player && SkillRegistry.getPlayer(player).isActive()) {
            int level = SkillRegistry.getSkillLevel(player, STEALTHDAMAGE);
            if (level > 0) {
                if (src instanceof IndirectEntityDamageSource) {
                    if (StealthLib.getDistanceFrom(evt.getEntityLiving(), player) < 256F && player.isSneaking() && !StealthLib.canSeePlayer(evt.getEntityLiving()) && !StealthLib.entityIsFacing(evt.getEntityLiving(), player)) {
                        dmg *= 1.0F + (0.15F * level);
                        player.sendStatusMessage(new TextComponentTranslation("sneak.attack", 1.0 + (0.15 * level)), true);
                    }
                } else {
                    if (player.isSneaking() && !StealthLib.canSeePlayer(evt.getEntityLiving()) && !StealthLib.entityIsFacing(evt.getEntityLiving(), player)) {
                        dmg *= 1.0F + (0.3F * level);
                        player.sendStatusMessage(new TextComponentTranslation("sneak.attack", 1.0 + (0.3 * level)), true);
                    }
                }
            }
            level = SkillRegistry.getSkillLevel(player, SWORDCRIT);
            if (level > 0) {
                if (!(src instanceof IndirectEntityDamageSource)) {
                    if (!player.getHeldItemMainhand().isEmpty()) {
                        if (player.getRNG().nextDouble() <= level / CapabilityEventHandler.getDivisor(SWORDCRIT))
                            dmg *= 2.0F;
                    }
                }
            }
            level = SkillRegistry.getSkillLevel(player, SWORDDAMAGE);
            if (level > 0 && !(src instanceof IndirectEntityDamageSource)) {
                if (!player.getMainHandItem().isEmpty()) {
                    dmg *= 1.0F + level / CapabilityEventHandler.getDivisor(SWORDDAMAGE);
                    if (LevelUpConfig.damageScaling && !(evt.getEntityLiving() instanceof Player) && evt.getEntityLiving().getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getAttributeValue() > 20) {
                        double health = evt.getEntityLiving().getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getAttributeValue();
                        float skillOutput = level / 40F;
                        dmg += Math.min(health * skillOutput, health * 0.375F);
                    }
                }
            } else if (SkillRegistry.getSkillLevel(player, ARROWSPEED) > 0 && src.getDamageType().equals("arrow")) {
                level = SkillRegistry.getSkillLevel(player, ARROWSPEED);
                dmg *= 1.0F + level / (float)(2F * CapabilityEventHandler.getDivisor(ARROWSPEED));
                if (LevelUpConfig.damageScaling && !(evt.getEntityLiving() instanceof Player) && evt.getEntityLiving().getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getAttributeValue() > 20) {
                    double health = evt.getEntityLiving().getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getAttributeValue();
                    float skillOutput = level / 40F;
                    dmg += Math.min(health * skillOutput, health * 0.375F);
                }
            }

            if (dmg != evt.getAmount()) {
                evt.setAmount(dmg);
            }
        }
    }

    private boolean isBlocking(Player player) {
        return player.isHandActive() && !player.getActiveItemStack().isEmpty() && player.getActiveItemStack().getItem().getItemUseAction(player.getActiveItemStack()) == EnumAction.BLOCK;
    }

    @SubscribeEvent
    public void onTargetSet(LivingChangeTargetEvent evt) {
        if (evt.getTarget() instanceof Player && evt.getEntityLiving() instanceof EntityMob) {
            if (evt.getTarget().isSneaking() && !StealthLib.entityHasVisionOf(evt.getEntityLiving(), (Player)evt.getTarget())
                    && evt.getEntityLiving().getRevengeTimer() != ((EntityMob) evt.getEntityLiving()).ticksExisted) {
                ((EntityMob) evt.getEntityLiving()).setAttackTarget(null);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerSneak(TickEvent.PlayerTickEvent evt) {
        if (evt.phase == TickEvent.Phase.START && SkillRegistry.getPlayer(evt.player).isActive()) {
            int skill = SkillRegistry.getSkillLevel(evt.player, STEALTHSPEED);
            if (skill > 0) {
                IAttributeInstance attrib = evt.player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
                AttributeModifier mod = new AttributeModifier(Library.sneakID, "SneakingSkillSpeed", skill / CapabilityEventHandler.getDivisor(STEALTHSPEED), 2);
                if (evt.player.isSneaking()) {
                    if (attrib.getModifier(Library.sneakID) == null)
                        attrib.applyModifier(mod);
                }
                else if (attrib.getModifier(Library.sneakID) != null)
                    attrib.removeModifier(mod);
            }
            else if (SkillRegistry.getSkillLevel(evt.player, SPRINTSPEED) > 0) {
                IAttributeInstance attrib = evt.player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
                AttributeModifier mod = new AttributeModifier(Library.speedID, "SprintingSkillSpeed", SkillRegistry.getSkillLevel(evt.player, SPRINTSPEED) / (float)CapabilityEventHandler.getDivisor(SPRINTSPEED), 2);
                if (evt.player.isSprinting()) {
                    if (attrib.getModifier(Library.speedID) == null)
                        attrib.applyModifier(mod);
                }
                else if (attrib.getModifier(Library.speedID) != null)
                    attrib.removeModifier(mod);
            }
        }
    }

    @SubscribeEvent
    public void getCombatBonus(LivingDeathEvent evt) {
        if (evt.getEntityLiving() instanceof EntityMob && evt.getSource().getTrueSource() instanceof Player) {
            if (SkillRegistry.getSkillLevel((Player)evt.getSource().getTrueSource(), COMBATBONUS) > 0) {
                int deathXP = (int) evt.getEntityLiving().getMaxHealth();
                SkillRegistry.addExperience((Player) evt.getSource().getTrueSource(), deathXP / (int)CapabilityEventHandler.getDivisor(COMBATBONUS));
            }
        }
    }
}
