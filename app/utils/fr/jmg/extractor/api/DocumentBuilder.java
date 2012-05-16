package utils.fr.jmg.extractor.api;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class DocumentBuilder {

    private static final int CIRCUIT_BREAKER_PACE = 1000;
    private static final int THRESHOLD_INIT = 5;
    private int circuitBreakerThreshold = THRESHOLD_INIT;

    public DocumentBuilder() {
    }

    /*
     * Builds the document to the given url
     */
    public final Document buildDocument(String url) throws IOException {
        Document doc = null;
        while (doc == null) {
            try {
                doc = Jsoup.connect(url).followRedirects(false).get();
                circuitBreakerThreshold = THRESHOLD_INIT;
            } catch (IOException e) {
                if (circuitBreakerThreshold <= 0) {
                    throw e;
                } else {
                    System.err.println(e.getClass()
                            + " caught while processing document building");
                    e.printStackTrace();
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
