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
package tech.feldman.betterrecords.api.connection

import net.minecraft.util.math.BlockPos

class RecordConnection() {
    var x1: Int = 0
    var y1: Int = 0
    var z1: Int = 0
    var x2: Int = 0
    var y2: Int = 0
    var z2: Int = 0

    constructor(home: BlockPos, to: BlockPos) : this() {
        setHomePosition(home.x, home.y, home.z)
        setToPosition(to.x, to.y, to.z)
    }

    constructor(string: String) : this() {
        val str = string.split(',').dropLastWhile { it.isEmpty() }.map { x -> Integer.parseInt(x) }
        setHomePosition(str[0], str[1], str[2])
        setToPosition(str[3], str[4], str[5])
    }

    override fun toString(): String {
        return "$x1,$y1,$z1,$x2,$y2,$z2"
    }

    fun setHomePosition(x: Int, y: Int, z: Int): RecordConnection {
        x1 = x
        y1 = y
        z1 = z
        return this
    }

    fun setToPosition(x: Int, y: Int, z: Int): RecordConnection {
        x2 = x
        y2 = y
        z2 = z
        return this
    }

    fun getHomePosition() = BlockPos(x1, y1, z1)
    fun getToPosition() = BlockPos(x2, y2, z2)

    fun same(rec: RecordConnection): Boolean {
        return x1 == rec.x1 && y1 == rec.y1 && z1 == rec.z1 && x2 == rec.x2 && y2 == rec.y2 && z2 == rec.z2
    }
}
