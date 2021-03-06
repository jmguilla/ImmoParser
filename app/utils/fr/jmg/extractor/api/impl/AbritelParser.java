package utils.fr.jmg.extractor.api.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.Description;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import utils.fr.jmg.extractor.api.AbstractParser;
import utils.fr.jmg.extractor.api.DocumentBuilder;
import utils.fr.jmg.extractor.api.GoogleGeocodeClient;
import utils.fr.jmg.extractor.api.HTMLTags;
import utils.fr.jmg.extractor.api.Type;

public class AbritelParser extends AbstractParser {

    public static final String PREFIX_URL = "http://www.abritel.fr";
    public static final String BASE_URL = PREFIX_URL
            + "/search/refined/monde/region:1/";
    public static final String URL_VAR_ZIPCODE = "keyword";
    public static final String URL_VAR_ROOMS = "chambres";
    public static final String URL_VAR_TYPE = "type+location:appartement";
    public static final String URL_VAR_PAGE = "page";
    public static final String URL_VAR_STUDIO = "studio";
    protected static final String REGEXP_PRICE = "([ 0-9]+,*[ 0-9]*)";
    protected static final String HTML_ANNOUNCE_MAPPROP_KEY = "ha.map.property.init";
    protected static final String REGEXP_MAPPROPS = HTML_ANNOUNCE_MAPPROP_KEY
            + "\\(\\{location: \\[\\{a:'([^']+)', b:'([^']+)'";
    protected static final String HTML_ANNOUNCES_LIST = "h3.listing-title > a";
    protected static final String HTML_ANNOUNCE_AUTH = "div.inquiry-form-sidebar > h2";
    protected static final String HTML_ANNOUNCE_AUTH2 = "div.contact-info > h3";
    protected static final String HTML_ANNOUNCE_SUMMARY = "div[id=summary-amenities] > ul > li.summary-list-item";
    protected static final String HTML_ANNOUNCE_AMENITIES = "div[id=amenities-container] > div";
    protected static final String HTML_ANNOUNCE_PRICE_TAB = "tbody.ratesBody > tr";
    protected static final String HTML_ANNOUNCE_PERIOD_BEGIN = "td.periodBegin";
    protected static final String HTML_ANNOUNCE_PERIOD_END = "td.periodEnd";
    protected static final String HTML_ANNOUNCE_EXCHANGE = "td.exchange";
    protected static final String HTML_ANNOUNCE_PERIOD = ".period";
    protected static final String HTML_ANNOUNCE_WEEK_KEY = "semaine";
    protected static final String HTML_ANNOUNCE_MIN_PRICE = ".rate";
    protected static final String HTML_ANNOUNCE_MAP = "div.map-box > script";
    protected static final String HTML_ANNOUNCE_TITLE = ".title";
    protected static final String HTML_ANNOUNCE_COUNT = ".count";
    protected static final String HTML_ANNOUNCE_FIRSTCOLUMN = ".firstColumn";
    protected static final String HTML_ANNOUNCE_COLUMN = ".column";
    protected static final String HTML_ANNOUNCE_TYPE_KEY = "Chambres";
    protected static final String HTML_ANNOUNCE_PEOPLE_KEY = "Personnes";
    protected static final String HTML_ANNOUNCE_AREA_KEY = "Superficie:";
    protected static final String UTF_8 = "UTF-8";
    protected static final String GEO_STATUS_OK = "OK";
    protected static final String GEO_STATUS_KEY = "status";
    protected static final String GEO_RESULTS_KEY = "results";
    protected static final String GEO_FORMATTED_KEY = "formatted_address";
    protected static final String GEO_COMPONENTS_KEY = "address_components";
    protected static final String GEO_ZIP_KEY = "postal_code";
    protected static final String GEO_ADDRESS_KEY = "route";
    protected static final String GEO_CITY_KEY = "locality";
    protected static final String GEO_NAME_KEY = "long_name";
    protected static final String GEO_TYPE_KEY = "types";
    private static final int CIRCUIT_BREAKER_PACE = 2000;
    private static final int THRESHOLD_INIT = 5;
    private int circuitBreakerThreshold = THRESHOLD_INIT;
    private static final String DATE_FORMAT = "dd MMM yyyy";

