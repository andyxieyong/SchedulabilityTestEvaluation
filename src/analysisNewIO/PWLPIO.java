package analysisNewIO;

import java.util.ArrayList;

import entity.Resource;
import entity.SporadicTask;
import generatorTools.PriorityGeneator;
import utils.AnalysisUtils;

public class PWLPIO {

	public long[][] getResponseTimeBySBPO(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, boolean isprint) {
		if (tasks == null)
			return null;

		// Default as deadline monotonic
		tasks = new PriorityGeneator().assignPrioritiesByDM(tasks);

		// now we check each task. we begin from the task with largest deadline
		for (int i = 0; i < tasks.size(); i++) {
			ArrayList<SporadicTask> unassignedTasks = new ArrayList<>(tasks.get(i));
			int sratingP = 500 - unassignedTasks.size() * 2;
			int prioLevels = tasks.get(i).size();

			for (int currentLevel = 0; currentLevel < prioLevels; currentLevel++) {

				int startingIndex = unassignedTasks.size() - 1;
				for (int j = startingIndex; j >= 0; j--) {
					SporadicTask task = unassignedTasks.get(j);
					int originalP = task.priority;
					task.priority = sratingP;

					tasks.get(i).sort((t1, t2) -> -Integer.compare(t1.priority, t2.priority));
					long time = getResponseTimeForOneTask(task, tasks, resources, true);
					task.priority = originalP;
					tasks.get(i).sort((t1, t2) -> -Integer.compare(t1.priority, t2.priority));

					task.addition_slack_by_newOPA = task.deadline - time;
				}

				unassignedTasks.sort((t1, t2) -> -compareSlack(t1, t2));

				if (isprint) {
					for (int k = 0; k < unassignedTasks.size(); k++) {
						SporadicTask task = unassignedTasks.get(k);
						System.out.print("T" + task.id + ":  " + task.addition_slack_by_newOPA + " | " + task.deadline + " 	  ");
					}
					System.out.println();
				}

				for (int k = 0; k < unassignedTasks.size() - 1; k++) {
					SporadicTask task1 = unassignedTasks.get(k);
					SporadicTask task2 = unassignedTasks.get(k + 1);

					if (task1.addition_slack_by_newOPA < task2.addition_slack_by_newOPA) {
						System.err.println("newOPA reuslt error! in Task " + task1.id + " and task " + task2.id);
						System.exit(-1);
					}

					if (task1.addition_slack_by_newOPA == task2.addition_slack_by_newOPA && task1.deadline < task2.deadline) {
						System.err.println("newOPA reuslt error! in Task " + task1.id + " and task " + task2.id);
						System.exit(-1);
					}

				}

				unassignedTasks.get(0).priority = sratingP;
				tasks.get(i).sort((t1, t2) -> -Integer.compare(t1.priority, t2.priority));
				unassignedTasks.remove(0);

				sratingP += 2;
			}
		}

		for (int i = 0; i < tasks.size(); i++) {
			tasks.get(i).sort((t1, t2) -> -Integer.compare(t1.priority, t2.priority));
		}

		long count = 0;
		boolean isEqual = false, missdeadline = false;
		long[][] response_time = new AnalysisUtils().initResponseTime(tasks);

		/* a huge busy window to get a fixed Ri */
		while (!isEqual) {
			isEqual = true;
			long[][] response_time_plus = busyWindow(tasks, resources, response_time, true, true);

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

		if (isprint) {
			System.out.println("FIFONP JAVA    after " + count + " tims of recursion, we got the response time.");
			new AnalysisUtils().printResponseTime(response_time, tasks);
		}

		return response_time;
	}

	private int compareSlack(SporadicTask t1, SporadicTask t2) {
		long slack1 = t1.addition_slack_by_newOPA;
		long deadline1 = t1.deadline;

		long slack2 = t2.addition_slack_by_newOPA;
		long deadline2 = t2.deadline;

		if (slack1 < slack2) {
			return -1;
		}

		if (slack1 > slack2) {
			return 1;
		}

		if (slack1 == slack2) {
			if (deadline1 < deadline2)
				return -1;
			if (deadline1 > deadline2)
				return 1;
			if (deadline1 == deadline2)
				return 0;
		}

		System.err.println(
				"New OPA comparator error!" + " slack1:  " + slack1 + " deadline1:  " + deadline1 + " slack2:  " + slack2 + " deadline2:  " + deadline2);
		System.exit(-1);
		return 0;
	}

	public long[][] getResponseTimeByOPA(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, boolean isprint) {
		if (tasks == null)
			return null;

		// Default as deadline monotonic
		tasks = new PriorityGeneator().assignPrioritiesByDM(tasks);

		// now we check each task. we begin from the task with largest deadline
		for (int i = 0; i < tasks.size(); i++) {

			ArrayList<SporadicTask> unassignedTasks = new ArrayList<>(tasks.get(i));
			int sratingP = 500 - unassignedTasks.size() * 2;
			int prioLevels = tasks.get(i).size();

			for (int currentLevel = 0; currentLevel < prioLevels; currentLevel++) {
				boolean isTaskSchedulable = false;
				int startingIndex = unassignedTasks.size() - 1;

				for (int j = startingIndex; j >= 0; j--) {
					SporadicTask task = unassignedTasks.get(j);
					int originalP = task.priority;
					task.priority = sratingP;

					tasks.get(i).sort((t1, t2) -> -Integer.compare(t1.priority, t2.priority));
					long time = getResponseTimeForOneTask(task, tasks, resources, true);
					boolean isSchedulable = time <= task.deadline;

					if (!isSchedulable) {
						task.priority = originalP;
						tasks.get(i).sort((t1, t2) -> -Integer.compare(t1.priority, t2.priority));
						if (isprint) {
							System.out.println("Task T" + task.id + " unschedulable");
						}
					} else {
						unassignedTasks.remove(task);
						isTaskSchedulable = true;
						break;
					}
				}

				if (!isTaskSchedulable) {
					long[][] response_time = new long[tasks.size()][];
					for (int j = 0; j < tasks.size(); j++) {
						response_time[j] = new long[tasks.get(j).size()];
						for (int k = 0; k < tasks.get(j).size(); k++) {
							response_time[j][k] = tasks.get(j).get(k).Ri;
						}
					}
					return response_time;
				}

				sratingP += 2;
			}
		}

		long[][] response_time = new long[tasks.size()][];
		for (int j = 0; j < tasks.size(); j++) {
			response_time[j] = new long[tasks.get(j).size()];
			for (int k = 0; k < tasks.get(j).size(); k++) {
				response_time[j][k] = tasks.get(j).get(k).Ri;
			}
		}

		if (isprint) {
			System.out.println("OPA, we got the response time.");
			new AnalysisUtils().printResponseTime(response_time, tasks);
		}

		return response_time;
	}

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
					if (response_time[i][j] != response_time_plus[i][j]) {
						isEqual = false;
					}
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

				task.spin_delay_by_preemptions = 0;
				task.implementation_overheads = 0;
				task.implementation_overheads += AnalysisUtils.FULL_CONTEXT_SWTICH1;

				task.spin = getSpinDelay(task, tasks, resources, response_time[i][j], response_time, btbHit, useRi);
				task.interference = highPriorityInterference(task, tasks, resources, response_time, response_time[i][j], btbHit, useRi);
				task.local = localBlocking(task, tasks, resources, response_time, response_time[i][j], btbHit, useRi);

				long implementation_overheads = (long) Math.ceil(task.implementation_overheads);
				response_time_plus[i][j] = task.Ri = task.WCET + task.spin + task.interference + task.local + implementation_overheads;

				if (task.Ri > task.deadline) {
					return response_time_plus;
				}
			}
		}
		return response_time_plus;
	}

	private long getResponseTimeForOneTask(SporadicTask caltask, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, boolean btbHit) {

		long[][] dummy_response_time = new long[tasks.size()][];
		for (int i = 0; i < dummy_response_time.length; i++) {
			dummy_response_time[i] = new long[tasks.get(i).size()];
		}

		SporadicTask task = caltask;
		long Ri = 0;
		long newRi = task.WCET + task.pure_resource_execution_time;

		if (newRi > task.deadline) {
			return newRi;
		}

		while (Ri != newRi) {
			if (newRi > task.deadline) {
				return newRi;
			}

			Ri = newRi;
			newRi = oneCalculation(task, tasks, resources, dummy_response_time, Ri, btbHit, false);

			if (newRi > task.deadline) {
				return newRi;
			}
		}

		return newRi;
	}

	private long oneCalculation(SporadicTask task, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, long[][] response_time, long Ri,
			boolean btbHit, boolean useRi) {

		task.spin_delay_by_preemptions = 0;
		task.implementation_overheads = 0;
		task.implementation_overheads += AnalysisUtils.FULL_CONTEXT_SWTICH1;

		task.spin = getSpinDelay(task, tasks, resources, Ri, response_time, btbHit, useRi);
		task.interference = highPriorityInterference(task, tasks, resources, response_time, Ri, btbHit, useRi);
		task.local = localBlocking(task, tasks, resources, response_time, Ri, btbHit, useRi);

		long implementation_overheads = (long) Math.ceil(task.implementation_overheads);
		long newRi = task.Ri = task.WCET + task.spin + task.interference + task.local + implementation_overheads;

		return newRi;
	}

	private long getSpinDelay(SporadicTask task, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, long time, long[][] Ris,
			boolean btbHit, boolean useRi) {
		long spin = 0;
		ArrayList<ArrayList<Long>> requestsLeftOnRemoteP = new ArrayList<>();
		ArrayList<Resource> fifop_resources = new ArrayList<>();
		for (int i = 0; i < resources.size(); i++) {
			requestsLeftOnRemoteP.add(new ArrayList<Long>());
			fifop_resources.add(resources.get(i));
			Resource res = resources.get(i);
			spin += getSpinDelayForOneResoruce(task, res, tasks, requestsLeftOnRemoteP.get(i), time, Ris, btbHit, useRi);
		}

		// Preemption
		long preemptions = 0;
		long request_by_preemptions = 0;
		for (int i = 0; i < tasks.get(task.partition).size(); i++) {
			if (tasks.get(task.partition).get(i).priority > task.priority) {
				preemptions += (int) Math.ceil((double) (time) / (double) tasks.get(task.partition).get(i).period);
			}
		}

		task.implementation_overheads += preemptions * (AnalysisUtils.FIFOP_CANCEL);

		while (preemptions > 0) {

			long max_delay = 0;
			int max_delay_resource_index = -1;
			for (int i = 0; i < fifop_resources.size(); i++) {
				if (max_delay < fifop_resources.get(i).csl * requestsLeftOnRemoteP.get(i).size()) {
					max_delay = fifop_resources.get(i).csl * requestsLeftOnRemoteP.get(i).size();
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

	private long getSpinDelayForOneResoruce(SporadicTask task, Resource resource, ArrayList<ArrayList<SporadicTask>> tasks,
			ArrayList<Long> requestsLeftOnRemoteP, long time, long[][] Ris, boolean btbHit, boolean useRi) {
		long spin = 0;
		long ncs = 0;

		for (int i = 0; i < tasks.get(task.partition).size(); i++) {
			SporadicTask hpTask = tasks.get(task.partition).get(i);
			if (hpTask.priority > task.priority && hpTask.resource_required_index.contains(resource.id - 1)) {
				ncs += (int) Math.ceil((double) (time + (btbHit ? (useRi ? Ris[hpTask.partition][i] : hpTask.deadline) : 0)) / (double) hpTask.period)
						* hpTask.number_of_access_in_one_release.get(hpTask.resource_required_index.indexOf(resource.id - 1));
			}
		}

		if (task.resource_required_index.contains(resource.id - 1))
			ncs += task.number_of_access_in_one_release.get(task.resource_required_index.indexOf(resource.id - 1));

		if (ncs > 0) {
			for (int i = 0; i < tasks.size(); i++) {
				if (task.partition != i) {
					/* For each remote partition */
					long number_of_request_by_Remote_P = 0;
					for (int j = 0; j < tasks.get(i).size(); j++) {
						if (tasks.get(i).get(j).resource_required_index.contains(resource.id - 1)) {
							SporadicTask remote_task = tasks.get(i).get(j);
							int indexR = getIndexRInTask(remote_task, resource);
							int number_of_release = (int) Math
									.ceil((double) (time + (btbHit ? (useRi ? Ris[i][j] : remote_task.deadline) : 0)) / (double) remote_task.period);
							number_of_request_by_Remote_P += number_of_release * remote_task.number_of_access_in_one_release.get(indexR);
						}
					}

					long possible_spin_delay = Long.min(number_of_request_by_Remote_P, ncs);
					spin += possible_spin_delay;
					if (number_of_request_by_Remote_P - ncs > 0)
						requestsLeftOnRemoteP.add(number_of_request_by_Remote_P - ncs);
				}
			}
		}

		task.implementation_overheads += (spin + ncs) * (AnalysisUtils.FIFOP_LOCK + AnalysisUtils.FIFOP_UNLOCK);
		return spin * resource.csl + ncs * resource.csl;
	}

	/*
	 * Calculate the local high priority tasks' interference for a given task t.
	 * CI is a set of computation time of local tasks, including spin delay.
	 */
	private long highPriorityInterference(SporadicTask t, ArrayList<ArrayList<SporadicTask>> allTasks, ArrayList<Resource> resources, long[][] Ris, long time,
			boolean btbHit, boolean useRi) {
		long interference = 0;
		int partition = t.partition;
		ArrayList<SporadicTask> tasks = allTasks.get(partition);

		for (int i = 0; i < tasks.size(); i++) {
			if (tasks.get(i).priority > t.priority) {
				SporadicTask hpTask = tasks.get(i);
				interference += Math.ceil((double) (time) / (double) hpTask.period) * (hpTask.WCET);
				t.implementation_overheads += Math.ceil((double) (time) / (double) hpTask.period) * (AnalysisUtils.FULL_CONTEXT_SWTICH2);
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
			local_blocking_each_resource.add(local_blocking);
		}

		if (local_blocking_each_resource.size() > 1)
			local_blocking_each_resource.sort((l1, l2) -> -Double.compare(l1, l2));

		if (local_blocking_each_resource.size() > 0)
			t.implementation_overheads += AnalysisUtils.FIFOP_LOCK + AnalysisUtils.FIFOP_UNLOCK;

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
}
