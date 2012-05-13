package utils.fr.jmg.extractor.api.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.Description;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import utils.fr.jmg.extractor.api.HTMLTags;
import utils.fr.jmg.extractor.api.Type;
import utils.fr.jmg.extractor.api.WebParser;

public class LongTermLeBonCoinParser implements WebParser{

	protected String BASE_URL = "http://www.leboncoin.fr/locations/offres/";
	protected static final String URL_VAR_PAGE = "o";
	protected static final String URL_VAR_MIN_ROOMS = "roe";
	protected static final String URL_VAR_MAX_ROOMS = "ros";
	protected static final String URL_VAR_ZIPCODE = "location";
	protected static final String HTML_ANNOUNCES_LIST = "div.list-ads > a";
	protected static final String HTML_ANNOUNCE_DESC = "div.lbcParams > table > tbody > *";
	protected static final String HTML_ANNOUNCE_AUTH = "div.upload_by";
	protected static final String HTML_ANNOUNCE_KEY = "th";
	protected static final String HTML_ANNOUNCE_VALUE = "td";
	protected static final String HTML_ANNOUNCE_PRICE_KEY = "Loyer";
	protected static final String HTML_ANNOUNCE_CITY_KEY = "Ville";
	protected static final String HTML_ANNOUNCE_ZC_KEY = "Code postal";
	protected static final String HTML_ANNOUNCE_CHARGE_KEY = "Charges comprises";
	protected static final String HTML_ANNOUNCE_CHARGE_OK_VALUE = "Oui";
	protected static final String HTML_ANNOUNCE_SUPERTYPE_KEY = "Type de bien";
	protected static final String HTML_ANNOUNCE_TYPE_KEY = "Pi&egrave;ces";
	protected static final String HTML_ANNOUNCE_AREA_KEY = "Surface";
	protected static final String REGEXP_PRICE = "([0-9 ]+,*[ 0-9]*)";
	protected static final String REGEXP_NB_ROOMS = "([0-9]+)";
	protected static final String REGEXP_NB_DATE = "([0-9]+[ ]*[^ ]+)";
	protected static final String DEFAULT_STRING = "NOT FOUND";
	protected static final int DEFAULT_PRICE = 1;
	protected static final int CIRCUIT_BREAKER_PACE = 1000;
	protected static final int THRESHOLD_INIT = 5;
	protected int circuitBreakerThreshold = THRESHOLD_INIT;

	/*
	 * Extracts a description a an announce at the given url
	 */
	protected Description extractDescription(String url, Type type, String zipCode, BigDecimal validityThreshold) throws IOException{
		Document doc = buildDocument(url);
		Elements links = doc.select(HTML_ANNOUNCE_DESC);
		//We found a matching announce
		//We are first looking for the description of the flat
		if(links.size() > 0){
			BigDecimal price = null, area = null;
			String city = null, zc = null, author = null;
			boolean chargesIncluded = false, valid = false;
			Date creation = null;
			for(Element element : links){
				//setting default value for key and value if not found
				Element key = element.select(HTML_ANNOUNCE_KEY).first();
				String keyString = (key==null?DEFAULT_STRING:key.ownText());
				Element value = element.select(HTML_ANNOUNCE_VALUE).first();
				String valueString = (value==null?DEFAULT_STRING:value.ownText());
				if(keyString.contains(HTML_ANNOUNCE_CHARGE_KEY)){
					//we found the charges included key
					if(valueString.contains(HTML_ANNOUNCE_CHARGE_OK_VALUE)){
						chargesIncluded = true;
					}
				}else if(keyString.contains(this.getPriceKey())){
					//we found the price key
					try{
						Pattern p = Pattern.compile(REGEXP_PRICE);
						Matcher matcher = p.matcher(value.select(HTMLTags.SPAN.name).first().ownText());
						if(matcher.find()){
							price = new BigDecimal(matcher.group(1).replace(" ", ""));
							continue;
						}
					}catch(Exception e){
						System.err.println("Exception while computing price of " + url);
						e.printStackTrace();
					}
				}else if(keyString.contains(HTML_ANNOUNCE_CITY_KEY)){
					//we found the city key
					city = valueString.trim();
				}else if(keyString.contains(HTML_ANNOUNCE_ZC_KEY)){
					//we found the zip code key
					zc = valueString.trim();
				}else if(keyString.contains(HTML_ANNOUNCE_SUPERTYPE_KEY)){
					//we found the supertype
					//TODO ignoring at the moment
				}else if(keyString.contains(HTML_ANNOUNCE_TYPE_KEY)){
					//we found the number of rooms
					//TODO ignoring at the moment
				}else if(keyString.contains(HTML_ANNOUNCE_AREA_KEY)){
					//we found the area of the appartment
					Pattern p = Pattern.compile(REGEXP_PRICE);
					Matcher matcher = p.matcher(valueString);
					if(matcher.find()){
						area = new BigDecimal(matcher.group(1));
					}
				}
			}
			zc = (zc==null?zipCode:zc);
			price = (price==null?new BigDecimal(DEFAULT_PRICE):price);

			//We can now look for the author and the creation date
			links = doc.select(HTML_ANNOUNCE_AUTH);
			if(links.size() > 0){
				try{
					author = links.first().select(HTMLTags.A.name).first().ownText();
				}catch(Exception e){
					System.err.println("Exception occurred while defining author of " + url);
					e.printStackTrace();
				}
				try{
					Pattern p = Pattern.compile(REGEXP_NB_DATE);
					Matcher matcher = p.matcher(links.first().ownText());
					if(matcher.find()){
						SimpleDateFormat sdf = new SimpleDateFormat("dd MMMMM yyyy");
						String date = matcher.group(1).replace("&eacute;", "e") + " " + Calendar.getInstance().get(Calendar.YEAR);
						Date tmp = sdf.parse(date);
						if(tmp.after(Calendar.getInstance().getTime())){
							date = matcher.group(1).replace("&eacute;", "e") + " " + (Calendar.getInstance().get(Calendar.YEAR) - 1);
						}
						creation = sdf.parse(date);
					}
				}catch(Exception e){
					System.err.println("Exception occurred while defining creation of " + url);
					e.printStackTrace();
				}
			}
			if(validityThreshold != null && validityThreshold.compareTo(price) <= 0){
				valid = true;
			}
			return new Description(type, price, zc, null, city, null, null, url, valid, area, creation, author, false);
		}
		return null;
	}