    @Override
    protected ArrayList<String> extractAddresses(int nbRooms, String zipCode,
            int page) throws IOException {
        ArrayList<String> result = new ArrayList<String>();
        String url = buildURL(page, nbRooms, zipCode);
        System.out.println("Extracting addresses for url: " + url);
        Document doc = (new DocumentBuilder()).buildDocument(url);
        Elements links = doc.select(HTML_ANNOUNCES_LIST);
        if (links.size() > 0) {
            for (Element element : links) {
                result.add(PREFIX_URL + element.attr(HTMLTags.HREF.name));
            }
        } else {
            System.out.println("No announce found");
        }
        return result;
    }

    @Override
    protected Description extractDescription(String address, Type type,
            String cityName, BigDecimal validityThreshold) throws IOException {
        // TODO doesn't accept zipCode as input but city name...
        BigDecimal price = null, area = null, latitude = null, longitude = null;
        String route = null, city = cityName, zc = cityName, author = null, formattedAddress = null;
        boolean chargesIncluded = false, valid = false;
        Date creation = null;
        int nbPeople = -1;
        Document doc = (new DocumentBuilder()).buildDocument(address);
        //***********************************************************************//
        //                      Type of flat, nb people                          //
        //***********************************************************************//
        Elements links = doc.select(HTML_ANNOUNCE_SUMMARY);
        for (Element link : links) {
            for (Element span : link.select(HTMLTags.SPAN.name
                    + HTML_ANNOUNCE_TITLE)) {
                if (HTML_ANNOUNCE_TYPE_KEY.contains(span.ownText())) {
                    // TODO we found the type of good
                } else if (HTML_ANNOUNCE_PEOPLE_KEY.contains(span.ownText())) {
                    try {
                        // TODO we found the number of people who can live in
                        // the appartment
                        nbPeople = Integer.parseInt(link
                                .select(HTMLTags.SPAN.name
                                        + HTML_ANNOUNCE_COUNT).first()
                                        .ownText());
                    } catch (Exception e) {
                        System.err
                        .println("Cannot compute number of people for "
                                + address);
                        e.printStackTrace();
                    }
                }
            }
        }

        //***********************************************************************//
        //                                Author                                 //
        //***********************************************************************//
        try {
            author = doc.select(HTML_ANNOUNCE_AUTH).first().ownText();
        } catch (Exception e) {
            // fallback http://www.abritel.fr/location-vacances/p109695t
            System.out.println("Fallback behavior for author computation for: "
                    + address);
            try {
                author = doc.select(HTML_ANNOUNCE_AUTH2).first().ownText();
            } catch (Exception eBis) {
                System.err.println("Cannot compute author for " + address);
                e.printStackTrace();
                eBis.printStackTrace();
            }
        }

        //***********************************************************************//
        //                                 Area                                  //
        //***********************************************************************//
        links = doc.select(HTML_ANNOUNCE_AMENITIES);
        for (Element link : links) {
            for (Element span : link.select(HTMLTags.SPAN.name
                    + HTML_ANNOUNCE_FIRSTCOLUMN)) {
                if (HTML_ANNOUNCE_AREA_KEY.contains(span.ownText())) {
                    String tmp = link
                            .select(HTMLTags.SPAN.name + HTML_ANNOUNCE_COLUMN)
                            .first().children().first().children().first()
                            .ownText();
                    Pattern p = Pattern.compile(REGEXP_PRICE);
                    Matcher matcher = p.matcher(tmp);
                    if (matcher.find()) {
                        area = new BigDecimal(matcher.group(1).trim());
                    }
                }
            }
        }

        //***********************************************************************//
        //                  The minimum price wreathed                           //
        //***********************************************************************//
        try {
            Hashtable<Rate, ArrayList<String>> rates = new Hashtable<Rate, ArrayList<String>>();
            for (Element element : doc.select(HTML_ANNOUNCE_PRICE_TAB)) {
                String begin = null, end = null;
                Rate rate = null;
                BigDecimal bg = null;
                //The beginning of the period
                Element beginning = element.select(HTML_ANNOUNCE_PERIOD_BEGIN).first();
                if(beginning == null){
                    throw new Exception("Missing tag " + HTML_ANNOUNCE_PERIOD_BEGIN);
                }else{
                    begin = beginning.ownText();
                }

                //The ending of the period
                Element ending = element.select(HTML_ANNOUNCE_PERIOD_END).first();
                if(ending == null){
                    throw new Exception("Missing tag " + HTML_ANNOUNCE_PERIOD_END);
                }else{
                    end = ending.ownText();
                }

                //The price
                Elements exchanges = element.select(HTML_ANNOUNCE_EXCHANGE);
                for(int i = 0;i < exchanges.size(); i++){
                    Pattern p = Pattern.compile(REGEXP_PRICE);
                    //replaces non-breaking spaces in text
                    Matcher matcher = p.matcher(exchanges.get(i).ownText().replace(new Character((char)0x00A0).toString(), ""));
                    if (matcher.find()) {
                        bg = new BigDecimal(matcher.group(1).trim());
                        if(i == 0){
                            rate = new Rate(Rate.Type.SEMAINE, bg);
                        }else{
                            rate = new Rate(Rate.Type.NUITE, bg);
                        }
                        ArrayList<String> tmp = new ArrayList<String>();
                        tmp.add(begin);
                        tmp.add(end);
                        rates.put(rate, tmp);
                        break;
                    }
                    if(i >= 4) break;//end of week/nuitee period
                }
            }
            price = computeWreathedPrice(rates).divide(new BigDecimal(nbPeople), RoundingMode.HALF_UP);
            if(validityThreshold == null || price.compareTo(validityThreshold) >= 0){
            	valid = true;
            }
        } catch (Exception e) {
            System.err.println("Cannot compute wreathed price for " + address);
            e.printStackTrace();
        }

        //***********************************************************************//
        //                        Latitude & Longitude                           //
        //***********************************************************************//
        try {
            String tmp = doc.select(HTML_ANNOUNCE_MAP).first().data();
            Pattern p = Pattern.compile(REGEXP_MAPPROPS);
            Matcher matcher = p.matcher(tmp);
            if (matcher.find()) {
                latitude = new BigDecimal(URLDecoder.decode(matcher.group(1),
                        UTF_8).trim());
                longitude = new BigDecimal(URLDecoder.decode(matcher.group(2),
                        UTF_8).trim());

                // We have the lat/lng couple, we can compute the address thx to
                // reverse geocode
                GoogleGeocodeClient geocodeClient = new GoogleGeocodeClient();
                while (true) {
                    JSONObject json = geocodeClient.getReverseGeocodeJson(
                            latitude, longitude);
                    if (GEO_STATUS_OK.equalsIgnoreCase(json
                            .getString(GEO_STATUS_KEY))) {
                        // The result is correct
                        JSONArray results = json.getJSONArray(GEO_RESULTS_KEY);
                        // results[0] = route + formatted address
                        // results[1] = zip code
                        // results[2] = city
                        // results[3] = dept
                        // results[4] = region
                        // results[5] = country
                        JSONArray components = results.getJSONObject(0)
                                .getJSONArray(GEO_COMPONENTS_KEY);
                        formattedAddress = results.getJSONObject(0).getString(
                                GEO_FORMATTED_KEY);
                        for (int i = 0; i < components.length(); i++) {
                            JSONObject component = components.getJSONObject(i);
                            String typeValue = component.getJSONArray(
                                    GEO_TYPE_KEY).getString(0);
                            if (typeValue != null) {
                                if (typeValue.contains(GEO_ADDRESS_KEY)) {
                                    route = component.getString(GEO_NAME_KEY);
                                } else if (typeValue.contains(GEO_CITY_KEY)) {
                                    city = component.getString(GEO_NAME_KEY);
                                } else if (typeValue.contains(GEO_ZIP_KEY)) {
                                    zc = component.getString(GEO_NAME_KEY);
                                }
                            }
                        }
                        circuitBreakerThreshold = THRESHOLD_INIT;
                        break;
                    } else {
                        if (circuitBreakerThreshold <= 0) {
                            circuitBreakerThreshold = THRESHOLD_INIT;
                            System.err
                            .println("Cannot compute reverse geocode: "
                                    + json.getString(GEO_STATUS_KEY));
                            break;
                        } else {
                            System.err
                            .println("Reverse geocode result (circuit broker): "
                                    + json.getString(GEO_STATUS_KEY));
                            circuitBreakerThreshold--;
                            try {
                                Thread.sleep(CIRCUIT_BREAKER_PACE);
                            } catch (InterruptedException e1) {
                                throw new IOException(e1);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Cannot compute lat & long for " + address);
            e.printStackTrace();
        }

        return new Description(type, price, zc, route, city, formattedAddress,
                latitude, longitude, address, valid, area, creation, author,
                false, nbPeople);
    }

    /*
     * Compute the wreathed price for an annouce
     */
    private BigDecimal computeWreathedPrice(Hashtable<Rate, ArrayList<String>> rates) {
        BigDecimal result = new BigDecimal(0);
        int nbDays = 0;
        for(Rate rate : rates.keySet()){
            BigDecimal tmpRate = null;
            ArrayList<String> dates = rates.get(rate);
            if(dates.size() < 2) continue;
            String begin = dates.get(0), end = dates.get(1);
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
            Date beginning, ending;
            try {
                beginning = sdf.parse(begin);
                ending = sdf.parse(end);
            } catch (ParseException e) {
                System.err.println("Cannot compute date for rate: " + rate.rate + " - " + begin + " - " + end);
                e.printStackTrace();
                continue;
            }
            if(rate.type == Rate.Type.SEMAINE){
                tmpRate = rate.rate.divide(new BigDecimal(7), RoundingMode.HALF_UP);
            }else{
                tmpRate = rate.rate;
            }
            long difference = (ending.getTime() - beginning.getTime()) / 1000 / 60 / 60 / 24;
            result = result.add(tmpRate.multiply(new BigDecimal(difference)));
            nbDays += difference;
        }
        if(nbDays != 0){
            return result.divide(new BigDecimal(nbDays), RoundingMode.HALF_UP);
        }
        return result; 
    }

    /*
     * Builds the url for the parsing
     */
    protected String buildURL(int page, int nbRooms, String zipCode) {
        String nbChambre = null;
        if (nbRooms <= 1) {
            nbChambre = URL_VAR_STUDIO;
        } else {
            nbChambre = new Integer(nbRooms - 1).toString();
        }
        String result = BASE_URL + URL_VAR_ROOMS + ":" + nbChambre + "/"
                + URL_VAR_ZIPCODE + ":" + zipCode + "/" + URL_VAR_TYPE;
        if (page != 1) {
            result += "/" + URL_VAR_PAGE + ":" + page;
        }
        return result;
    }

    /**
     * @param args
     * @throws IOException
     * @throws JSONException
     */
    public static void main(String[] args) throws IOException, JSONException {
        AbritelParser parser = new AbritelParser();
        // Description desc =
        // parser.extractDescription("http://www.abritel.fr/location-vacances/p872693a",
        // Type.STUDIO, "cannes", new BigDecimal(100));
        Description desc = parser.extractDescription(
                "http://www.abritel.fr/location-vacances/p662592",
                Type.STUDIO, "cannes", new BigDecimal(100));
        System.out.println(desc);
        System.out.println(desc.author);
        System.out.println(desc.address);
        System.out.println(desc.zipCode);
        System.out.println(desc.city);
        System.out.println(desc.latitude);
        System.out.println(desc.longitude);
        System.out.println(desc.formattedAddress);
        System.out.println(desc.price);
    }

    /*
     * Private nested class for rate computation purposes
     */
    private static class Rate{
        static enum Type{
            NUITE, SEMAINE;
        }

        private final Type type;
        private final BigDecimal rate;
        Rate(Type type, BigDecimal rate){
            this.type = type;
            this.rate = rate;
        }
    }
}
