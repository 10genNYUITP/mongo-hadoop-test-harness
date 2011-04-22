
import java.io.BufferedInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * this class represents property values to cycle through when running tests
 */
class PropertyCycle {

    private String propName;
    
    private List<String> vals = new ArrayList<String>();
    private PropertyCycle when;
    private PropertyCycle next;
    private String baseline;
    String cC, sC;

    PropertyCycle(String name) {
        this.propName = name;
    }
    public String toString(){
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
<<<<<<< HEAD

	    StringBuilder indent = new StringBuilder();
	    for(int i = 0; i < setParams.size(); i++)
	    	indent.append(" ");
    	if (next == null) {
    		if(true){
    			System.out.println(indent+"I am "+this+" next is null, RUNNING TEST CASE, setParams is: "+setParams);
    			//return ;
=======
    	if (next == null) {
    		if(true){
    			System.out.println("runn test case, setParams is: "+setParams);
    			return ;
>>>>>>> 4b771e631fd5709d1d71b0a319460398e0db6016
    		}
    		
    		
    		ConfigFileReader cfr = new ConfigFileReader(); 
    		final long start = System.currentTimeMillis();  
    		
    		
            tool.run(args);
            
            
            final long end = System.currentTimeMillis();
            final double runtime = ((double)(end - start))/1000;
            java.text.NumberFormat nf = java.text.NumberFormat.getInstance();
            nf.setMaximumFractionDigits(3);
			
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] dataBytes = new byte[10000];	
            System.out.println("bin path is:" + cfr.getBinpath() + " port is: " + cfr.getDBPort());
            Process proc = Runtime.getRuntime().exec(cfr.getBinpath() + "mongodump -h localhost:" + cfr.getDBPort() + " -d " + cfr.getDBName() + " -c " + cfr.getCollection() + " -o -");
            BufferedInputStream bfrIS = new BufferedInputStream(proc.getInputStream());
            int tempRead;
			while((tempRead = bfrIS.read(dataBytes, 0, 9999)) > 0) {
                md.update(dataBytes);
            }
            bfrIS.close();

            byte[] mdbytes = md.digest();
            StringBuilder sb = new StringBuilder();
            String md5sum = null;
            for (int j = 0; j < mdbytes.length; j++){
                sb.append(Integer.toString((mdbytes[j] & 0xff) + 0x100, 16).substring(1));
                md5sum = sb.toString();
            }
            System.out.println("MD5: " + md5sum); 
            for(String str : args) {
            	System.out.println(str);
            }
            
            ResultStorage resultObj = new ResultStorage();
            resultObj.name = ConfigFileReader.getTestName();
            resultObj.args = args;
            resultObj.performance = runtime;
            resultObj.md5 = md5sum;
            resultObj.chunksCondition = cC;
            resultObj.splitsCondition = sC;
            
            ResultStorage.addResults(resultObj);
            
<<<<<<< HEAD
    	} else{
    		System.out.println(indent+"I am "+this+" running next pc: "+next);
            next.run(tool, args, setParams, baselineSoFar);
    	}
=======
    	} else
            next.runTool(tool, args, setParams, baselineSoFar);
>>>>>>> 4b771e631fd5709d1d71b0a319460398e0db6016
    }

    void run(org.apache.hadoop.util.Tool tool, String[] args) throws Exception {	
    	run(tool,args,new HashMap<String, String>(), true);
    }
    private void run(org.apache.hadoop.util.Tool tool, String[] args, Map<String,String> setParams,  boolean baselineSoFar) throws Exception{
        final org.apache.hadoop.conf.Configuration conf = tool.getConf();
<<<<<<< HEAD
        setParams = new HashMap<String, String>(setParams);
        if (when != null && !when.is_satifisifed(conf)){
        	System.out.println("when not satisfied, calling runTool()");
            runTool(tool, args, setParams, baselineSoFar);
        }else
            for (String val : vals) {
                conf.set(propName, val);
                setParams.put(propName, val);
                System.out.println("     I am "+propName+" calling RunTool");
=======
        if (when != null && !when.is_satifisifed(conf))
            runTool(tool, args, setParams, baselineSoFar);
        else
            for (String val : vals) {
                conf.set(propName, val);
                setParams.put(propName, val);
>>>>>>> 4b771e631fd5709d1d71b0a319460398e0db6016
                runTool(tool, args, setParams, baselineSoFar && (val != null && val.equals(baseline)));
            }
    }
}