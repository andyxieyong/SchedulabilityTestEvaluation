package analysisILP;

import java.util.ArrayList;

import analysisBasic.RTAWithoutBlocking;
import entity.Resource;
import entity.SporadicTask;
import javaToC.MIPSolverC;
import utils.AnalysisUtils;

public class FIFOLinearC {

	long count = 0;
	MIPSolverC solver = new MIPSolverC();

	public long[][] getResponseTime(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, boolean isPreemptable, boolean printDebug) {
		long[][] init_Ri = null;
		if (isPreemptable) {
			init_Ri = new RTAWithoutBlocking().getResponseTime(tasks, resources, false);
		} else {
			init_Ri = new AnalysisUtils().initResponseTime(tasks);
		}

		for (int i = 0; i < tasks.size(); i++) {
			for (int j = 0; j < tasks.get(i).size(); j++) {
				SporadicTask t = tasks.get(i).get(j);
				t.Ri = init_Ri[i][j];
				t.interference = t.local = t.spin = 0;
				if (t.Ri > t.deadline)
					return init_Ri;
			}
		}

		long[][] response_time = new long[tasks.size()][];
		boolean isEqual = false, missDeadline = false;
		count = 0;

		for (int i = 0; i < init_Ri.length; i++) {
			response_time[i] = new long[init_Ri[i].length];
		}

		new AnalysisUtils().cloneList(init_Ri, response_time);

		/* a huge busy window to get a fixed Ri */
		while (!isEqual) {
			isEqual = true;
			long[][] response_time_plus = busyWindow(tasks, resources, response_time, isPreemptable);

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
			if (missDeadline) {
				if (isPreemptable)
					System.out.println("FIFO Preemptive LinearC    after " + count + " tims of recursion, the tasks miss the deadline.");
				else
					System.out.println("FIFO NONE Preemptive LinearC    after " + count + " tims of recursion, the tasks miss the deadline.");
			}

			else {
				if (isPreemptable)
					System.out.println("FIFO Preemptive LinearC    after " + count + " tims of recursion, we got the response time.");
				else
					System.out.println("FIFO NONE Preemptive LinearC    after " + count + " tims of recursion, we got the response time.");
			}

			new AnalysisUtils().printResponseTime(response_time, tasks);
		}

		return response_time;
	}

	private long[][] busyWindow(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, long[][] response_time, boolean isPreemptable) {
		int taskSize = 0;
		long[][] response_time_plus = new long[tasks.size()][];

		for (int i = 0; i < response_time.length; i++) {
			response_time_plus[i] = new long[response_time[i].length];
			taskSize += response_time[i].length;
		}

		solver.solveMIP(tasks, resources, taskSize, isPreemptable);

		for (int i = 0; i < tasks.size(); i++) {
			for (int j = 0; j < tasks.get(i).size(); j++) {
				SporadicTask task = tasks.get(i).get(j);
				task.interference = highPriorityInterference(task, tasks, response_time[i][j], response_time, resources);
				response_time_plus[i][j] = task.Ri = task.WCET + task.pure_resource_execution_time + task.spin + task.interference + task.local;
				if (task.Ri > task.deadline)
					return response_time_plus;
			}
		}
		return response_time_plus;
	}

	/*
	 * Calculate the local high priority tasks' interference for a given task t.
	 * CI is a set of computation time of local tasks, including spin delay.
	 */
	private long highPriorityInterference(SporadicTask t, ArrayList<ArrayList<SporadicTask>> allTasks, long Ri, long[][] Ris, ArrayList<Resource> resources) {
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

}
