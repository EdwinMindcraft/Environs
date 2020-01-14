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

package org.orecruncher.environs.effects.emitters;

import net.minecraft.world.IWorldReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/*
 * Base for particle entities that are long lived and generate
 * other particles as a jet.  This entity does not render - just
 * serves as a particle factory.
 */
@OnlyIn(Dist.CLIENT)
public abstract class Jet extends ParticleEmitter {

	protected final int jetStrength;
	protected final int updateFrequency;

	protected final int particleMaxAge;
	protected int particleAge;

	public Jet(final int strength, final IWorldReader world, final double x, final double y, final double z) {
		this(0, strength, world, x, y, z, 3);
	}

	public Jet(final int layer, final int strength, final IWorldReader world, final double x, final double y,
			   final double z, final int freq) {
		super(world, x, y, z);

		this.jetStrength = strength;
		this.updateFrequency = freq;
		this.particleMaxAge = (RANDOM.nextInt(strength) + 2) * 20;
	}

	/*
	 * Override in derived class to provide particle for the jet.
	 */
	protected abstract void spawnJetParticle();

	@Override
	public boolean shouldDie() {
		return this.particleAge >= this.particleMaxAge;
	}

	/*
	 * During update see if a particle needs to be spawned so that it can rise up.
	 */
	@Override
	public void think() {

		// Check to see if a particle needs to be generated
		if (this.particleAge % this.updateFrequency == 0) {
			spawnJetParticle();
		}

		// Grow older
		this.particleAge++;
	}
}
