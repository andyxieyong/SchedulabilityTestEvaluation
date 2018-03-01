package evaluationSection4;

import java.util.ArrayList;

import entity.Resource;
import entity.SporadicTask;
import generatorTools.AllocationGeneator;
import generatorTools.SystemGenerator;
import utils.AnalysisUtils.CS_LENGTH_RANGE;
import utils.AnalysisUtils.RESOURCES_RANGE;

public class testAllocation {
	public static int TOTAL_NUMBER_OF_SYSTEMS = 9999999;

	public static int MAX_PERIOD = 1000;
	public static int MIN_PERIOD = 1;
	static int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 3;
	static int NUMBER_OF_TASKS_ON_EACH_PARTITION = 4;
	static CS_LENGTH_RANGE range = CS_LENGTH_RANGE.MEDIUM_CS_LEN;
	static double RESOURCE_SHARING_FACTOR = 0.3;
	public static int TOTAL_PARTITIONS = 16;
	public static boolean useRi = true;
	public static boolean btbHit = true;
	public static int PROTOCOLS = 3;

	AllocationGeneator allocGeneator = new AllocationGeneator();

	public static void main(String[] args) throws Exception {
		testAllocation test = new testAllocation();

		for (int i = 1; i < 7; i++) {
			test.experimentIncreasingCriticalSectionLength(i);
		}
	}

	public void experimentIncreasingCriticalSectionLength(int cs_len) {
		final CS_LENGTH_RANGE cs_range;
		switch (cs_len) {
		case 1:
			cs_range = CS_LENGTH_RANGE.VERY_SHORT_CS_LEN;
			break;
		case 2:
			cs_range = CS_LENGTH_RANGE.SHORT_CS_LEN;
			break;
		case 3:
			cs_range = CS_LENGTH_RANGE.MEDIUM_CS_LEN;
			break;
		case 4:
			cs_range = CS_LENGTH_RANGE.LONG_CSLEN;
			break;
		case 5:
			cs_range = CS_LENGTH_RANGE.VERY_LONG_CSLEN;
			break;
		case 6:
			cs_range = CS_LENGTH_RANGE.Random;
			break;
		default:
			cs_range = null;
			break;
		}

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, true, TOTAL_PARTITIONS, NUMBER_OF_TASKS_ON_EACH_PARTITION * TOTAL_PARTITIONS,
				RESOURCE_SHARING_FACTOR, cs_range, RESOURCES_RANGE.PARTITIONS, NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE, false);

		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
			ArrayList<SporadicTask> tasksToAlloc = null;
			ArrayList<Resource> resources = null;
			tasksToAlloc = generator.generateTasks(true);
			resources = generator.generateResources();

			generator.generateResourceUsage(tasksToAlloc, resources);

			allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 4);
			// allocGeneator.allocateTasks(tasksToAlloc, resources,
			// generator.total_partitions, 5);
			// allocGeneator.allocateTasks(tasksToAlloc, resources,
			// generator.total_partitions, 6);
			// allocGeneator.allocateTasks(tasksToAlloc, resources,
			// generator.total_partitions, 7);

			System.out.println("times: " + i);
		}

	}

}
