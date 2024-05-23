package snownee.snow;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.IntegerValue;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiGO;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.KiwiModule.Name;
import snownee.kiwi.KiwiModule.NoItem;
import snownee.kiwi.KiwiModule.RenderLayer;
import snownee.kiwi.RenderLayerEnum;
import snownee.kiwi.loader.event.InitEvent;
import snownee.kiwi.util.KiwiEntityTypeBuilder;
import snownee.snow.block.ExtraCollisionSnowLayerBlock;
import snownee.snow.block.SRMSnowLayerBlock;
import snownee.snow.block.SnowFenceBlock;
import snownee.snow.block.SnowFenceGateBlock;
import snownee.snow.block.SnowSlabBlock;
import snownee.snow.block.SnowStairsBlock;
import snownee.snow.block.SnowWallBlock;
import snownee.snow.block.entity.SnowBlockEntity;
import snownee.snow.block.entity.SnowCoveredBlockEntity;
import snownee.snow.entity.FallingSnowEntity;
import snownee.snow.loot.NormalizeLoot;
import snownee.snow.mixin.BlockAccess;

@KiwiModule
public class CoreModule extends AbstractModule {
	public static final TagKey<Block> SNOW = blockTag(SnowRealMagic.MODID, "snow");

	public static final TagKey<Block> SNOWY_SETTING = blockTag(SnowRealMagic.MODID, "snowy_setting");

	public static final TagKey<Block> CONTAINABLES = blockTag(SnowRealMagic.MODID, "containables");

	public static final TagKey<Block> PLANTS = blockTag(SnowRealMagic.MODID, "plants");

	public static final TagKey<Block> NOT_CONTAINABLES = blockTag(SnowRealMagic.MODID, "not_containables");

	public static final TagKey<Block> ENTITY_INSIDE = blockTag(SnowRealMagic.MODID, "entity_inside");

	public static final TagKey<Block> OFFSET_Y = blockTag(SnowRealMagic.MODID, "offset_y");

	public static final TagKey<Block> CANNOT_ACCUMULATE_ON = blockTag(SnowRealMagic.MODID, "cannot_accumulate_on");

	@NoItem
	@Name("snow")
	public static final KiwiGO<SRMSnowLayerBlock> SNOW_BLOCK = go(() -> new ExtraCollisionSnowLayerBlock(blockProp(Blocks.SNOW).dynamicShape()));

	@NoItem
	@Name("snow_no_collision")
	public static final KiwiGO<SRMSnowLayerBlock> SNOW_NO_COLLISION_BLOCK = go(() -> new SRMSnowLayerBlock(blockProp(Blocks.SNOW).dynamicShape()));

	@NoItem
	@Name("snow_plant")
	public static final KiwiGO<SRMSnowLayerBlock> SNOW_PLANT_BLOCK = go(() -> new SRMSnowLayerBlock(blockProp(Blocks.SNOW).dynamicShape()));

	@NoItem
	@Name("snow_doubleplant_lower")
	public static final KiwiGO<SRMSnowLayerBlock> SNOW_DOUBLEPLANT_LOWER_BLOCK = go(() -> new SRMSnowLayerBlock(blockProp(Blocks.SNOW).dynamicShape()));

	@NoItem
	@Name("snow_doubleplant_upper")
	public static final KiwiGO<SRMSnowLayerBlock> SNOW_DOUBLEPLANT_UPPER_BLOCK = go(() -> new SRMSnowLayerBlock(blockProp(Blocks.SNOW).dynamicShape()));

	@NoItem
	@RenderLayer(RenderLayerEnum.CUTOUT)
	public static final KiwiGO<Block> FENCE = go(() -> new SnowFenceBlock(blockProp(Blocks.OAK_FENCE).mapColor(MapColor.SNOW)
			.randomTicks()
			.dynamicShape()));

