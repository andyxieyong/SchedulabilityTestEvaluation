package test;

import java.util.ArrayList;

import analysis.FIFONonPreemptiveLinearC;
import analysis.NewMrsPRTA;
import entity.Resource;
import entity.SporadicTask;
import generatorTools.SystemGenerator;

public class DifferentTest {

	public static int TOTAL_NUMBER_OF_SYSTEMS = 1000;
	public static int TOTAL_PARTITIONS = 16;
	public static int MIN_PERIOD = 1;
	public static int MAX_PERIOD = 1000;
	public static int NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION = 9;
	public static int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 2;
	public static double RESOURCE_SHARING_FACTOR = .2;

	public static void main(String[] args) {

		NewMrsPRTA new_mrsp = new NewMrsPRTA();
		FIFONonPreemptiveLinearC fnp = new FIFONonPreemptiveLinearC();

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, 0.1 * (double) NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION,
				TOTAL_PARTITIONS, NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION, true, Analysiser.CS_LENGTH_RANGE.VERY_SHORT_CS_LEN,
				Analysiser.RESOURCES_RANGE.PARTITIONS, RESOURCE_SHARING_FACTOR, NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);

		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
			ArrayList<ArrayList<SporadicTask>> tasks = generator.generateTasks();
			ArrayList<Resource> resources = generator.generateResources();
			generator.generateResourceUsage(tasks, resources);

			new_mrsp.NewMrsPRTATest(tasks, resources, false);
			fnp.NewMrsPRTATest(tasks, resources, true, false);
			fnp.NewMrsPRTATest(tasks, resources, false, false);
		}

	}

}
