package utils.fr.jmg.extractor.api;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.concurrent.locks.ReentrantLock;

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
	private static long LAST_GEOCODE_CALL = System.currentTimeMillis();
	private static final long GEOCODE_TEMPO_PACE = 1500;
	private static final ReentrantLock GEOCODE_LOCL = new ReentrantLock();

	/*
	 * Returns a json node from the given lat long passed in
	 */
	public final JSONObject getReverseGeocodeJson(BigDecimal lat, BigDecimal lng) throws IOException, JSONException{
		JsonBuilder builder = new JsonBuilder();
		synchronized (GEOCODE_LOCL) {
			long tempo = System.currentTimeMillis() - LAST_GEOCODE_CALL - GEOCODE_TEMPO_PACE;
			if(tempo > 0L){
				System.out.println("Tempo for geocode: " + tempo);
				try {
					Thread.sleep(tempo);
				} catch (InterruptedException e) {
					throw new IOException(e);
				}
			}
			URL url = new URL(BASE_URL+PARAM_JSON+"?"+PARAM_LATLNG+lat+","+lng+"&"+PARAM_SENSOR);
			System.out.println("GEOCODE call to " + url.toExternalForm());
			JSONObject result = builder.buildJson(url.toExternalForm());
			LAST_GEOCODE_CALL = System.currentTimeMillis();
			return result;
		}
	}

	/*
	 * Returns a xml node from the given lat long passed in
	 */
	public final Document getReverseGeocodeXML(BigDecimal lat, BigDecimal lng) throws IOException{
		DocumentBuilder builder = new DocumentBuilder();
		synchronized (GEOCODE_LOCL) {
			long tempo = System.currentTimeMillis() - LAST_GEOCODE_CALL - GEOCODE_TEMPO_PACE;
			if(tempo > 0L){
				System.out.println("Tempo for geocode: " + tempo);
				try {
					Thread.sleep(tempo);
				} catch (InterruptedException e) {
					throw new IOException(e);
				}
			}
			URL url = new URL(BASE_URL+PARAM_XML+"?"+PARAM_LATLNG+lat+","+lng+"&"+PARAM_SENSOR);
			System.out.println("GEOCODE call to " + url.toExternalForm());
			Document result = builder.buildDocument(url.toExternalForm());
			LAST_GEOCODE_CALL = System.currentTimeMillis();
			return result;
		}
	}
}
