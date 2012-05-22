package main;

import java.io.FileNotFoundException;

import scala.actors.threadpool.Arrays;
import de.lmu.ifi.dbs.elki.algorithm.clustering.KMeans;
import de.lmu.ifi.dbs.elki.data.Cluster;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.DoubleVector;
import de.lmu.ifi.dbs.elki.data.model.MeanModel;
import de.lmu.ifi.dbs.elki.database.HashmapDatabase;
import de.lmu.ifi.dbs.elki.database.UpdatableDatabase;
import de.lmu.ifi.dbs.elki.database.ids.DBID;
import de.lmu.ifi.dbs.elki.datasource.FileBasedDatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.bundle.SingleObjectBundle;
import de.lmu.ifi.dbs.elki.datasource.parser.AbstractParser;
import de.lmu.ifi.dbs.elki.datasource.parser.NumberVectorLabelParser;
import de.lmu.ifi.dbs.elki.distance.distancevalue.DoubleDistance;
import de.lmu.ifi.dbs.elki.utilities.ClassGenericsUtil;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.ListParameterization;


public class Main {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		ListParameterization params = new ListParameterization();
	    params.addParameter(FileBasedDatabaseConnection.INPUT_ID, "C:\\Users\\jmguilla\\extraction\\valid\\1337708464586\\aaelki.csv");
	    params.addParameter(KMeans.K_ID, 5);
	    params.addParameter(KMeans.SEED_ID, 1);
	    params.addParameter(KMeans.MAXITER_ID, 1000);
	    params.addParameter(AbstractParser.COLUMN_SEPARATOR_ID, ";");
	    params.addParameter(AbstractParser.QUOTE_ID, AbstractParser.QUOTE_CHAR);
	    params.addParameter(NumberVectorLabelParser.LABEL_INDICES_ID, Arrays.asList(new Integer[]{0,1,4,5,6,7,8,9,10,11}));

	    UpdatableDatabase db = ClassGenericsUtil.parameterizeOrAbort(HashmapDatabase.class, params);
	    params.failOnErrors();


	    // get database
	    db.initialize();

		KMeans<DoubleVector, DoubleDistance> kmeans = ClassGenericsUtil.parameterizeOrAbort(KMeans.class, params);

	    // run KMeans on database
	    Clustering<MeanModel<DoubleVector>> result = kmeans.run(db);
	    for(Cluster<MeanModel<DoubleVector>> cluster : result.getAllClusters()){
//	    	BigDecimal mean = new BigDecimal(0);
//	    	int nbPrices = 0;
	    	System.out.println("*************** Cluster: " + cluster + " ***************");
	    	MeanModel<DoubleVector> model = cluster.getModel();
	    	DoubleVector vector = model.getMean();
	    	System.out.print("*************** Mean: ");
	    	for(double d: vector.getValues()){
	    		System.out.print(d);
	    		System.out.print(" ");
	    	}
	    	System.out.println(" ***************");
	    	for(DBID child : cluster.getIDs()){
	    		SingleObjectBundle sob = db.getBundle(child);
//	    		System.out.println("###### Child #" + child + " ######");
//	    		System.out.println("Data lenght: " + sob.dataLength());
//	    		System.out.println("Meta lenght: " + sob.metaLength());
//	    		for(int i = 0; i < sob.metaLength(); i++){
//	    		System.out.println(sob.data(1));
//	    		LabelList ll = (LabelList)sob.data(2);
//	    		mean = mean.add(new BigDecimal(ll.get(2)));
//	    		nbPrices++;
//	    		}
	    	}
//	    	if(nbPrices>0)
//	    	System.out.println("Mean price: " + mean.divide(new BigDecimal(nbPrices), RoundingMode.HALF_UP));
	    }
	}

}
