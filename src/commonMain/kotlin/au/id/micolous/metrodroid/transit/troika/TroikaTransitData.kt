/*
 * TroikaTransitData.kt
 *
 * Copyright 2015-2016 Michael Farrell <micolous+git@gmail.com>
 * Copyright 2018 Google
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
package au.id.micolous.metrodroid.transit.troika

import au.id.micolous.metrodroid.card.CardType
import au.id.micolous.metrodroid.card.classic.ClassicCard
import au.id.micolous.metrodroid.card.classic.UnauthorizedClassicSector
import au.id.micolous.metrodroid.multi.*
import au.id.micolous.metrodroid.transit.*
import au.id.micolous.metrodroid.ui.HeaderListItem
import au.id.micolous.metrodroid.ui.ListItemInterface

/**
 * Troika cards.
 */

@Parcelize
class TroikaTransitData(private val mBlocks: List<TroikaIndexedBlock>) : Parcelable {

    val serialNumber: String?
        get() = mBlocks[0].block.serialNumber

    val info: List<ListItemInterface>?
        get() = mBlocks.flatMap { (_, block) -> block.info.orEmpty() }.ifEmpty { null }

    val balance: TransitBalance
        get() = mBlocks[0].block.balance ?: TransitCurrency.RUB(0)

    val warning: String?
        get() = if (mBlocks[0].block.balance == null && subscriptions.isEmpty())
            Localizer.localizeString(R.string.troika_unformatted)
        else
            null

    val trips: List<Trip>
        get() = mBlocks.flatMap { (_, block) -> block.trips }

    val subscriptions: List<Subscription>
        get() = mBlocks.mapNotNull {  (_, block) -> block.subscription }

    val debug: List<ListItemInterface>
        get() =
        mBlocks.flatMap {  (blockNum, block) -> listOf(HeaderListItem("Block $blockNum")) + block.debug }

    constructor(card: ClassicCard) : this(
            listOf(8, 7, 4, 1).mapNotNull {idx ->
                decodeSector(card, idx)?.let { TroikaIndexedBlock(idx, it) }
            }
        )

    companion object {
        internal val CARD_INFO = CardInfo(
                imageId = R.drawable.troika_card,
                imageAlphaId = R.drawable.iso7810_id1_alpha,
                name = R.string.card_name_troika,
                locationId = R.string.location_moscow,
                cardType = CardType.MifareClassic,
                resourceExtraNote = R.string.card_note_russia,
                region = TransitRegion.RUSSIA,
                keysRequired = true, preview = true, keyBundle = "troika")

        @Parcelize
        data class TroikaIndexedBlock(
            val sectorNum: Int,
            val block: TroikaBlock
        ): Parcelable

        private const val TAG = "TroikaTransitData"

        private fun decodeSector(card: ClassicCard, idx: Int): TroikaBlock? {
            try {
                val sector = card.getSector(idx)
                if (sector is UnauthorizedClassicSector)
                    return null
                val block = sector.allData
                return if (!TroikaBlock.check(block)) null else TroikaBlock.parseBlock(block)
            } catch (e: Exception) {
                Log.w(TAG, "Error decoding troika sector", e)
                return null
            }
        }
    }
}
