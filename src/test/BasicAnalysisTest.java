package test;

import java.util.ArrayList;

import entity.Resource;
import entity.SporadicTask;
import generatorTools.SystemGenerator;
import generatorTools.SystemGenerator.CS_LENGTH_RANGE;
import generatorTools.SystemGenerator.RESOURCES_RANGE;
import implementationAwareAnalysis.IAFIFONP;
import implementationAwareAnalysis.IAFIFOP;
import implementationAwareAnalysis.IANewMrsPRTAWithMCNP;
import implementationAwareAnalysis.IASUtils;

public class BasicAnalysisTest {

	public static int TOTAL_NUMBER_OF_SYSTEMS = 99999999;
	public static int TOTAL_PARTITIONS = 16;
	public static int MIN_PERIOD = 1;
	public static int MAX_PERIOD = 1000;
	public static int NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION = 5;
	public static int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 2;
	public static double RESOURCE_SHARING_FACTOR = .3;

	public static void main(String[] args) {
		IAFIFOP fp = new IAFIFOP();
		IAFIFONP fnp = new IAFIFONP();
		IANewMrsPRTAWithMCNP mrsp = new IANewMrsPRTAWithMCNP();

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD,
				0.1 * (double) NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION, TOTAL_PARTITIONS,
				NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION, true, CS_LENGTH_RANGE.VERY_SHORT_CS_LEN,
				RESOURCES_RANGE.PARTITIONS, RESOURCE_SHARING_FACTOR, NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);

		ArrayList<ArrayList<SporadicTask>> tasks = generator.generateTasks();
		ArrayList<Resource> resources = generator.generateResources();
		generator.generateResourceUsage(tasks, resources);

		System.out.println(IASUtils.MrsP_PREEMPTION_AND_MIGRATION);

		int i = 0;
		while (i <= TOTAL_NUMBER_OF_SYSTEMS) {
			fp.NewMrsPRTATest(tasks, resources, false);
			fnp.NewMrsPRTATest(tasks, resources, false);
			mrsp.NewMrsPRTATest(tasks, resources, 5, false);
			i++;
			System.out.println(i);
		}
	}

}
