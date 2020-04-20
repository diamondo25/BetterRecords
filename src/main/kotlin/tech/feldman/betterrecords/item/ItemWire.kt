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
package tech.feldman.betterrecords.item

import tech.feldman.betterrecords.api.connection.RecordConnection
import tech.feldman.betterrecords.api.wire.IRecordWire
import tech.feldman.betterrecords.api.wire.IRecordWireHome
import tech.feldman.betterrecords.api.wire.IRecordWireManipulator
import tech.feldman.betterrecords.helper.ConnectionHelper
import tech.feldman.betterrecords.network.PacketHandler
import tech.feldman.betterrecords.network.PacketWireConnection
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import tech.feldman.betterrecords.ModConfig

class ItemWire(name: String) : ModItem(name), IRecordWireManipulator {
    var firstPosition: BlockPos? = null

    override fun onItemUse(player: EntityPlayer, world: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult {
        if (!world.isRemote) {
            return EnumActionResult.PASS
        }

        val te = world.getTileEntity(pos)
        if (te is IRecordWire) {
            firstPosition?.let { firstPos ->

                val distance = firstPos.getDistance(pos.x, pos.y, pos.z)

                if (distance == 0.0) {
                    // Trying to connect to the same object
                    println("Cable connecting to same object")
                    return EnumActionResult.PASS
                }

                if (distance > ModConfig.maxCableLength) {
                    // Cable too long
                    println("Cable too long")
                    return EnumActionResult.PASS
                }

                // Setup initial connection object
                val connection = if (te !is IRecordWireHome) {
                    RecordConnection(firstPos, pos)
                } else {
                    RecordConnection(pos, firstPos)
                }

                // Assign connections to both objects
                val homeTileEntity = world.getTileEntity(connection.getHomePosition()) as IRecordWire
                val toTileEntity = world.getTileEntity(connection.getToPosition()) as IRecordWire
                ConnectionHelper.addConnection(world, homeTileEntity, connection, world.getBlockState(connection.getHomePosition()))
                ConnectionHelper.addConnection(world, toTileEntity, connection, world.getBlockState(connection.getToPosition()))

                PacketHandler.sendToServer(PacketWireConnection(connection))
                player.getHeldItem(hand).count--

                println("Bound ${homeTileEntity} to ${toTileEntity}")
                firstPosition = null
                return EnumActionResult.PASS
            }

            firstPosition = pos
        }

        return EnumActionResult.PASS
    }
}
