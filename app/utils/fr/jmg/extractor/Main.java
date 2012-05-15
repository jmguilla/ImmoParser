package utils.fr.jmg.extractor;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Hashtable;

import models.Description;
import utils.fr.jmg.extractor.api.DAOManager;
import utils.fr.jmg.extractor.api.Type;
import utils.fr.jmg.extractor.api.WebParser;
import utils.fr.jmg.extractor.api.impl.AbritelParser;

/*
 * Class used to launch the extration process
 */
public class Main {

	private static final int VALIDITY_THRESHOLD = 50;

	/*
	 * main method to launch the extraction process
	 */
	public static void main(String[] args) throws IOException {
		long startTime = System.currentTimeMillis();
		//should be done thanks to injection
		WebParser[] parsers = new WebParser[]{new AbritelParser()};
		//should be done thanks to injection
		String[] zipCodes = new String[]{"Cannes"};
		System.out.print("******************** Starting extraction for ");
		for(String zipCode : zipCodes){
			System.out.print(zipCode + " ");
		}
		System.out.println("********************");
		DAOManager daoManager = new DAOManager();
		try{
			for(WebParser parser : parsers){
				Hashtable<Type, ArrayList<Description>> rejected = new Hashtable<Type, ArrayList<Description>>();
				Hashtable<Type, ArrayList<Description>> result = new Hashtable<Type, ArrayList<Description>>();
				//				for(Type type : Type.getValidTypes()){
				Type type = Type.getType(1);
				parser.extractDescriptions(type, zipCodes, new BigDecimal(VALIDITY_THRESHOLD), result, rejected);
				printRejectStats(rejected);
				printValidStats(result);
				long startPersist = System.currentTimeMillis();
				System.out.println("******************** Persisting results ********************");
				persist(result, daoManager);
				persist(rejected, daoManager);
				System.out.println("******************** Persisting donne in " + (System.currentTimeMillis() - startPersist) +"ms ********************");
				System.out.println("******************** Extraction processed in " + (System.currentTimeMillis() - startTime) + "ms ********************");
				//				}
			}
		}finally{
			daoManager.close();
		}
	}

	private static void persist(Hashtable<Type, ArrayList<Description>> result, DAOManager daoManager) {
		for(Type type : result.keySet()){
			for(Description description : result.get(type)){
				try {
					daoManager.getDAO(description).save(description);
					Description previous = null;
//					if((previous = Description.find.byId(description.url)) == null){
//						description.save();
//					}else{
//						System.out.println(previous + " already in db");
//					}
				} catch (IOException e) {
					System.err.println("Exception occurred while saving " + description);
					e.printStackTrace();
				}
			}
		}
	}

	private static void printValidStats(Hashtable<Type, ArrayList<Description>> result) {

	}

	private static void printRejectStats(Hashtable<Type, ArrayList<Description>> result) {

	}
}
