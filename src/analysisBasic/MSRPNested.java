package analysisBasic;

import entity.NestedResource;
import entity.SporadicTask;
import utils.AnalysisUtils;

import java.util.ArrayList;

public class MSRPNested extends MSRPOriginal{

    public long[][] getNestedResponseTime(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<NestedResource> resources, boolean printBebug) {
        long[][] init_Ri = new AnalysisUtils().initResponseTime(tasks);

        long[][] response_time = new long[tasks.size()][];
        boolean isEqual = false, missDeadline = false;
        count = 0;

        for (int i = 0; i < init_Ri.length; i++)
            response_time[i] = new long[init_Ri[i].length];

        new AnalysisUtils().cloneList(init_Ri, response_time);

        /* a huge busy window to get a fixed Ri */
        while (!isEqual) {
            isEqual = true;
            long[][] response_time_plus = busyWindow(tasks, resources, response_time);

            for (int i = 0; i < response_time_plus.length; i++) {
                for (int j = 0; j < response_time_plus[i].length; j++) {
                    if (response_time[i][j] != response_time_plus[i][j])
                        isEqual = false;
                    if (response_time_plus[i][j] > tasks.get(i).get(j).deadline)
                        missDeadline = true;
                }
            }

            count++;
            new AnalysisUtils().cloneList(response_time_plus, response_time);

            if (missDeadline)
                break;
        }

        if (printBebug) {
            if (missDeadline)
                System.out.println("MSRPRTA    after " + count + " tims of recursion, the tasks miss the deadline.");
            else
                System.out.println("MSRPRTA	   after " + count + " tims of recursion, we got the response time.");

            new AnalysisUtils().printResponseTime(response_time, tasks);
        }

        return response_time;
    }

    private long[][] busyWindow(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<NestedResource> resources, long[][] response_time) {
        long[][] response_time_plus = new long[tasks.size()][];
        for (int i = 0; i < response_time.length; i++)
            response_time_plus[i] = new long[response_time[i].length];

        for (int i = 0; i < tasks.size(); i++) {
            for (int j = 0; j < tasks.get(i).size(); j++) {
                SporadicTask task = tasks.get(i).get(j);

                task.spin = resourceAccessingTime(task, resources);
                task.interference = highPriorityInterference(task, tasks, response_time[i][j]);
                task.local = localBlocking(task, tasks, resources, response_time, response_time[i][j]);

                response_time_plus[i][j] = task.Ri = task.WCET + task.spin + task.interference + task.local;
                if (task.Ri > task.deadline)
                    return response_time_plus;
            }
        }
        return response_time_plus;

    }

    /*
     * Calculate the spin delay for a given task t.
     */
    private long resourceAccessingTime(SporadicTask t, ArrayList<NestedResource> resources) {
        long spin_delay = 0;
        /* Resource accessing time is the sum_of all accessed resources costs * times accessed */
        for (int k = 0; k < t.resource_required_index.size(); k++) {
            NestedResource resource = resources.get(t.resource_required_index.get(k));
            spin_delay += resourceAccessCost(resource, resources) * t.number_of_access_in_one_release.get(k);
        }
        return spin_delay;
    }


    /* Returns the access cost of a given nested resource, including remote contention and
       potential inner resources costs
     */
    private long resourceAccessCost(NestedResource res, ArrayList<NestedResource> resources){
        long access_cost = res.csl;
        /* Length of FIFO queue as function of direct calls + outer resources calls */
        long queue = res.partitions.size() + res.outer_resources.size();
        /* Cost of inner resources = Sum_of their access cost * times accessed */
        for (int i = 0; i < res.inner_resources.size(); i++){
            NestedResource inner = resources.get(res.inner_resources.get(i));
            access_cost += resourceAccessCost(inner, resources)
                    * res.number_accesses_inner_resource.get(i);
        }
        /* Final cost is the cost of each access times the length of the FIFO queue */
        return queue * access_cost;
    }

    /*
     * Calculate the local blocking for task t.
     */
    private long localBlocking(SporadicTask t, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<NestedResource> resources, long[][] Ris, long Ri) {
        ArrayList<NestedResource> LocalBlockingResources = getLocalBlockingResources(t, resources);
        ArrayList<Long> local_blocking_each_resource = new ArrayList<>();

        /* For each potentially blocking resource, add its access cost to the list */
        for (int i = 0; i < LocalBlockingResources.size(); i++) {
            NestedResource res = LocalBlockingResources.get(i);
            local_blocking_each_resource.add(resourceAccessCost(res, resources));
        }

        if (local_blocking_each_resource.size() > 1)
            local_blocking_each_resource.sort((l1, l2) -> -Double.compare(l1, l2));

        /* Return the highest */
        return local_blocking_each_resource.size() > 0 ? local_blocking_each_resource.get(0) : 0;

    }

    /*
     * gives a set of resources that can cause local blocking for a given task
     */
    private ArrayList<NestedResource> getLocalBlockingResources(SporadicTask task, ArrayList<NestedResource> resources) {
        ArrayList<NestedResource> localBlockingResources = new ArrayList<>();
        int partition = task.partition;

        /* Check for resources involved in the partition */
        for (int i = 0; i < resources.size(); i++) {
            NestedResource resource = resources.get(i);

            /* If the resource is not global, it can be preempted, so normal ICPP
             * For a resource not to be global, its all_partitions must be of size 1
             * A resource can be inner but not global, if just accesses by outer resources
             * with the same single partition. */
            if (resource.all_partitions.size() == 1 && resource.all_partitions.get(0)
                    == task.partition && resource.ceiling.get(0) >= task.priority) {
                for (int j = 0; j < resource.requested_tasks.size(); j++) {
                    SporadicTask LP_task = resource.requested_tasks.get(j);
                    if (LP_task.partition == partition && LP_task.priority < task.priority) {
                        localBlockingResources.add(resource);
                        break;
                    }
                }
            }

            /* If it is global and used in the partition, if there is at least one lower priority
             * task accessing it, then it can cause blocking. */
            if (resource.all_partitions.contains(task.partition)) {
                for (int j = 0; j < resource.requested_tasks.size(); j++) {
                    SporadicTask LP_task = resource.requested_tasks.get(j);
                    if (LP_task.partition == partition && LP_task.priority < task.priority) {
                        localBlockingResources.add(resource);
                        break;
                    }
                }
            }
        }

        return localBlockingResources;
    }


}
