package utils.fr.jmg.extractor.api;

import java.io.IOException;

import models.Description;

/*
 * Interface where persistence implementation goes
 */
public interface DescriptionDAO {
	public void save(Description toSave) throws IOException;
	public void close();
	public void check();
}
