package analysisWithPriority;

import java.util.ArrayList;

import entity.Resource;
import entity.SporadicTask;

public abstract class RuntimeCostAnalysis {

	int extendCal = 5;

	public abstract long[][] getResponseTimeSBPO(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, boolean isprint);

	public abstract long[][] getResponseTimeRPA(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, boolean isprint);

	public abstract long[][] getResponseTimeOPA(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, boolean isprint);

	public abstract long[][] getResponseTimeDM(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, boolean btbHit, boolean useRi,
			boolean printDebug);

	public long[][] initResponseTime(ArrayList<ArrayList<SporadicTask>> tasks) {
		long[][] response_times = new long[tasks.size()][];

		for (int i = 0; i < tasks.size(); i++) {
			ArrayList<SporadicTask> task_on_a_partition = tasks.get(i);
			task_on_a_partition.sort((t1, t2) -> -Integer.compare(t1.priority, t2.priority));

			long[] Ri = new long[task_on_a_partition.size()];

			for (int j = 0; j < task_on_a_partition.size(); j++) {
				SporadicTask t = task_on_a_partition.get(j);
				Ri[j] = t.Ri = t.WCET + t.pure_resource_execution_time;
				t.spin = t.interference = t.local = t.indirectspin = t.total_blocking = 0;
				t.blocking_overheads = t.implementation_overheads = t.migration_overheads_plus = t.mrsp_arrivalblocking_overheads = t.fifonp_arrivalblocking_overheads = t.fifop_arrivalblocking_overheads = 0;

			}
			response_times[i] = Ri;
		}
		return response_times;
	}

	public boolean isSystemSchedulable(ArrayList<ArrayList<SporadicTask>> tasks, long[][] Ris) {
		for (int i = 0; i < tasks.size(); i++) {
			for (int j = 0; j < tasks.get(i).size(); j++) {
				if (tasks.get(i).get(j).deadline < Ris[i][j])
					return false;
			}
		}
		return true;
	}

	public void cloneList(long[][] oldList, long[][] newList) {
		for (int i = 0; i < oldList.length; i++) {
			for (int j = 0; j < oldList[i].length; j++) {
				newList[i][j] = oldList[i][j];
			}
		}
	}

	public boolean isArrayContain(int[] array, int value) {

		for (int i = 0; i < array.length; i++) {
			if (array[i] == value)
				return true;
		}
		return false;
	}

	public void printResponseTime(long[][] Ris, ArrayList<ArrayList<SporadicTask>> tasks) {

		for (int i = 0; i < Ris.length; i++) {
			for (int j = 0; j < Ris[i].length; j++) {
				System.out.println(
						"T" + tasks.get(i).get(j).id + " RT: " + Ris[i][j] + ", P: " + tasks.get(i).get(j).priority + ", D: " + tasks.get(i).get(j).deadline
								+ ", S = " + tasks.get(i).get(j).spin + ", L = " + tasks.get(i).get(j).local + ", I = " + tasks.get(i).get(j).interference
								+ ", WCET = " + tasks.get(i).get(j).WCET + ", Resource: " + tasks.get(i).get(j).pure_resource_execution_time + ", B = "
								+ tasks.get(i).get(j).indirectspin + ", implementation_overheads: " + tasks.get(i).get(j).implementation_overheads);

			}
			System.out.println();
		}
	}
}
