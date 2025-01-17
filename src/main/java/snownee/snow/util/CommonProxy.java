package snownee.snow.util;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.commands.DebugMobSpawningCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.material.FluidState;
import snownee.kiwi.Mod;
import snownee.kiwi.loader.Platform;
import snownee.snow.GameEvents;
import snownee.snow.SnowCommonConfig;
import snownee.snow.SnowRealMagic;
import snownee.snow.compat.sereneseasons.SereneSeasonsCompat;

@Mod(SnowRealMagic.ID)
public class CommonProxy implements ModInitializer {
	public static boolean terraforged;
	public static boolean fabricSeasons = Platform.isModLoaded("seasons");
	public static boolean sereneSeasons = Platform.isModLoaded("sereneseasons");

	public static boolean isHot(FluidState fluidState, Level level, BlockPos pos) {
		return fluidState.getType().getPickupSound().orElse(null) == SoundEvents.BUCKET_FILL_LAVA || fluidState.is(FluidTags.LAVA);
	}

	public static void weatherTick(ServerLevel level, Runnable action) {
		if (sereneSeasons) {
			SereneSeasonsCompat.weatherTick(level, action);
			return;
		}
		if (level.random.nextInt(SnowCommonConfig.weatherTickSlowness) == 0) {
			action.run();
		}
	}

	public static boolean snowAccumulationNow(Level level) {
		if (!level.isRaining()) {
			return false;
		}
		if (SnowCommonConfig.snowAccumulationDuringSnowfall) {
			return true;
		}
		// there is no thundering in winter in Serene Seasons
		if (SnowCommonConfig.snowAccumulationDuringSnowstorm && (level.isThundering() || sereneSeasons)) {
			return true;
		}
		return false;
	}

	public static boolean shouldMelt(Level level, BlockPos pos) {
		return shouldMelt(level, pos, level.getBiome(pos), 1);
	}

	public static boolean shouldMelt(Level level, BlockPos pos, Holder<Biome> biome, int layers) {
		if (SnowCommonConfig.snowNeverMelt) {
			return false;
		}
		if (sereneSeasons) {
			return SereneSeasonsCompat.shouldMelt(level, pos, biome);
		}
		if (snowAndIceMeltInWarmBiomes(level.dimension(), biome) && biome.value().warmEnoughToRain(pos) && skyLightEnoughToMelt(
				level,
				pos,
				layers)) {
			return true;
		}
		if (layers <= 1) {
			if (SnowCommonConfig.snowAccumulationMaxLayers < 9) {
				return false;
			}
			if (!(level.getBlockState(pos.below()).getBlock() instanceof SnowLayerBlock)) {
				return false;
			}
		}
		return SnowCommonConfig.snowNaturalMelt && skyLightEnoughToMelt(level, pos, layers);
	}

	public static boolean snowAndIceMeltInWarmBiomes(ResourceKey<Level> dimension, Holder<Biome> biome) {
		if (SnowCommonConfig.snowAndIceMeltInWarmBiomes) {
			return true;
		}
		if (sereneSeasons) {
			return SereneSeasonsCompat.snowAndIceMeltInWarmBiomes(dimension, biome);
		}
		return fabricSeasons;
	}

	public static boolean skyLightEnoughToMelt(Level level, BlockPos pos, int layers) {
		return level.getBrightness(LightLayer.SKY, layers == 8 ? pos.above() : pos) > 2;
	}

	public static boolean coldEnoughToSnow(Level level, BlockPos pos, Holder<Biome> biome) {
		if (sereneSeasons) {
			return SereneSeasonsCompat.coldEnoughToSnow(level, pos, biome);
		}
		return biome.value().coldEnoughToSnow(pos);
	}

	public static boolean isWinter(Level level, BlockPos pos, Holder<Biome> biome) {
		if (sereneSeasons) {
			return SereneSeasonsCompat.isWinter(level, pos, biome);
		}
		return false;
	}

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			if (SnowCommonConfig.debugSpawningCommand) {
				DebugMobSpawningCommand.register(dispatcher);
			}
		});
		UseBlockCallback.EVENT.register(GameEvents::onItemUse);
		PlayerBlockBreakEvents.BEFORE.register(GameEvents::onDestroyedByPlayer);
		if (sereneSeasons) {
			SnowRealMagic.LOGGER.info("SereneSeasons detected. Overriding weather behavior.");
		}
	}
}
