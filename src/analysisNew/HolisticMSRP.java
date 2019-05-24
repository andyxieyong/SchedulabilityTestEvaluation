package analysisNew;

import java.util.ArrayList;

import entity.Resource;
import entity.SporadicTask;
import generatorTools.PriorityGeneator;
import utils.AnalysisUtils;

public class HolisticMSRP {

	public long[][] getResponseTimeDM(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, boolean btbHit, boolean useRi,
			boolean printDebug) {
		if (tasks == null)
			return null;

		// assign priorities by Deadline Monotonic
		tasks = new PriorityGeneator().assignPrioritiesByDM(tasks);

		long count = 0;
		boolean isEqual = false, missdeadline = false;
		long[][] response_time = new AnalysisUtils().initResponseTime(tasks);

		/* a huge busy window to get a fixed Ri */
		while (!isEqual) {
			isEqual = true;
			long[][] response_time_plus = busyWindow(tasks, resources, response_time, btbHit, useRi);

			for (int i = 0; i < response_time_plus.length; i++) {
				for (int j = 0; j < response_time_plus[i].length; j++) {
					if (response_time[i][j] != response_time_plus[i][j])
						isEqual = false;
					if (response_time_plus[i][j] > tasks.get(i).get(j).deadline)
						missdeadline = true;

				}
			}

			count++;
			new AnalysisUtils().cloneList(response_time_plus, response_time);

			if (missdeadline)
				break;
		}

		if (printDebug) {
			System.out.println("FIFONP JAVA    after " + count + " tims of recursion, we got the response time.");
			new AnalysisUtils().printResponseTime(response_time, tasks);
		}

		return response_time;
	}

	private long[][] busyWindow(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, long[][] response_time, boolean btbHit,
			boolean useRi) {
		long[][] response_time_plus = new long[tasks.size()][];

		for (int i = 0; i < response_time.length; i++) {
			response_time_plus[i] = new long[response_time[i].length];
		}

		for (int i = 0; i < tasks.size(); i++) {
			for (int j = 0; j < tasks.get(i).size(); j++) {
				SporadicTask task = tasks.get(i).get(j);
				if (response_time[i][j] > task.deadline) {
					response_time_plus[i][j] = response_time[i][j];
					continue;
				}

				task.spin = spinDelay(task, tasks, resources, response_time, response_time[i][j], btbHit, useRi);
				task.interference = highPriorityInterference(task, tasks, response_time[i][j]);
				task.local = localBlocking(task, tasks, resources, response_time, response_time[i][j], btbHit, useRi);
				response_time_plus[i][j] = task.Ri = task.WCET + task.spin + task.interference + task.local;

				if (task.Ri > task.deadline) {
					return response_time_plus;
				}
			}
		}
		return response_time_plus;
	}

	/*
	 * Calculate the spin delay for a given task t.
	 */
	private long spinDelay(SporadicTask t, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, long[][] Ris, long Ri, boolean btbHit,
			boolean useRi) {
		long spin_delay = 0;

		for (int k = 0; k < resources.size(); k++) {
			Resource resource = resources.get(k);
			int noqT = t.resource_required_index.contains(resource.id - 1)
					? t.number_of_access_in_one_release.get(t.resource_required_index.indexOf(resource.id - 1))
					: 0;
			int noqHT = getNoRFromHP(t, resource, tasks.get(t.partition), Ri);
			int spin = noqT + noqHT;

			for (int j = 0; j < tasks.size(); j++) {
				if (j != t.partition) {
					int noqRemote = getNoRRemote(resource, tasks.get(j), Ris[j], Ri, btbHit, useRi);
					spin += Math.min(noqT + noqHT, noqRemote);
				}
			}
			spin_delay += spin;
		}

		return spin_delay;
	}

	/*
	 * Calculate the local high priority tasks' interference for a given task t.
	 * CI is a set of computation time of local tasks, including spin delay.
	 */
	private long highPriorityInterference(SporadicTask t, ArrayList<ArrayList<SporadicTask>> allTasks, long Ri) {
		long interference = 0;
		int partition = t.partition;
		ArrayList<SporadicTask> tasks = allTasks.get(partition);

		for (int i = 0; i < tasks.size(); i++) {
			if (tasks.get(i).priority > t.priority) {
				SporadicTask hpTask = tasks.get(i);
				interference += Math.ceil((double) (Ri) / (double) hpTask.period) * (hpTask.WCET);
			}
		}
		return interference;
	}

