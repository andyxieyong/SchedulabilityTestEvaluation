package entity;

import java.util.ArrayList;

public class NestedResource extends Resource{

    public ArrayList<Integer> outer_resources;
    public ArrayList<Integer> inner_resources;
    public ArrayList<Integer> number_accesses_inner_resource;
    /* Partitions pure or inherited of outer resources */
    public ArrayList<Integer> inherited_partitions;
    /* Set of partitions including pure and inherited */
    public ArrayList<Integer> all_partitions;


    /* Separate:
        Critical section lenght (e) -> csl
        Access time (including inner resources) -> access_time
        Blocking time -> response time equations
     */
    public long access_time;


    public NestedResource(int id,
                          long cs_len) {
        super(id, cs_len);
        outer_resources = new ArrayList<Integer>();
        inner_resources = new ArrayList<Integer>();
        number_accesses_inner_resource = new ArrayList<Integer>();
        inherited_partitions = new ArrayList<Integer>();
        all_partitions = new ArrayList<Integer>();
    }

    @Override
    public String toString() {
        return "R" + this.id + " : cs len = " + this.csl + ", access_time: " + access_time + ", partitions: " + partitions.size() + ", inherited: " + inherited_partitions.size() + ", tasks: " + requested_tasks.size() + ", isGlobal: "
                + isGlobal + ", outer resources: " + this.outer_resources.size() + ", inner resources: " + this.inner_resources.size();
    }
}
