import java.io.*;
import java.util.*;
import java.security.MessageDigest;

import com.mongodb.*;

public class testHarness{
	
    String [] testcases = null;	

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception{
        String fileName  = "Config.xml";


	
        ConfigFileReader cfrNew = ConfigFileReader.parse(new FileReader(fileName));
				
        java.util.Iterator<Map.Entry<String, TestCase>> i1 = cfrNew.testcases.entrySet().iterator();
        while (i1.hasNext()) {	
            Map.Entry<String, TestCase> pairs = (Map.Entry) i1.next();
            TestCase tcase = (TestCase) pairs.getValue();
            String tname = (String) pairs.getKey();
            String collection = tname + "_" + Arrays.toString(cfrNew.args);
            //^^^ This is not what you want. first of all this would put something like @[1234 in the string,
            //which is not what you want. Arrays.toString(cfrNew.args) would be better.  But this might not be
            //a valid mongo collection name.  You will probably have to create a method to get a collection name
            //from a testcase
            
            //^^^ We are creating a collection name to store it in Mongo. We fetch them later, tokenize based on this format and evaluate and display results.
            // I think your suggestion works fine
            
            DBAddress dba = new DBAddress("localhost", cfrNew.dbport, cfrNew.dbname);
            DB db = Mongo.connect(dba);
            DBCollection coll = db.getCollection(collection);
            db.removeAll(coll);
            final long start = System.currentTimeMillis();
            tcase.runTest(cfrNew.propertyCycle);
            final long end = System.currentTimeMillis();
            final float runtime = ((float)(end - start))/1000;
            java.text.NumberFormat nf = java.text.NumberFormat.getInstance();
            nf.setMaximumFractionDigits(3);
					
            Process proc = Runtime.getRuntime().exec(binpath + "mongodump -h localhost:" + cfrNew.dbport + " -d " + cfrNew.dbname + " -c " + collection);
            BufferedReader bfrME = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String tempLine;
            while((tempLine = bfrME.readLine()) != null) {
                System.out.println(tempLine);
            }
            bfrME.close();

            FileReader fr = new FileReader("/dump/" + cfrNew.dbname + "/" + cfrNew.dbname + crfNew.args.toArray() + ".bson");
            BufferedReader tbfr = new BufferedReader(fr);
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] dataBytes = new byte[10000];	
            String line;
            while((line = tbfr.readLine()) != null){
                md.update(dataBytes, 0, line.length());
            }
            tbfr.close();

            byte[] mdbytes = md.digest();
            StringBuilder sb = new StringBuilder();
            String md5sum = null;
            for (int j = 0; j < mdbytes.length; j++){
                sb.append(Integer.toString((mdbytes[j] & 0xff) + 0x100, 16).substring(1));
                md5sum = sb.toString();
            }
            System.out.println("MD5: " + md5sum);

            BasicDBObject doc = new BasicDBObject();
            doc.put("Performance: ", runtime);
            doc.put("MD5: ", md5sum); 
            coll.insert(doc);
        }
    }//main()
}
