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

import tech.feldman.betterrecords.api.sound.ISoundHolder
import tech.feldman.betterrecords.api.wire.IRecordWire
import tech.feldman.betterrecords.api.wire.IRecordWireManipulator
import tech.feldman.betterrecords.block.tile.TileRadio
import tech.feldman.betterrecords.client.render.RenderRadio
import tech.feldman.betterrecords.helper.ConnectionHelper
import tech.feldman.betterrecords.helper.nbt.getSounds
import tech.feldman.betterrecords.item.ModItems
import tech.feldman.betterrecords.network.PacketHandler
import tech.feldman.betterrecords.network.PacketRadioPlay
import tech.feldman.betterrecords.network.PacketSoundStop
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.InventoryHelper
import net.minecraft.item.ItemStack
import net.minecraft.util.*
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.client.MinecraftForgeClient
import net.minecraftforge.common.MinecraftForge
import tech.feldman.betterrecords.helper.nbt.hasSounds
import java.util.*

class BlockRadio(name: String) : ModBlockDirectional(Material.WOOD, name), TESRProvider<TileRadio>, ItemModelProvider {

    init {
        setHardness(2f)
        setResistance(6.3f)
    }

    override fun getTileEntityClass() = TileRadio::class
    override fun getRenderClass() = RenderRadio::class

    override fun onBlockAdded(world: World, pos: BlockPos, state: IBlockState) {
        world.notifyBlockUpdate(pos, state, state, 3)
    }

    private fun getSongPacket(te: TileRadio, world: World, pos: BlockPos) = getSounds(te.crystal).first().let {
        PacketRadioPlay(
                pos,
                world.provider.dimension,
                te.songRadius,
                it.name,
                it.url
        )
    }

    override fun getBoundingBox(state: IBlockState, block: IBlockAccess, pos: BlockPos) = when (getMetaFromState(state)) {
        0, 2 -> AxisAlignedBB(0.13, 0.0, 0.2, 0.87, 0.98, 0.8)
        1, 3 -> AxisAlignedBB(0.2, 0.0, 0.13, 0.8, 0.98, 0.87)
        else -> Block.FULL_BLOCK_AABB
    }

    override fun onBlockActivated(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer, hand: EnumHand, side: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean {
        if (player.heldItemMainhand.item is IRecordWireManipulator) return false

        (world.getTileEntity(pos) as? TileRadio)?.let { te ->
            if (player.isSneaking) {
                te.opening = !te.opening
                world.notifyBlockUpdate(pos, state, state, 3)
                world.playSound(pos.x.toDouble(), pos.y.toDouble() + 0.5, pos.z.toDouble(), SoundEvent.REGISTRY.getObject(ResourceLocation("block.chest.close")), SoundCategory.NEUTRAL, 0.2f, world.rand.nextFloat() * 0.2f + 3f, false)
            } else if (te.opening) {
                if (!te.crystal.isEmpty) {
                    dropItem(world, pos)
                    te.crystal = ItemStack.EMPTY
                    world.notifyBlockUpdate(pos, state, state, 3)
                } else if (player.heldItemMainhand.item == ModItems.itemFrequencyCrystal && getSounds(player.heldItemMainhand).isNotEmpty()) {
                    // Play a song for the people
                    te.crystal = player.heldItemMainhand
                    world.notifyBlockUpdate(pos, state, state, 3)
                    player.heldItemMainhand.count--
                    if (!world.isRemote) {
                        PacketHandler.sendToAll(getSongPacket(te, world, pos))
                    }
                }
            }
            return true
        }
        return false
    }

    override fun removedByPlayer(state: IBlockState, world: World, pos: BlockPos, player: EntityPlayer, willHarvest: Boolean): Boolean {
        if (!world.isRemote) {
            (world.getTileEntity(pos) as? IRecordWire)?.let { te ->
                ConnectionHelper.clearConnections(world, te, cleanupOnly = false)
            }
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest)
    }

    override fun breakBlock(world: World, pos: BlockPos, state: IBlockState) {
        dropItem(world, pos)
        super.breakBlock(world, pos, state)
    }

    private fun dropItem(world: World, pos: BlockPos) {
        if (world.isRemote) return

        (world.getTileEntity(pos) as? TileRadio)?.let { te ->
            if (!te.crystal.isEmpty) {
                InventoryHelper.spawnItemStack(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), te.crystal.copy())
                te.crystal = ItemStack.EMPTY
            }
            PacketHandler.sendToAll(PacketSoundStop(te.pos, world.provider.dimension))
        }

    }
}
