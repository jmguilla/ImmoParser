package utils.fr.jmg.extractor.api.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.Description;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import utils.fr.jmg.extractor.api.AbstractParser;
import utils.fr.jmg.extractor.api.DocumentBuilder;
import utils.fr.jmg.extractor.api.HTMLTags;
import utils.fr.jmg.extractor.api.Type;

public class AbritelParser extends AbstractParser {

	public static final String PREFIX_URL = "http://www.abritel.fr";
	public static final String BASE_URL = PREFIX_URL + "/search/refined/";
	public static final String URL_VAR_ZIPCODE = "keyword";
	public static final String URL_VAR_ROOMS = "chambres";
	public static final String URL_VAR_TYPE = "type+location:appartement";
	public static final String URL_VAR_PAGE = "page";
	public static final String URL_VAR_STUDIO = "studio";
    protected static final String REGEXP_PRICE = "([0-9 ]+,*[ 0-9]*)";
    protected static final String HTML_ANNOUNCE_MAPPROP_KEY = "ha.map.property.init";
    protected static final String REGEXP_MAPPROPS = HTML_ANNOUNCE_MAPPROP_KEY + "\\(\\{location: \\[\\{a:'([^']+)', b:'([^']+)'";
	protected static final String HTML_ANNOUNCES_LIST = "h3.listing-title > a";
    protected static final String HTML_ANNOUNCE_AUTH = "div.inquiry-form-sidebar > h2";
    protected static final String HTML_ANNOUNCE_SUMMARY = "div[id=summary-amenities] > ul > li.summary-list-item";
    protected static final String HTML_ANNOUNCE_AMENITIES = "div[id=amenities-container] > div";
    protected static final String HTML_ANNOUNCE_MIN_PRICE = "div.summary-list-item > span.rate";
    protected static final String HTML_ANNOUNCE_MAP = "div.map-box > script";
    protected static final String HTML_ANNOUNCE_TITLE = ".title";
    protected static final String HTML_ANNOUNCE_COUNT = ".count";
    protected static final String HTML_ANNOUNCE_FIRSTCOLUMN = ".firstColumn";
    protected static final String HTML_ANNOUNCE_COLUMN = ".column";
    protected static final String HTML_ANNOUNCE_TYPE_KEY = "Chambres";
    protected static final String HTML_ANNOUNCE_PEOPLE_KEY = "Personnes";
    protected static final String HTML_ANNOUNCE_AREA_KEY = "Superficie:";

	@Override
	protected ArrayList<String> extractAddresses(int nbRooms, String zipCode, int page) throws IOException {
		ArrayList<String> result = new ArrayList<String>();
		String url = buildURL(page, nbRooms, zipCode, page);
		System.out.println("Extracting addresses for url: " + url);
		Document doc = (new DocumentBuilder()).buildDocument(url);
		Elements links = doc.select(HTML_ANNOUNCES_LIST);
		if(links.size() > 0){
			for(Element element : links){
				result.add(PREFIX_URL + element.attr(HTMLTags.HREF.name));
			}
		}else{
			System.out.println("No announce found");
		}
		return result;
	}

	@Override
	protected Description extractDescription(String address, Type type,	String zipCode, BigDecimal validityThreshold) throws IOException {
        BigDecimal price = null, area = null, latitude = null, longitude = null;
        String city = zipCode, zc = null, author = null;
        boolean chargesIncluded = false, valid = false;
        Date creation = null;
        int nbPeople = -1;
	    Document doc = (new DocumentBuilder()).buildDocument(address);
	    //First, we look for the type of flat and the number of people
        Elements links = doc.select(HTML_ANNOUNCE_SUMMARY);
        for(Element link : links){
            for(Element span : link.select(HTMLTags.SPAN.name + HTML_ANNOUNCE_TITLE)){
                if(HTML_ANNOUNCE_TYPE_KEY.contains(span.ownText())){
                    //TODO we found the type of good
                }else if(HTML_ANNOUNCE_PEOPLE_KEY.contains(span.ownText())){
                    try{
                        nbPeople = Integer.parseInt(link.select(HTMLTags.SPAN.name + HTML_ANNOUNCE_COUNT).first().ownText());
                    }catch(Exception e){
                        System.err.println("Cannot compute number of people for " + address);
                        e.printStackTrace();
                    }
                }
            }
        }

        //Then, the author
        try{
            author = doc.select(HTML_ANNOUNCE_AUTH).first().ownText();
        }catch(Exception e){
            System.err.println("Cannot compute author for " + address);
            e.printStackTrace();
        }

        //Now the area
        links = doc.select(HTML_ANNOUNCE_AMENITIES);
        for(Element link : links){
            for(Element span : link.select(HTMLTags.SPAN.name + HTML_ANNOUNCE_FIRSTCOLUMN)){
                if(HTML_ANNOUNCE_AREA_KEY.contains(span.ownText())){
                    String tmp = link.select(HTMLTags.SPAN.name + HTML_ANNOUNCE_COLUMN).first().children().first().children().first().ownText();
                    Pattern p = Pattern.compile(REGEXP_PRICE);
                    Matcher matcher = p.matcher(tmp);
                    if(matcher.find()){
                        area = new BigDecimal(matcher.group(1).trim());
                    }
                }
            }
        }

        //The minimum price
        try{
            String tmp = doc.select(HTML_ANNOUNCE_MIN_PRICE).first().ownText();
            Pattern p = Pattern.compile(REGEXP_PRICE);
            Matcher matcher = p.matcher(tmp);
            if(matcher.find()){
                price = new BigDecimal(matcher.group(1).trim());
            }
            if(validityThreshold == null || validityThreshold.compareTo(price) <= 0){
                valid = true;
            }
        }catch(Exception e){
            System.err.println("Cannot compute min price for " + address);
            e.printStackTrace();
        }

        //The longitude & latitude
        try{
            String tmp = doc.select(HTML_ANNOUNCE_MAP).first().data();
            Pattern p = Pattern.compile(REGEXP_MAPPROPS);
            Matcher matcher = p.matcher(tmp);
            if(matcher.find()){
                latitude = new BigDecimal(URLDecoder.decode(matcher.group(1), "UTF-8").trim());
                longitude = new BigDecimal(URLDecoder.decode(matcher.group(2), "UTF-8").trim());
            }
        }catch(Exception e){
            System.err.println("Cannot compute lat & long for " + address);
            e.printStackTrace();
        }
        
        return new Description(type, price, zc, null, city, latitude, longitude, address, valid, area, creation, author, false);
    }

	/*
	 * Builds the url for the parsing
	 */
	protected String buildURL(int page, int nbRooms, String zipCode, int page2) {
		String nbChambre = null;
		if(nbRooms <= 1){
			nbChambre = URL_VAR_STUDIO;
		}else{
			nbChambre = new Integer(nbRooms - 1).toString();
		}
		return BASE_URL + URL_VAR_PAGE	+ ":" + page + "/" + URL_VAR_ROOMS + ":" + nbChambre +
				"/" + URL_VAR_TYPE + "/" + URL_VAR_ZIPCODE + ":" + zipCode;
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		AbritelParser parser = new AbritelParser();
		Description desc = parser.extractDescription("http://localhost:8080/test/announce-abritel-st.html", Type.STUDIO, "cannes", new BigDecimal(100));
		System.out.println(desc);
	}
}
