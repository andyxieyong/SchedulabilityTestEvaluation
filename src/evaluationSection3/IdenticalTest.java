package evaluationSection3;

import java.util.ArrayList;

import analysisILP.FIFOLinearC;
import analysisNew.MSRPNew;
import analysisNew.PWLPNew;
import entity.Resource;
import entity.SporadicTask;
import generatorTools.SimpleSystemGenerator;
import utils.AnalysisUtils.CS_LENGTH_RANGE;
import utils.AnalysisUtils.RESOURCES_RANGE;

public class IdenticalTest {

	public static int TOTAL_NUMBER_OF_SYSTEMS = 99999999;
	public static int TOTAL_PARTITIONS = 10;
	public static int MIN_PERIOD = 1;
	public static int MAX_PERIOD = 1000;
	public static int NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION = 4;
	public static int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 2;
	public static double RESOURCE_SHARING_FACTOR = .4;

	static long maxC = 0;

	public static void main(String[] args) {
		FIFOLinearC fp_c = new FIFOLinearC();
		PWLPNew fp_java = new PWLPNew();
		MSRPNew fnp_java = new MSRPNew();
		long[][] r1, r2, r3, r4;

		SimpleSystemGenerator generator = new SimpleSystemGenerator(MIN_PERIOD, MAX_PERIOD, TOTAL_PARTITIONS,
				NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION * TOTAL_PARTITIONS, true, CS_LENGTH_RANGE.VERY_SHORT_CS_LEN, RESOURCES_RANGE.PARTITIONS,
				RESOURCE_SHARING_FACTOR, NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);

		int i = 0;
		while (i <= TOTAL_NUMBER_OF_SYSTEMS) {

			ArrayList<SporadicTask> tasksToAlloc = generator.generateTasks();
			ArrayList<Resource> resources = generator.generateResources();
			ArrayList<ArrayList<SporadicTask>> tasks = generator.generateResourceUsage(tasksToAlloc, resources);

			r1 = fp_c.getResponseTime(tasks, resources, true, false);
			r2 = fp_java.getResponseTime(tasks, resources, false);
			boolean isEqual1 = isEqual(r1, r2, false);

			if (!isEqual1 && isSystemSchedulable(tasks, r1) && isSystemSchedulable(tasks, r2)) {
				System.out.println("not equal");
				isEqual(r1, r2, true);
				r1 = fp_c.getResponseTime(tasks, resources, true, false);
				r2 = fp_java.getResponseTime(tasks, resources, false);
				System.exit(0);
			}

			r3 = fp_c.getResponseTime(tasks, resources, false, false);
			r4 = fnp_java.getResponseTime(tasks, resources, false);
			boolean isEqual2 = isEqual(r3, r4, true);

			if (!isEqual2 && isSystemSchedulable(tasks, r3) && isSystemSchedulable(tasks, r4)) {
				System.out.println("not equal");
				isEqual(r3, r4, true);
				r3 = fp_c.getResponseTime(tasks, resources, true, false);
				r4 = fp_java.getResponseTime(tasks, resources, false);
				System.exit(0);
			}

			if (isEqual1 && isEqual2 && isSystemSchedulable(tasks, r1) && isSystemSchedulable(tasks, r2) && isSystemSchedulable(tasks, r3)
					&& isSystemSchedulable(tasks, r4)) {
				System.out.println(i);
				i++;
			}

		}

	}

	public static boolean isEqual(long[][] r1, long[][] r2, boolean print) {
		boolean isequal = true;
		for (int i = 0; i < r1.length; i++) {
			for (int j = 0; j < r1[i].length; j++) {
				if (r1[i][j] != r2[i][j]) {
					if (print)
						System.out.println("not equal at:  i=" + i + "  j=" + j + "   r1: " + r1[i][j] + "   r2:" + r2[i][j]);
					isequal = false;
				}
			}
		}
		return isequal;
	}

	public static boolean isSystemSchedulable(ArrayList<ArrayList<SporadicTask>> tasks, long[][] Ris) {
		for (int i = 0; i < tasks.size(); i++) {
			for (int j = 0; j < tasks.get(i).size(); j++) {
				if (tasks.get(i).get(j).deadline < Ris[i][j])
					return false;
			}
		}
		return true;
	}

}
