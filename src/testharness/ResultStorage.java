
import java.net.UnknownHostException;
import java.util.*;

import com.mongodb.*;

public class ResultStorage {

	static List<ResultStorage> objs = new ArrayList<ResultStorage>();

	String name;
	String [] args; 
	Double performance;
	String md5;
	String splitsCondition;
	String chunksCondition;
	String slaveokCondition;
	public static void addResults(ResultStorage rst) {
		objs.add(rst);
	}

	public void storeDB() throws UnknownHostException {
		ConfigFileReader cfrRS = new ConfigFileReader();
		DBAddress dba = new DBAddress("localhost", cfrRS.getDBPort(), cfrRS.getDBOut());
		DB db = Mongo.connect(dba);
		DBCollection coll = db.getCollection("output");
		if (db.collectionExists("output")) 
			coll.drop();
		BasicDBObject doc = new BasicDBObject();
		for(ResultStorage rst : objs) {
			doc.append("Name", rst.name);
			doc.append("Arguments", rst.args);
			doc.append("Performace", rst.performance);
			doc.append("MD5 Checksum", rst.md5);
			doc.append("Chunks-OK?", rst.chunksCondition);
			doc.append("Splits-OK?", rst.splitsCondition);
			doc.append("Slave-OK?", rst.slaveokCondition);
			
		} 
		coll.insert(doc);
	}
}
