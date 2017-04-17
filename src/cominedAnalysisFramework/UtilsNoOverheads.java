package cominedAnalysisFramework;

import java.util.ArrayList;

import entity.SporadicTask;

public class UtilsNoOverheads {
	private static double LINUX_SCHED = 0;
	private static double LINUX_SCHED_AWAY = 0;
	private static double LINUX_CONTEXT_SWTICH = 0;

	private static double PFP_SCHEDULER = 0;
	private static double LITMUS_RELEASE = 0;
	private static double LITMUS_COMPLETE = 0;

	public static double FIFONP_LOCK = 0;
	public static double FIFONP_UNLOCK = 0;

	public static double FIFOP_LOCK = 0;
	public static double FIFOP_UNLOCK = 0;
	public static double FIFOP_DEQUEUE_IN_SCHEDULE = 0;
	public static double FIFOP_RE_REQUEST = 0;

	public static double MrsP_LOCK = 0;
	public static double MrsP_UNLOCK = 0;
	public static double MrsP_HELP_IN_LOCK = 0;
	public static double MrsP_INSERT = 0;
	public static double MrsP_HELP_IN_SCHEDULE = 0;

	public static double FULL_CONTEXT_SWTICH1 = LINUX_SCHED + LINUX_SCHED_AWAY + LINUX_CONTEXT_SWTICH + PFP_SCHEDULER;
	public static double FULL_CONTEXT_SWTICH2 = FULL_CONTEXT_SWTICH1 + LITMUS_RELEASE + LITMUS_COMPLETE;

	public static double MrsP_PREEMPTION_AND_MIGRATION = 6;

	public static void main(String args[]) {
		System.out.println(" FIFO-P Lock:   " + FIFOP_LOCK + "   FIFO-P UNLOCK:   " + FIFOP_UNLOCK);
		System.out.println(" FIFO-NP Lock:   " + FIFONP_LOCK + "   FIFO-NP UNLOCK:   " + FIFONP_UNLOCK + "   RE-REQUEST:   "
				+ (UtilsNoOverheads.FIFOP_DEQUEUE_IN_SCHEDULE + UtilsNoOverheads.FIFOP_RE_REQUEST));
		System.out.println(" MrsP Lock:   " + MrsP_LOCK + "   MrsP UNLOCK:   " + MrsP_UNLOCK + "   MIG:   " + MrsP_PREEMPTION_AND_MIGRATION);
		System.out.println(" CX1:    " + FULL_CONTEXT_SWTICH1 + "   CX2:   " + FULL_CONTEXT_SWTICH2);
	}

	public static long[][] initResponseTime(ArrayList<ArrayList<SporadicTask>> tasks) {
		long[][] response_times = new long[tasks.size()][];

		for (int i = 0; i < tasks.size(); i++) {
			ArrayList<SporadicTask> task_on_a_partition = tasks.get(i);
			task_on_a_partition.sort((t1, t2) -> -Integer.compare(t1.priority, t2.priority));

			long[] Ri = new long[task_on_a_partition.size()];

			for (int j = 0; j < task_on_a_partition.size(); j++) {
				SporadicTask t = task_on_a_partition.get(j);
				Ri[j] = t.Ri = t.WCET + t.pure_resource_execution_time;
				t.interference = t.local = t.spin = t.total_blocking = 0;
			}
			response_times[i] = Ri;
		}
		return response_times;
	}

	public static void printResponseTime(long[][] Ris, ArrayList<ArrayList<SporadicTask>> tasks) {
		int task_id = 1;
		for (int i = 0; i < Ris.length; i++) {
			for (int j = 0; j < Ris[i].length; j++) {
				System.out.println("T" + task_id + " RT: " + Ris[i][j] + ", D: " + tasks.get(i).get(j).deadline + ", S = " + tasks.get(i).get(j).spin
						+ ", L = " + tasks.get(i).get(j).local + ", I = " + tasks.get(i).get(j).interference + ", WCET = " + tasks.get(i).get(j).WCET
						+ ", Resource: " + tasks.get(i).get(j).pure_resource_execution_time);
				task_id++;
			}
			System.out.println();
		}
	}

	public static void cloneList(long[][] oldList, long[][] newList) {
		for (int i = 0; i < oldList.length; i++) {
			for (int j = 0; j < oldList[i].length; j++) {
				newList[i][j] = oldList[i][j];
			}
		}
	}

	public static boolean isArrayContain(int[] array, int value) {

		for (int i = 0; i < array.length; i++) {
			if (array[i] == value)
				return true;
		}
		return false;
	}

}
