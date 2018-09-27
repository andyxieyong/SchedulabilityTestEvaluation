package analysisNew;

import entity.NestedResource;
import entity.SporadicTask;
import utils.AnalysisUtils;

import java.util.ArrayList;

import static java.lang.Math.min;

public class MrsPNestedNew extends MrsPNew {

    /* Longest queue of a resource */
    private ArrayList<Integer> q;
    /* Tasks that access a resource directly or indirectly */
    private ArrayList<ArrayList<SporadicTask>> g;

    public long[][] getNestedResponseTime(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<NestedResource> resources, boolean printDebug) {
        long[][] init_Ri = new AnalysisUtils().initResponseTime(tasks);

        long[][] response_time = new long[tasks.size()][];
        boolean isEqual = false, missDeadline = false;
        count = 0;

        for (int i = 0; i < init_Ri.length; i++) {
            response_time[i] = new long[init_Ri[i].length];
        }

        new AnalysisUtils().cloneList(init_Ri, response_time);

        /* Study fixed properties of resources */
        /* Q (longest possible queue on nested resource) */
        q = calculateQ(resources);

        /* a huge busy window to get a fixed Ri */
        while (!isEqual) {
            isEqual = true;
            long[][] response_time_plus = busyWindow(tasks, resources, response_time, true);

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

        if (printDebug) {
            if (missDeadline)
                System.out.println("NewMrsPRTA    after " + count + " tims of recursion, the tasks miss the deadline.");
            else
                System.out.println("NewMrsPRTA    after " + count + " tims of recursion, we got the response time.");

            new AnalysisUtils().printResponseTime(response_time, tasks);
        }

        return response_time;
    }

    private long[][] busyWindow(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<NestedResource> resources, long[][] response_time, boolean btbHit) {
        long[][] response_time_plus = new long[tasks.size()][];

        for (int i = 0; i < response_time.length; i++) {
            response_time_plus[i] = new long[response_time[i].length];
        }

        for (int i = 0; i < tasks.size(); i++) {
            for (int j = 0; j < tasks.get(i).size(); j++) {
                SporadicTask task = tasks.get(i).get(j);
                task.spin = directRemoteDelay(task, tasks, resources, response_time, response_time[i][j], btbHit);
                task.interference = highPriorityInterference(task, tasks, response_time[i][j], response_time, resources, btbHit);
                task.local = localBlocking(task, tasks, resources, response_time, response_time[i][j], btbHit);
                /* Task pure resource accesse time does not make sense any longer, spin delay includes it */
                response_time_plus[i][j] = task.Ri = task.WCET + task.spin + task.interference + task.local;

                if (task.Ri > task.deadline)
                    return response_time_plus;

            }
        }
        return response_time_plus;
    }

    /*
     * Calculate the local blocking for task t.
     */
    private long localBlocking(SporadicTask t, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<NestedResource> resources, long[][] Ris, long Ri, boolean btbHit) {
        ArrayList<NestedResource> LocalBlockingResources = getLocalBlockingResources(t, resources, tasks.get(t.partition));
        ArrayList<Long> local_blocking_each_resource = new ArrayList<>();

        /*Calculate the potential local blocking for each identified resource */
        for (int i = 0; i < LocalBlockingResources.size(); i++) {
            NestedResource res = LocalBlockingResources.get(i);

            if (res.isGlobal) {
                /* We have take into account all partitions from where the resource is accessed */
                /* Local blocking is eventual next access to the shared resource by the task */
                int accesses = getTotalAccessesResource(t,res,resources);
                /* If not accessed, the cost is that of a potential first access */
                if (accesses > 0)
                    local_blocking_each_resource.add(resourceAccessCost(t, tasks, resources,Ris,Ri,btbHit,res,1));
                /* Otherwise is the difference between cost of accessing the resource one more time and the real number of accesses */
                else
                    local_blocking_each_resource.add(resourceAccessCost(t, tasks, resources,Ris,Ri,btbHit,res,accesses + 1)
                    - resourceAccessCost(t, tasks, resources,Ris,Ri,btbHit,res,accesses + 1));
            }
        }

        /* Order the results and return the highest */
        if (local_blocking_each_resource.size() > 1)
            local_blocking_each_resource.sort((l1, l2) -> -Double.compare(l1, l2));

        return local_blocking_each_resource.size() > 0 ? local_blocking_each_resource.get(0) : 0;

    }

    /*
     * gives a set of resources that can cause local blocking for a given task
     */
    private ArrayList<NestedResource> getLocalBlockingResources(SporadicTask task, ArrayList<NestedResource> resources, ArrayList<SporadicTask> LocalTasks) {
        ArrayList<NestedResource> localBlockingResources = new ArrayList<>();
        int partition = task.partition;

        for (int i = 0; i < resources.size(); i++) {
            NestedResource resource = resources.get(i);

            if (resource.all_partitions.contains(partition) && resource.ceiling.get(partition) >= task.priority) {
                /* Now we have G set calculated for each resource, so check if there is a lower priority task there */
                ArrayList<SporadicTask> partition_tasks = g.get(partition);
                for (int j = 0; j < partition_tasks.size(); j++) {
                    SporadicTask LP_task = partition_tasks.get(j);
                    if (LP_task.partition == partition && LP_task.priority < task.priority) {
                        localBlockingResources.add(resource);
                        break;
                    }
                }
            }
        }

        return localBlockingResources;
    }

    /*
     * Calculate the local high priority tasks' interference for a given task t.
     * CI is a set of computation time of local tasks, including spin delay.
     */
    private long highPriorityInterference(SporadicTask t, ArrayList<ArrayList<SporadicTask>> allTasks, long Ri, long[][] Ris, ArrayList<NestedResource> resources,
                                          boolean btbHit) {
        long interference = 0;
        int partition = t.partition;
        ArrayList<SporadicTask> tasks = allTasks.get(partition);

        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).priority > t.priority) {
                SporadicTask hpTask = tasks.get(i);
                interference += Math.ceil((double) (Ri) / (double) hpTask.period) * (hpTask.WCET);

                long btb_interference = getIndirectSpinDelay(hpTask, Ri, Ris[partition][i], Ris, allTasks, resources, btbHit);
                interference += btb_interference;
            }
        }
        return interference;
    }

    /*
     * for a high priority task hpTask, return its back to back hit time when
     * the given task is pending
     */
    private long getIndirectSpinDelay(SporadicTask hpTask, long Ri, long Rihp, long[][] Ris, ArrayList<ArrayList<SporadicTask>> allTasks,
                                      ArrayList<NestedResource> resources, boolean btbHit) {
        long BTBhit = 0;

        for (int i = 0; i < hpTask.resource_required_index.size(); i++) {
            /* for each resource that a high priority task request */
            NestedResource resource = resources.get(hpTask.resource_required_index.get(i));

            int number_of_request_with_btb = (int) Math.ceil((double) (Ri + (btbHit ? Rihp : 0)) / (double) hpTask.period)
                    * hpTask.number_of_access_in_one_release.get(i);

            BTBhit += resourceAccessCost(hpTask,allTasks,resources,Ris,Ri,btbHit,resource, number_of_request_with_btb) ;

        }
        return BTBhit;
    }

    /*
     * Calculate the spin delay for a given task t.
     */
    private long directRemoteDelay(SporadicTask t, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<NestedResource> resources, long[][] Ris, long Ri,
                                   boolean btbHit) {
        long spin_delay = 0;
        for (int k = 0; k < t.resource_required_index.size(); k++) {
            NestedResource resource = resources.get(t.resource_required_index.get(k));
            int NoRFromT = t.number_of_access_in_one_release.get(getIndexRInTask(t, resource));
            spin_delay += resourceAccessCost(t, tasks, resources, Ris, Ri, btbHit, resource, NoRFromT);
        }
        return spin_delay;
    }

    /* Gets the total access cost to a resource from a given entry point and calling task by calculating
     * recursively the cost of inner resources and own access cost and multiplying it by the number of spin delay
     * and own accesses*/
    private long resourceAccessCost (SporadicTask t, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<NestedResource> resources, long[][] Ris, long Ri,
                                     boolean btbHit, NestedResource resource, int NoRFromT){
        long access_time = resource.csl;
        for (int i = 0; i < resource.inner_resources.size(); i++){
            NestedResource inner = resources.get(resource.inner_resources.get(i));
            int n = resource.number_accesses_inner_resource.get(i);
            access_time += resourceAccessCost(t, tasks, resources, Ris, Ri, btbHit, inner, n);
        }
        return access_time * (getNoSpinDelay(t, resource, tasks, resources, Ris, Ri, btbHit, NoRFromT) + NoRFromT);
    }

    /* Calculates the number of remote spin delay to be accounted for on the access analysis */
    private int getNoSpinDelay(SporadicTask task, NestedResource resource, ArrayList<ArrayList<SporadicTask>> tasks,
                               ArrayList<NestedResource> resources, long[][] Ris, long Ri, boolean btbHit, int NoRFromT) {
        int number_of_spin_delay = 0;

        /* In the case of nested resources, all tasks can cause spin delay, even local ones (result of being helped) */
        int number_of_requests = 0;
        for (int i = 0; i < tasks.size(); i++) {
            /* Number of requests per partition (Np) */
            for (int j = 0; j < tasks.get(i).size(); j++) {
                SporadicTask remote_task = tasks.get(i).get(j);
                /* Check that we are not including the task under analysis*/
                if (!remote_task.equals(task)) {
                    if (true){
                    /* Calculate the number of accesses by the remote task */
                    //if (remote_task.resource_required_index.contains(resource.id - 1)) {
                        int indexR = getIndexRInTask(remote_task, resource);
                        int number_of_release = (int) Math.ceil((double) (Ri + (btbHit ? Ris[i][j] : 0)) / (double) remote_task.period);
                        /* For the total accesses to the resource we need also nested accesses */
                        number_of_requests += number_of_release * getTotalAccessesResource(remote_task, resource, resources);
                    }
                }
            }
        }

        /* HP tasks requests during the period of study (Nh)*/
        int getNoRFromHP = getNoRFromHP(resource, task, tasks.get(task.partition), resources, Ris[task.partition], Ri, btbHit);
        /* Remaining requests (NS) */
        int possible_spin_delay = number_of_requests - getNoRFromHP < 0 ? 0 : number_of_requests - getNoRFromHP;

        /* The spin delay should be, for each access the number of remaining Q(l)(n)*/
        /* Calculate spin delay on each access */
        for (int n = 0; n < NoRFromT; n++){
            /* For each access calculate NS - already accounted for accesses */
            int iter = possible_spin_delay - (n * q.get(resource.id-1)-1);
            /* If there is spin delay */
            if (iter > 0){
                /* Check that the spin delay to account for on this iteration is not higher than the longest
                 * possible queue for the resource */
                if (iter < q.get(resource.id-1))
                    /* If bigger than 0but lower than lowest queue, its a valid calue */
                    number_of_spin_delay += iter;
                else
                    /* If it is bigger or equal the longest queue, account only the longest queue (except itself) */
                    number_of_spin_delay += q.get(resource.id-1) - 1;
            }
        }

        return number_of_spin_delay;
    }

    /*
     * gives the number of requests from remote partitions for a resource that
     * is required by the given task.
     */
    private int getNoRRemote(NestedResource resource, ArrayList<SporadicTask> tasks, ArrayList<NestedResource> resources, long[] Ris, long Ri, boolean btbHit) {
        int number_of_request_by_Remote_P = 0;

        /* For each remote partition, number of requests (Np) */
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).resource_required_index.contains(resource.id - 1)) {
                SporadicTask remote_task = tasks.get(i);
                int indexR = getIndexRInTask(remote_task, resource);
                number_of_request_by_Remote_P += Math.ceil((double) (Ri + (btbHit ? Ris[i] : 0)) / (double) remote_task.period)
                        * getTotalAccessesResource(remote_task, resource, resources);
            }
        }
        return number_of_request_by_Remote_P;
    }

    /*
     * gives that number of requests from HP local tasks for a resource that is
     * required by the given task.
     */
    private int getNoRFromHP(NestedResource resource, SporadicTask task, ArrayList<SporadicTask> tasks, ArrayList<NestedResource> resources,
                             long[] Ris, long Ri, boolean btbHit) {
        int number_of_request_by_HP = 0;
        int priority = task.priority;

        /* For each local task, check if priority is higher, then compute number of requests (Nh) */
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).priority > priority && tasks.get(i).resource_required_index.contains(resource.id - 1)) {
                SporadicTask hpTask = tasks.get(i);
                int indexR = getIndexRInTask(hpTask, resource);
                number_of_request_by_HP += Math.ceil((double) (Ri + (btbHit ? Ris[i] : 0)) / (double) hpTask.period)
                        * getTotalAccessesResource(hpTask, resource, resources);
            }
        }
        return number_of_request_by_HP;
    }

    /* Returns the total number of accesses to a given resource from an entry resource */
    private int getTotalAccessesResource (NestedResource source, NestedResource target, ArrayList<NestedResource> resources, int NoAccesses){
        int accesses = 0;
        /* Check if the resource is accessed from this resource by checking each inner resource
         * If an inner resource is the target, sum the requests to the number of accesses
         * Otherwise go deeper in the nesting */
        for (int i = 0; i < source.inner_resources.size(); i++){
            NestedResource inner = resources.get(source.inner_resources.get(i));
            int InnerAccesses = source.number_accesses_inner_resource.get(i);
            if (inner.equals(target))
                accesses += InnerAccesses;
            else
                accesses += getTotalAccessesResource(inner, target, resources, InnerAccesses);
        }
        return accesses * NoAccesses;
    }

    /* Returns the total number of accesses to a given resource from a task */
    private int getTotalAccessesResource (SporadicTask source, NestedResource target, ArrayList<NestedResource> resources){
        int accesses = 0;
        /* For each accessed resource: if it is the studied resource, then just add the already known accesses
         *                             if another resource, study inner accesses */
        for (int i = 0; i < source.resource_required_index.size(); i++){
            NestedResource inner = resources.get(source.resource_required_index.get(i));
            int InnerAccesses = source.number_of_access_in_one_release.get(i);
            if (inner.equals(target))
                accesses += InnerAccesses;
            else
                accesses += getTotalAccessesResource(inner, target, resources, InnerAccesses);
        }
        return accesses;
    }

    /*
     * Return the index of a given resource in stored in a task.
     */
    private int getIndexRInTask(SporadicTask task, NestedResource resource) {
        int indexR = -1;
        if (task.resource_required_index.contains(resource.id - 1)) {
            for (int j = 0; j < task.resource_required_index.size(); j++) {
                if (resource.id - 1 == task.resource_required_index.get(j)) {
                    indexR = j;
                    break;
                }
            }
        }
        return indexR;
    }

    /* Calculates the longest possible queue for a given resource taking into account:
     * Its usage by tasks directly
     * Its outer resources
     * The nested usage of tasks via outer resources
     */
    private ArrayList<Integer> calculateQ (ArrayList<NestedResource> resources){
        ArrayList<Integer> resourceQ = new ArrayList<>();
        g = calculateG(resources);
        for (int i = 0; i < resources.size(); i++){
            NestedResource res = resources.get(i);
            if (res.outer_resources.size() > 0){
                /* In links */
                int in = res.partitions.size() + res.outer_resources.size();
                /* G size */
                int tasks = g.get(i).size();
                /* Add the min value */
                resourceQ.add(min(in, tasks));
            }
            else{
                resourceQ.add(res.partitions.size());
            }
        }
        return resourceQ;
    }

    private ArrayList<ArrayList<SporadicTask>> calculateG (ArrayList<NestedResource> resources){
        ArrayList<ArrayList<SporadicTask>> resourceG = new ArrayList<>();
        for (int i = 0; i < resources.size(); i++) {
            NestedResource res = resources.get(i);
            ArrayList<SporadicTask> tasks = new ArrayList<>();
            /* Include tasks that access the resource directly */
            for (int t = 0; t < res.requested_tasks.size(); t++) {
                tasks.add(res.requested_tasks.get(t));
            }
            /* Add those inherited by outer resources.
             * Since resources are ordered, outer resources tasks are already calculated */
            for (int o = 0; o < res.outer_resources.size(); o ++){
                ArrayList<SporadicTask> outerTasks = resourceG.get(res.outer_resources.get(o));
                for (int t = 0; t < outerTasks.size(); t++){
                    if (!tasks.contains(outerTasks.get(t))){
                        tasks.add(outerTasks.get(t));
                    }
                }
            }
            resourceG.add(tasks);
        }
        return resourceG;
    }

}
