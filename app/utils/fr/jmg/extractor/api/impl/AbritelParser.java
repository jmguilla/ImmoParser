package utils.fr.jmg.extractor.api.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

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
	protected static final String HTML_ANNOUNCES_LIST = "h3.listing-title > a";

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
	protected Description extractDescription(String address, Type type,
			String zipCode, BigDecimal validityThreshold) throws IOException {
		// TODO Auto-generated method stub
		return null;
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
		for(String s : parser.extractAddresses(1, "cannes", 1)){
			System.out.println(s);
		}
	}
}