	private long localBlocking(SporadicTask t, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, long[][] Ris, long Ri, boolean btbHit,
			boolean useRi) {
		ArrayList<Resource> LocalBlockingResources = getLocalBlockingResources(t, resources, tasks.get(t.partition));
		ArrayList<Long> local_blocking_each_resource = new ArrayList<>();

		for (int i = 0; i < LocalBlockingResources.size(); i++) {
			Resource res = LocalBlockingResources.get(i);
			long local_blocking = res.csl;

			if (res.isGlobal) {
				for (int parition_index = 0; parition_index < res.partitions.size(); parition_index++) {
					int partition = res.partitions.get(parition_index);
					int norHP = getNoRFromHP(t, res, tasks.get(t.partition), Ri);
					int norT = t.resource_required_index.contains(res.id - 1)
							? t.number_of_access_in_one_release.get(t.resource_required_index.indexOf(res.id - 1))
							: 0;
					int norR = getNoRRemote(res, tasks.get(partition), Ris[partition], Ri, btbHit, useRi);

					if (partition != t.partition && (norHP + norT) < norR) {
						local_blocking += res.csl;
					}
				}
			}
			local_blocking_each_resource.add(local_blocking);
		}

		if (local_blocking_each_resource.size() >= 1) {
			local_blocking_each_resource.sort((l1, l2) -> -Double.compare(l1, l2));
		}

		return local_blocking_each_resource.size() > 0 ? local_blocking_each_resource.get(0) : 0;
	}

	private ArrayList<Resource> getLocalBlockingResources(SporadicTask task, ArrayList<Resource> resources, ArrayList<SporadicTask> localTasks) {
		ArrayList<Resource> localBlockingResources = new ArrayList<>();
		int partition = task.partition;

		for (int i = 0; i < resources.size(); i++) {
			Resource resource = resources.get(i);
			// local resources that have a higher ceiling
			if (resource.partitions.size() == 1 && resource.partitions.get(0) == partition && resource.getCeilingForProcessor(localTasks) >= task.priority) {
				for (int j = 0; j < resource.requested_tasks.size(); j++) {
					SporadicTask LP_task = resource.requested_tasks.get(j);
					if (LP_task.partition == partition && LP_task.priority < task.priority) {
						localBlockingResources.add(resource);
						break;
					}
				}
			}
			// global resources that are accessed from the partition
			if (resource.partitions.contains(partition) && resource.partitions.size() > 1) {
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
	 * gives that number of requests from HP local tasks for a resource that is
	 * required by the given task.
	 */
	private int getNoRFromHP(SporadicTask task, Resource resource, ArrayList<SporadicTask> tasks, long Ri) {
		int number_of_request_by_HP = 0;
		int priority = task.priority;

		for (int i = 0; i < tasks.size(); i++) {
			if (tasks.get(i).priority > priority && tasks.get(i).resource_required_index.contains(resource.id - 1)) {
				SporadicTask hpTask = tasks.get(i);
				int indexR = getIndexRInTask(hpTask, resource);
				number_of_request_by_HP += Math.ceil((double) Ri / (double) hpTask.period) * hpTask.number_of_access_in_one_release.get(indexR);
			}
		}
		return number_of_request_by_HP;
	}

	private int getNoRRemote(Resource resource, ArrayList<SporadicTask> tasks, long[] Ris, long Ri, boolean btbHit, boolean useRi) {
		int number_of_request_by_Remote_P = 0;

		for (int i = 0; i < tasks.size(); i++) {
			if (tasks.get(i).resource_required_index.contains(resource.id - 1)) {
				SporadicTask remote_task = tasks.get(i);
				int indexR = getIndexRInTask(remote_task, resource);
				number_of_request_by_Remote_P += (Math
						.ceil((double) (Ri + (btbHit ? (useRi ? Ris[i] : remote_task.deadline) : 0)) / (double) remote_task.period))
						* remote_task.number_of_access_in_one_release.get(indexR);
			}
		}
		return number_of_request_by_Remote_P;
	}

	/*
	 * Return the index of a given resource stored in a task.
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

}
