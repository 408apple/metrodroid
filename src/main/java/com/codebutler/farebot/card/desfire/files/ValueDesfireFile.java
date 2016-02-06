package com.codebutler.farebot.card.desfire.files;

import com.codebutler.farebot.card.desfire.settings.DesfireFileSettings;
import com.codebutler.farebot.card.desfire.settings.RecordDesfireFileSettings;
import com.codebutler.farebot.util.Utils;

import org.apache.commons.lang3.ArrayUtils;
import org.simpleframework.xml.Root;


/**
 * Represents a value file in Desfire
 */
@Root(name="file")
public class ValueDesfireFile extends DesfireFile {
    private int mValue;

    private ValueDesfireFile() { /* For XML Serializer */ }

    ValueDesfireFile(int fileId, DesfireFileSettings fileSettings, byte[] fileData) {
        super(fileId, fileSettings, fileData);

        byte[] myData = ArrayUtils.clone(fileData);
        ArrayUtils.reverse(myData);
        mValue = Utils.byteArrayToInt(myData);

    }

    public int getValue() { return mValue; }

}

