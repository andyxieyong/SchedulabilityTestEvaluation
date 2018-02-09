package generatorTools;

import java.util.ArrayList;
import java.util.Random;

import entity.Resource;
import entity.SporadicTask;

public class SimpleSystemGenerator {
	
	public static final int MAX_PRIORITY = 1000;
	
	/* define how long the critical section can be */
	public static enum CS_LENGTH_RANGE {
		VERY_LONG_CSLEN, LONG_CSLEN, MEDIUM_CS_LEN, SHORT_CS_LEN, VERY_SHORT_CS_LEN, Random
	};

	/* define how many resources in the system */
	public static enum RESOURCES_RANGE {
		HALF_PARITIONS, /* partitions / 2 us */
		PARTITIONS, /* partitions us */
		DOUBLE_PARTITIONS, /* partitions * 2 us */
	};

	public CS_LENGTH_RANGE cs_len_range;
	long csl = -1;
	public boolean isLogUni;
	public int maxT;
	public int minT;

	public int number_of_max_access;
	public RESOURCES_RANGE range;
	public double rsf;

	public int total_tasks;
	public int total_partitions;
	public double totalUtil;
	boolean print;

	public SimpleSystemGenerator(int minT, int maxT, int total_partitions, int totalTasks, boolean isPeriodLogUni, CS_LENGTH_RANGE cs_len_range,
			RESOURCES_RANGE numberOfResources, double rsf, int number_of_max_access) {
		this.minT = minT;
		this.maxT = maxT;
		this.totalUtil = 0.1 * (double) totalTasks;
		this.total_partitions = total_partitions;
		this.total_tasks = totalTasks;
		this.isLogUni = isPeriodLogUni;
		this.cs_len_range = cs_len_range;
		this.range = numberOfResources;
		this.rsf = rsf;
		this.number_of_max_access = number_of_max_access;
		this.print = false;
	}

	/*
	 * generate task sets for multiprocessor fully partitioned fixed-priority
	 * system
	 */
	public ArrayList<SporadicTask> generateTasks() {
		ArrayList<SporadicTask> tasks = null;
		while (tasks == null) {
			tasks = generateT();
			if (tasks != null && WorstFitAllocation(tasks, total_partitions) == null)
				tasks = null;
		}
		return tasks;
	}

	private ArrayList<ArrayList<SporadicTask>> WorstFitAllocation(ArrayList<SporadicTask> tasksToAllocate, int partitions) {
		// clear tasks' partitions
		for (int i = 0; i < tasksToAllocate.size(); i++) {
			tasksToAllocate.get(i).partition = -1;
		}

		// Init allocated tasks array
		ArrayList<ArrayList<SporadicTask>> tasks = new ArrayList<>();
		for (int i = 0; i < partitions; i++) {
			ArrayList<SporadicTask> task = new ArrayList<>();
			tasks.add(task);
		}

		// init util array
		ArrayList<Double> utilPerPartition = new ArrayList<>();
		for (int i = 0; i < partitions; i++) {
			utilPerPartition.add((double) 0);
		}

		for (int i = 0; i < tasksToAllocate.size(); i++) {
			SporadicTask task = tasksToAllocate.get(i);
			int target = -1;
			double minUtil = 2;
			for (int j = 0; j < partitions; j++) {
				if (minUtil > utilPerPartition.get(j)) {
					minUtil = utilPerPartition.get(j);
					target = j;
				}
			}

			if (target == -1) {
				System.err.println("WF error!");
				return null;
			}

			if ((double) 1 - minUtil >= task.util) {
				task.partition = target;
				utilPerPartition.set(target, utilPerPartition.get(target) + task.util);
			} else
				return null;
		}

		for (int i = 0; i < tasksToAllocate.size(); i++) {
			int partition = tasksToAllocate.get(i).partition;
			tasks.get(partition).add(tasksToAllocate.get(i));
		}

		for (int i = 0; i < tasks.size(); i++) {
			tasks.get(i).sort((p1, p2) -> Double.compare(p1.period, p2.period));
		}

		return tasks;
	}

