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
package tech.feldman.betterrecords.block

import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyDirection
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.Mirror
import net.minecraft.util.Rotation
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import tech.feldman.betterrecords.api.wire.IRecordWire
import tech.feldman.betterrecords.api.wire.IRecordWireManipulator
import tech.feldman.betterrecords.block.tile.TileLaser
import tech.feldman.betterrecords.client.render.RenderLaser
import tech.feldman.betterrecords.helper.ConnectionHelper
import tech.feldman.betterrecords.network.PacketHandler
import tech.feldman.betterrecords.network.PacketLaserLengthUpdate

class BlockLaser(name: String) : ModBlock(Material.WOOD, name), TESRProvider<TileLaser>, ItemModelProvider {
    companion object {
        val MOUNTING = PropertyDirection.create("mounting")
    }

    init {
        setHardness(3.2f)
        setResistance(4.3f)
        defaultState = blockState.baseState.withProperty(MOUNTING, EnumFacing.DOWN)
    }

    override fun getTileEntityClass() = TileLaser::class
    override fun getRenderClass() = RenderLaser::class

    override fun getBoundingBox(state: IBlockState?, block: IBlockAccess?, pos: BlockPos?) =
            AxisAlignedBB(0.25, 0.0, 0.25, 0.75, 0.75, 0.74)

    override fun onBlockAdded(world: World?, pos: BlockPos?, state: IBlockState?) =
            world!!.notifyBlockUpdate(pos!!, state!!, state, 3)

    // override fun getLightValue(state: IBlockState, access: IBlockAccess, pos: BlockPos): Int TODO: value from flash

    override fun getStateForPlacement(world: World, pos: BlockPos, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float, meta: Int, placer: EntityLivingBase, hand: EnumHand): IBlockState {
        val oppositeFacing = facing.opposite
        return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand).withProperty(MOUNTING, oppositeFacing)
    }

    override fun createBlockState(): BlockStateContainer? {
        return BlockStateContainer(this, MOUNTING)
    }

    override fun getMetaFromState(state: IBlockState): Int {
        var i = 0
        i = i or (state.getValue(MOUNTING) as EnumFacing).index
        return i
    }

    override fun withRotation(state: IBlockState, rot: Rotation): IBlockState {
        return state.withProperty(MOUNTING, rot.rotate(state.getValue(MOUNTING) as EnumFacing))
    }

    override fun withMirror(state: IBlockState, mirrorIn: Mirror): IBlockState {
        return state.withRotation(mirrorIn.toRotation(state.getValue(MOUNTING) as EnumFacing))
    }

    override fun onBlockPlacedBy(world: World, pos: BlockPos, state: IBlockState, entityLiving: EntityLivingBase, itemStack: ItemStack) {
        (world.getTileEntity(pos) as? TileLaser)?.let { te ->
            te.pitch = entityLiving.rotationPitch
            te.yaw = entityLiving.rotationYaw

            te.mounting = when (state.getValue(MOUNTING)) {
                EnumFacing.DOWN -> TileLaser.Mounting.FLOOR
                EnumFacing.UP -> TileLaser.Mounting.CEILING
                EnumFacing.EAST -> TileLaser.Mounting.EAST
                EnumFacing.NORTH -> TileLaser.Mounting.NORTH
                EnumFacing.SOUTH -> TileLaser.Mounting.SOUTH
                EnumFacing.WEST -> TileLaser.Mounting.WEST
                else -> TileLaser.Mounting.FLOOR
            }
        }
    }

    override fun removedByPlayer(state: IBlockState, world: World, pos: BlockPos, player: EntityPlayer, willHarvest: Boolean): Boolean {
        if (!world.isRemote) {
            (world.getTileEntity(pos) as? IRecordWire)?.let { te ->
                ConnectionHelper.clearConnections(world, te, cleanupOnly = false)
            }
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest)
    }

    override fun onBlockActivated(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer, hand: EnumHand, side: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean {
        if (player.heldItemMainhand.item is IRecordWireManipulator) return false

        (world.getTileEntity(pos) as? TileLaser)?.let { te ->
            val length = te.length

            if (player.isSneaking && te.length > 0) {
                te.length--
            } else if (!player.isSneaking && te.length < 25) {
                te.length++
            }

            if (te.length != length && !world.isRemote) {
                world.notifyBlockUpdate(pos, state, state, 3)
                // PacketHandler.sendToAll(PacketLaserLengthUpdate(te.pos, world.provider.dimension, te.length))

                val adjustment = if (te.length > length) "increase" else "decrease"
                player.sendMessage(TextComponentTranslation("tile.betterrecords:laser.msg.$adjustment", te.length))
            }
            return true
        }
        return false
    }
}