	protected String getPriceKey() {
		return HTML_ANNOUNCE_PRICE_KEY;
	}

	/*
	 * Extract the different addresses of announces on a given page
	 */
	protected ArrayList<String> extractAddresses(int nbRooms, String zipCode, int page) throws IOException {
		ArrayList<String> result = new ArrayList<String>();
		String url = buildURL(page, nbRooms, zipCode, page);
		System.out.println("Extracting addresses for url: " + url);
		Document doc = buildDocument(url);
		Elements links = doc.select(HTML_ANNOUNCES_LIST);
		if(links.size() > 0){
			for(Element element : links){
				result.add(element.attr(HTMLTags.HREF.name));
			}
		}else{
			System.out.println("No announce found");
		}
		return result;
	}

	/*
	 * Builds the document to the given url
	 */
	protected final Document buildDocument(String url) throws IOException{
		Document doc = null;
		while(doc == null){
			try{
				doc = Jsoup.connect(url).get();
				circuitBreakerThreshold = THRESHOLD_INIT;
			}catch(IOException e){
				if(circuitBreakerThreshold <= 0){
					throw e;
				}else{
					System.err.println(e.getClass() + " caught while processing document building");
					circuitBreakerThreshold--;
					try {
						Thread.sleep(CIRCUIT_BREAKER_PACE);
					} catch (InterruptedException e1) {
						throw new IOException(e1);
					}
				}
			}
		}
		return doc;
	}

	/*
	 * Builds the url for the parsing
	 */
	protected String buildURL(int page, int nbRooms, String zipCode, int page2) {
		return BASE_URL+ "?" + URL_VAR_PAGE	+ "=" + page + "&" + URL_VAR_MIN_ROOMS + "=" + nbRooms +
				"&" + URL_VAR_MAX_ROOMS + "=" + nbRooms + "&" + URL_VAR_ZIPCODE + "=" + zipCode;
	}

	@Override
	public final void extractDescriptions(
			Type inType, String[] zipCodes, BigDecimal validityThreshold,
			Hashtable<Type, ArrayList<Description>> result,
			Hashtable<Type, ArrayList<Description>> rejected)
					throws IOException {
		//We process every zip codes passed in
		for(String zipCode : zipCodes){
			try{
				//We process every number of rooms
				if(inType == null){
					for(Type type : Type.getValidTypes()){
						populateDescription(type, zipCode, validityThreshold, result, rejected);
					}
				}else{
					populateDescription(inType, zipCode, validityThreshold, result, rejected);
				}
				circuitBreakerThreshold = THRESHOLD_INIT;
			}catch(IOException e){
				if(circuitBreakerThreshold <= 0){
					throw e;
				}else{
					System.err.println(e.getClass() + " caught while processing document building");
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

	/*
	 * Populates result and rejected thanks to the passed in parameters
	 */
	private void populateDescription(
			Type type, String zipCode, BigDecimal validityThreshold,
			Hashtable<Type, ArrayList<Description>> result,
			Hashtable<Type, ArrayList<Description>> rejected) throws IOException{
		//We first create the resulting list
		if(result.get(type) == null){
			result.put(type, new ArrayList<Description>());
		}
		if(rejected != null && rejected.get(type) == null){
			rejected.put(type, new ArrayList<Description>());
		}
		//We process every pages
		for(int pageNumber = 1; pageNumber < Integer.MAX_VALUE; pageNumber++){
			ArrayList<String> addresses = this.extractAddresses(type.getNbRooms(), zipCode, pageNumber);
			if(addresses == null || addresses.size() <= 0){
				break;
			}
			for(String address : addresses){
				System.out.print(address + " ");
				Description desc = this.extractDescription(address, type, zipCode, validityThreshold);
				//Either result or reject
				if(desc != null){
					//Valid result
					if(desc.valid){
						System.out.println("OK Desc " + desc);
						result.get(desc.type).add(desc);
					}else{
						System.out.println("KO Desc " + desc);
						rejected.get(desc.type).add(desc);
					}
				}else{
					System.out.println("KO URL " + address);
				}
			}
		}
	}
}
