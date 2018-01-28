/*
 * DesfireCard.java
 *
 * Copyright 2011 Eric Butler <eric@codebutler.com>
 * Copyright 2016 Michael Farrell <micolous+git@gmail.com>
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

package au.id.micolous.metrodroid.card.desfire;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;

import au.id.micolous.metrodroid.card.Card;
import au.id.micolous.metrodroid.card.CardRawDataFragmentClass;
import au.id.micolous.metrodroid.card.CardType;
import au.id.micolous.metrodroid.card.desfire.files.DesfireFile;
import au.id.micolous.metrodroid.card.desfire.files.InvalidDesfireFile;
import au.id.micolous.metrodroid.card.desfire.files.UnauthorizedDesfireFile;
import au.id.micolous.metrodroid.card.desfire.settings.DesfireFileSettings;
import au.id.micolous.metrodroid.card.desfire.settings.StandardDesfireFileSettings;
import au.id.micolous.metrodroid.card.desfire.settings.ValueDesfireFileSettings;
import au.id.micolous.metrodroid.fragment.DesfireCardRawDataFragment;
import au.id.micolous.metrodroid.transit.TransitData;
import au.id.micolous.metrodroid.transit.TransitIdentity;
import au.id.micolous.metrodroid.transit.clipper.ClipperTransitData;
import au.id.micolous.metrodroid.transit.hsl.HSLTransitData;
import au.id.micolous.metrodroid.transit.myki.MykiTransitData;
import au.id.micolous.metrodroid.transit.opal.OpalTransitData;
import au.id.micolous.metrodroid.transit.orca.OrcaTransitData;
import au.id.micolous.metrodroid.transit.stub.AdelaideMetrocardStubTransitData;
import au.id.micolous.metrodroid.transit.stub.AtHopStubTransitData;
import au.id.micolous.metrodroid.util.Utils;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.io.IOException;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

@Root(name = "card")
@CardRawDataFragmentClass(DesfireCardRawDataFragment.class)
public class DesfireCard extends Card {
    @Element(name = "manufacturing-data")
    private DesfireManufacturingData mManfData;
    @ElementList(name = "applications")
    private List<DesfireApplication> mApplications;

    private DesfireCard() { /* For XML Serializer */ }

    public DesfireCard(byte[] tagId, Calendar scannedAt, DesfireManufacturingData manfData, DesfireApplication[] apps) {
        super(CardType.MifareDesfire, tagId, scannedAt);
        mManfData = manfData;
        mApplications = Utils.arrayAsList(apps);
    }

    public static DesfireCard dumpTag(Tag tag) throws Exception {
        List<DesfireApplication> apps = new ArrayList<>();

        IsoDep tech = IsoDep.get(tag);

        tech.connect();

        DesfireManufacturingData manufData;
        DesfireApplication[] appsArray;

        try {
            DesfireProtocol desfireTag = new DesfireProtocol(tech);

            manufData = desfireTag.getManufacturingData();

            for (int appId : desfireTag.getAppList()) {
                desfireTag.selectApp(appId);

                List<DesfireFile> files = new ArrayList<>();

                for (int fileId : desfireTag.getFileList()) {
                    DesfireFileSettings settings = null;
                    try {
                        settings = desfireTag.getFileSettings(fileId);
                        byte[] data;
                        if (settings instanceof StandardDesfireFileSettings) {
                            data = desfireTag.readFile(fileId);
                        } else if (settings instanceof ValueDesfireFileSettings) {
                            data = desfireTag.getValue(fileId);
                        } else {
                            data = desfireTag.readRecord(fileId);
                        }
                        files.add(DesfireFile.create(fileId, settings, data));
                    } catch (AccessControlException ex) {
                        files.add(new UnauthorizedDesfireFile(fileId, ex.getMessage(), settings));
                    } catch (IOException ex) {
                        throw ex;
                    } catch (Exception ex) {
                        files.add(new InvalidDesfireFile(fileId, ex.toString(), settings));
                    }
                }

                DesfireFile[] filesArray = new DesfireFile[files.size()];
                files.toArray(filesArray);

                apps.add(new DesfireApplication(appId, filesArray));
            }

            appsArray = new DesfireApplication[apps.size()];
            apps.toArray(appsArray);
        } finally {
            if (tech.isConnected())
                tech.close();
        }

        return new DesfireCard(tag.getId(), GregorianCalendar.getInstance(), manufData, appsArray);
    }

    @Override
    public TransitIdentity parseTransitIdentity() {
        if (OrcaTransitData.check(this))
            return OrcaTransitData.parseTransitIdentity(this);
        if (ClipperTransitData.check(this))
            return ClipperTransitData.parseTransitIdentity(this);
        if (HSLTransitData.check(this))
            return HSLTransitData.parseTransitIdentity(this);
        if (OpalTransitData.check(this))
            return OpalTransitData.parseTransitIdentity(this);
        if (MykiTransitData.check(this))
            return MykiTransitData.parseTransitIdentity(this);

        // Stub card types go last
        if (AdelaideMetrocardStubTransitData.check(this))
            return AdelaideMetrocardStubTransitData.parseTransitIdentity(this);
        if (AtHopStubTransitData.check(this))
            return AtHopStubTransitData.parseTransitIdentity(this);
        return null;
    }

    @Override
    public TransitData parseTransitData() {
        if (OrcaTransitData.check(this))
            return new OrcaTransitData(this);
        if (ClipperTransitData.check(this))
            return new ClipperTransitData(this);
        if (HSLTransitData.check(this))
            return new HSLTransitData(this);
        if (OpalTransitData.check(this))
            return new OpalTransitData(this);
        if (MykiTransitData.check(this))
            return new MykiTransitData(this);

        // Stub card types go last
        if (AdelaideMetrocardStubTransitData.check(this))
            return new AdelaideMetrocardStubTransitData(this);
        if (AtHopStubTransitData.check(this))
            return new AtHopStubTransitData(this);
        return null;
    }

    public List<DesfireApplication> getApplications() {
        return mApplications;
    }

    public DesfireApplication getApplication(int appId) {
        for (DesfireApplication app : mApplications) {
            if (app.getId() == appId)
                return app;
        }
        return null;
    }

    public DesfireManufacturingData getManufacturingData() {
        return mManfData;
    }
}
