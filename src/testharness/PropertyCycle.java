
import com.mongodb.hadoop.util.MongoTool;
import java.util.ArrayList;
import java.util.List;

/**
 * this class represents property values to cycle through when running tests
 */
class PropertyCycle {

    private String name;
    private List<String> vals = new ArrayList<String>();
    private PropertyCycle when;
    private PropertyCycle next; //next in chain
    private String baseline; //If value matches this is the baseline for the test

    PropertyCycle(String name) {
        this.name = name;
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
        return vals.contains(conf.get(name));
    }
    /** Either actually run the tool, or pass to the next PropertyCycle in the chain. */
    private void runTool(org.apache.hadoop.util.Tool tool, String[] args, boolean baselineSoFar) throws Exception {
        if (next == null)
            tool.run(args);
        else
            next.run(tool, args, baselineSoFar);
    }
    /** Called externally on the first PropertyCycle */
    void run(org.apache.hadoop.util.Tool tool, String[] args) throws Exception {
        run(tool, args, true);
    }
    private void run(org.apache.hadoop.util.Tool tool, String[] args, boolean baselineSoFar) throws Exception {
        final org.apache.hadoop.conf.Configuration conf = tool.getConf();
        //If we have a when node only cycle through our values if then when node is satisfied.
        if (when != null && !when.is_satifisifed(conf))
            runTool(tool, args, baselineSoFar);
        else
            for (String val : vals) {
                //ideally clone conf and work with the clone, but I don't know if that is possible
                conf.set(name, val);
                runTool(tool, args, baselineSoFar && (val != null && val.equals(baseline)));
            }
    } //run()
}
