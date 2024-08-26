package dev.latvian.mods.kubejs.block.custom;

import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.block.BlockBuilder;
import dev.latvian.mods.kubejs.block.BlockRenderType;
import dev.latvian.mods.kubejs.block.RandomTickCallbackJS;
import dev.latvian.mods.kubejs.block.SeedItemBuilder;
import dev.latvian.mods.kubejs.client.VariantBlockStateGenerator;
import dev.latvian.mods.kubejs.generator.KubeAssetGenerator;
import dev.latvian.mods.kubejs.level.BlockContainerJS;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.util.ReturnsSelf;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

@ReturnsSelf
public class CropBlockBuilder extends BlockBuilder {
	public static final ResourceLocation[] CROP_BLOCK_TAGS = {
		BlockTags.CROPS.location(),
	};

	public static final ResourceLocation[] CROP_ITEM_TAGS = {
		Tags.Items.SEEDS.location(),
	};

	@FunctionalInterface
	public interface SurviveCallback {
		boolean survive(BlockState state, LevelReader reader, BlockPos pos);
	}

	public static class ShapeBuilder {
		private final List<VoxelShape> shapes;

		public ShapeBuilder(int age) {
			this.shapes = new ArrayList<>(Collections.nCopies(age + 1, Block.box(0.0d, 0.0d, 0.0d, 16.0d, 16.0d, 16.0d)));
		}

		@Info("""
			Describe the shape of the crop at a specific age.
						
			min/max coordinates are double values between 0 and 16.
			""")
		public ShapeBuilder shape(int age, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
			shapes.set(age, Block.box(minX, minY, minZ, maxX, maxY, maxZ));
			return this;
		}

