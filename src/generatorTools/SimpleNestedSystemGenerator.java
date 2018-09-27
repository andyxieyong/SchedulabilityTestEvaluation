package generatorTools;

import entity.NestedResource;
import entity.SporadicTask;
import utils.AnalysisUtils;

import java.util.ArrayList;
import java.util.Random;

public class SimpleNestedSystemGenerator extends SimpleSystemGenerator{

    /* Resource nesting factor */
    public double rnf = 0.2;

    public SimpleNestedSystemGenerator(int minT, int maxT, int total_partitions, int totalTasks, boolean isPeriodLogUni, AnalysisUtils.CS_LENGTH_RANGE cs_len_range, AnalysisUtils.RESOURCES_RANGE numberOfResources, double rsf, int number_of_max_access) {
        super(minT, maxT, total_partitions, totalTasks, isPeriodLogUni, cs_len_range, numberOfResources, rsf, number_of_max_access);

    }

    /*
     * Generate a set of resources.
     */
    public ArrayList<NestedResource> generateNestedResources() {
        /* generate resources from partitions/2 to partitions*2 */
        Random ran = new Random();
        int number_of_resources = 0;

        switch (range) {
            case PARTITIONS:
                number_of_resources = total_partitions;
                break;
            case HALF_PARITIONS:
                number_of_resources = total_partitions / 2;
                break;
            case DOUBLE_PARTITIONS:
                number_of_resources = total_partitions * 2;
                break;
            default:
                break;
        }

        ArrayList<NestedResource> resources = new ArrayList<>(number_of_resources);

        for (int i = 0; i < number_of_resources; i++) {
            long cs_len = 0;
            if (csl == -1) {
                switch (cs_len_range) {
                    case VERY_LONG_CSLEN:
                        cs_len = ran.nextInt(300 - 200) + 201;
                        break;
                    case LONG_CSLEN:
                        cs_len = ran.nextInt(200 - 100) + 101;
                        break;
                    case MEDIUM_CS_LEN:
                        cs_len = ran.nextInt(100 - 50) + 51;
                        break;
                    case SHORT_CS_LEN:
                        cs_len = ran.nextInt(50 - 15) + 16;
                        break;
                    case VERY_SHORT_CS_LEN:
                        cs_len = ran.nextInt(15) + 1;
                        break;
                    case Random:
                        cs_len = ran.nextInt(300) + 1;
                    default:
                        break;
                }
            } else
                cs_len = csl;

            // Actually creates the new resource with the specified parameters
            NestedResource resource = new NestedResource(i + 1, cs_len);

            /* Add the resource to the array */
            resources.add(resource);
        }

        /* I hope we dont need the resources to be ordered by accessing time */
        //resources.sort((r2, r1) -> Long.compare(r1.csl, r2.csl));

        /* Give each resource an Id */
        for (int i = 0; i < resources.size(); i++) {
            NestedResource res = resources.get(i);
            res.id = i + 1;
        }

        /* Nesting generation
           Main rule: a resource can only access resources of lower Ids  */
        for (int i = 0; i < resources.size() - 1; i++) {
            NestedResource res = resources.get(i);
            /* Number of inner resources based on nesting factor and remaining resources */
            long number_of_inner_resources = Math.round(rnf * (resources.size() - i - 1));
            /* Find a suitable inner resource not already been accessed by the given resource */
            for (int j = 0; j < number_of_inner_resources; j++) {
                while (true) {
                    int resource_index = ran.nextInt(resources.size());
                    if (resource_index > i && !res.inner_resources.contains(resource_index)) {
                        res.inner_resources.add(resource_index);
                        resources.get(resource_index).outer_resources.add(i);
                        /* number of times accessed */
                        int number_of_requests = ran.nextInt(number_of_max_access) + 1;
                        res.number_accesses_inner_resource.add(number_of_requests);
                        break;
                    }
                }
            }
        }

        /* Calculate access times as sum of resource csl + inner resources csl * number_accesses */
        for (int i = resources.size() - 1; i >= 0 ; i--) {
            NestedResource res = resources.get(i);
            long access_time = res.csl;
            for (int j = 0; j < res.inner_resources.size(); j++) {
                NestedResource inner = resources.get(res.inner_resources.get(j));
                access_time += inner.access_time * res.number_accesses_inner_resource.get(j);
            }
            res.access_time = access_time;
            //System.out.println(res.toString());
        }

        return resources;
    }

