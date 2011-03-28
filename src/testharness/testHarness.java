import java.io.*;
import java.lang.reflect.Method;
import java.security.MessageDigest;

import com.mongodb.*;

public class testHarness{

	@SuppressWarnings("unchecked")
	public static void main(String[] args){
		String filenAME  = "Config.xml";
		boolean useQuery = false;
		boolean testSlaveOk = false;
		boolean[] tf = {false, true};
		Boolean[] ntf = {null};

		try{

			ConfigFileReader cfrNew = ConfigFileReader.parse(new FileReader(fileName));

			List<String> testcases = null;
            System.out.print("Enter the testcase class names: ");
			BufferedReder bfrTC = new BufferedReader(new InputStreamReader(System.in));
			while((String tcLine = bfrTC.readLine()) != null) {
				StringTokenizer stz = new StringTokenizer(tcLine);
				while(stz.hasMoreTokens) {
					testcases.add(stz.nextToken());
				}
			}
			for(String classname : testcases){
				org.apache.hadoop.util.Tool tool = Class.forName(classname);
				String[] args2 = null;
				cfrNew.propertyCycle.run(tool, args2);
			}

			System.out.print("Set use-query? (Y/N): ");
			BufferedReader bfrq = new BufferedReader(new InputStreamReader(System.in));
			if(bfrq.readLine().equals("Y"))
				useQuery = true;
			else
				useQuery = false;

			System.out.print("Set slave-ok? (Y/N): ");
			BufferedReader bfrs = new BufferedReader(new InputStreamReader(System.in));
			if(bfrs.readLine().equals("Y"))
				testSlaveOk = true;
			else
				testSlaveOk = false;

			if (testSlaveOk)
				ntf = new Boolean[]{null, Boolean.TRUE, Boolean.FALSE}; 
		} catch(IOException ioe){
			ioe.printStackTrace();
		}

		for( int i = 0; i < args.length; i++ ){
			try{
				System.out.print("Please provide the name of your database: ");
				BufferedReader mDB = new BufferedReader(new InputStreamReader(System.in));
				String mongoDB = mDB.readLine();

				System.out.print("Please provide the port of your database: ");
				BufferedReader p = new BufferedReader(new InputStreamReader(System.in));
				String port = p.readLine();

				System.out.println("The argument is: " + args[i]);
				Class c = Class.forName(args[i]);
				Object obj = c.newInstance();

				System.out.print("Enter the method name which starts the map-reduce functionality in your application: ");
				BufferedReader methodNameCall = new BufferedReader(new InputStreamReader(System.in));
				Method methodTest = c.getDeclaredMethod(methodNameCall.readLine(), boolean.class, boolean.class, Boolean.class, boolean.class);

				System.out.print("Enter the desired number of runs: ");
				BufferedReader bfrRuns = new BufferedReader(new InputStreamReader(System.in));
				int runs = Integer.parseInt(bfrRuns.readLine());

				mDB.close();
				p.close();
				methodNameCall.close();
				bfrRuns.close();

				for(int k = 0; k < runs; k++){
					DBAddress dba = new DBAddress("localhost", 20000, "run_"+(k+1));
					DB db = Mongo.connect(dba);				
					StringBuilder sBuilder = new StringBuilder();
					for(boolean useShards : tf){
						for(boolean useChunks : tf){
							for(Boolean slaveok : ntf){
								String condition = null;
								String md5sum = null;
								if (useChunks){
									if(useShards)
										condition = "with_shards_and_chunks";
									else
										condition = "with_chunks";
								}
								else{
									if(useShards)
										condition = "with_shards";
									else
										condition = "no_splits";
								}
								if (slaveok != null){
									condition += "_" + slaveok;
								}
								DBCollection coll = db.getCollection(condition);
								BasicDBObject doc = new BasicDBObject();
								final long start = System.currentTimeMillis();
								methodTest.invoke(obj, useShards, useChunks, slaveok, useQuery);
								final long end = System.currentTimeMillis();
								final float runtime = ((float)(end - start))/1000;
								java.text.NumberFormat nf = java.text.NumberFormat.getInstance();
								nf.setMaximumFractionDigits(3);
								System.out.println("Condition: " + condition + " was run in: " + nf.format(runtime) + " seconds");

								DBAddress dbaE = new DBAddress("localhost", Integer.parseInt(port), mongoDB);
								DB dbE = Mongo.connect(dbaE);
								DBCollection collE = dbE.getCollection(condition);
								long count = collE.getCount();							

								System.out.println("/home/r_omio/mongodb-linux-i686-1.8.0/bin/mongodump -h localhost:" + Integer.parseInt(port) + " -d " + mongoDB + " -c " + condition);
								Process proc = Runtime.getRuntime().exec("/home/r_omio/mongodb-linux-i686-1.8.0/bin/mongodump -h localhost:" + Integer.parseInt(port) + " -d " + mongoDB + " -c " + condition);
								BufferedReader bfrME = new BufferedReader(new InputStreamReader(proc.getInputStream()));
								String tempLine;
								while((tempLine = bfrME.readLine()) != null) {
									System.out.println(tempLine);
								}
								bfrME.close();

								FileReader fr = new FileReader("/home/r_omio/NYUMongoHadoop-mongo-hadoop-testharness/build/testharness/dump/" + mongoDB + "/" + condition + ".bson");
								BufferedReader tbfr = new BufferedReader(fr);
								MessageDigest md = MessageDigest.getInstance("MD5");
								byte[] dataBytes = new byte[10000];	
								String line;
								while((line = tbfr.readLine()) != null){
									md.update(dataBytes, 0, line.length());
								}
								tbfr.close();

								byte[] mdbytes = md.digest();
								StringBuffer sb = new StringBuffer();
								for (int j = 0; j < mdbytes.length; j++){
									sb.append(Integer.toString((mdbytes[j] & 0xff) + 0x100, 16).substring(1));
									md5sum = sb.toString();
								}
								System.out.println("MD5: " + md5sum);

								doc.put("Condition", condition);
								doc.put("Time it took to run: ", runtime);
								doc.put("MD5 Value: ", md5sum);
								doc.put("Count: ", count); 
								coll.insert(doc);
							}
						}
					}						
				} 				
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}
