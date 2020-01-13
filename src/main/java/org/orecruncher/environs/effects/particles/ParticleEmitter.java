/*
 * This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.orecruncher.environs.effects.particles;

import java.util.Random;

import javax.annotation.Nonnull;

import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.random.XorShiftRandom;

import net.minecraft.client.particle.Particle;
import net.minecraft.util.math.BlockPos;

@OnlyIn(Dist.CLIENT)
public abstract class ParticleEmitter implements ITickable {

	protected static final Random RANDOM = XorShiftRandom.current();

	protected final IWorldReader world;
	protected final double posX;
	protected final double posY;
	protected final double posZ;
	protected final BlockPos position;
	private boolean isAlive = true;

	protected ParticleEmitter(final IWorldReader worldIn, final double posXIn, final double posYIn, final double posZIn) {
		this.world = worldIn;
		this.posX = posXIn;
		this.posY = posYIn;
		this.posZ = posZIn;
		this.position = new BlockPos(posXIn, posYIn, posZIn);
	}

	@Nonnull
	public BlockPos getPos() {
		return this.position;
	}

	/*
	 * Adds a particle to the internal tracking list as well as adds it to the
	 * Minecraft particle manager.
	 */
	public void addParticle(@Nonnull final Particle particle) {
		GameUtils.getMC().particles.addEffect(particle);
	}

	public boolean isAlive() {
		return this.isAlive;
	}

	public void setExpired() {
		this.isAlive = false;
		cleanUp();
	}

	/*
	 * By default a system will stay alive indefinitely until the
	 * ParticleSystemManager kills it. Override to provide termination capability.
	 */
	public boolean shouldDie() {
		return false;
	}

	/*
	 * Perform any cleanup activities prior to dying.
	 */
	protected void cleanUp() {

	}

	/*
	 * Update the state of the particle system. Any particles are queued into the
	 * Minecraft particle system or to a ParticleCollection so they do not have to
	 * be ticked.
	 */
	@Override
	public void tick() {
		if (shouldDie()) {
			setExpired();
			return;
		}

		// Let the system mull over what it wants to do
		think();

		if (isAlive())
			// Update any sounds
			soundUpdate();
	}

	/*
	 * Override to provide sound for the particle effect. Will be invoked whenever
	 * the particle system is updated by the particle manager.
	 */
	protected void soundUpdate() {

	}

	/*
	 * Override to provide some sort of intelligence to the system. The logic can do
	 * things like add new particles, remove old ones, update positions, etc. Will
	 * be invoked during the systems onUpdate() call.
	 */
	public abstract void think();

}
