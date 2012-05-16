package utils.fr.jmg.extractor.api;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Hashtable;

import models.Description;

/*
 * Webparser interface. Specifies the methods to be implemented by the different modules responsible of the gathering of the different
 * announces on the different web sites.
 */
public interface WebParser {
    /*
     * Given different zip codes and a validity threshold for the price (to
     * avoid dummy announces), finds all announces available on the web site the
     * module is responsible for. The implementation can populate the output
     * parameter rejected as well with the announces not compliant to the
     * threshold for instance.
     */
    public void extractDescriptions(Type type, String[] zipCodes,
            BigDecimal validityThreshold,
            Hashtable<Type, ArrayList<Description>> result,
            Hashtable<Type, ArrayList<Description>> rejected)
            throws IOException;
}
