/*
 * SmartCard.kt
 *
 * Copyright 2019 Michael Farrell <micolous+git@gmail.com>
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
package au.id.micolous.metrodroid.cli

import au.id.micolous.metrodroid.card.*
import au.id.micolous.metrodroid.card.desfire.DesfireCardReader
import au.id.micolous.metrodroid.card.felica.FelicaReader
import au.id.micolous.metrodroid.card.iso7816.ISO7816Card
import au.id.micolous.metrodroid.printCard
import au.id.micolous.metrodroid.serializers.JsonKotlinFormat
import au.id.micolous.metrodroid.time.TimestampFull
import au.id.micolous.metrodroid.transit.CardInfo
import au.id.micolous.metrodroid.util.makeFilename
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.file
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Paths
import java.util.*
import javax.smartcardio.CardTerminal


/**
 * Ignore certain CCID readers
 */
val CardTerminal.ignored : Boolean
    get() {
        val iname = name.toLowerCase(Locale.ENGLISH)

        // Yubikey CCID
        return iname.contains("yubi")
    }

/**
 * Communicates with a card using the PC/SC API.
 */
class SmartCard: CliktCommand(help="Communicates with a card using the PC/SC API") {

    private val reader: String? by option("-r", "--reader", metavar = "DEVICE",
        help="Name of the reader device to use. If not specified, tries to pick the first reader " +
            "which has a card inserted, and is not a security key. eg: -r \"ACS ACR122U\"")
    private val listReaders: Boolean by option("-l", "--list",
        help="Lists all connected reader devices.").flag(default=false)

    private val noParse: Boolean by option("-P", "--no-parse",
        help="Skip parsing the card. Useful if you only want to save a dump of the card " +
            "data.").flag(default=false)
    private val traces: Boolean by option("-t", "--trace",
        help="Emits raw APDU data for debugging.").flag(default=false)
    private val noUID: Boolean by option("-U", "--no-uid",
        help="Skip requesting the card's UID. Required for contact card readers. Breaks " +
            "communication with FeliCa and several contactless cards.").flag(default=false)

    private val outFile: File? by option("-o", "--output", metavar = "FILE_OR_DIR",
        help="Specify a path that does not exist to create a new file with this name. " +
            "Specify a directory name that already exists to create a new dump file in " +
            "it, automatically generating a name.").file().validate {
        require(it.exists() == it.isDirectory) {
            "must be a directory that exists, or must be a file that doesn't exist: $it" }
        if (!it.isDirectory) {
            val parent = it.canonicalFile.parentFile
            require(parent.isDirectory && parent.exists()) {
                "parent must be a directory that exists: $it" }
        }
    }

    override fun run() {
        val o = Object()
        val factory = jnasmartcardio.Smartcardio.JnaTerminalFactorySpi(o)
        val allTerminals = factory.engineTerminals().list()
        val outFile : File? = outFile

        if (listReaders) {
            printTerminals(allTerminals)
            return
        }

        val terminal = if (reader == null) {
            val usableTerminals = allTerminals.filter { !it.ignored && it.isCardPresent }

            if (usableTerminals.count() != 1) {
                printTerminals(allTerminals)
                println("Expected 1 terminal, got ${usableTerminals.count()} instead.")
                return
            }

            usableTerminals.getOrNull(0)
        } else {
            allTerminals.filter { it.name == reader }.getOrNull(0)
        }

        if (terminal == null) {
            printTerminals(allTerminals)
            println("Couldn't find terminal: $reader")
            return
        }

        println("Terminal: ${terminal.name}")

        if (!terminal.isCardPresent) {
            println("Card not present, insert into / move in range of ${terminal.name}")
            return
        }

        val card = runBlocking { dumpTag(terminal) }

        if (outFile != null) {
            val fn = if (outFile.isDirectory) {
                Paths.get(outFile.path, makeFilename(card)).toFile()
            } else { outFile }

            if (!fn.createNewFile()) {
                println("File already exists: $fn")
                return
            }

            FileOutputStream(fn).use {
                JsonKotlinFormat.writeCard(it, card)
                println("Wrote card data to: ${fn.path}")
            }
        }

        if (!noParse) {
            println("Card info:")
            printCard(card)
        }
    }

    /** Prints a list of connected terminals. */
    private fun printTerminals(terminals: List<CardTerminal>) {
        println("Found ${terminals.count()} card terminal(s):")
        terminals.forEachIndexed { index, cardTerminal ->
            println(
                "#$index: ${cardTerminal.name} (card ${
                if (cardTerminal.isCardPresent) { "present" } else { "missing" } }) ${
                if (cardTerminal.ignored) { "(ignored)" } else { "" }}")
        }
    }

    private val feedbackInterface = object : TagReaderFeedbackInterface {
        override fun updateStatusText(msg: String) {
            println(msg)
        }

        override fun updateProgressBar(progress: Int, max: Int) {
            println("Dumping: ($progress / $max)")
        }

        override fun showCardType(cardInfo: CardInfo?) {
            if (cardInfo == null) {
                println("Empty card type")
                return
            }

            println("Card type: ${cardInfo.name}")
        }
    }

    private suspend fun dumpTag(terminal: CardTerminal) : Card {
        JavaCardTransceiver(terminal, traces, noUID).use {
            it.connect()

            // TODO
            val tagId = it.uid!!
            val scannedAt = TimestampFull.now()

            when (it.cardType) {
                CardType.ISO7816 -> {
                    val d = DesfireCardReader.dumpTag(it, feedbackInterface)
                    if (d != null) {
                        return Card(tagId = tagId, scannedAt = scannedAt, mifareDesfire = d)
                    }

                    val isoCard = ISO7816Card.dumpTag(it, feedbackInterface)
                    return Card(tagId = tagId, scannedAt = scannedAt, iso7816 = isoCard)
                }

                CardType.FeliCa -> {
                    val t = JavaFeliCaTransceiver.wrap(it)
                    val f = FelicaReader.dumpTag(t, feedbackInterface)
                    return Card(tagId = tagId, scannedAt = scannedAt, felica = f)
                }

                else -> throw Exception("Unhandled card type ${it.cardType}")
            }
        }
    }
}
