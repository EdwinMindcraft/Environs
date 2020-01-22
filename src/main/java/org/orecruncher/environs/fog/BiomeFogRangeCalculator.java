/*
 *  Dynamic Surroundings: Environs
 *  Copyright (C) 2020  OreCruncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package org.orecruncher.environs.fog;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.environs.library.BiomeInfo;
import org.orecruncher.environs.library.BiomeUtil;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.math.MathStuff;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.EntityViewRenderEvent;

/**
 * Scans the biome area around the player to determine the fog parameters.
 */
@OnlyIn(Dist.CLIENT)
public class BiomeFogRangeCalculator extends VanillaFogRangeCalculator {

	protected static final int DISTANCE = 20;
	protected static final float DUST_FOG_IMPACT = 0.9F;

	private static class Context {
		public int posX;
		public int posZ;
		public float rain;
		public float lastFarPlane;
		public boolean doScan = true;
		public final FogResult cached = new FogResult();

		public boolean returnCached(final int pX, final int pZ, final float r,
				@Nonnull final EntityViewRenderEvent.RenderFogEvent event) {
			return !this.doScan && pX == this.posX && pZ == this.posZ && r == this.rain
					&& this.lastFarPlane == event.getFarPlaneDistance() && this.cached.isValid(event);
		}
	}

	protected final Context[] context = { new Context(), new Context() };

	public BiomeFogRangeCalculator() {

	}

	@Override
	@Nonnull
	public FogResult calculate(@Nonnull final EntityViewRenderEvent.RenderFogEvent event) {

		final double partialTicks = event.getRenderPartialTicks();
		final PlayerEntity player = GameUtils.getPlayer();
		final int playerX = MathStuff.floor(player.posX);
		final int playerZ = MathStuff.floor(player.posZ);
		final World world = GameUtils.getWorld();
		final float rainStr = world.getRainStrength((float) partialTicks);

		final Context ctx = this.context[event.getFogMode() == -1 ? 0 : 1];

		if (ctx.returnCached(playerX, playerZ, rainStr, event))
			return ctx.cached;

		final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(0, 0, 0);

		float fpDistanceBiomeFog = 0F;
		float weightBiomeFog = 0;

		final boolean isRaining = rainStr > 0;
		ctx.rain = rainStr;
		ctx.doScan = false;

		for (int z = -DISTANCE; z <= DISTANCE; ++z) {
			for (int x = -DISTANCE; x <= DISTANCE; ++x) {
				pos.setPos(playerX + x, 0, playerZ + z);

				// If the chunk is not available doScan will be set true. This will force
				// another scan on the next tick.
				ctx.doScan = ctx.doScan | !world.isBlockPresent(pos);
				final BiomeInfo biome = BiomeUtil.getBiomeData(world.getBiome(pos));

				float distancePart = 1F;
				final float weightPart = 1;

				if (isRaining && biome.getHasDust()) {
					distancePart = 1F - DUST_FOG_IMPACT * rainStr;
				} else if (biome.getHasFog()) {
					distancePart = biome.getFogDensity();
				}

				fpDistanceBiomeFog += distancePart;
				weightBiomeFog += weightPart;
			}
		}

		final float weightMixed = (DISTANCE * 2 + 1) * (DISTANCE * 2 + 1);
		final float weightDefault = weightMixed - weightBiomeFog;

		final float fpDistanceBiomeFogAvg = (weightBiomeFog == 0) ? 0 : fpDistanceBiomeFog / weightBiomeFog;

		final float rangeConst = Math.max(240, event.getFarPlaneDistance() - 16);
		float farPlaneDistance = (fpDistanceBiomeFog * rangeConst + event.getFarPlaneDistance() * weightDefault)
				/ weightMixed;
		final float farPlaneDistanceScaleBiome = (0.1f * (1 - fpDistanceBiomeFogAvg) + 0.75f * fpDistanceBiomeFogAvg);
		final float farPlaneDistanceScale = (farPlaneDistanceScaleBiome * weightBiomeFog + 0.75f * weightDefault)
				/ weightMixed;

		ctx.posX = playerX;
		ctx.posZ = playerZ;
		ctx.lastFarPlane = event.getFarPlaneDistance();
		farPlaneDistance = Math.min(farPlaneDistance, event.getFarPlaneDistance());

		ctx.cached.set(event.getFogMode(), farPlaneDistance, farPlaneDistanceScale);

		return ctx.cached;
	}
}
