package utils.fr.jmg.extractor.api;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class DocumentBuilder {

	protected static final int CIRCUIT_BREAKER_PACE = 1000;
	protected static final int THRESHOLD_INIT = 5;
	protected int circuitBreakerThreshold = THRESHOLD_INIT;

	public DocumentBuilder(){}

	/*
	 * Builds the document to the given url
	 */
	public final Document buildDocument(String url) throws IOException{
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

}
