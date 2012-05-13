package utils.fr.jmg.extractor.api;

import java.io.IOException;
import java.util.Hashtable;

import models.Description;


/*
 * To manager access to DAOs
 */
//TODO implement fail fast behabior
public class DAOManager {

	private final Hashtable<Class<? extends Persistable>, DescriptionDAO> cache = new Hashtable<Class<? extends Persistable>, DescriptionDAO>(); 

	public DescriptionDAO getDAO(Persistable persistable) throws IOException{
		DescriptionDAO result = cache.get(persistable.getClass());
		if(result == null){
			if(persistable instanceof Description){
				//TODO should be done thanks to injection
				result = new DescriptionDAOFile();
				cache.put(persistable.getClass(), result);
			}
		}
		if(result != null){
			return result;
		}else{
			throw new IOException("Cannot find a valid DAO for " + persistable);
		}
	}

	/*
	 * Closes every dao present in the cache
	 */
	public void close(){
		for(Class<? extends Persistable> klass : cache.keySet()){
			cache.get(klass).close();
		}
		cache.clear();
	}
}
