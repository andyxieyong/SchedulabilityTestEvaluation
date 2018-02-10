package evaluationSection4;

import java.util.ArrayList;

import entity.Resource;
import entity.SporadicTask;
import generatorTools.AllocationGeneator;
import generatorTools.SystemGenerator;
import utils.AnalysisUtils.CS_LENGTH_RANGE;
import utils.AnalysisUtils.RESOURCES_RANGE;

/**
 * OLD Approach Work Load CS Length 11 WF: 0.958 0.776 BF: 0.969 0.795 FF: 0.963
 * 0.793 NF: 0.972 0.838 RRF: 0.897 0.853 RLF: 0.865 0.786 RLDF: 0.896 0.836
 * RLIF: 0.898 0.845 12 WF: 0.812 0.667 BF: 0.829 0.701 FF: 0.826 0.699 NF: 0.85
 * 0.724 RRF: 0.844 0.784 RLF: 0.792 0.724 RLDF: 0.834 0.769 RLIF: 0.831 0.777
 * 13 WF: 0.543 0.552 BF: 0.613 0.568 FF: 0.613 0.569 NF: 0.676 0.645 RRF: 0.763
 * 0.714 RLF: 0.686 0.629 RLDF: 0.75 0.708 RLIF: 0.758 0.703 14 WF: 0.12 0.394
 * BF: 0.125 0.376 FF: 0.122 0.378 NF: 0.131 0.449 RRF: 0.547 0.525 RLF: 0.397
 * 0.449 RLDF: 0.554 0.523 RLIF: 0.544 0.534 15 WF: 0.046 0.316 BF: 0.024 0.286
 * FF: 0.023 0.289 NF: 0.04 0.373 RRF: 0.433 0.456 RLF: 0.293 0.384 RLDF: 0.408
 * 0.431 RLIF: 0.401 0.426 16 WF: 0.077 0.404 BF: 0.071 0.387 FF: 0.074 0.391
 * NF: 0.081 0.464 RRF: 0.526 0.528 RLF: 0.366 0.456 RLDF: 0.541 0.526 RLIF:
 * 0.482 0.52 Resource Access Parallel RSF
 *
 */

public class testResourceOrientedAlgorithms {
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
		testResourceOrientedAlgorithms test = new testResourceOrientedAlgorithms();

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
			tasksToAlloc = generator.generateTasks();
			resources = generator.generateResources();

			generator.generateResourceUsage(tasksToAlloc, resources);

			allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 4);
			allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 5);
			allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 6);
			allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 7);

			System.out.println("times: " + i);
		}

	}

}
