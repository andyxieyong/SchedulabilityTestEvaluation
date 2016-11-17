package discardAnalysis;

import java.util.ArrayList;

import analysis.MSRPRTA;
import analysis.OriginalMrsPRTA;
import entity.Resource;
import entity.SporadicTask;
import generatorTools.SystemGenerator;
import test.Analysiser.CS_LENGTH_RANGE;
import test.Analysiser.RESOURCES_RANGE;

public class parallism {
	public static void experimentIncreasingParallelism() {
		int MIN_PERIOD = 1;
		int MAX_PERIOD = 1000;
		int TOTAL_NUMBER_OF_SYSTEMS = 10000;

		double UTIL_PER_PARTITION = 0.5;
		int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 5;
		int TOTAL_PARTITIONS = 128;

		double RESOURCE_SHARING_FACTOR = 0.5;
		int NUMBER_OF_TASKS_ON_EACH_PARTITION = 6;

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, UTIL_PER_PARTITION, TOTAL_PARTITIONS,
				NUMBER_OF_TASKS_ON_EACH_PARTITION, true, CS_LENGTH_RANGE.SHORT_CS_LEN, RESOURCES_RANGE.PARTITIONS,
				RESOURCE_SHARING_FACTOR, NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);
		long[][] Ris;

		NewMrsPRTA newRTA2 = new NewMrsPRTA();
		OriginalMrsPRTA originalRTA = new OriginalMrsPRTA();
		MSRPRTA msrp = new MSRPRTA();
//		RTAWithoutBlocking no_blocking = new RTAWithoutBlocking();

		for (int j = 4; j <= TOTAL_PARTITIONS;) {
			int schedulableSystem_New_MrsP_Analysis2 = 0;
			int schedulableSystem_Original_MrsP_Analysis = 0;
			int schedulableSystem_MSRP_Analysis = 0;

			for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {

				ArrayList<ArrayList<SporadicTask>> tasks = generator.generateTasks();
				ArrayList<Resource> resources = generator.generateResources();
				generator.generateResourceUsage(tasks, resources);

				Ris = newRTA2.NewMrsPRTATest(tasks, resources, false);
				if (isSystemSchedulable(tasks, Ris))
					schedulableSystem_New_MrsP_Analysis2++;

				Ris = originalRTA.NewMrsPRTATest(tasks, resources, false);
				if (isSystemSchedulable(tasks, Ris))
					schedulableSystem_Original_MrsP_Analysis++;

				Ris = msrp.NewMrsPRTATest(tasks, resources, false);
				if (isSystemSchedulable(tasks, Ris))
					schedulableSystem_MSRP_Analysis++;

			}

			System.err.println("number of partitions: " + j + " ; System number: " + TOTAL_NUMBER_OF_SYSTEMS + " ; New MrsP: "
					+ (double) schedulableSystem_New_MrsP_Analysis2 / (double) TOTAL_NUMBER_OF_SYSTEMS + " Original MrsP: "
					+ (double) schedulableSystem_Original_MrsP_Analysis / (double) TOTAL_NUMBER_OF_SYSTEMS + " MSRP: "
					+ (double) schedulableSystem_MSRP_Analysis / (double) TOTAL_NUMBER_OF_SYSTEMS);

			j = j * 2;
		}
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