		@Info("Makes the block to have a box like wheat for each stage.")
		public ShapeBuilder wheat() {
			shapes.clear();
			shapes.addAll(List.of(new VoxelShape[]{
				Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
				Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
				Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0),
				Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0),
				Block.box(0.0, 0.0, 0.0, 16.0, 10.0, 16.0),
				Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0),
				Block.box(0.0, 0.0, 0.0, 16.0, 14.0, 16.0),
				Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)
			}));
			return this;
		}

		@Info("Makes the block to have a box like carrot for each stage.")
		public ShapeBuilder carrot() {
			shapes.clear();
			shapes.addAll(List.of(new VoxelShape[]{
				Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
				Block.box(0.0, 0.0, 0.0, 16.0, 3.0, 16.0),
				Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
				Block.box(0.0, 0.0, 0.0, 16.0, 5.0, 16.0),
				Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0),
				Block.box(0.0, 0.0, 0.0, 16.0, 7.0, 16.0),
				Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0),
				Block.box(0.0, 0.0, 0.0, 16.0, 9.0, 16.0)
			}));
			return this;
		}

		@Info("Makes the block to have a box like beetroot for each stage.")
		public ShapeBuilder beetroot() {
			shapes.clear();
			shapes.addAll(List.of(new VoxelShape[]{
				Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
				Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
				Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0),
				Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0)
			}));
			return this;
		}

		@Info("Makes the block to have a box like potato for each stage.")
		public ShapeBuilder potato() {
			return carrot();
		}

		public List<VoxelShape> getShapes() {
			return List.copyOf(shapes);
		}
	}

	public transient int age;
	protected transient List<VoxelShape> shapeByAge;
	public transient boolean dropSeed;
	public transient ToDoubleFunction<RandomTickCallbackJS> growSpeedCallback;
	public transient ToIntFunction<RandomTickCallbackJS> fertilizerCallback;
	public transient SurviveCallback surviveCallback;

	public transient List<Pair<Item, NumberProvider>> outputs;

	public CropBlockBuilder(ResourceLocation i) {
		super(i);
		age = 7;
		shapeByAge = Collections.nCopies(8, Block.box(0.0d, 0.0d, 0.0d, 16.0d, 16.0d, 16.0d));
		growSpeedCallback = null;
		fertilizerCallback = null;
		surviveCallback = null;
		renderType = BlockRenderType.CUTOUT;
		noCollision = true;
		itemBuilder = new SeedItemBuilder(newID("", "_seed"));
		((SeedItemBuilder) itemBuilder).blockBuilder = this;
		hardness = 0.0f;
		resistance = 0.0f;
		dropSeed = true;
		outputs = new ArrayList<>();
		notSolid = true;

		soundType(SoundType.CROP);
		mapColor(MapColor.PLANT);

		for (int a = 0; a <= age; a++) {
			texture(String.valueOf(a), id.getNamespace() + ":block/" + id.getPath() + a);
		}

		tagBlock(CROP_BLOCK_TAGS);
		tagItem(CROP_ITEM_TAGS);
	}

	@Info("Add a crop output with exactly one output.")
	public CropBlockBuilder crop(Item output) {
		crop(output, ConstantValue.exactly(1.0f));
		return this;
	}

	@Info("Add a crop output with a specific amount.")
	public CropBlockBuilder crop(Item output, NumberProvider chance) {
		outputs.add(new Pair<>(output, chance));
		return this;
	}

	@Info("Set the age of the crop. Note that the box will be the same for all ages (A full block size).")
	public CropBlockBuilder age(int age) {
		age(age, (builder) -> {
		});
		return this;
	}

	@Info("Set if the crop should drop seeds when harvested.")
	public CropBlockBuilder dropSeed(boolean dropSeed) {
		this.dropSeed = dropSeed;
		return this;
	}

	@Info("Set the age of the crop and the shape of the crop at that age.")
	public CropBlockBuilder age(int age, Consumer<ShapeBuilder> builder) {
		this.age = age;
		ShapeBuilder shapes = new ShapeBuilder(age);
		builder.accept(shapes);
		this.shapeByAge = shapes.getShapes();
		for (int i = 0; i <= age; i++) {
			texture(String.valueOf(i), id.getNamespace() + ":block/" + id.getPath() + i);
		}
		return this;
	}

	@Override
	public BlockBuilder texture(String id, String tex) {
		try {
			int intId = (int) Double.parseDouble(id);
			return super.texture(String.valueOf(intId), tex);
		} catch (NumberFormatException e) {
			return super.texture(id, tex);
		}
	}

	public CropBlockBuilder farmersCanPlant() {
		this.tagItem(new ResourceLocation[]{ResourceLocation.fromNamespaceAndPath("minecraft", "villager_plantable_seeds")});
		return this;
	}

	public CropBlockBuilder bonemeal(ToIntFunction<RandomTickCallbackJS> bonemealCallback) {
		this.fertilizerCallback = bonemealCallback;
		return this;
	}

	public CropBlockBuilder survive(SurviveCallback surviveCallback) {
		this.surviveCallback = surviveCallback;
		return this;
	}

	public CropBlockBuilder growTick(ToDoubleFunction<RandomTickCallbackJS> growSpeedCallback) {
		this.growSpeedCallback = growSpeedCallback;
		return this;
	}

	@Override
	public BlockBuilder randomTick(@Nullable Consumer<RandomTickCallbackJS> randomTickCallback) {
		KubeJS.LOGGER.warn("randomTick is overridden by growTick to return grow speed, use it instead.");
		return this;
	}

	// FIXME
	// TODO: Get lookup for enchantments here to apply fortune bonus
	@Override
	@Nullable
	public LootTable generateLootTable() {
		LootItemCondition.Builder mature = LootItemBlockStatePropertyCondition.hasBlockStateProperties(this.get())
			.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CropBlock.AGE, age));

		var builder = LootTable.lootTable();
		for (Pair<Item, NumberProvider> output : outputs) {
			var cropItem = LootItem.lootTableItem(output.getFirst())
				.apply(SetItemCountFunction.setCount(output.getSecond()))
				.when(mature);
			builder.withPool(LootPool.lootPool().add(cropItem));
		}

		if (dropSeed) {
			var pool = LootPool.lootPool()
				.add(
					LootItem.lootTableItem(itemBuilder.get())
						.when(mature)
						.otherwise(LootItem.lootTableItem(itemBuilder.get()))
				);
			builder.withPool(pool);
		}

		return builder.build();
	}


	@Override
	protected void generateBlockStateJson(VariantBlockStateGenerator bs) {
		for (int i = 0; i <= age; i++) {
			bs.simpleVariant("age=%s".formatted(i), model.isEmpty() ? (id.getNamespace() + ":block/" + id.getPath() + i) : model);
		}
	}

	@Override
	protected void generateBlockModelJsons(KubeAssetGenerator generator) {
		for (int i = 0; i <= age; i++) {
			final int fi = i;
			generator.blockModel(newID("", String.valueOf(i)), m -> {
				m.parent("minecraft:block/crop");
				m.texture("crop", textures.get(String.valueOf(fi)).getAsString());
			});
		}

	}

	@Override
	public Block createObject() {
		IntegerProperty ageProperty = IntegerProperty.create("age", 0, age);
		return new BasicCropBlockJS(this) {
			@Override
			public IntegerProperty getAgeProperty() {
				/*
				 * Overriding getAgeProperty here because Minecraft calls getAgeProperty
				 * when CropBlock.class initializes. This happens when nothing is registered
				 * or assigned yet.
				 */
				return ageProperty;
			}

			@Override
			protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
				builder.add(ageProperty);
			}

			@Override
			public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource random) {
				double f = growSpeedCallback == null ? -1 : growSpeedCallback.applyAsDouble(new RandomTickCallbackJS(new BlockContainerJS(serverLevel, blockPos), random));
				int age = this.getAge(blockState);
				if (age < this.getMaxAge()) {
					if (f < 0) {
						f = getGrowthSpeed(blockState, serverLevel, blockPos);
					}
					if (f > 0 && random.nextInt((int) (25.0F / f) + 1) == 0) {
						serverLevel.setBlock(blockPos, this.getStateForAge(age + 1), 2);
					}
				}
			}

			@Override
			public void growCrops(Level level, BlockPos blockPos, BlockState blockState) {
				if (fertilizerCallback == null) {
					super.growCrops(level, blockPos, blockState);
				} else {
					int effect = fertilizerCallback.applyAsInt(new RandomTickCallbackJS(new BlockContainerJS(level, blockPos), level.random));
					if (effect > 0) {
						level.setBlock(blockPos, this.getStateForAge(Integer.min(getAge(blockState) + effect, getMaxAge())), 2);
					}
				}
			}

			@Override
			public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
				return surviveCallback != null ? surviveCallback.survive(blockState, levelReader, blockPos) : super.canSurvive(blockState, levelReader, blockPos);
			}
		};
	}
}
