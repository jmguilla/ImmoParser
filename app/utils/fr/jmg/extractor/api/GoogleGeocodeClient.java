package utils.fr.jmg.extractor.api;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;

/*
 * A very basic implementation of geocode api client
 */
public class GoogleGeocodeClient {
    private static final String BASE_URL = "http://maps.googleapis.com/maps/api/geocode/";
    private static final String PARAM_LATLNG = "latlng=";
    private static final String PARAM_SENSOR = "sensor=false";
    private static final String PARAM_JSON = "json";
    private static final String PARAM_XML = "xml";

    /*
     * Returns a json node from the given lat long passed in
     */
    public final JSONObject getReverseGeocodeJson(BigDecimal lat, BigDecimal lng) throws IOException, JSONException{
        JsonBuilder builder = new JsonBuilder();
        URL url = new URL(BASE_URL+PARAM_JSON+"?"+PARAM_LATLNG+lat+","+lng+"&"+PARAM_SENSOR);
        System.out.println(url);
        return builder.buildJson(url.toExternalForm());
    }

    /*
     * Returns a xml node from the given lat long passed in
     */
    public final Document getReverseGeocodeXML(BigDecimal lat, BigDecimal lng) throws IOException{
      DocumentBuilder builder = new DocumentBuilder();
      URL url = new URL(BASE_URL+PARAM_XML+"?"+PARAM_LATLNG+lat+","+lng+"&"+PARAM_SENSOR);
      System.out.println(url);
      return builder.buildDocument(url.toExternalForm());
  }
}
