package testharness;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBAddress;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.hadoop.util.MongoConfigUtil;
import java.io.CharArrayWriter;
import java.io.InputStreamReader;

public class PropertyCycle {

    private String propName;
    private ConfigFileReader cfr;
    private List<GenerateXML> objs = new ArrayList<GenerateXML>();
    private final static java.util.Date testHarnessStart = new java.util.Date();
    private List<String> vals = new ArrayList<String>();
    private PropertyCycle when;
    private PropertyCycle next;
    private String baseline;
    String uShards = "FALSE";
    String uChunks = "FALSE";
    String sOk = "FALSE";
        
    PropertyCycle() {               
    }

    PropertyCycle(ConfigFileReader cfr, String name) {
        this.cfr = cfr;
        this.propName = name;
    }
        
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{"+propName+",vals="+vals);
        if (next != null)
            sb.append(",next="+next);
        sb.append("}");
        return sb.toString();
    }

    public void addValue(String... v) {
        vals.addAll(java.util.Arrays.asList(v));
    }
        
    public List<GenerateXML> getGens() {
        return objs;
    }

    /** Only apply the values of this PropertyCycle to the test if {@code when} is satisfied. */
    public void setWhen(PropertyCycle when) {
        this.when = when;
    }

    public void setBaseline(String baseline) {
        if (this.baseline != null)
            throw new IllegalStateException("Baseline is already set to \""+this.baseline+"\", it cannot be set to \""+baseline+"\"");
        this.baseline = baseline;
    }
    void add(PropertyCycle pc) {
        if (next == null)
            next = pc;
        else
            next.add(pc);
    }

    /** for when nodes */
    private boolean is_satifisifed(final org.apache.hadoop.conf.Configuration conf) {
        return vals.contains(conf.get(propName));
    }

