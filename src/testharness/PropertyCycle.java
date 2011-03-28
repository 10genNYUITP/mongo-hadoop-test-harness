
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

    private void runTool(MongoTool tool, String[] args) throws Exception {
        tool.run(args);
    }

    void run(MongoTool tool, String[] args) throws Exception {
        final org.apache.hadoop.conf.Configuration conf = tool.getConf();
        //If we have a when node only cycle through our values if then when node is satisfied.
        if (when != null && !when.is_satifisifed(conf))
            runTool(tool, args);
        else
            for (String val : vals) {
                //ideally clone conf and work with the clone, but I don't know if that is possible
                conf.set(name, val);
                if (next == null)
                    runTool(tool, args);
                else
                    next.runTool(tool, args);
            }
    } //run()
    //run()
}
