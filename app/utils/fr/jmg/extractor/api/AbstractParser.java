package utils.fr.jmg.extractor.api;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Hashtable;

import models.Description;

public abstract class AbstractParser implements WebParser{

	protected static final int CIRCUIT_BREAKER_PACE = 1000;
	protected static final int THRESHOLD_INIT = 5;
	protected int circuitBreakerThreshold = THRESHOLD_INIT;

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
	protected void populateDescription(
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

	/*
	 * Extracts a description a an announce at the given url
	 */
	protected abstract Description extractDescription(String address, Type type,
			String zipCode, BigDecimal validityThreshold) throws IOException;

	/*
	 * Extract the different addresses of announces on a given page
	 */
	protected abstract ArrayList<String> extractAddresses(int nbRooms, String zipCode, int pageNumber) throws IOException;
}
