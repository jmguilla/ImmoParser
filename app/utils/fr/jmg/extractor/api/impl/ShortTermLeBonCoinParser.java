package utils.fr.jmg.extractor.api.impl;

public class ShortTermLeBonCoinParser extends LongTermLeBonCoinParser {

    protected String BASE_URL = "http://www.leboncoin.fr/locations_de_vacances/offres/";
    protected static final String HTML_ANNOUNCE_PRICE_KEY = "Prix";

    @Override
    protected String buildURL(int page, int nbRooms, String zipCode, int page2) {
        return BASE_URL + "?" + URL_VAR_PAGE + "=" + page + "&"
                + URL_VAR_MIN_ROOMS + "=" + nbRooms + "&" + URL_VAR_MAX_ROOMS
                + "=" + nbRooms + "&" + URL_VAR_ZIPCODE + "=" + zipCode;
    }

    @Override
    protected String getPriceKey() {
        return HTML_ANNOUNCE_PRICE_KEY;
    }
}
