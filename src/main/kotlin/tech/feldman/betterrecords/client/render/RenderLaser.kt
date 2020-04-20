/**
 * The MIT License
 *
 * Copyright (c) 2019 Nicholas Feldman
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
package tech.feldman.betterrecords.client.render

import tech.feldman.betterrecords.ID
import tech.feldman.betterrecords.ModConfig
import tech.feldman.betterrecords.block.tile.TileLaser
import tech.feldman.betterrecords.client.model.ModelLaserUnit
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import tech.feldman.betterrecords.client.model.ModelLaserBase

class RenderLaser : TileEntitySpecialRenderer<TileLaser>() {

    val MODEL_UNIT = ModelLaserUnit()
    val MODEL_BASE = ModelLaserBase()
    val TEXTURE = ResourceLocation(ID, "textures/models/laser.png")

    override fun render(te: TileLaser?, x: Double, y: Double, z: Double, scale: Float, destroyStage: Int, alpha: Float) {

        pushMatrix()

        translate(x.toFloat() + 0.5f, y.toFloat() + 1.5f, z.toFloat() + 0.5f)
        rotate(180f, 0.0f, 0.0f, 1.0f)

        bindTexture(TEXTURE)

        val (basePitch, baseYaw) = te?.mounting?.getPitchAndYaw() ?: Pair(0f, 0f)

        MODEL_BASE.render(null, basePitch, baseYaw, 0.0f, 0.0f, 0.0f, 0.0625f)


        val yaw = te?.let{ it.yaw + it.yawAnim.current } ?: 0F
        val pitch = te?.let{ it.pitch + it.pitchAnim.current } ?: 0F
        MODEL_UNIT.render(null, pitch, yaw, 0.0f, 0.0f, 0.0f, 0.0625f)

        rotate(-180f, 0.0f, 0.0f, 1.0f)
        translate(0.0f, -.926f, 0.0f)

        te?.let {
            if (it.bass != 0F && ModConfig.client.flashMode > 0) {
                pushMatrix()

                disableTexture2D()
                enableBlend()
                disableCull()

                rotate(-yaw + 180f, 0f, 1f, 0f)
                rotate(-pitch + 90f, 1f, 0f, 0f)

                val length = it.length
                val width = it.bass / 400f

                glBegin(GL11.GL_QUADS)

                color(it.r, it.g, it.b, if (ModConfig.client.flashMode == 1) .3f else .8f)

                glVertex3f(width, 0f, -width)
                glVertex3f(-width, 0f, -width)
                glVertex3f(-width, length.toFloat(), -width)
                glVertex3f(width, length.toFloat(), -width)

                glVertex3f(-width, 0f, width)
                glVertex3f(width, 0f, width)
                glVertex3f(width, length.toFloat(), width)
                glVertex3f(-width, length.toFloat(), width)

                glVertex3f(width, 0f, width)
                glVertex3f(width, 0f, -width)
                glVertex3f(width, length.toFloat(), -width)
                glVertex3f(width, length.toFloat(), width)

                glVertex3f(-width, 0f, -width)
                glVertex3f(-width, 0f, width)
                glVertex3f(-width, length.toFloat(), width)
                glVertex3f(-width, length.toFloat(), -width)

                glEnd()

                enableCull()
                disableBlend()
                enableTexture2D()

                GL11.glPopMatrix()

                color(1f, 1f, 1f, 1f)
            }
        }

        popMatrix()
    }
}
