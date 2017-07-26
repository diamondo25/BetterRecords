package com.codingforcookies.betterrecords.block.tile

import net.minecraft.entity.item.EntityItem
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemRecord
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ITickable

class TileRecordEtcher : ModInventoryTile(), IInventory, ITickable {

    var record: ItemStack? = null
        set(value) {
            value?.let {
                field = value.copy()
                field!!.stackSize = 1
                recordEntity = EntityItem(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), field)
                recordEntity!!.hoverStart = 0F
                recordRotation = 0F
            }
        }

    var recordEntity: EntityItem? = null

    var recordRotation = 0F
    var needleLocation = 0F
    var needleOut = true

    override fun update() {
        record?.let {
            recordRotation += .08F

            if (needleOut) {
                when {
                    needleLocation < .3F -> needleLocation += .001F
                    else -> needleOut = true
                }
                return
            }
        }

        needleLocation = when {
            needleLocation > 0F -> needleLocation - .005F
            else -> 0F
        }
    }

    override fun getName() = "Record Etcher"

    override fun getSizeInventory() = 1
    override fun getInventoryStackLimit() = 1

    override fun getStackInSlot(index: Int) = record
    override fun setInventorySlotContents(index: Int, stack: ItemStack?) {
        record = stack
    }

    override fun isItemValidForSlot(index: Int, stack: ItemStack) =
            stack.item is ItemRecord && (!stack.hasTagCompound() || !stack.tagCompound!!.hasKey("url"))

    override fun readFromNBT(compound: NBTTagCompound) = compound.run {
        super.readFromNBT(compound)

        record = ItemStack.loadItemStackFromNBT(getCompoundTag("record"))
    }

    override fun writeToNBT(compound: NBTTagCompound) = compound.apply {
        setTag("record", getStackTagCompound(record))
    }

    fun getStackTagCompound(stack: ItemStack?): NBTTagCompound {
        val tag = NBTTagCompound()
        stack?.writeToNBT(tag)
        return tag
    }
}