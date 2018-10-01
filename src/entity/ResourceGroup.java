package entity;

import java.util.ArrayList;

public class ResourceGroup {

    /* Id for debug purposes */
    public int id;
    /* Set of outer resources i.e. requested directly by at least a task */
    /* Do we really need this. Dont think so, since csl is just max_csl */
    public ArrayList<NestedResource> outers_resources;
    /* Total cost of accessing each inner resource */
    //public ArrayList<Long> csl;
    /* Set of all resources in the group */
    public ArrayList<NestedResource> all_resources;
    /* Longest csl */
    public Long max_csl = 0L;

    public ResourceGroup (int id) {
        this.id = id;
        this.outers_resources = new ArrayList<NestedResource>();
        //this.csl = new ArrayList<Long>();
        this.all_resources = new ArrayList<NestedResource>();
    }

    public void addOuterResource(NestedResource r, ArrayList<NestedResource> resources){
        /* add the resource to both lists */
        outers_resources.add(r);
        all_resources.add(r);
        /* Check if the csl is the new max */
        if (r.access_time > max_csl) max_csl = r.access_time;
        /* add its inner resources */
        for (int i = 0; i < r.inner_resources.size(); i++) {
            addInnerResource(resources.get(r.inner_resources.get(i)), resources);
        }
    }

    private void addInnerResource(NestedResource r, ArrayList<NestedResource> resources){
        if (!all_resources.contains(r))
            all_resources.add(r);
        for (int i = 0; i < r.inner_resources.size(); i++) {
            NestedResource res = resources.get(r.inner_resources.get(i));
            if (!all_resources.contains(res))
                addInnerResource(res, resources);
        }
        for (int i = 0; i < r.outer_resources.size(); i++) {
            NestedResource res = resources.get(r.outer_resources.get(i));
            if (!all_resources.contains(res))
                addInnerResource(res, resources);
        }
    }

    public boolean contains(NestedResource r){
        return all_resources.contains(r);
    }

    public boolean containsId(int id){
        for (int i = 0; i < all_resources.size(); i++) {
            if(all_resources.get(i).id == id) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "G" + this.id + " outer resources: " + outers_resources.size() + " all_resources: " + all_resources.size(); }
}
