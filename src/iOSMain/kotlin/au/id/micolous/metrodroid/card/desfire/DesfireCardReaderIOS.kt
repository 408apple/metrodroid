/*
 * DesfireCardReaderIOS.kt
 *
 * Copyright 2019 Google
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package au.id.micolous.metrodroid.card.desfire

import au.id.micolous.metrodroid.card.Card
import au.id.micolous.metrodroid.card.TagReaderFeedbackInterface
import au.id.micolous.metrodroid.multi.Log
import au.id.micolous.metrodroid.multi.logAndSwiftWrap
import au.id.micolous.metrodroid.time.TimestampFull
import platform.CoreNFC.NFCMiFareTagProtocol

@Suppress("unused") // Used from Swift
object DesfireCardReaderIOS {
    @Throws(Throwable::class)
    fun dump(tag: NFCMiFareTagProtocol,
             feedback: TagReaderFeedbackInterface): Card = logAndSwiftWrap (TAG, "Failed to dump"){
        val xfer = DesfireTransceiver(tag)
        Log.d(TAG, "Start dump ${xfer.uid}")
        val df = DesfireCardReader.dumpTag(xfer, feedback)
        Card(
            tagId = xfer.uid?.let { if (it.size == 10) it.sliceOffLen(0, 7) else it }!!,
            scannedAt = TimestampFull.now(), mifareDesfire = df
        )
    }

    private const val TAG = "DesfireCardReaderIOS"
}
