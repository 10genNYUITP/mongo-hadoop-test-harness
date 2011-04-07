
/**
 * Created: Mar 28, 2011  10:56:01 PM
 *
 */
public class TestCase {
    private org.apache.hadoop.util.Tool tool;
    private String [] args;
    
    TestCase(org.apache.hadoop.util.Tool tool, String[] args) {
    	this.tool = tool;
    	this.args = args;
    }
    
    void runTest(PropertyCycle pc) throws Exception{
        pc.run(tool, args);
    }
}
