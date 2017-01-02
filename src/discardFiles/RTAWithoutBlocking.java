package discardFiles;

import java.util.ArrayList;

import entity.SporadicTask;

public class RTAWithoutBlocking {

	public long[][] schedulabilityTestForMultiprocessor(ArrayList<ArrayList<SporadicTask>> tasks, boolean printDebugInfo) {
		long[][] response_times = new long[tasks.size()][];
		for (int i = 0; i < tasks.size(); i++) {
			response_times[i] = schedulabilityTestForOnePartition(tasks.get(i), printDebugInfo);
		}
		return response_times;
	}

	public long[] schedulabilityTestForOnePartition(ArrayList<SporadicTask> hardRTTasks, boolean printDebugInfo) {
		hardRTTasks.sort((t1, t2) -> -Integer.compare(t1.priority, t2.priority));
		long[] Wi = new long[hardRTTasks.size()];

		for (int i = 0; i < hardRTTasks.size(); i++) {
			SporadicTask t = hardRTTasks.get(i);

			Wi[i] = t.WCET + t.pure_resource_execution_time; /* Wi */
			long WiPlus1;
			while (true) {
				WiPlus1 = busyWindow(t, hardRTTasks, Wi[i]);
				if (Wi[i] == WiPlus1) {
					break;
				} else {
					Wi[i] = WiPlus1;
				}
			}
			if (printDebugInfo) {
				System.out.print("T" + t.id + " : Ri = " + Wi[i] + ", D = " + t.deadline + " ");
				if (Wi[i] > t.deadline)
					System.out.println("T" + t.id + " : Misses the deadline.");
				else
					System.out.println("T" + t.id + " : Schedulable.  ");
			}

		}
		return Wi;
	}

	public long busyWindow(SporadicTask t, ArrayList<SporadicTask> tasks, long Ri) {
		long interference = 0;
		for (int i = 0; i < tasks.size(); i++) {
			SporadicTask hp = tasks.get(i);
			if (hp.priority > t.priority)
				interference += Math.ceil((double) Ri / (double) hp.period) * (hp.WCET + hp.pure_resource_execution_time);
		}
		long responseTime = t.WCET + t.pure_resource_execution_time + interference;
		return responseTime;
	}
}