	@NoItem
	@RenderLayer(RenderLayerEnum.CUTOUT)
	public static final KiwiGO<Block> FENCE2 = go(() -> new SnowFenceBlock(blockProp(Blocks.NETHER_BRICK_FENCE).mapColor(MapColor.SNOW)
			.randomTicks()
			.dynamicShape()));

	@NoItem
	@RenderLayer(RenderLayerEnum.CUTOUT)
	public static final KiwiGO<Block> STAIRS = go(() -> new SnowStairsBlock(blockProp(Blocks.OAK_STAIRS).mapColor(MapColor.SNOW)
			.randomTicks()));

	@NoItem
	@RenderLayer(RenderLayerEnum.CUTOUT)
	public static final KiwiGO<Block> SLAB = go(() -> new SnowSlabBlock(blockProp(Blocks.OAK_SLAB).mapColor(MapColor.SNOW).randomTicks()));

	@NoItem
	@RenderLayer(RenderLayerEnum.CUTOUT)
	public static final KiwiGO<Block> FENCE_GATE = go(() -> new SnowFenceGateBlock(blockProp(Blocks.OAK_FENCE_GATE).mapColor(MapColor.SNOW)
			.randomTicks()
			.dynamicShape()));

	@NoItem
	@RenderLayer(RenderLayerEnum.CUTOUT)
	public static final KiwiGO<Block> WALL = go(() -> new SnowWallBlock(blockProp(Blocks.COBBLESTONE_WALL).mapColor(MapColor.SNOW)
			.randomTicks()
			.dynamicShape()));

	@Name("snow")
	public static final KiwiGO<BlockEntityType<SnowBlockEntity>> TILE = blockEntity(SnowBlockEntity::new, null, SNOW_BLOCK);

	public static final KiwiGO<BlockEntityType<SnowCoveredBlockEntity>> TEXTURE_TILE = blockEntity(
			SnowCoveredBlockEntity::new,
			null,
			FENCE,
			FENCE2,
			STAIRS,
			SLAB,
			FENCE_GATE,
			WALL);

	@Name("snow")
	public static final KiwiGO<EntityType<FallingSnowEntity>> ENTITY = go(() -> KiwiEntityTypeBuilder.<FallingSnowEntity>create()
			.entityFactory((spawnEntity, world) -> new FallingSnowEntity(world))
			.dimensions(EntityDimensions.fixed(0.98F, 0.001F))
			.trackRangeChunks(10)
			.trackedUpdateRate(20)
			.build());

	public static final KiwiGO<LootPoolEntryType> NORMALIZE = go(() -> new LootPoolEntryType(NormalizeLoot.CODEC));

	public static final GameRules.Key<IntegerValue> BLIZZARD_STRENGTH = GameRuleRegistry.register(
			SnowRealMagic.MODID + ":blizzardStrength",
			GameRules.Category.MISC,
			IntegerValue.create(0));

	public static final GameRules.Key<IntegerValue> BLIZZARD_FREQUENCY = GameRuleRegistry.register(
			SnowRealMagic.MODID + ":blizzardFrequency",
			GameRules.Category.MISC,
			IntegerValue.create(10000));

	public CoreModule() {
		decorators.remove(BuiltInRegistries.BLOCK);
	}

	@Override
	protected void init(InitEvent event) {
		event.enqueueWork(() -> {
			Item.BY_BLOCK.put(CoreModule.SNOW_BLOCK.get(), Items.SNOW);
			BlockBehaviour.StateArgumentPredicate<EntityType<?>> predicate = (blockState, blockGetter, blockPos, entityType) -> {
				final var below = blockPos.below();
				return blockState.getValue(BlockStateProperties.LAYERS) <= SnowCommonConfig.mobSpawningMaxLayers &&
						blockGetter.getBlockState(below).isValidSpawn(blockGetter, below, entityType);
			};
			((BlockAccess) Blocks.SNOW).getProperties().isValidSpawn(predicate);
			((BlockAccess) SNOW_BLOCK.get()).getProperties().isValidSpawn(predicate);
		});
	}

}
