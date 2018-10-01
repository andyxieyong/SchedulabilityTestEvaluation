package analysisNew;

import entity.NestedResource;
import entity.ResourceGroup;
import entity.SporadicTask;
import utils.AnalysisUtils;

import java.util.ArrayList;

public class PWLPNestedNew extends PWLPNew {

    private int id = 1;

    public PWLPNestedNew (){}

    public long[][] getNestedResponseTime(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<NestedResource> resources, boolean btbHit, boolean printDebug) {
        long[][] init_Ri = new AnalysisUtils().initResponseTime(tasks);

        long[][] response_time = new long[tasks.size()][];
        boolean isEqual = false, missDeadline = false;
        count = 0;

        for (int i = 0; i < init_Ri.length; i++) {
            response_time[i] = new long[init_Ri[i].length];
        }

        /* Create the resource grouping for the nested analysis */
        ArrayList<ResourceGroup> groups = new ArrayList<ResourceGroup>();
        groupResources(resources, groups);

        new AnalysisUtils().cloneList(init_Ri, response_time);

        /* a huge busy window to get a fixed Ri */
        while (!isEqual) {
            isEqual = true;
            long[][] response_time_plus = busyWindow(tasks, resources, groups, response_time, btbHit);

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
                System.out.println("FIFO-P-NEW    after " + count + " tims of recursion, the tasks miss the deadline.");
            else
                System.out.println("FIFO-P-NEW    after " + count + " tims of recursion, we got the response time.");
            new AnalysisUtils().printResponseTime(response_time, tasks);
        }

        return response_time;
    }

    private long[][] busyWindow(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<NestedResource> resources,
                                ArrayList<ResourceGroup> groups, long[][] response_time, boolean btbHit) {
        long[][] response_time_plus = new long[tasks.size()][];

        for (int i = 0; i < response_time.length; i++) {
            response_time_plus[i] = new long[response_time[i].length];
        }

        for (int i = 0; i < tasks.size(); i++) {
            for (int j = 0; j < tasks.get(i).size(); j++) {
                SporadicTask task = tasks.get(i).get(j);
                task.spin_delay_by_preemptions = 0;
                task.spin = getSpinDelay(task, tasks, resources, groups, response_time[i][j], response_time, btbHit);
                task.interference = highPriorityInterference(task, tasks, response_time[i][j], response_time, resources);
                task.local = localBlocking(task, tasks, resources, response_time, response_time[i][j]);
                response_time_plus[i][j] = task.Ri = task.WCET + task.spin + task.interference + task.local;

                if (task.Ri > task.deadline)
                    return response_time_plus;

            }
        }
        return response_time_plus;
    }

    private long getSpinDelay(SporadicTask task, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<NestedResource> resources,
                              ArrayList<ResourceGroup> groups, long time, long[][] Ris, boolean btbHit) {
        long spin = 0;
        /* Count number of requests on remote processors per resource */
        ArrayList<ArrayList<Long>> requestsLeftOnRemoteP = new ArrayList<>();
        for (int i = 0; i < groups.size(); i++) {
            requestsLeftOnRemoteP.add(new ArrayList<Long>());
            ResourceGroup group = groups.get(i);
            spin += getSpinDelayForOneGroup(task, tasks, group, groups, time, Ris, requestsLeftOnRemoteP.get(i), btbHit);
        }

        /* Count number of potential preemptions */
        long preemptions = 0;
        long request_by_preemptions = 0;
        for (int i = 0; i < tasks.get(task.partition).size(); i++) {
            if (tasks.get(task.partition).get(i).priority > task.priority) {
                preemptions += (int) Math.ceil((double) (time) / (double) tasks.get(task.partition).get(i).period);
            }
        }

        /* For each preemption, calculate the maximum cost of being preempted while spinning depending on remote processor accesses */
        while (preemptions > 0) {

            long max_delay = 0;
            int max_delay_resource_index = -1;
            for (int i = 0; i < groups.size(); i++) {
                if (max_delay < groups.get(i).max_csl * requestsLeftOnRemoteP.get(i).size()) {
                    max_delay = groups.get(i).max_csl * requestsLeftOnRemoteP.get(i).size();
                    max_delay_resource_index = i;
                }
            }

            if (max_delay > 0) {
                spin += max_delay;
                for (int i = 0; i < requestsLeftOnRemoteP.get(max_delay_resource_index).size(); i++) {
                    requestsLeftOnRemoteP.get(max_delay_resource_index).set(i, requestsLeftOnRemoteP.get(max_delay_resource_index).get(i) - 1);
                    if (requestsLeftOnRemoteP.get(max_delay_resource_index).get(i) < 1) {
                        requestsLeftOnRemoteP.get(max_delay_resource_index).remove(i);
                        i--;
                    }
                }
                preemptions--;
                request_by_preemptions++;
            } else
                break;
        }

        task.spin_delay_by_preemptions = request_by_preemptions;

        return spin;
    }

