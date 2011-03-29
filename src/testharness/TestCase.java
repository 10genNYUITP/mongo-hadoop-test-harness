
/**
 * Created: Mar 28, 2011  10:56:01 PM
 *
 */
public class TestCase {
    org.apache.hadoop.util.Tool tool;
    String[] args;
    
    void run(PropertyCycle pc) throws Exception{
        pc.run(tool, args);
    }
}
