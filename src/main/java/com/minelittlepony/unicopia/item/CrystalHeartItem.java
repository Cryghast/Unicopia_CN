package com.minelittlepony.unicopia.item;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.entity.mob.FloatingArtefactEntity;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.entity.mob.FloatingArtefactEntity.State;
import com.minelittlepony.unicopia.particle.FollowingParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.block.EndRodBlock;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class CrystalHeartItem extends Item implements FloatingArtefactEntity.Artifact {
    static final Predicate<Entity> TARGET_PREDICATE = EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR
            .and(EntityPredicates.VALID_ENTITY)
            .and(e -> (e instanceof PlayerEntity || e instanceof MobEntity));
    private static final Supplier<Map<Item, Item>> ITEM_MAP = Suppliers.memoize(() -> {
        return Map.of(
                Items.BUCKET, UItems.LOVE_BUCKET,
                Items.GLASS_BOTTLE, UItems.LOVE_BOTTLE,
                UItems.MUG, UItems.LOVE_MUG
        );
    });

    private static boolean isFillable(ItemStack stack) {
        return ITEM_MAP.get().containsKey(stack.getItem());
    }

    private static ItemStack fill(ItemStack stack) {
        Item item = ITEM_MAP.get().getOrDefault(stack.getItem(), stack.getItem());
        return item == stack.getItem() ? stack : stack.withItem(item);
    }

    public CrystalHeartItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {

        World world = context.getWorld();
        BlockPos blockPos = new ItemPlacementContext(context).getBlockPos();

        Box placementArea = UEntities.FLOATING_ARTEFACT.getDimensions().getBoxAt(Vec3d.ofBottomCenter(blockPos));

        if (!world.isSpaceEmpty(null, placementArea)) {
            return ActionResult.FAIL;
        }

        if (world instanceof ServerWorld serverWorld) {
            Consumer<FloatingArtefactEntity> consumer = EntityType.copier(serverWorld, context.getStack(), context.getPlayer());
            FloatingArtefactEntity entity = UEntities.FLOATING_ARTEFACT.create(serverWorld, consumer, blockPos, SpawnReason.SPAWN_EGG, false, true);

            if (entity == null) {
                return ActionResult.FAIL;
            }

            entity.setStack(context.getStack().split(1));
            serverWorld.spawnEntity(entity);

            if (findStructure(entity)) {
                entity.playSound(USounds.ENTITY_CRYSTAL_HEART_ACTIVATE, 0.75F, 0.8F);
                UCriteria.POWER_UP_HEART.trigger(context.getPlayer());
            }
        } else {
            context.getStack().decrement(1);
        }

        return ActionResult.success(world.isClient);
    }

    @Override
    public void onArtifactTick(FloatingArtefactEntity entity) {

        if (entity.getState() == State.INITIALISING) {
            if (entity.age % 30 == 0 && findStructure(entity)) {
                entity.setState(State.RUNNING);
                entity.setRotationSpeed(4, 30);
            }
        } else {
            if (entity.age % 30 == 0 && !findStructure(entity)) {
                entity.setState(State.INITIALISING);
            }

            BlockPos pos = entity.getBlockPos();
            entity.getWorld().addParticle(ParticleTypes.COMPOSTER,
                    pos.getX() + entity.getWorld().getRandom().nextFloat(),
                    pos.getY() + entity.getWorld().getRandom().nextFloat(),
                    pos.getZ() + entity.getWorld().getRandom().nextFloat(),
                    0, 0, 0);

            if (entity.age % 80 == 0 && !entity.getWorld().isClient) {
                List<LivingEntity> inputs = new ArrayList<>();
                List<LivingEntity> outputs = new ArrayList<>();
                List<ItemEntity> containers = new ArrayList<>();

                VecHelper.findInRange(entity, entity.getWorld(), entity.getPos(), 20, TARGET_PREDICATE).forEach(e -> {
                    LivingEntity living = (LivingEntity)e;

                    if (e instanceof PlayerEntity
                            || (e instanceof TameableEntity t && t.isTamed())
                            || (e instanceof Saddleable s && s.isSaddled())
                            || (e instanceof MerchantEntity)) {
                        if (living.getHealth() < living.getMaxHealth()) {
                            outputs.add(living);
                        }
                    } else if (e.getType().getSpawnGroup() == SpawnGroup.MONSTER) {
                        inputs.add(living);
                    }
                });
                VecHelper.findInRange(entity, entity.getWorld(), entity.getPos(), 20, i -> {
                    return i instanceof ItemEntity ie && isFillable(ie.getStack()) && EquinePredicates.CHANGELING.test(i);
                }).forEach(i -> containers.add((ItemEntity)i));

                int demand = outputs.size() + containers.stream().mapToInt(i -> i.getStack().getCount()).sum();
                int supply = inputs.size();

                if (demand == 0 || supply == 0) {
                    return;
                }

                float gives = supply > demand ? supply / demand : 1;
                float takes = demand > supply ? demand / supply : 1;

                inputs.forEach(input -> {
                    input.damage(entity.damageOf(UDamageTypes.LIFE_DRAINING), takes);
                    ParticleUtils.spawnParticles(new FollowingParticleEffect(UParticles.HEALTH_DRAIN, entity, 0.2F), input, 1);
                });
                outputs.forEach(output -> {
                    ParticleUtils.spawnParticles(new FollowingParticleEffect(UParticles.HEALTH_DRAIN, output, 0.2F), entity, 1);
                    output.heal(gives);
                });
                containers.forEach(container -> {
                    ParticleUtils.spawnParticles(new FollowingParticleEffect(UParticles.HEALTH_DRAIN, container, 0.2F), entity, 1);
                    container.setStack(fill(container.getStack()));
                });

                if (gives > 0) {
                    entity.setRotationSpeed(37, 80);
                }
            }
        }

    }

    @Override
    public ActionResult onArtifactDestroyed(FloatingArtefactEntity entity) {
        entity.playSound(USounds.ENTITY_CRYSTAL_HEART_DEACTIVATE, 0.75F, 1);
        entity.dropStack(new ItemStack(UItems.CRYSTAL_SHARD, 1 + entity.getWorld().random.nextInt(5)), 0);
        return ActionResult.SUCCESS;
    }

    private boolean findStructure(FloatingArtefactEntity entity) {
        return findPyramid(entity, Direction.UP) && findPyramid(entity, Direction.DOWN);
    }

    private boolean findPyramid(FloatingArtefactEntity entity, Direction direction) {

        BlockPos tip = entity.getBlockPos().offset(direction);
        BlockState tipState = entity.getWorld().getBlockState(tip);
        if (!tipState.isIn(UTags.Blocks.CRYSTAL_HEART_ORNAMENT) || (!tipState.contains(EndRodBlock.FACING)|| tipState.get(EndRodBlock.FACING) != direction.getOpposite())) {
            return false;
        }

        tip = tip.offset(direction);
        if (!isDiamond(entity.getWorld().getBlockState(tip))) {
            return false;
        }
        tip = tip.offset(direction);

        final BlockPos center = tip;

        return BlockPos.streamOutwards(center, 1, 0, 1)
                .filter(p -> p.getX() == center.getX() || p.getZ() == center.getZ())
                .map(entity.getWorld()::getBlockState)
                .allMatch(this::isDiamond);
    }

    private boolean isDiamond(BlockState state) {
        return state.isIn(UTags.Blocks.CRYSTAL_HEART_BASE);
    }
}
