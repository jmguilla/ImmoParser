package utils.fr.jmg.extractor.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;

import models.Description;



/*
 * Saves the description object to a file
 */
class DescriptionDAOFile implements DescriptionDAO{

	//A cache to be able to keep files open
	private final Hashtable<String, Hashtable<Type, OutputStream>> validCache = new Hashtable<String, Hashtable<Type, OutputStream>>();
	private final Hashtable<String, Hashtable<Type, OutputStream>> rejectedCache = new Hashtable<String, Hashtable<Type, OutputStream>>();
	private final Long timeStamp = System.currentTimeMillis();
	private static final String ROOT_DIR_NAME = "extraction";
	private static final String VALID_DIR_NAME = "valid";
	private static final String REJECTED_DIR_NAME = "rejected";
	private final String lf = System.getProperty("line.separator");

	@Override
	public void save(Description toSave) throws IOException {
		if(toSave.valid){
			save(toSave, validCache, VALID_DIR_NAME);
		}else{
			save(toSave, rejectedCache, REJECTED_DIR_NAME);
		}
	}

	private void save(Description toSave, final Hashtable<String, Hashtable<Type, OutputStream>> cache, String directory) throws IOException{
		Hashtable<Type, OutputStream> tmp = cache.get(toSave.zipCode);
		final String zipCode = (toSave.zipCode!=null? toSave.zipCode:toSave.city);
		final Type type = toSave.type;
		//creating the cache for that current zip code
		if(tmp == null){
			tmp = new Hashtable<Type, OutputStream>();
			cache.put(zipCode, tmp);
		}
		OutputStream os = tmp.get(toSave.type);
		//creating the cache for the current type
		if(os == null){
			File tmpDir = new File(System.getProperty("user.home") + File.separator + ROOT_DIR_NAME + File.separator +
					directory + File.separator + timeStamp + File.separator + zipCode);
			tmpDir.mkdirs();
			File tmpFile = new File(tmpDir, type + ".csv");
			os = new FileOutputStream(tmpFile);
			tmp.put(toSave.type, os);
			os.write(("URL;Type;ZipCode;Price;Address;City;Lattitude;Longitude;Valid;Area;Creation;Author;Weekly" + lf).getBytes());
		}
		StringBuilder sb = new StringBuilder();
		sb.append(toSave.url); sb.append(";"); sb.append(type); sb.append(";");
		sb.append(toSave.zipCode); sb.append(";"); sb.append(toSave.price); sb.append(";");
		sb.append(toSave.address); sb.append(";"); sb.append(toSave.city); sb.append(";");
		sb.append(toSave.latitude); sb.append(";"); sb.append(toSave.longitude); sb.append(";");
		sb.append(toSave.valid); sb.append(";"); sb.append(toSave.area); sb.append(";"); sb.append(toSave.creation);
		sb.append(";"); sb.append(toSave.author); sb.append(";"); sb.append(toSave.weekly);
		sb.append(lf);
		os.write(sb.toString().getBytes());
		os.flush();
	}

	@Override
	public void close() {
		for(String zipCode : validCache.keySet()){
			Hashtable<Type, OutputStream> tmp = validCache.get(zipCode);
			for(Type type : tmp.keySet()){
				try {
					tmp.get(type).close();
				} catch (IOException e) {
					System.err.println("Error while closing valid stream for zip: " + zipCode + " and type: " + type);
					e.printStackTrace();
				}
			}
		}

		for(String zipCode : rejectedCache.keySet()){
			Hashtable<Type, OutputStream> tmp = rejectedCache.get(zipCode);
			for(Type type : tmp.keySet()){
				try {
					tmp.get(type).close();
				} catch (IOException e) {
					System.err.println("Error while closing rejected stream for zip: " + zipCode + " and type: " + type);
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void check() {
		//TODO add implementation to check the configuration at startup for
		//fail fast behavior
	}

}
