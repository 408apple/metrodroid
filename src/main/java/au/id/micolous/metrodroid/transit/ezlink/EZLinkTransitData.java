/*
 * EZLinkTransitData.java
 *
 * Copyright 2011 Sean Cross <sean@chumby.com>
 * Copyright 2011-2012 Eric Butler <eric@codebutler.com>
 * Copyright 2012 Victor Heng
 * Copyright 2012 Toby Bonang
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

package au.id.micolous.metrodroid.transit.ezlink;

import android.os.Parcel;
import android.support.annotation.Nullable;

import au.id.micolous.metrodroid.card.Card;
import au.id.micolous.metrodroid.card.cepas.CEPASCard;
import au.id.micolous.metrodroid.card.cepas.CEPASTransaction;
import au.id.micolous.metrodroid.transit.Station;
import au.id.micolous.metrodroid.transit.TransitCurrency;
import au.id.micolous.metrodroid.transit.TransitData;
import au.id.micolous.metrodroid.transit.TransitIdentity;
import au.id.micolous.metrodroid.transit.Trip;
import au.id.micolous.metrodroid.util.StationTableReader;
import au.id.micolous.metrodroid.util.Utils;

import java.util.HashSet;
import java.util.List;

public class EZLinkTransitData extends TransitData {
    public static final Creator<EZLinkTransitData> CREATOR = new Creator<EZLinkTransitData>() {
        public EZLinkTransitData createFromParcel(Parcel parcel) {
            return new EZLinkTransitData(parcel);
        }

        public EZLinkTransitData[] newArray(int size) {
            return new EZLinkTransitData[size];
        }
    };
    private static final String EZLINK_STR = "ezlink";
    static HashSet<String> sbsBuses = new HashSet<String>() {
        private static final long serialVersionUID = 1L;

        {
            add("CT18");
            add("CT8");
            add("1N");
            add("2");
            add("2N");
            add("3");
            add("3N");
            add("4N");
            add("5");
            add("5N");
            add("6");
            add("6N");
            add("7");
            add("8");
            add("9");
            add("10");
            add("10e");
            add("11");
            add("12");
            add("13");
            add("14");
            add("14e");
            add("15");
            add("16");
            add("17");
            add("18");
            add("19");
            add("21");
            add("22");
            add("23");
            add("24");
            add("25");
            add("26");
            add("27");
            add("28");
            add("29");
            add("30");
            add("30e");
            add("31");
            add("32");
            add("33");
            add("34");
            add("35");
            add("36");
            add("37");
            add("38");
            add("39");
            add("40");
            add("42");
            add("43");
            add("45");
            add("48");
            add("51");
            add("52");
            add("53");
            add("54");
            add("55");
            add("56");
            add("57");
            add("58");
            add("59");
            add("60");
            add("62");
            add("63");
            add("64");
            add("65");
            add("66");
            add("69");
            add("70");
            add("70M");
            add("72");
            add("73");
            add("74");
            add("74e");
            add("76");
            add("78");
            add("79");
            add("80");
            add("81");
            add("82");
            add("83");
            add("85");
            add("86");
            add("87");
            add("88");
            add("89");
            add("89e");
            add("90");
            add("91");
            add("92");
            add("93");
            add("94");
            add("95");
            add("96");
            add("97");
            add("97e");
            add("98");
            add("98M");
            add("99");
            add("100");
            add("101");
            add("103");
            add("105");
            add("107");
            add("107M");
            add("109");
            add("111");
            add("112");
            add("113");
            add("115");
            add("119");
            add("123");
            add("123M");
            add("124");
            add("125");
            add("128");
            add("130");
            add("131");
            add("132");
            add("133");
            add("133M");
            add("135");
            add("136");
            add("138");
            add("139");
            add("142");
            add("143");
            add("145");
            add("147");
            add("151");
            add("151e");
            add("153");
            add("154");
            add("155");
            add("156");
            add("157");
            add("158");
            add("159");
            add("160");
            add("161");
            add("162");
            add("162M");
            add("163");
            add("163M");
            add("165");
            add("166");
            add("168");
            add("170");
            add("170X");
            add("174");
            add("174e");
            add("175");
            add("179");
            add("179A");
            add("181");
            add("182");
            add("182M");
            add("183");
            add("185");
            add("186");
            add("191");
            add("192");
            add("193");
            add("194");
            add("195");
            add("196");
            add("196e");
            add("197");
            add("198");
            add("199");
            add("200");
            add("222");
            add("225");
            add("228");
            add("229");
            add("231");
            add("232");
            add("235");
            add("238");
            add("240");
            add("241");
            add("242");
            add("243");
            add("246");
            add("249");
            add("251");
            add("252");
            add("254");
            add("255");
            add("257");
            add("261");
            add("262");
            add("265");
            add("268");
            add("269");
            add("272");
            add("273");
            add("275");
            add("282");
            add("284");
            add("284M");
            add("285");
            add("291");
            add("292");
            add("293");
            add("315");
            add("317");
            add("325");
            add("333");
            add("334");
            add("335");
            add("354");
            add("358");
            add("359");
            add("372");
            add("400");
            add("401");
            add("402");
            add("403");
            add("405");
            add("408");
            add("409");
            add("410");
            add("502");
            add("502A");
            add("506");
            add("518");
            add("518A");
            add("532");
            add("533");
            add("534");
            add("535");
            add("536");
            add("538");
            add("539");
            add("542");
            add("543");
            add("544");
            add("545");
            add("548");
            add("549");
            add("550");
            add("552");
            add("553");
            add("554");
            add("555");
            add("556");
            add("557");
            add("558");
            add("559");
            add("560");
            add("561");
            add("563");
            add("564");
            add("565");
            add("566");
            add("569");
            add("585");
            add("761");
        }
    };

    private final String mSerialNumber;
    private final double mBalance;
    private final EZLinkTrip[] mTrips;

    public EZLinkTransitData(Parcel parcel) {
        mSerialNumber = parcel.readString();
        mBalance = parcel.readDouble();

        mTrips = new EZLinkTrip[parcel.readInt()];
        parcel.readTypedArray(mTrips, EZLinkTrip.CREATOR);
    }

    public EZLinkTransitData(Card card) {
        CEPASCard cepasCard = (CEPASCard) card;
        mSerialNumber = Utils.getHexString(cepasCard.getPurse(3).getCAN(), "<Error>");
        mBalance = cepasCard.getPurse(3).getPurseBalance();
        mTrips = parseTrips(cepasCard);
    }

    private static String getCardIssuer(String canNo) {
        int issuerId = Integer.parseInt(canNo.substring(0, 3));
        switch (issuerId) {
            case 100:
                return "EZ-Link";
            case 111:
                return "NETS";
            default:
                return "CEPAS";
        }
    }

    public static Station getStation(String code) {
        if (code.length() != 3)
            return null;
        return StationTableReader.getStation(EZLINK_STR, Utils.byteArrayToInt(Utils.stringToByteArray(code)));
    }

    public static boolean check(Card card) {
        if (card instanceof CEPASCard) {
            CEPASCard cepasCard = (CEPASCard) card;
            return cepasCard.getHistory(3) != null
                    && cepasCard.getHistory(3).isValid()
                    && cepasCard.getPurse(3) != null
                    && cepasCard.getPurse(3).isValid();
        }

        return false;
    }

    public static TransitIdentity parseTransitIdentity(Card card) {
        String canNo = Utils.getHexString(((CEPASCard) card).getPurse(3).getCAN(), "<Error>");
        return new TransitIdentity(getCardIssuer(canNo), canNo);
    }

    @Override
    public String getCardName() {
        return getCardIssuer(mSerialNumber);
    }

    @Override
    @Nullable
    public TransitCurrency getBalance() {
        // This is stored in cents of SGD
        return new TransitCurrency((int) mBalance, "SGD");
    }


    @Override
    public String getSerialNumber() {
        return mSerialNumber;
    }

    @Override
    public Trip[] getTrips() {
        return mTrips;
    }

    private EZLinkTrip[] parseTrips(CEPASCard card) {
        List<CEPASTransaction> transactions = card.getHistory(3).getTransactions();
        if (transactions != null) {
            EZLinkTrip[] trips = new EZLinkTrip[transactions.size()];

            for (int i = 0; i < trips.length; i++)
                trips[i] = new EZLinkTrip(transactions.get(i), getCardName());

            return trips;
        }
        return new EZLinkTrip[0];
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(mSerialNumber);
        parcel.writeDouble(mBalance);

        parcel.writeInt(mTrips.length);
        parcel.writeTypedArray(mTrips, flags);
    }

}