    private void runTool(org.apache.hadoop.util.Tool tool, String[] args, Map<String,String> setParams, boolean baselineSoFar) throws Exception {


        final Configuration conf = tool.getConf();
        final com.mongodb.MongoURI outputUri =  MongoConfigUtil.getOutputURI( conf);

        StringBuilder indent = new StringBuilder();
        if (next == null) {
            dropOldCollection(outputUri);
            final long start = System.currentTimeMillis();
            tool.run(args);
            final long end = System.currentTimeMillis();
            final double runtime = ((double)(end - start))/1000;
            java.text.NumberFormat nf = java.text.NumberFormat.getInstance();
            nf.setMaximumFractionDigits(3);
            String md5sum = md5calc(cfr.getBinpath(), outputUri);
            if (true) {
                String md2 = md5calc(cfr.getBinpath(), outputUri);
            }

            GenerateXML gx = new GenerateXML();
            BasicDBObject resultDoc = new BasicDBObject();
            resultDoc.append("Name", tool.getClass().getName());
            gx.setName(tool.getClass().getName());
            resultDoc.append("Arguments", args); 
            StringBuilder sb = new StringBuilder();
            for(String arg : args) {
                sb.append(arg + "; ");
            }
            resultDoc.append("testHarnessStart", testHarnessStart);
            resultDoc.append("Runtime", runtime);
            gx.setRuntime(runtime);
            resultDoc.append("MD5Checksum", md5sum);
            gx.setMD5(md5sum);
            resultDoc.append("outputByteSize", savedMd5Size);
            resultDoc.append("Count", getCount(outputUri)); 
            gx.setCount(getCount(outputUri)); 
            java.util.Iterator<?> it = setParams.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry me = (Map.Entry) it.next();
                if(me.getKey().equals("mongo.splits.use-chunks")) {
                    uChunks = (String) me.getValue();
                } else if (me.getKey().equals("mongo.splits.use-shards")) {
                    uShards = (String) me.getValue();
                } else {
                    sOk = (String) me.getValue();
                }
            }
            resultDoc.append("Use-Shards Condition", uShards);
            gx.setUS(uShards);
            resultDoc.append("Use-Chunks Condition", uChunks);
            gx.setUC(uChunks);
            resultDoc.append("Slave-Ok Condition", sOk);
            gx.setSO(sOk);
            resultDoc.append("StartTime", new java.util.Date(start)); 
            gx.setStart(new java.util.Date(start)); 
            if (baselineSoFar) {
                resultDoc.append("Baseline", Boolean.TRUE); 
                gx.setBaseline(Boolean.TRUE); 
            }
            storeResults(resultDoc);
            objs.add(gx);
        } else {
            next.run(tool, args, setParams, baselineSoFar);
        }
    }
        
    private void storeResults (BasicDBObject ResultDoc) throws MongoException, UnknownHostException {
        com.mongodb.MongoURI uri =  cfr.getResultURI();
        Mongo mongo = new Mongo(uri);
        try {
            DB db = mongo.getDB(uri.getDatabase());
            DBCollection coll = db.getCollection(uri.getCollection());
            coll.insert(ResultDoc);
        } finally {
            if(mongo != null)
                mongo.close();
        }
    }
    private void dropOldCollection(com.mongodb.MongoURI outputUri) throws MongoException, UnknownHostException {
        Mongo mongo = new Mongo(outputUri);
        try {
            DB db = mongo.getDB(outputUri.getDatabase());

            if (db.collectionExists(outputUri.getCollection())) {
                DBCollection coll = db.getCollection(outputUri.getCollection());
                coll.drop();
            }
        }finally {
            if (mongo != null)
                mongo.close();
        }
    }
    private long getCount( com.mongodb.MongoURI outputUri) throws MongoException, UnknownHostException {
        Mongo mongo = new Mongo(outputUri);
        try {
            DB db = mongo.getDB(outputUri.getDatabase());
            if (db.collectionExists(outputUri.getCollection())) {
                DBCollection coll = db.getCollection(outputUri.getCollection());
                return coll.count();
            }
        } finally {
            if (mongo != null)
                mongo.close();
        }
        return -1;
    }
    private int savedMd5Size = -1;
    private String md5calc( String binpath, com.mongodb.MongoURI outputUri)
        throws NoSuchAlgorithmException, IOException {
        int size = 0;
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] dataBytes = new byte[10000];
        List<String> hosts = outputUri.getHosts();
        assert hosts.size() == 1;
        String hostname = hosts.get(0);
        String commandString = binpath + "mongodump -h "+hostname + " -d " + outputUri.getDatabase() + " -c "+ outputUri.getCollection()+ " -o -";
        final Process proc = Runtime.getRuntime().exec(commandString);
        BufferedInputStream bfrIS = new BufferedInputStream(proc.getInputStream());
        final BufferedReader stderrIS = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
        final java.io.CharArrayWriter stdErrCap = new CharArrayWriter();
        (new Thread(new Runnable() {
        	public void run() {
        		while(true){
        			try {
        				String line = stderrIS.readLine();
                        if (line == null)
                        	return;
                            stdErrCap.append(line);
                        }catch (IOException ex) {
                        	ex.printStackTrace();
                        }
                    }
                }
            }, "md5sum stderr capture thread")).start();

        int tempRead;
        while((tempRead = bfrIS.read(dataBytes, 0, 9999)) > 0) {
            md.update(dataBytes, 0, tempRead);
            size += tempRead;
        }
        bfrIS.close();
        savedMd5Size = size;
        int exitValue = -1;
        try {
            exitValue = proc.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(exitValue != 0) {
        }

        byte[] mdbytes = md.digest();
        StringBuilder sb = new StringBuilder(mdbytes.length * 2);
        String md5sum = null;
        for (int j = 0; j < mdbytes.length; j++){
            sb.append(Integer.toString((mdbytes[j] & 0xff) + 0x100, 16).substring(1));
            md5sum = sb.toString();
        }
        return md5sum;
    }

    void run(org.apache.hadoop.util.Tool tool, String[] args) throws Exception {
        run(tool,args,new HashMap<String, String>(), true);
    }
    private void run(org.apache.hadoop.util.Tool tool, String[] args, Map<String,String> setParams, boolean baselineSoFar) throws Exception {
        final org.apache.hadoop.conf.Configuration conf = tool.getConf();
        setParams = new HashMap<String, String>(setParams);

        if ((when != null) && !when.is_satifisifed(conf)) {
            if(shardsCondition(setParams)) {
                for(String val : vals) {
                    conf.set(propName, val);
                    setParams.put(propName, val);
                    runTool(tool, args, setParams, baselineSoFar);
                }
            } else {
                runTool(tool, args, setParams, baselineSoFar);
            }
        }
        else {
            for (String val : vals) {
                conf.set(propName, val);
                setParams.put(propName, val);
                runTool(tool, args, setParams, baselineSoFar && (val != null && val.equals(baseline)));
            }
        }
    }
    private boolean shardsCondition(Map<String, String> s) {
        java.util.Iterator it = s.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry me = (Map.Entry)it.next();
            if(me.getKey().equals("mongo.splits.use-shards") && me.getValue().equals("true")) 
                return true;
        }
        return false;
    }
}