    private long getSpinDelayForOneGroup(SporadicTask task, ArrayList<ArrayList<SporadicTask>> tasks, ResourceGroup group,
                                            ArrayList<ResourceGroup> groups, long time, long[][] Ris, ArrayList<Long> requestsLeftOnRemoteP, boolean btbHit) {
        long spin = 0;
        long ncs = 0;

        /* For each task on partition under analysis */
        for (int i = 0; i < tasks.get(task.partition).size(); i++) {
            SporadicTask hpTask = tasks.get(task.partition).get(i);
            /* If the task has a higher priority, lets get the number of access requests to the group */
            if (hpTask.priority > task.priority ){
                for (int j = 0; j < group.all_resources.size(); j++) {
                    NestedResource res = group.all_resources.get(j);
                    if (hpTask.resource_required_index.contains(res.id - 1)) {
                        ncs += (int) Math.ceil((double) (time + (btbHit ? Ris[hpTask.partition][i] : 0)) / (double) hpTask.period)
                                * hpTask.number_of_access_in_one_release.get(hpTask.resource_required_index.indexOf(res.id - 1));
                    }
                }
            }
        }

        /* Sum the accesses of the task under analysis */
        for (int j = 0; j < group.all_resources.size(); j++) {
            NestedResource res = group.all_resources.get(j);
            if (task.resource_required_index.contains(res.id - 1))
                ncs += task.number_of_access_in_one_release.get(task.resource_required_index.indexOf(res.id - 1));
        }


        /* If there is at least one access */
        if (ncs > 0) {
            for (int i = 0; i < tasks.size(); i++) {
                if (task.partition != i) {
                    /* For each remote partition count how many accesses are issued to the group */
                    long number_of_request_by_Remote_P = 0;
                    for (int j = 0; j < tasks.get(i).size(); j++) {
                        for (int k = 0; k < group.all_resources.size(); k++) {
                            NestedResource res = group.all_resources.get(k);
                            if (tasks.get(i).get(j).resource_required_index.contains(res.id - 1)) {
                                SporadicTask remote_task = tasks.get(i).get(j);
                                int indexR = getIndexRInTask(remote_task, res);
                                int number_of_release = (int) Math.ceil((double) (time + (btbHit ? Ris[i][j] : 0)) / (double) remote_task.period);
                                number_of_request_by_Remote_P += number_of_release * remote_task.number_of_access_in_one_release.get(indexR);
                            }
                        }
                    }

                    /* Calculate the real possible spin delay */
                    long possible_spin_delay = Long.min(number_of_request_by_Remote_P, ncs);
                    spin += possible_spin_delay;
                    if (number_of_request_by_Remote_P - ncs > 0)
                        requestsLeftOnRemoteP.add(number_of_request_by_Remote_P - ncs);
                }
            }
        }

        return spin * group.max_csl + group.max_csl;
    }


    /*
     * Calculate the local high priority tasks' interference for a given task t.
     * CI is a set of computation time of local tasks, including spin delay.
     */
    private long highPriorityInterference(SporadicTask t, ArrayList<ArrayList<SporadicTask>> allTasks, long time, long[][] Ris, ArrayList<NestedResource> resources) {
        long interference = 0;
        int partition = t.partition;
        ArrayList<SporadicTask> tasks = allTasks.get(partition);

        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).priority > t.priority) {
                SporadicTask hpTask = tasks.get(i);
                interference += Math.ceil((double) (time) / (double) hpTask.period) * (hpTask.WCET);
            }
        }
        return interference;
    }

    private long localBlocking(SporadicTask t, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<NestedResource> resources, long[][] Ris, long Ri) {
        /* Identify potentially blocking resources */
        ArrayList<NestedResource> LocalBlockingResources = getLocalBlockingResources(t, resources);
        ArrayList<Long> local_blocking_each_resource = new ArrayList<>();

        /* For each blocking resource, add its cost to the list */
        for (int i = 0; i < LocalBlockingResources.size(); i++) {
            NestedResource res = LocalBlockingResources.get(i);
            long local_blocking = res.access_time;
            local_blocking_each_resource.add(local_blocking);
        }

        /* Order by potentially blocking times */
        if (local_blocking_each_resource.size() > 1)
            local_blocking_each_resource.sort((l1, l2) -> -Double.compare(l1, l2));

        /* Return the highest value or 0 if none */
        return local_blocking_each_resource.size() > 0 ? local_blocking_each_resource.get(0) : 0;
    }

    private ArrayList<NestedResource> getLocalBlockingResources(SporadicTask task, ArrayList<NestedResource> resources) {
        ArrayList<NestedResource> localBlockingResources = new ArrayList<>();
        int partition = task.partition;

        for (int i = 0; i < resources.size(); i++) {
            NestedResource resource = resources.get(i);
            /* If the resource is accessed from the partition */
            if (resource.partitions.contains(partition)) {
                for (int j = 0; j < resource.requested_tasks.size(); j++) {
                    /* If a lower priority task accesses the resource, then it can cause local blocking */
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


    public void groupResources(ArrayList<NestedResource> resources, ArrayList<ResourceGroup> groups) {
        /* Iterate over resources */
        NestedResource res;
        for (int i = 0; i < resources.size(); i++) {
            boolean associated = false;
            res = resources.get(i);
            /* Check if it is already part of a group */
            for (int j = 0; j < groups.size(); j++) {
                if (groups.get(j).contains(res)) {
                    associated = true;
                    break;
                }
            }
            if (!associated) {
                ResourceGroup newG = new ResourceGroup(id++);
                newG.addOuterResource(res, resources);
                groups.add(newG);
            }
        }
        debugGroups(resources, groups);
    }

    public void debugGroups (ArrayList<NestedResource> resources, ArrayList<ResourceGroup> groups){
        for (int i = 0; i < resources.size(); i++) {
            NestedResource res = resources.get(i);
            int count = 0;
            for (int j = 0; j < groups.size(); j++) {
                if (groups.get(j).contains(res)) count++;
            }
            if (count != 1)
                System.out.println("Error, R" + res.id + " in " + count + " groups.");
        }
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

    /*
     * Return the group to which a resource belongs.
     */
    private ResourceGroup getResourceGroup(NestedResource res, ArrayList<ResourceGroup> groups) {
        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).contains(res))
                return groups.get(i);
        }
        return null;
    }
}