    public ArrayList<ArrayList<SporadicTask>> generateNestedResourceUsage(ArrayList<SporadicTask> tasks, ArrayList<NestedResource> resources) {
        while (tasks == null)
            tasks = generateTasks();

        int fails = 0;
        Random ran = new Random();
        /* Number of tasks that will require at least one resource */
        long number_of_resource_requested_tasks = Math.round(rsf * tasks.size());

        /* Generate resource usage for the number of tasks previously determined */
        for (long l = 0; l < number_of_resource_requested_tasks; l++) {
            if (fails > 1000) {
                tasks = generateTasks();
                while (tasks == null)
                    tasks = generateTasks();
                l = 0;
                fails++;
            }
            int task_index = ran.nextInt(tasks.size());
            /* Find a task that has already not been assigned any resource usage */
            while (true) {
                if (tasks.get(task_index).resource_required_index.size() == 0)
                    break;
                task_index = ran.nextInt(tasks.size());
            }
            SporadicTask task = tasks.get(task_index);

            /* Find the number of resources that we are going to access */
            int number_of_requested_resource = ran.nextInt((int)(rnf * resources.size())) + 1;
            //int number_of_requested_resource = ran.nextInt(resources.size() + 1);
            /* Add resources not already been accessed by the given task */
            for (int j = 0; j < number_of_requested_resource; j++) {
                while (true) {
                    int resource_index = ran.nextInt(resources.size());
                    if (!task.resource_required_index.contains(resource_index)) {
                        task.resource_required_index.add(resource_index);
                        break;
                    }
                }
            }

            /* Reorder indexes by resource access time cost */
            task.resource_required_index.sort((r1, r2) -> Integer.compare(r1, r2));

            /* Calculate access time of assgined resources as Sum_of csl * number_of_accesses */
            long total_resource_execution_time = 0;
            for (int k = 0; k < task.resource_required_index.size(); k++) {
                int number_of_requests = ran.nextInt(number_of_max_access) + 1;
                task.number_of_access_in_one_release.add(number_of_requests);
                total_resource_execution_time += number_of_requests * resources.get(task.resource_required_index.get(k)).access_time;
                /* Changed csl to access time to account for inner resources */
            }

            /* Check wether the access time is longer than the remaining task WCET */
            if (total_resource_execution_time > task.WCET) {
                /* If true, the generation fails */
                l--;
                task.resource_required_index.clear();
                task.number_of_access_in_one_release.clear();
                fails++;
            } else {
                /* If not, substract the execution time from the remaining WCET */
                task.WCET = task.WCET - total_resource_execution_time;
                task.pure_resource_execution_time = total_resource_execution_time;
                if (task.resource_required_index.size() > 0) {
                    task.hasResource = 1;

                    task.resource_required_index_cpoy = new int[task.resource_required_index.size()];
                    task.number_of_access_in_one_release_copy = new int[task.number_of_access_in_one_release.size()];
                    if (task.number_of_access_in_one_release_copy.length != task.resource_required_index_cpoy.length) {
                        System.err.println("error, task copyies not equal size");
                        System.exit(-1);
                    }
                    for (int resource_index = 0; resource_index < task.resource_required_index.size(); resource_index++) {
                        task.resource_required_index_cpoy[resource_index] = task.resource_required_index.get(resource_index);
                        task.number_of_access_in_one_release_copy[resource_index] = task.number_of_access_in_one_release.get(resource_index);
                    }
                }
            }
        }

        /* Allocate tasks to partitions */
        ArrayList<ArrayList<SporadicTask>> generatedTaskSets = WorstFitAllocation(tasks, total_partitions);

        if (generatedTaskSets != null) {
            for (int i = 0; i < generatedTaskSets.size(); i++) {
                if (generatedTaskSets.get(i).size() == 0) {
                    generatedTaskSets.remove(i);
                    i--;
                }
            }

            for (int i = 0; i < generatedTaskSets.size(); i++) {
                for (int j = 0; j < generatedTaskSets.get(i).size(); j++) {
                    generatedTaskSets.get(i).get(j).partition = i;
                }
            }

            /* Assign priorities to tasks according to Deadline Monotonic */
            new PriorityGeneator().assignPrioritiesByDM(generatedTaskSets);

            /* Assign local ceiling priorities to resources */
            /* Up to here order has not changed: resources.get(0) is necessarily outermost resource,
                                                 i.e. no outer resources
                                                 resources.get(resources.size()-1) is innermost/terminal
                                                 i.e. no inner resources */
            if (resources != null && resources.size() > 0) {
                for (int i = 0; i < resources.size(); i++) {
                    NestedResource res = resources.get(i);
                    res.isGlobal = false;
                    res.partitions.clear();
                    res.requested_tasks.clear();
                    res.ceiling.clear();
                }

                /* for each resource */
                for (int i = 0; i < resources.size(); i++) {
                    NestedResource resource = resources.get(i);

                    /* for each partition */
                    for (int j = 0; j < generatedTaskSets.size(); j++) {
                        int ceiling = 0;
                        /* for each task in the given partition */
                        for (int k = 0; k < generatedTaskSets.get(j).size(); k++) {
                            SporadicTask task = generatedTaskSets.get(j).get(k);

                            if (task.resource_required_index.contains(resource.id - 1)) {
                                resource.requested_tasks.add(task);
                                ceiling = task.priority > ceiling ? task.priority : ceiling;
                                if (!resource.partitions.contains(task.partition)) {
                                    resource.partitions.add(task.partition);
                                }
                            }
                        }

                        /* for each outer resource, check if it has a ceiling on that partition */
                        for (int k = 0; k < resource.outer_resources.size(); k++) {
                            NestedResource outer = resources.get(resource.outer_resources.get(k));
                            if (outer.partitions.contains(j)){
                                ceiling = outer.ceiling.get(j) > ceiling ? outer.ceiling.get(j) : ceiling;
                                if (!resource.inherited_partitions.contains(j)) {
                                    resource.inherited_partitions.add(j);
                                }
                            }
                        }

                        if (ceiling > 0)
                            resource.all_partitions.add(j);
                            resource.ceiling.add(ceiling);
                    }

                    /* A resource is global if directly or indirectly accessed from a partition */
                    if (resource.all_partitions.size() > 1)
                        resource.isGlobal = true;
                }
            }

        } else {
            System.err.print("ERROR at resource usage, taskset is NULL!");
            System.exit(-1);
        }

        return generatedTaskSets;
    }
}
