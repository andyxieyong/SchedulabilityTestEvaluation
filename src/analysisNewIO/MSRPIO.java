package analysisNewIO;

import java.util.ArrayList;

import entity.Resource;
import entity.SporadicTask;
import generatorTools.PriorityGeneator;
import utils.AnalysisUtils;

public class MSRPIO extends RuntimeCostAnalysis {

	public long[][] getResponseTimeSBPO(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, boolean isprint) {
		if (tasks == null)
			return null;

		// Default as deadline monotonic
		tasks = new PriorityGeneator().assignPrioritiesByDM(tasks);

		long npsection = 0;
		for (int i = 0; i < resources.size(); i++) {
			Resource resource = resources.get(i);
			if (npsection < resource.csl)
				npsection = resources.get(i).csl;
		}

		long[][] dummy_response_time = new long[tasks.size()][];
		for (int i = 0; i < dummy_response_time.length; i++) {
			dummy_response_time[i] = new long[tasks.get(i).size()];
			for (int j = 0; j < tasks.get(i).size(); j++) {
				dummy_response_time[i][j] = tasks.get(i).get(j).deadline;
			}
		}

		// now we check each task. For each processor
		for (int i = 0; i < tasks.size(); i++) {
			int partition = i;
			ArrayList<SporadicTask> unassignedTasks = new ArrayList<>(tasks.get(partition));
			int sratingP = 500 - unassignedTasks.size() * 2;
			int prioLevels = tasks.get(partition).size();

			// For each priority level
			for (int currentLevel = 0; currentLevel < prioLevels; currentLevel++) {

				int startingIndex = unassignedTasks.size() - 1;
				for (int j = startingIndex; j >= 0; j--) {
					SporadicTask task = unassignedTasks.get(j);
					int originalP = task.priority;
					task.priority = sratingP;

					tasks.get(partition).sort((t1, t2) -> -Integer.compare(t1.priority, t2.priority));

					// Init response time of tasks in this partition
					for (int k = 0; k < tasks.get(partition).size(); k++) {
						dummy_response_time[partition][k] = tasks.get(partition).get(k).WCET + tasks.get(partition).get(k).pure_resource_execution_time;
					}

					boolean isEqual = false;
					long[] dummy_response_time_plus = null;
					/* a huge busy window to get a fixed Ri */
					while (!isEqual) {
						isEqual = true;
						boolean should_finish = true;

						dummy_response_time_plus = getResponseTimeForSBPO(task.partition, tasks, resources, true, extendCal, dummy_response_time, task);

						for (int resposneTimeIndex = 0; resposneTimeIndex < dummy_response_time_plus.length; resposneTimeIndex++) {
							if (task != tasks.get(partition).get(resposneTimeIndex)
									&& dummy_response_time[partition][resposneTimeIndex] != dummy_response_time_plus[resposneTimeIndex])
								isEqual = false;

							if (task != tasks.get(partition).get(resposneTimeIndex)
									&& dummy_response_time_plus[resposneTimeIndex] <= tasks.get(partition).get(resposneTimeIndex).deadline * extendCal)
								should_finish = false;
						}

						for (int resposneTimeIndex = 0; resposneTimeIndex < dummy_response_time[partition].length; resposneTimeIndex++) {
							dummy_response_time[partition][resposneTimeIndex] = dummy_response_time_plus[resposneTimeIndex];
						}

						if (should_finish)
							break;
					}

					long time = dummy_response_time_plus[tasks.get(partition).indexOf(task)];
					task.priority = originalP;
					tasks.get(partition).sort((t1, t2) -> -Integer.compare(t1.priority, t2.priority));

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
				tasks.get(partition).sort((t1, t2) -> -Integer.compare(t1.priority, t2.priority));
				unassignedTasks.remove(0);

				sratingP += 2;
			}

			tasks.get(partition).sort((t1, t2) -> -Integer.compare(t1.priority, t2.priority));

			// Init response time of tasks in this partition
			for (int k = 0; k < tasks.get(partition).size(); k++) {
				dummy_response_time[partition][k] = tasks.get(partition).get(k).WCET + tasks.get(partition).get(k).pure_resource_execution_time;
			}

			boolean isEqual = false;
			long[] dummy_response_time_plus = null;
			/* a huge busy window to get a fixed Ri */
			while (!isEqual) {
				isEqual = true;
				boolean should_finish = true;

				dummy_response_time_plus = getResponseTimeForOnePartition(partition, tasks, resources, true, 1, dummy_response_time);

				for (int resposneTimeIndex = 0; resposneTimeIndex < dummy_response_time_plus.length; resposneTimeIndex++) {
					if (dummy_response_time[partition][resposneTimeIndex] != dummy_response_time_plus[resposneTimeIndex])
						isEqual = false;

					if (dummy_response_time_plus[resposneTimeIndex] <= tasks.get(partition).get(resposneTimeIndex).deadline)
						should_finish = false;
				}

				for (int resposneTimeIndex = 0; resposneTimeIndex < dummy_response_time_plus.length; resposneTimeIndex++) {
					if (dummy_response_time_plus[resposneTimeIndex] > tasks.get(partition).get(resposneTimeIndex).deadline) {
						dummy_response_time[partition][resposneTimeIndex] = tasks.get(partition).get(resposneTimeIndex).deadline;
					} else {
						dummy_response_time[partition][resposneTimeIndex] = dummy_response_time_plus[resposneTimeIndex];
					}
				}

				if (should_finish)
					break;
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

	public long[][] getResponseTimeRPA(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, boolean isprint) {
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

					task.addition_slack_by_newOPA = time <= task.deadline ? task.deadline - time : -1;
				}

				unassignedTasks.sort((t1, t2) -> -compareSlack(t1, t2));

				if (unassignedTasks.get(0).addition_slack_by_newOPA < 0) {
					long[][] dummy_response_time = new long[tasks.size()][];
					for (int h = 0; h < dummy_response_time.length; h++) {
						dummy_response_time[h] = new long[tasks.get(h).size()];
					}
					dummy_response_time[0][0] = tasks.get(0).get(0).deadline + 1;

					return dummy_response_time;
				}

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

	public long[][] getResponseTimeOPA(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, boolean isprint) {
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

		task.indirectspin = 0;
		task.implementation_overheads = 0;
		task.implementation_overheads += AnalysisUtils.FULL_CONTEXT_SWTICH1;

		task.spin = directRemoteDelay(task, tasks, resources, response_time, Ri, btbHit, useRi);
		task.interference = highPriorityInterference(task, tasks, resources, response_time, Ri, btbHit, useRi);
		task.local = localBlocking(task, tasks, resources, response_time, Ri, btbHit, useRi);

		long implementation_overheads = (long) Math.ceil(task.implementation_overheads);
		long newRi = task.Ri = task.WCET + task.spin + task.interference + task.local + implementation_overheads;

		return newRi;
	}

	private long[] getResponseTimeForOnePartition(int partition, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, boolean btbHit,
			int extenstionCal, long[][] response_time) {

		long[] response_time_plus = new long[tasks.get(partition).size()];

		for (int i = 0; i < tasks.get(partition).size(); i++) {
			SporadicTask task = tasks.get(partition).get(i);
			if (response_time[partition][i] >= task.deadline * extenstionCal) {
				response_time_plus[i] = task.deadline * extenstionCal;
				continue;
			}

			task.indirectspin = 0;
			task.implementation_overheads = 0;
			task.implementation_overheads += AnalysisUtils.FULL_CONTEXT_SWTICH1;

			task.spin = directRemoteDelay(task, tasks, resources, response_time, response_time[partition][i], btbHit, true);
			task.interference = highPriorityInterference(task, tasks, resources, response_time, response_time[partition][i], btbHit, true);
			task.local = localBlocking(task, tasks, resources, response_time, response_time[partition][i], btbHit, true);

			long implementation_overheads = (long) Math.ceil(task.implementation_overheads);

			response_time_plus[i] = task.Ri = task.WCET + task.spin + task.interference + task.local + implementation_overheads;
		}

		return response_time_plus;
	}

	private long[] getResponseTimeForSBPO(int partition, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, boolean btbHit,
			int extenstionCal, long[][] response_time, SporadicTask calT) {

		long[] response_time_plus = new long[tasks.get(partition).size()];

		for (int i = 0; i < tasks.get(partition).size(); i++) {
			SporadicTask task = tasks.get(partition).get(i);
			if (response_time[partition][i] >= task.deadline * extenstionCal && task != calT) {
				response_time_plus[i] = task.deadline * extenstionCal;
				continue;
			}
			task.indirectspin = 0;
			task.implementation_overheads = 0;
			task.implementation_overheads += AnalysisUtils.FULL_CONTEXT_SWTICH1;

			task.spin = directRemoteDelay(task, tasks, resources, response_time, response_time[partition][i], btbHit, true);
			task.interference = highPriorityInterference(task, tasks, resources, response_time, response_time[partition][i], btbHit, true);
			task.local = localBlocking(task, tasks, resources, response_time, response_time[partition][i], btbHit, true);

			long implementation_overheads = (long) Math.ceil(task.implementation_overheads);

			response_time_plus[i] = task.Ri = task.WCET + task.spin + task.interference + task.local + implementation_overheads;
		}

		return response_time_plus;
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

				task.indirectspin = 0;
				task.implementation_overheads = 0;
				task.implementation_overheads += AnalysisUtils.FULL_CONTEXT_SWTICH1;

				task.spin = directRemoteDelay(task, tasks, resources, response_time, response_time[i][j], btbHit, useRi);
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

	/*
	 * Calculate the spin delay for a given task t.
	 */
	private long directRemoteDelay(SporadicTask t, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, long[][] Ris, long Ri,
			boolean btbHit, boolean useRi) {
		long spin_delay = 0;
		for (int k = 0; k < t.resource_required_index.size(); k++) {
			Resource resource = resources.get(t.resource_required_index.get(k));
			long NoS = getNoSpinDelay(t, resource, tasks, Ris, Ri, btbHit, useRi);
			spin_delay += (NoS + t.number_of_access_in_one_release.get(t.resource_required_index.indexOf(resource.id - 1))) * resource.csl;
			t.implementation_overheads += (NoS + t.number_of_access_in_one_release.get(t.resource_required_index.indexOf(resource.id - 1)))
					* (AnalysisUtils.FIFONP_LOCK + AnalysisUtils.FIFONP_UNLOCK);
		}
		return spin_delay;
	}

	/*
	 * gives the number of requests from remote partitions for a resource that
	 * is required by the given task.
	 */
	private int getNoSpinDelay(SporadicTask task, Resource resource, ArrayList<ArrayList<SporadicTask>> tasks, long[][] Ris, long Ri, boolean btbHit,
			boolean useRi) {
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
								.ceil((double) (Ri + (btbHit ? (useRi ? Ris[i][j] : remote_task.deadline) : 0)) / (double) remote_task.period);
						number_of_request_by_Remote_P += number_of_release * remote_task.number_of_access_in_one_release.get(indexR);
					}
				}
				int getNoRFromHP = getNoRFromHP(task, resource, tasks.get(task.partition), Ris[task.partition], Ri, btbHit, useRi);
				int possible_spin_delay = number_of_request_by_Remote_P - getNoRFromHP < 0 ? 0 : number_of_request_by_Remote_P - getNoRFromHP;

				int NoRFromT = task.number_of_access_in_one_release.get(getIndexRInTask(task, resource));
				number_of_spin_dealy += Integer.min(possible_spin_delay, NoRFromT);
			}
		}
		return number_of_spin_dealy;
	}

	/*
	 * Calculate the local high priority tasks' interference for a given task t.
	 * CI is a set of computation time of local tasks, including spin delay.
	 */
	private long highPriorityInterference(SporadicTask t, ArrayList<ArrayList<SporadicTask>> allTasks, ArrayList<Resource> resources, long[][] Ris, long Ri,
			boolean btbHit, boolean useRi) {
		long interference = 0;
		int partition = t.partition;
		ArrayList<SporadicTask> tasks = allTasks.get(partition);

		for (int i = 0; i < tasks.size(); i++) {
			if (tasks.get(i).priority > t.priority) {
				SporadicTask hpTask = tasks.get(i);
				interference += Math.ceil((double) (Ri) / (double) hpTask.period) * (hpTask.WCET);
				t.implementation_overheads += Math.ceil((double) (Ri) / (double) hpTask.period) * (AnalysisUtils.FULL_CONTEXT_SWTICH2);

				long btb_interference = getIndirectSpinDelay(hpTask, allTasks, resources, Ris, Ri, Ris[partition][i], btbHit, useRi, t);
				t.indirectspin += btb_interference;
				interference += btb_interference;
			}
		}
		return interference;
	}

	/*
	 * for a high priority task hpTask, return its back to back hit time when
	 * the given task is pending
	 */
	private long getIndirectSpinDelay(SporadicTask hpTask, ArrayList<ArrayList<SporadicTask>> allTasks, ArrayList<Resource> resources, long[][] Ris, long Ri,
			long Rihp, boolean btbHit, boolean useRi, SporadicTask calTask) {
		long BTBhit = 0;

		for (int i = 0; i < hpTask.resource_required_index.size(); i++) {
			/* for each resource that a high priority task request */
			Resource resource = resources.get(hpTask.resource_required_index.get(i));

			int number_of_higher_request = getNoRFromHP(hpTask, resource, allTasks.get(hpTask.partition), Ris[hpTask.partition], Ri, btbHit, useRi);
			int number_of_request_with_btb = (int) Math.ceil((double) (Ri + (btbHit ? (useRi ? Rihp : hpTask.deadline) : 0)) / (double) hpTask.period)
					* hpTask.number_of_access_in_one_release.get(i);

			BTBhit += number_of_request_with_btb * resource.csl;
			calTask.implementation_overheads += number_of_request_with_btb * (AnalysisUtils.FIFONP_LOCK + AnalysisUtils.FIFONP_UNLOCK);

			for (int j = 0; j < resource.partitions.size(); j++) {
				if (resource.partitions.get(j) != hpTask.partition) {
					int remote_partition = resource.partitions.get(j);
					int number_of_remote_request = getNoRRemote(resource, allTasks.get(remote_partition), Ris[remote_partition], Ri, btbHit, useRi);

					int possible_spin_delay = number_of_remote_request - number_of_higher_request < 0 ? 0 : number_of_remote_request - number_of_higher_request;

					int spin_delay_with_btb = Integer.min(possible_spin_delay, number_of_request_with_btb);

					BTBhit += spin_delay_with_btb * resource.csl;
					calTask.implementation_overheads += spin_delay_with_btb * (AnalysisUtils.FIFONP_LOCK + AnalysisUtils.FIFONP_UNLOCK);
				}
			}

		}
		return BTBhit;
	}

	private long localBlocking(SporadicTask t, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, long[][] Ris, long Ri, boolean btbHit,
			boolean useRi) {
		ArrayList<Resource> LocalBlockingResources = getLocalBlockingResources(t, resources, tasks.get(t.partition));
		ArrayList<Long> local_blocking_each_resource = new ArrayList<>();
		ArrayList<Double> overheads = new ArrayList<>();

		for (int i = 0; i < LocalBlockingResources.size(); i++) {
			Resource res = LocalBlockingResources.get(i);
			long local_blocking = res.csl;

			if (res.isGlobal) {
				for (int parition_index = 0; parition_index < res.partitions.size(); parition_index++) {
					int partition = res.partitions.get(parition_index);
					int norHP = getNoRFromHP(t, res, tasks.get(t.partition), Ris[t.partition], Ri, btbHit, useRi);
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
			overheads.add((local_blocking / res.csl) * (AnalysisUtils.FIFONP_LOCK + AnalysisUtils.FIFONP_UNLOCK));
		}

		if (local_blocking_each_resource.size() >= 1) {
			local_blocking_each_resource.sort((l1, l2) -> -Double.compare(l1, l2));
			overheads.sort((l1, l2) -> -Double.compare(l1, l2));
			t.implementation_overheads += overheads.get(0);
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
	private int getNoRFromHP(SporadicTask task, Resource resource, ArrayList<SporadicTask> tasks, long[] Ris, long Ri, boolean btbHit, boolean useRi) {
		int number_of_request_by_HP = 0;
		int priority = task.priority;

		for (int i = 0; i < tasks.size(); i++) {
			if (tasks.get(i).priority > priority && tasks.get(i).resource_required_index.contains(resource.id - 1)) {
				SporadicTask hpTask = tasks.get(i);
				int indexR = getIndexRInTask(hpTask, resource);
				number_of_request_by_HP += Math.ceil((double) (Ri + (btbHit ? (useRi ? Ris[i] : hpTask.deadline) : 0)) / (double) hpTask.period)
						* hpTask.number_of_access_in_one_release.get(indexR);
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
				number_of_request_by_Remote_P += Math.ceil((double) (Ri + (btbHit ? (useRi ? Ris[i] : remote_task.deadline) : 0)) / (double) remote_task.period)
						* remote_task.number_of_access_in_one_release.get(indexR);
			}
		}
		return number_of_request_by_Remote_P;
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
