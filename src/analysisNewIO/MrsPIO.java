package analysisNewIO;

import java.util.ArrayList;

import entity.Resource;
import entity.SporadicTask;
import generatorTools.PriorityGeneator;
import utils.AnalysisUtils;

public class MrsPIO extends RuntimeCostAnalysis {

	public long[][] getResponseTimeSBPO(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, boolean isprint) {
		if (tasks == null)
			return null;

		int extendCal = 3;

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
			ArrayList<SporadicTask> unassignedTasks = new ArrayList<>(tasks.get(i));
			int sratingP = 500 - unassignedTasks.size() * 2;
			int prioLevels = tasks.get(i).size();

			// For each priority level
			for (int currentLevel = 0; currentLevel < prioLevels; currentLevel++) {

				int startingIndex = unassignedTasks.size() - 1;
				for (int j = startingIndex; j >= 0; j--) {
					SporadicTask task = unassignedTasks.get(j);
					int originalP = task.priority;
					task.priority = sratingP;

					tasks.get(i).sort((t1, t2) -> -Integer.compare(t1.priority, t2.priority));

					// Init response time of tasks in this partition
					for (int k = 0; k < tasks.get(i).size(); k++) {
						dummy_response_time[i][k] = tasks.get(i).get(k).WCET + tasks.get(i).get(k).pure_resource_execution_time;
					}

					boolean isEqual = false;
					long[] dummy_response_time_plus = null;
					/* a huge busy window to get a fixed Ri */
					while (!isEqual) {
						isEqual = true;
						boolean should_finish = true;

						dummy_response_time_plus = getResponseTimeForSBPO(task.partition, tasks, resources, AnalysisUtils.MrsP_PREEMPTION_AND_MIGRATION,
								npsection, true, extendCal, dummy_response_time, task);

						for (int resposneTimeIndex = 0; resposneTimeIndex < dummy_response_time_plus.length; resposneTimeIndex++) {
							if (dummy_response_time[i][resposneTimeIndex] != dummy_response_time_plus[resposneTimeIndex])
								isEqual = false;

							if (task != tasks.get(i).get(resposneTimeIndex)
									&& dummy_response_time_plus[resposneTimeIndex] <= tasks.get(i).get(resposneTimeIndex).deadline * extendCal)
								should_finish = false;
						}

						for (int resposneTimeIndex = 0; resposneTimeIndex < dummy_response_time[i].length; resposneTimeIndex++) {
							dummy_response_time[i][resposneTimeIndex] = dummy_response_time_plus[resposneTimeIndex];
						}

						if (should_finish)
							break;
					}

					long time = dummy_response_time_plus[tasks.get(i).indexOf(task)];
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

			tasks.get(i).sort((t1, t2) -> -Integer.compare(t1.priority, t2.priority));

			// Init response time of tasks in this partition
			for (int k = 0; k < tasks.get(i).size(); k++) {
				dummy_response_time[i][k] = tasks.get(i).get(k).WCET + tasks.get(i).get(k).pure_resource_execution_time;
			}

			boolean isEqual = false;
			long[] dummy_response_time_plus = null;
			/* a huge busy window to get a fixed Ri */
			while (!isEqual) {
				isEqual = true;
				boolean should_finish = true;

				dummy_response_time_plus = getResponseTimeForOnePartition(i, tasks, resources, AnalysisUtils.MrsP_PREEMPTION_AND_MIGRATION, npsection, true, 1,
						dummy_response_time);

				for (int resposneTimeIndex = 0; resposneTimeIndex < dummy_response_time_plus.length; resposneTimeIndex++) {
					if (dummy_response_time[i][resposneTimeIndex] != dummy_response_time_plus[resposneTimeIndex])
						isEqual = false;

					if (dummy_response_time_plus[resposneTimeIndex] <= tasks.get(i).get(resposneTimeIndex).deadline)
						should_finish = false;
				}

				for (int resposneTimeIndex = 0; resposneTimeIndex < dummy_response_time_plus.length; resposneTimeIndex++) {
					if (dummy_response_time_plus[resposneTimeIndex] > tasks.get(i).get(resposneTimeIndex).deadline) {
						dummy_response_time[i][resposneTimeIndex] = tasks.get(i).get(resposneTimeIndex).deadline;
					} else {
						dummy_response_time[i][resposneTimeIndex] = dummy_response_time_plus[resposneTimeIndex];
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
			long[][] response_time_plus = busyWindow(tasks, resources, response_time, AnalysisUtils.MrsP_PREEMPTION_AND_MIGRATION, npsection, true, true);

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

		long npsection = 0;
		for (int i = 0; i < resources.size(); i++) {
			Resource resource = resources.get(i);
			if (npsection < resource.csl)
				npsection = resources.get(i).csl;
		}

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
					long time = getResponseTimeForOneTask(task, tasks, resources, AnalysisUtils.MrsP_PREEMPTION_AND_MIGRATION, npsection, true);
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

				if (unassignedTasks.get(0).addition_slack_by_newOPA < 0) {
					long[][] dummy_response_time = new long[tasks.size()][];
					for (int h = 0; h < dummy_response_time.length; h++) {
						dummy_response_time[h] = new long[tasks.get(h).size()];
					}
					dummy_response_time[0][0] = tasks.get(0).get(0).deadline + 1;

					return dummy_response_time;
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
			long[][] response_time_plus = busyWindow(tasks, resources, response_time, AnalysisUtils.MrsP_PREEMPTION_AND_MIGRATION, npsection, true, true);

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

		System.err
				.println("Slack comparator error!" + " slack1:  " + slack1 + " deadline1:  " + deadline1 + " slack2:  " + slack2 + " deadline2:  " + deadline2);
		System.exit(-1);
		return 0;
	}

	public long[][] getResponseTimeOPA(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, boolean isprint) {
		if (tasks == null)
			return null;

		long npsection = 0;
		for (int i = 0; i < resources.size(); i++) {
			Resource resource = resources.get(i);
			if (npsection < resource.csl)
				npsection = resources.get(i).csl;
		}

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
					long time = getResponseTimeForOneTask(task, tasks, resources, AnalysisUtils.MrsP_PREEMPTION_AND_MIGRATION, npsection, true);
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

		long npsection = 0;

		for (int i = 0; i < resources.size(); i++) {
			if (npsection < resources.get(i).csl)
				npsection = resources.get(i).csl;
		}

		/* a huge busy window to get a fixed Ri */
		while (!isEqual) {
			isEqual = true;
			long[][] response_time_plus = busyWindow(tasks, resources, response_time, AnalysisUtils.MrsP_PREEMPTION_AND_MIGRATION, npsection, btbHit, useRi);

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
			System.out.println("MrsP NP    after " + count + " tims of recursion, we got the response time.");
			new AnalysisUtils().printResponseTime(response_time, tasks);
		}

		return response_time;
	}

	public long[][] getResponseTimeDM(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, boolean mig, boolean np, boolean btbHit,
			boolean useRi, boolean printDebug) {
		if (tasks == null)
			return null;

		// assign priorities by Deadline Monotonic
		tasks = new PriorityGeneator().assignPrioritiesByDM(tasks);

		long count = 0;
		boolean isEqual = false, missdeadline = false;
		long[][] response_time = new AnalysisUtils().initResponseTime(tasks);

		long npsection = 0;
		if (np) {
			for (int i = 0; i < resources.size(); i++) {
				if (npsection < resources.get(i).csl)
					npsection = resources.get(i).csl;
			}
		}

		/* a huge busy window to get a fixed Ri */
		while (!isEqual) {
			isEqual = true;
			long[][] response_time_plus = busyWindow(tasks, resources, response_time, mig ? AnalysisUtils.MrsP_PREEMPTION_AND_MIGRATION : 0, npsection, btbHit,
					useRi);

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
			System.out.println("MrsP NP    after " + count + " tims of recursion, we got the response time.");
			new AnalysisUtils().printResponseTime(response_time, tasks);
		}

		return response_time;
	}

	private long[][] busyWindow(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, long[][] response_time, double oneMig, long np,
			boolean btbHit, boolean useRi) {
		if (tasks == null)
			return null;
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
				task.migration_overheads_plus = 0;
				task.implementation_overheads += AnalysisUtils.FULL_CONTEXT_SWTICH1;

				task.spin = resourceAccessingTime(task, tasks, resources, response_time, response_time[i][j], 0, oneMig, np, btbHit, useRi, task);
				task.interference = highPriorityInterference(task, tasks, resources, response_time, response_time[i][j], oneMig, np, btbHit, useRi);
				task.local = localBlocking(task, tasks, resources, response_time, response_time[i][j], oneMig, np, btbHit, useRi);
				long npsection = (np > 0 && isTaskIncurNPSection(task, tasks.get(task.partition), resources) ? np : 0);

				if (npsection > task.local + task.mrsp_arrivalblocking_overheads) {
					task.local = npsection;
				} else {
					// task.local Remains.
					task.implementation_overheads += task.mrsp_arrivalblocking_overheads;
				}

				long implementation_overheads = (long) Math.ceil(task.implementation_overheads + task.migration_overheads_plus);

				response_time_plus[i][j] = task.Ri = task.WCET + task.spin + task.interference + task.local + implementation_overheads;

				if (task.Ri > task.deadline) {
					return response_time_plus;
				}
			}
		}
		return response_time_plus;
	}

	private long getResponseTimeForOneTask(SporadicTask caltask, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, double oneMig,
			long np, boolean btbHit) {

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
			newRi = oneCalculation(task, tasks, resources, dummy_response_time, Ri, oneMig, np, btbHit, false);

			if (newRi > task.deadline) {
				return newRi;
			}
		}

		return newRi;
	}

	private long[] getResponseTimeForOnePartition(int partition, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, double oneMig,
			long np, boolean btbHit, int extenstionCal, long[][] response_time) {

		long[] response_time_plus = new long[tasks.get(partition).size()];

		for (int i = 0; i < tasks.get(partition).size(); i++) {
			SporadicTask task = tasks.get(partition).get(i);
			if (response_time[partition][i] >= task.deadline * extenstionCal) {
				response_time_plus[i] = task.deadline * extenstionCal;
				continue;
			}

			task.indirectspin = 0;
			task.implementation_overheads = 0;
			task.migration_overheads_plus = 0;
			task.implementation_overheads += AnalysisUtils.FULL_CONTEXT_SWTICH1;

			task.spin = resourceAccessingTime(task, tasks, resources, response_time, response_time[partition][i], 0, oneMig, np, btbHit, true, task);
			task.interference = highPriorityInterference(task, tasks, resources, response_time, response_time[partition][i], oneMig, np, btbHit, true);
			task.local = localBlocking(task, tasks, resources, response_time, response_time[partition][i], oneMig, np, btbHit, true);
			long npsection = (np > 0 && isTaskIncurNPSection(task, tasks.get(task.partition), resources) ? np : 0);

			if (npsection > task.local + task.mrsp_arrivalblocking_overheads) {
				task.local = npsection;
			} else {
				// task.local Remains.
				task.implementation_overheads += task.mrsp_arrivalblocking_overheads;
			}

			long implementation_overheads = (long) Math.ceil(task.implementation_overheads + task.migration_overheads_plus);

			response_time_plus[i] = task.Ri = task.WCET + task.spin + task.interference + task.local + implementation_overheads;
		}

		return response_time_plus;
	}

	private long[] getResponseTimeForSBPO(int partition, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, double oneMig, long np,
			boolean btbHit, int extenstionCal, long[][] response_time, SporadicTask calT) {

		long[] response_time_plus = new long[tasks.get(partition).size()];

		for (int i = 0; i < tasks.get(partition).size(); i++) {
			SporadicTask task = tasks.get(partition).get(i);
			if (response_time[partition][i] >= task.deadline * extenstionCal && task != calT) {
				response_time_plus[i] = task.deadline * extenstionCal;
				continue;
			}

			task.indirectspin = 0;
			task.implementation_overheads = 0;
			task.migration_overheads_plus = 0;
			task.implementation_overheads += AnalysisUtils.FULL_CONTEXT_SWTICH1;

			task.spin = resourceAccessingTime(task, tasks, resources, response_time, response_time[partition][i], 0, oneMig, np, btbHit, true, task);
			task.interference = highPriorityInterference(task, tasks, resources, response_time, response_time[partition][i], oneMig, np, btbHit, true);
			task.local = localBlocking(task, tasks, resources, response_time, response_time[partition][i], oneMig, np, btbHit, true);
			long npsection = (np > 0 && isTaskIncurNPSection(task, tasks.get(task.partition), resources) ? np : 0);

			if (npsection > task.local + task.mrsp_arrivalblocking_overheads) {
				task.local = npsection;
			} else {
				// task.local Remains.
				task.implementation_overheads += task.mrsp_arrivalblocking_overheads;
			}

			long implementation_overheads = (long) Math.ceil(task.implementation_overheads + task.migration_overheads_plus);

			response_time_plus[i] = task.Ri = task.WCET + task.spin + task.interference + task.local + implementation_overheads;
		}

		return response_time_plus;
	}

	private long oneCalculation(SporadicTask task, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, long[][] response_time, long Ri,
			double oneMig, long np, boolean btbHit, boolean useRi) {

		task.indirectspin = 0;
		task.implementation_overheads = 0;
		task.migration_overheads_plus = 0;
		task.implementation_overheads += AnalysisUtils.FULL_CONTEXT_SWTICH1;

		task.spin = resourceAccessingTime(task, tasks, resources, response_time, Ri, 0, oneMig, np, btbHit, useRi, task);
		task.interference = highPriorityInterference(task, tasks, resources, response_time, Ri, oneMig, np, btbHit, useRi);
		task.local = localBlocking(task, tasks, resources, response_time, Ri, oneMig, np, btbHit, useRi);
		long npsection = (np > 0 && isTaskIncurNPSection(task, tasks.get(task.partition), resources) ? np : 0);
		if (npsection > task.local + task.mrsp_arrivalblocking_overheads) {
			task.local = npsection;
		} else {
			// task.local Remains.
			task.implementation_overheads += task.mrsp_arrivalblocking_overheads;
		}

		long implementation_overheads = (long) Math.ceil(task.implementation_overheads + task.migration_overheads_plus);
		long newRi = task.Ri = task.WCET + task.spin + task.interference + task.local + implementation_overheads;

		return newRi;
	}

	private boolean isTaskIncurNPSection(SporadicTask task, ArrayList<SporadicTask> tasksOnItsParititon, ArrayList<Resource> resources) {
		int partition = task.partition;
		int priority = task.priority;
		int minCeiling = 1000;

		for (int i = 0; i < resources.size(); i++) {
			Resource resource = resources.get(i);
			int ceiling = resource.getCeilingForProcessor(tasksOnItsParititon);

			if (resource.partitions.contains(partition) && minCeiling > ceiling) {
				minCeiling = ceiling;
			}
		}

		if (priority > minCeiling)
			return true;
		else
			return false;
	}

	/*
	 * Calculate the local high priority tasks' interference for a given task t.
	 * CI is a set of computation time of local tasks, including spin delay.
	 */
	private long highPriorityInterference(SporadicTask t, ArrayList<ArrayList<SporadicTask>> allTasks, ArrayList<Resource> resources, long[][] Ris, long time,
			double oneMig, long np, boolean btbHit, boolean useRi) {
		long interference = 0;
		int partition = t.partition;

		ArrayList<SporadicTask> tasks = allTasks.get(partition);

		for (int i = 0; i < tasks.size(); i++) {
			if (tasks.get(i).priority > t.priority) {
				SporadicTask hpTask = tasks.get(i);
				interference += Math.ceil((double) (time) / (double) hpTask.period) * (hpTask.WCET);
				long indriectblocking = resourceAccessingTime(hpTask, allTasks, resources, Ris, time,
						btbHit ? (useRi ? Ris[partition][i] : hpTask.deadline) : 0, oneMig, np, btbHit, useRi, t);
				interference += indriectblocking;
				t.indirectspin += indriectblocking;
				t.implementation_overheads += Math.ceil((double) (time) / (double) hpTask.period) * (AnalysisUtils.FULL_CONTEXT_SWTICH2);
			}
		}

		return interference;
	}

	private long resourceAccessingTime(SporadicTask task, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, long[][] Ris, long time,
			long jitter, double oneMig, long np, boolean btbHit, boolean useRi, SporadicTask calT) {
		long resource_accessing_time = 0;

		for (int i = 0; i < task.resource_required_index.size(); i++) {
			Resource resource = resources.get(task.resource_required_index.get(i));

			int number_of_request_with_btb = (int) Math.ceil((double) (time + jitter) / (double) task.period) * task.number_of_access_in_one_release.get(i);

			for (int j = 1; j < number_of_request_with_btb + 1; j++) {
				long oneAccess = 0;
				oneAccess += resourceAccessingTimeInOne(task, resource, tasks, Ris, time, jitter, j, btbHit, useRi, calT);

				if (oneMig != 0) {
					double mc = migrationCostForSpin(task, resource, tasks, Ris, time, j, oneMig, np, btbHit, useRi, calT);
					long mc_long = (long) Math.floor(mc);
					calT.migration_overheads_plus += mc - mc_long;
					if (mc - mc_long < 0) {
						System.err.println("MrsP mig error");
						System.exit(-1);
					}
					oneAccess += mc_long;
				}

				resource_accessing_time += oneAccess;
			}
		}

		return resource_accessing_time;
	}

	private long resourceAccessingTimeInOne(SporadicTask task, Resource resource, ArrayList<ArrayList<SporadicTask>> tasks, long[][] Ris, long time,
			long jitter, int request_number, boolean btbHit, boolean useRi, SporadicTask calTask) {
		int number_of_access = 0;

		for (int i = 0; i < tasks.size(); i++) {
			if (i != task.partition) {
				/* For each remote partition */
				int number_of_request_by_Remote_P = 0;
				for (int j = 0; j < tasks.get(i).size(); j++) {
					if (tasks.get(i).get(j).resource_required_index.contains(resource.id - 1)) {
						SporadicTask remote_task = tasks.get(i).get(j);
						int indexR = getIndexRInTask(remote_task, resource);
						int number_of_release = (int) Math
								.ceil((double) (time + (btbHit ? (useRi ? Ris[i][j] : remote_task.deadline) : 0)) / (double) remote_task.period);
						number_of_request_by_Remote_P += number_of_release * remote_task.number_of_access_in_one_release.get(indexR);
					}
				}
				int getNoRFromHP = getNoRFromHP(task, resource, tasks.get(task.partition), Ris[task.partition], time, btbHit, useRi);
				int possible_spin_delay = number_of_request_by_Remote_P - getNoRFromHP - request_number + 1 < 0 ? 0
						: number_of_request_by_Remote_P - getNoRFromHP - request_number + 1;
				number_of_access += Integer.min(possible_spin_delay, 1);
			}
		}

		// account for the request of the task itself
		number_of_access++;

		calTask.implementation_overheads += number_of_access * (AnalysisUtils.MrsP_LOCK + AnalysisUtils.MrsP_UNLOCK);
		return number_of_access * resource.csl;
	}

	/*
	 * Calculate the local blocking for task t.
	 */
	private long localBlocking(SporadicTask t, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, long[][] Ris, long time, double oneMig,
			long np, boolean btbHit, boolean useRi) {
		ArrayList<Resource> LocalBlockingResources = getLocalBlockingResources(t, resources, tasks.get(t.partition));
		ArrayList<Long> local_blocking_each_resource = new ArrayList<>();
		ArrayList<Double> overheads = new ArrayList<>();

		for (int i = 0; i < LocalBlockingResources.size(); i++) {
			double arrivalBlockingOverheads = 0;
			ArrayList<Integer> migration_targets = new ArrayList<>();

			Resource res = LocalBlockingResources.get(i);
			long local_blocking = res.csl;
			arrivalBlockingOverheads += AnalysisUtils.MrsP_LOCK + AnalysisUtils.MrsP_UNLOCK;

			migration_targets.add(t.partition);
			if (res.isGlobal) {
				int remoteblocking = 0;
				for (int parition_index = 0; parition_index < res.partitions.size(); parition_index++) {
					int partition = res.partitions.get(parition_index);
					int norHP = getNoRFromHP(t, res, tasks.get(t.partition), Ris[t.partition], time, btbHit, useRi);
					int norT = t.resource_required_index.contains(res.id - 1)
							? t.number_of_access_in_one_release.get(t.resource_required_index.indexOf(res.id - 1))
							: 0;
					int norR = 0;
					try {
						norR = getNoRRemote(res, tasks.get(partition), Ris[partition], time, btbHit, useRi);
					} catch (Exception e) {
						System.out.println("sss");
					}

					if (partition != t.partition && (norHP + norT) < norR) {
						local_blocking += res.csl;
						remoteblocking++;
						migration_targets.add(partition);
					}
				}

				arrivalBlockingOverheads += remoteblocking * (AnalysisUtils.MrsP_LOCK + AnalysisUtils.MrsP_UNLOCK);
				double mc_plus = 0;
				if (oneMig != 0) {
					double mc = migrationCostForArrival(oneMig, np, migration_targets, res, tasks, t);

					long mc_long = (long) Math.floor(mc);
					mc_plus += mc - mc_long;
					if (mc - mc_long < 0) {
						System.err.println("MrsP mig error");
						System.exit(-1);
					}
					local_blocking += mc_long;
				}
				arrivalBlockingOverheads += mc_plus;
			}

			local_blocking_each_resource.add(local_blocking);
			overheads.add(arrivalBlockingOverheads);
		}

		if (local_blocking_each_resource.size() >= 1) {
			if (overheads.size() <= 0) {
				System.err.println("overheads error!");
				System.exit(-1);
			}
			local_blocking_each_resource.sort((l1, l2) -> -Double.compare(l1, l2));
			overheads.sort((l1, l2) -> -Double.compare(l1, l2));
			t.mrsp_arrivalblocking_overheads = overheads.get(0);
		}

		return local_blocking_each_resource.size() > 0 ? local_blocking_each_resource.get(0) : 0;
	}

	/*
	 * gives a set of resources that can cause local blocking for a given task
	 */
	private ArrayList<Resource> getLocalBlockingResources(SporadicTask task, ArrayList<Resource> resources, ArrayList<SporadicTask> localTasks) {
		ArrayList<Resource> localBlockingResources = new ArrayList<>();
		int partition = task.partition;

		for (int i = 0; i < resources.size(); i++) {
			Resource resource = resources.get(i);

			if (resource.partitions.contains(partition) && resource.getCeilingForProcessor(localTasks) >= task.priority) {
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

	/***************************************************
	 ************* Migration Cost Calculation **********
	 ***************************************************/

	private double migrationCostForSpin(SporadicTask task, Resource resource, ArrayList<ArrayList<SporadicTask>> tasks, long[][] Ris, long time,
			int request_number, double oneMig, long np, boolean btbHit, boolean useRi, SporadicTask calT) {

		ArrayList<Integer> migration_targets = new ArrayList<>();

		// identify the migration targets
		migration_targets.add(task.partition);
		for (int i = 0; i < tasks.size(); i++) {
			if (i != task.partition) {
				int number_requests_left = 0;
				number_requests_left = getNoRRemote(resource, tasks.get(i), Ris[i], time, btbHit, useRi)
						- getNoRFromHP(task, resource, tasks.get(task.partition), Ris[task.partition], time, btbHit, useRi) - request_number + 1;

				if (number_requests_left > 0)
					migration_targets.add(i);
			}
		}

		double mcspin = migrationCost(oneMig, np, migration_targets, resource, tasks, calT);
		return mcspin;
	}

	private double migrationCostForArrival(double oneMig, long np, ArrayList<Integer> migration_targets, Resource resource,
			ArrayList<ArrayList<SporadicTask>> tasks, SporadicTask calT) {
		double mcarrival = migrationCost(oneMig, np, migration_targets, resource, tasks, calT);
		return mcarrival;
	}

	private double migrationCost(double oneMig, long np, ArrayList<Integer> migration_targets, Resource resource, ArrayList<ArrayList<SporadicTask>> tasks,
			SporadicTask calT) {
		double migrationCost = 0;
		ArrayList<Integer> migration_targets_with_P = new ArrayList<>();

		// identify the migration targets with preemptors
		for (int i = 0; i < migration_targets.size(); i++) {
			int partition = migration_targets.get(i);
			if (tasks.get(partition).get(0).priority > resource.getCeilingForProcessor(tasks, partition))
				migration_targets_with_P.add(migration_targets.get(i));
		}

		// check
		if (!migration_targets.containsAll(migration_targets_with_P)) {
			System.out.println("migration targets error!");
			System.exit(0);
		}

		// now we compute the migration cost for each request
		for (int i = 0; i < migration_targets.size(); i++) {
			double migration_cost_for_one_access = 0;
			int partition = migration_targets.get(i); // the request issued
														// from.

			// calculating migration cost
			// 1. If there is no preemptors on the task's partition OR there is
			// no
			// other migration targets
			if (!migration_targets_with_P.contains(partition) || (migration_targets.size() == 1 && migration_targets.get(0) == partition))
				migration_cost_for_one_access = 0;

			// 2. If there is preemptors on the task's partition AND there are
			// no
			// preemptors on other migration targets
			else if (migration_targets_with_P.size() == 1 && migration_targets_with_P.get(0) == partition && migration_targets.size() > 1)
				migration_cost_for_one_access = 2 * oneMig;

			// 3. If there exist multiple migration targets with preemptors.
			// With NP
			// section applied.
			else {
				if (np > 0) {
					double migCostWithNP = (long) (1 + Math.ceil((double) resource.csl / (double) np)) * oneMig;
					double migCostWithHP = migrationCostBusyWindow(migration_targets_with_P, oneMig, resource, tasks, calT, migCostWithNP);
					migration_cost_for_one_access = Math.min(migCostWithHP, migCostWithNP);
				} else {
					migration_cost_for_one_access = migrationCostBusyWindow(migration_targets_with_P, oneMig, resource, tasks, calT, -1);
				}
			}
			migrationCost += migration_cost_for_one_access;
		}

		return migrationCost;
	}

	private double migrationCostBusyWindow(ArrayList<Integer> migration_targets_with_P, double oneMig, Resource resource,
			ArrayList<ArrayList<SporadicTask>> tasks, SporadicTask calT, double migByNP) {
		double migCost = 0;

		double newMigCost = migrationCostOneCal(migration_targets_with_P, oneMig, resource.csl + migCost, resource, tasks);

		while (migCost != newMigCost) {
			migCost = newMigCost;
			newMigCost = migrationCostOneCal(migration_targets_with_P, oneMig, resource.csl + migCost, resource, tasks);

			if (newMigCost > calT.deadline) {
				return newMigCost;
			}
			if (migByNP > 0 && newMigCost > migByNP) {
				return newMigCost;
			}
		}

		return migCost;
	}

	private double migrationCostOneCal(ArrayList<Integer> migration_targets_with_P, double oneMig, double duration, Resource resource,
			ArrayList<ArrayList<SporadicTask>> tasks) {
		double migCost = 0;

		for (int i = 0; i < migration_targets_with_P.size(); i++) {
			int partition_with_p = migration_targets_with_P.get(i);

			for (int j = 0; j < tasks.get(partition_with_p).size(); j++) {
				SporadicTask hpTask = tasks.get(partition_with_p).get(j);

				if (hpTask.priority > resource.getCeilingForProcessor(tasks, partition_with_p))
					migCost += Math.ceil((duration) / hpTask.period) * oneMig;
			}
		}
		return migCost + oneMig;
	}

	/******************************************************
	 ************* Migration Cost Calculation END**********
	 ******************************************************/

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
