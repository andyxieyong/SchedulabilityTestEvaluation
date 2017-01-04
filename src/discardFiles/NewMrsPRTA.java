package discardFiles;

import java.util.ArrayList;

import entity.Resource;
import entity.SporadicTask;

public class NewMrsPRTA {

	private boolean use_deadline_insteadof_Ri = false;
	long count = 0;
	SporadicTask problemtask = null;
	int isTestPrint = 0;

	public long[][] NewMrsPRTATest(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, boolean printDebug) {
		long[][] init_Ri = initResponseTime(tasks);

		long[][] response_time = new long[tasks.size()][];
		boolean isEqual = false, missDeadline = false;
		count = 0;

		for (int i = 0; i < init_Ri.length; i++) {
			response_time[i] = new long[init_Ri[i].length];
		}

		cloneList(init_Ri, response_time);

		// printResponseTime(response_time, tasks);

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
			// if (count > 100 && isTestPrint <= 1) {
			// if (count % 100000 == 0) {
			// System.out.println("remian 0:");
			// isResponseTimeEqual(response_time, response_time_plus, tasks);
			// }
			// if (count % 100000 == 1) {
			// System.out.println("remian 1:");
			// isResponseTimeEqual(response_time, response_time_plus, tasks);
			// }
			// }

			cloneList(response_time_plus, response_time);

			// if (count > 100 && isTestPrint <= 1) {
			// if (count % 100000 == 0 || count % 100000 == 1) {
			// printResponseTime(response_time, tasks);
			// isTestPrint++;
			// }
			// }

			if (missDeadline)
				break;
		}
		if (missDeadline)
			System.out.println("after " + count + " tims of recursion, the tasks miss the deadline.");
		else
			System.out.println("after " + count + " tims of recursion, we got the response time.");

		if (printDebug)
			printResponseTime(response_time, tasks);

