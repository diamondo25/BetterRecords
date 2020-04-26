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
package tech.feldman.betterrecords.client.render.helper

import tech.feldman.betterrecords.api.wire.IRecordWireHome
import tech.feldman.betterrecords.api.wire.IRecordWireManipulator
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.math.BlockPos
import org.lwjgl.opengl.GL11
import tech.feldman.betterrecords.util.getGainForPlayerPosition

fun renderInfo(x: Double, y: Double, z: Double, blockHeight: Double, vararg lines: String) {
    GlStateManager.pushMatrix()

    // Try to put it in the middle of the box
    GlStateManager.translate(x + 0.5, y, z + 0.5)

    val scale = 100f

    // Convert scaling to CM, so textrendering doesn't offset a meter
    GlStateManager.scale(0.01F, -0.01F, 0.01F)

    //val lines = arrayOf("Line 1", "Line 2", "-------------------------- Line 3", "Line 4 ----------------------------")

    val lineHeight = 0.1
    GlStateManager.color(1F, 1F, 1F)

    val fontRenderer = Minecraft.getMinecraft().fontRenderer

    fun drawText(text: String, y: Int, color: Int) {
        fontRenderer.drawString(text, -fontRenderer.getStringWidth(text) / 2, y, color)
    }

    // Make billboard 'texture'
    GlStateManager.rotate(Minecraft.getMinecraft().renderManager.playerViewY + 180F, 0F, -1F, 0F)

    // Renders from top to bottom
    lines.reversed().fold(blockHeight + lineHeight, { renderY, text ->
        val lineY = -(renderY * scale).toInt()
        drawText(text, lineY, 0xFFFFFF)
        return@fold renderY + lineHeight
    })

    GlStateManager.popMatrix()
}