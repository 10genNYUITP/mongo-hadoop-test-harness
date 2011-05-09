package testharness;

import java.util.Arrays;

import org.apache.hadoop.conf.Configuration;

public class TestCase {
    private org.apache.hadoop.util.Tool tool;
    private String [] args;
    
    TestCase(org.apache.hadoop.util.Tool tool, String[] args) {
    	tool.setConf(new Configuration());
    	this.tool = tool;
    	this.args = args;
    }
    public String toString(){
    	return "{"+tool.getClass()+",args="+Arrays.toString(args)+"}";
    }
    void runTest(PropertyCycle pc) throws Exception{
        pc.run(tool, args);
    }
    public void setProperty(String key, String value) {
    	tool.getConf().set(key, value);
    }
}