		return response_time;
	}

	public boolean isResponseTimeEqual(long[][] oldRi, long[][] newRi, ArrayList<ArrayList<SporadicTask>> tasks) {
		boolean is_equal = true;

		for (int i = 0; i < oldRi.length; i++) {
			for (int j = 0; j < oldRi[i].length; j++) {
				if (oldRi[i][j] != newRi[i][j]) {
					is_equal = false;
					System.out.println("not equal: " + oldRi[i][j] + " vs " + newRi[i][j]);
					System.out.println("T" + tasks.get(i).get(j).id + " old: S = " + tasks.get(i).get(j).spin + ", I = "
							+ tasks.get(i).get(j).interference + ", Local =" + tasks.get(i).get(j).local);
					problemtask = tasks.get(i).get(j);
				}
			}
		}
		System.out.println();
		return is_equal;
	}

	private long[][] busyWindow(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, long[][] response_time) {

		long[][] response_time_plus = new long[tasks.size()][];

		for (int i = 0; i < response_time.length; i++) {
			response_time_plus[i] = new long[response_time[i].length];
		}

		for (int i = 0; i < tasks.size(); i++) {

			for (int j = 0; j < tasks.get(i).size(); j++) {
				SporadicTask task = tasks.get(i).get(j);

				task.spin = directRemoteDelay(task, tasks, resources, response_time, response_time[i][j]);
				task.interference = highPriorityInterference(task, tasks, response_time[i][j], response_time, resources);
				task.local = localBlocking(task, tasks, resources, response_time, response_time[i][j]);
				task.Ri = task.WCET + task.pure_resource_execution_time + task.spin + task.interference + task.local;
				response_time_plus[i][j] = task.Ri;

			}
		}
		return response_time_plus;

	}

	/*
	 * Calculate the local high priority tasks' interference for a given task t.
	 * CI is a set of computation time of local tasks, including spin delay.
	 */
	private long highPriorityInterference(SporadicTask t, ArrayList<ArrayList<SporadicTask>> allTasks, long Ri, long[][] Ris,
			ArrayList<Resource> resources) {
		long interference = 0;
		int partition = t.partition;
		ArrayList<SporadicTask> tasks = allTasks.get(partition);

		for (int i = 0; i < tasks.size(); i++) {
			if (tasks.get(i).priority > t.priority) {
				SporadicTask hpTask = tasks.get(i);
				interference += Math.ceil((double) (Ri) / (double) hpTask.period) * (hpTask.WCET + hpTask.pure_resource_execution_time + hpTask.spin);

				long btb_interference = getBackToBackHitTime(hpTask, Ri, Ris[partition][i], Ris, allTasks, resources);
				interference += btb_interference;

			}
		}
		return interference;
	}

	/*
	 * for a high priority task hpTask, return its back to back hit time when
	 * the given task is pending
	 */
	private long getBackToBackHitTime(SporadicTask hpTask, long Ri, long Rihp, long[][] Ris, ArrayList<ArrayList<SporadicTask>> allTasks,
			ArrayList<Resource> resources) {
		long BTBhit = 0;

		for (int i = 0; i < hpTask.resource_required_index.size(); i++) {

			/* for each resource that a high priority task request */
			Resource resource = resources.get(hpTask.resource_required_index.get(i));

			int number_of_higher_request = getNoRFromHP(resource, hpTask, allTasks.get(hpTask.partition), Ris[hpTask.partition], Ri);
			int number_of_request_with_btb = (int) Math
					.ceil((double) (Ri + (use_deadline_insteadof_Ri ? hpTask.deadline : Rihp)) / (double) hpTask.period)
					* hpTask.number_of_access_in_one_release.get(i);
			int number_of_request_without_btb = (int) Math.ceil((double) Ri / (double) hpTask.period) * hpTask.number_of_access_in_one_release.get(i);

			// System.out.println("withbtb: " + number_of_request_with_btb + ",
			// without btb: " + number_of_request_without_btb);

			for (int j = 0; j < resource.partitions.size(); j++) {
				if (resource.partitions.get(j) != hpTask.partition) {
					int remote_partition = resource.partitions.get(j);
					int number_of_remote_request = getNoRRemote(resource, allTasks.get(remote_partition), Ris[remote_partition], Ri);

					int possible_spin_delay = number_of_remote_request - number_of_higher_request < 0 ? 0
							: number_of_remote_request - number_of_higher_request;

					int spin_delay_with_btb = Integer.min(possible_spin_delay, number_of_request_with_btb);
					int spin_delay_without_btb = Integer.min(possible_spin_delay, number_of_request_without_btb);

					BTBhit += (spin_delay_with_btb - spin_delay_without_btb) * resource.csl;
				}
			}

		}
		return BTBhit;
	}

	/*
	 * Calculate the spin delay for a given task t.
	 */
	private long directRemoteDelay(SporadicTask t, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, long[][] Ris, long Ri) {
		long spin_delay = 0;
		for (int k = 0; k < t.resource_required_index.size(); k++) {
			Resource resource = resources.get(t.resource_required_index.get(k));
			spin_delay += getNoSpinDelay(t, resource, tasks, Ris, Ri) * resource.csl;
		}
		return spin_delay;
	}

	/*
	 * Calculate the local blocking for task t.
	 */
	private long localBlocking(SporadicTask t, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, long[][] Ris, long Ri) {
		ArrayList<Resource> LocalBlockingResources = getLocalBlockingResources(t, resources);
		ArrayList<Long> local_blocking_each_resource = new ArrayList<>();

		for (int i = 0; i < LocalBlockingResources.size(); i++) {
			Resource res = LocalBlockingResources.get(i);
			long local_blocking = res.csl;

			if (res.isGlobal) {
				for (int parition_index = 0; parition_index < res.partitions.size(); parition_index++) {
					int partition = res.partitions.get(parition_index);
					int norHP = getNoRFromHP(res, t, tasks.get(t.partition), Ris[t.partition], Ri);
					int norT = t.resource_required_index.contains(res.id - 1)
							? t.number_of_access_in_one_release.get(t.resource_required_index.indexOf(res.id - 1)) : 0;
					int norR = getNoRRemote(res, tasks.get(partition), Ris[partition], Ri);

					if (partition != t.partition && (norHP + norT) < norR) {
						local_blocking += res.csl;
					}
				}
			}
			local_blocking_each_resource.add(local_blocking);
		}

		if (local_blocking_each_resource.size() > 1)
			local_blocking_each_resource.sort((l1, l2) -> -Double.compare(l1, l2));

		return local_blocking_each_resource.size() > 0 ? local_blocking_each_resource.get(0) : 0;

	}

	private long[][] initResponseTime(ArrayList<ArrayList<SporadicTask>> tasks) {
		long[][] response_times = new long[tasks.size()][];

		for (int i = 0; i < tasks.size(); i++) {
			ArrayList<SporadicTask> task_on_a_partition = tasks.get(i);
			task_on_a_partition.sort((t1, t2) -> -Integer.compare(t1.priority, t2.priority));

			long[] Ri = new long[task_on_a_partition.size()];

			for (int j = 0; j < task_on_a_partition.size(); j++) {
				SporadicTask t = task_on_a_partition.get(j);
				Ri[j] = t.WCET + t.pure_resource_execution_time;
				t.Ri = t.spin = t.local = t.interference = 0;
			}

			response_times[i] = Ri;
		}
		return response_times;
	}

	/*
	 * gives the number of requests from remote partitions for a resource that
	 * is required by the given task.
	 */
	private int getNoSpinDelay(SporadicTask task, Resource resource, ArrayList<ArrayList<SporadicTask>> tasks, long[][] Ris, long Ri) {
		int number_of_spin_dealy = 0;

		for (int i = 0; i < tasks.size(); i++) {
			if (i != task.partition) {
				/* For each remote partition */
				int number_of_request_by_Remote_P = 0;
				for (int j = 0; j < tasks.get(i).size(); j++) {
					if (tasks.get(i).get(j).resource_required_index.contains(resource.id - 1)) {
						SporadicTask remote_task = tasks.get(i).get(j);
						int indexR = getIndexRInTask(remote_task, resource);
						int number_of_release = (int) Math
								.ceil((double) (Ri + (use_deadline_insteadof_Ri ? remote_task.deadline : Ris[i][j])) / (double) remote_task.period);
						number_of_request_by_Remote_P += number_of_release * remote_task.number_of_access_in_one_release.get(indexR);
					}
				}
				int getNoRFromHP = getNoRFromHP(resource, task, tasks.get(task.partition), Ris[task.partition], Ri);
				int possible_spin_delay = number_of_request_by_Remote_P - getNoRFromHP < 0 ? 0 : number_of_request_by_Remote_P - getNoRFromHP;

				int NoRFromT = task.number_of_access_in_one_release.get(getIndexRInTask(task, resource));
				number_of_spin_dealy += Integer.min(possible_spin_delay, NoRFromT);

			}
		}
		return number_of_spin_dealy;
	}

	private int getNoRRemote(Resource resource, ArrayList<SporadicTask> tasks, long[] Ris, long Ri) {
		int number_of_request_by_Remote_P = 0;

		for (int i = 0; i < tasks.size(); i++) {
			if (tasks.get(i).resource_required_index.contains(resource.id - 1)) {
				SporadicTask remote_task = tasks.get(i);
				int indexR = getIndexRInTask(remote_task, resource);
				number_of_request_by_Remote_P += Math
						.ceil((double) (Ri + (use_deadline_insteadof_Ri ? remote_task.deadline : Ris[i])) / (double) remote_task.period)
						* remote_task.number_of_access_in_one_release.get(indexR);
			}

		}

		return number_of_request_by_Remote_P;
	}

	/*
	 * gives that number of requests from HP local tasks for a resource that is
	 * required by the given task.
	 */
	private int getNoRFromHP(Resource resource, SporadicTask task, ArrayList<SporadicTask> tasks, long[] Ris, long Ri) {
		int number_of_request_by_HP = 0;
		int priority = task.priority;

		for (int i = 0; i < tasks.size(); i++) {
			if (tasks.get(i).priority > priority && tasks.get(i).resource_required_index.contains(resource.id - 1)) {
				SporadicTask hpTask = tasks.get(i);
				int indexR = getIndexRInTask(hpTask, resource);
				number_of_request_by_HP += Math.ceil((double) (Ri + (use_deadline_insteadof_Ri ? hpTask.deadline : Ris[i])) / (double) hpTask.period)
						* hpTask.number_of_access_in_one_release.get(indexR);
			}
		}
		return number_of_request_by_HP;
	}

	/*
	 * gives a set of resources that can cause local blocking for a given task
	 */
	private ArrayList<Resource> getLocalBlockingResources(SporadicTask task, ArrayList<Resource> resources) {
		ArrayList<Resource> localBlockingResources = new ArrayList<>();
		int partition = task.partition;

		for (int i = 0; i < resources.size(); i++) {
			Resource resource = resources.get(i);

			if (resource.partitions.contains(partition) && resource.ceiling.get(resource.partitions.indexOf(partition)) >= task.priority) {
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

	/*
	 * Return the index of a given resource in stored in a task.
	 */
	private int getIndexRInTask(SporadicTask task, Resource resource) {
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

	public void printResponseTime(long[][] Ris, ArrayList<ArrayList<SporadicTask>> tasks) {
		int task_id = 1;
		for (int i = 0; i < Ris.length; i++) {
			for (int j = 0; j < Ris[i].length; j++) {
				System.out.println("T" + task_id + " RT: " + Ris[i][j] + ", deadline: " + tasks.get(i).get(j).deadline + ", S = "
						+ tasks.get(i).get(j).spin + ", L = " + tasks.get(i).get(j).local + ", I = " + tasks.get(i).get(j).interference);
				task_id++;
			}
			System.out.println();
		}
	}

	public void cloneList(long[][] oldList, long[][] newList) {
		for (int i = 0; i < oldList.length; i++) {
			for (int j = 0; j < oldList[i].length; j++) {
				newList[i][j] = oldList[i][j];
			}
		}
	}
}