	private ArrayList<SporadicTask> generateT() {
		int task_id = 1;
		ArrayList<SporadicTask> tasks = new ArrayList<>(total_tasks);
		ArrayList<Long> periods = new ArrayList<>(total_tasks);
		Random random = new Random();

		/* generates random periods */
		while (true) {
			if (!isLogUni) {
				long period = (random.nextInt(maxT - minT) + minT) * 1000;
				if (!periods.contains(period))
					periods.add(period);
			} else {
				double a1 = Math.log(minT);
				double a2 = Math.log(maxT + 1);
				double scaled = random.nextDouble() * (a2 - a1);
				double shifted = scaled + a1;
				double exp = Math.exp(shifted);

				int result = (int) exp;
				result = Math.max(minT, result);
				result = Math.min(maxT, result);

				long period = result * 1000;
				if (!periods.contains(period))
					periods.add(period);
			}

			if (periods.size() >= total_tasks)
				break;
		}
		periods.sort((p1, p2) -> Double.compare(p1, p2));

		/* generate utils */
		UUnifastDiscard unifastDiscard = new UUnifastDiscard(totalUtil, total_tasks, 1000);
		ArrayList<Double> utils = null;
		while (true) {
			utils = unifastDiscard.getUtils();

			double tt = 0;
			for (int i = 0; i < utils.size(); i++) {
				tt += utils.get(i);
			}

			if (utils != null)
				if (utils.size() == total_tasks && tt <= totalUtil)
					break;
		}
		if (print) {
			System.out.print("task utils: ");
			double tt = 0;
			for (int i = 0; i < utils.size(); i++) {
				tt += utils.get(i);
				System.out.print(tt + "   ");
			}
			System.out.println("\n total uitls: " + tt);
		}

		/* generate sporadic tasks */
		for (int i = 0; i < utils.size(); i++) {
			long computation_time = (long) (periods.get(i) * utils.get(i));
			if (computation_time == 0) {
				return null;
			}
			SporadicTask t = new SporadicTask(-1, periods.get(i), computation_time, -1, task_id, utils.get(i));
			task_id++;
			tasks.add(t);
		}
		tasks.sort((p1, p2) -> -Double.compare(p1.util, p2.util));
		return tasks;
	}

	/*
	 * Generate a set of resources.
	 */
	public ArrayList<Resource> generateResources() {
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

		ArrayList<Resource> resources = new ArrayList<>(number_of_resources);

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

			Resource resource = new Resource(i + 1, cs_len);
			resources.add(resource);
		}

		resources.sort((r2, r1) -> Long.compare(r1.csl, r2.csl));

		for (int i = 0; i < resources.size(); i++) {
			Resource res = resources.get(i);
			res.id = i + 1;
		}

		return resources;
	}

	public ArrayList<ArrayList<SporadicTask>> generateResourceUsage(ArrayList<SporadicTask> tasks, ArrayList<Resource> resources) {
		while (tasks == null)
			tasks = generateTasks();

		int fails = 0;
		Random ran = new Random();
		long number_of_resource_requested_tasks = Math.round(rsf * tasks.size());

		/* Generate resource usage */
		for (long l = 0; l < number_of_resource_requested_tasks; l++) {
			if (fails > 1000) {
				tasks = generateTasks();
				while (tasks == null)
					tasks = generateTasks();
				l = 0;
				fails++;
			}
			int task_index = ran.nextInt(tasks.size());
			while (true) {
				if (tasks.get(task_index).resource_required_index.size() == 0)
					break;
				task_index = ran.nextInt(tasks.size());
			}
			SporadicTask task = tasks.get(task_index);

			/* Find the resources that we are going to access */
			int number_of_requested_resource = ran.nextInt(resources.size()) + 1;
			for (int j = 0; j < number_of_requested_resource; j++) {
				while (true) {
					int resource_index = ran.nextInt(resources.size());
					if (!task.resource_required_index.contains(resource_index)) {
						task.resource_required_index.add(resource_index);
						break;
					}
				}
			}
			task.resource_required_index.sort((r1, r2) -> Integer.compare(r1, r2));

			long total_resource_execution_time = 0;
			for (int k = 0; k < task.resource_required_index.size(); k++) {
				int number_of_requests = ran.nextInt(number_of_max_access) + 1;
				task.number_of_access_in_one_release.add(number_of_requests);
				total_resource_execution_time += number_of_requests * resources.get(task.resource_required_index.get(k)).csl;
			}

			if (total_resource_execution_time > task.WCET) {
				l--;
				task.resource_required_index.clear();
				task.number_of_access_in_one_release.clear();
				fails++;
			} else {
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
				
				deadlineMonotonicPriorityAssignment(generatedTaskSets.get(i), generatedTaskSets.get(i).size());
			}

			if (resources != null && resources.size() > 0) {
				for (int i = 0; i < resources.size(); i++) {
					Resource res = resources.get(i);
					res.isGlobal = false;
					res.partitions.clear();
					res.requested_tasks.clear();
					res.ceiling.clear();
				}

				/* for each resource */
				for (int i = 0; i < resources.size(); i++) {
					Resource resource = resources.get(i);

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

						if (ceiling > 0)
							resource.ceiling.add(ceiling);
					}

					if (resource.partitions.size() > 1)
						resource.isGlobal = true;
				}
			}

		} else {
			System.err.print("ERROR at resource usage, taskset is NULL!");
			System.exit(-1);
		}
		
		return generatedTaskSets;
	}
	
	public void deadlineMonotonicPriorityAssignment(ArrayList<SporadicTask> taskset, int number) {
		ArrayList<Integer> priorities = new ArrayList<>();
		for (int i = 0; i < number; i++)
			priorities.add(MAX_PRIORITY - (i + 1) * 2);
		
		/* deadline monotonic assignment */
		taskset.sort((t1, t2) -> Double.compare(t1.deadline, t2.deadline));
		priorities.sort((p1, p2) -> -Integer.compare(p1, p2));
		for (int i = 0; i < taskset.size(); i++) {
			taskset.get(i).priority = priorities.get(i);
		}
	}

}
