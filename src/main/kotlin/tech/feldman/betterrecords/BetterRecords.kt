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
package tech.feldman.betterrecords

import tech.feldman.betterrecords.item.ModItems
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Mod(modid = ID, name = NAME, version = VERSION, modLanguageAdapter = LANGUAGE_ADAPTER, dependencies = DEPENDENCIES)
object BetterRecords {

    val logger: Logger = LogManager.getLogger(ID)

    @SidedProxy(clientSide = CLIENT_PROXY, serverSide = SERVER_PROXY)
    lateinit var proxy: CommonProxy

    val creativeTab = object : CreativeTabs(ID) {
        override fun getTabIconItem() = ItemStack(ModItems.itemRecord)
    }

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) = proxy.preInit(event)

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) = proxy.init(event)

    @Mod.EventHandler
    fun postInit(event: FMLPostInitializationEvent) = proxy.postInit(event)
}
