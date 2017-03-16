package test;

import java.util.ArrayList;

import basicAnalysis.FIFOLinearC;
import basicAnalysis.FIFOP;
import entity.Resource;
import entity.SporadicTask;
import generatorTools.SystemGenerator;

public class IdenticalTest {

	public static int TOTAL_NUMBER_OF_SYSTEMS = 99999999;
	public static int TOTAL_PARTITIONS = 5;
	public static int MIN_PERIOD = 1;
	public static int MAX_PERIOD = 1000;
	public static int NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION = 5;
	public static int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 5;
	public static double RESOURCE_SHARING_FACTOR = .5;

	public static void main(String[] args) {

		// FIFOLinearC f_c = new FIFOLinearC();
		// FIFOPLinearJava fp_java = new FIFOPLinearJava();

		FIFOP fnp = new FIFOP();
		FIFOLinearC fnp_java = new FIFOLinearC();

		// NewMrsPRTA mrsp = new NewMrsPRTA();
		// NewMrsPRTAWithMigrationCostAsIndividual s_mrsp = new
		// NewMrsPRTAWithMigrationCostAsIndividual();
		long[][] r1, r2;

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, 0.1 * (double) NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION,
				TOTAL_PARTITIONS, NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION, true, SchedulabilityTest.CS_LENGTH_RANGE.SHORT_CS_LEN,
				SchedulabilityTest.RESOURCES_RANGE.PARTITIONS, RESOURCE_SHARING_FACTOR, NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);

		int i = 0;
		while (i <= TOTAL_NUMBER_OF_SYSTEMS) {

			ArrayList<ArrayList<SporadicTask>> tasks = generator.generateTasks();
			ArrayList<Resource> resources = generator.generateResources();
			generator.generateResourceUsage(tasks, resources);

			r1 = fnp.NewMrsPRTATest(tasks, resources, false);
			r2 = fnp_java.NewMrsPRTATest(tasks, resources, true, false);
			boolean isEqual = isEqual(r1, r2, false);

			if (!isEqual && isSystemSchedulable(tasks, r1) && isSystemSchedulable(tasks, r2)) {
				System.out.println("not equal");
				isEqual(r1, r2, true);
				SystemGenerator.testifyGeneratedTasksetAndResource(tasks, resources);
				r1 = fnp.NewMrsPRTATest(tasks, resources, true);
				r2 = fnp_java.NewMrsPRTATest(tasks, resources, true, true);
				System.exit(0);
			}
			if (isEqual && isSystemSchedulable(tasks, r1) && isSystemSchedulable(tasks, r2)) {
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
