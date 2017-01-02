package test;

import java.util.ArrayList;

import analysis.NewMrsPRTA;
import analysis.NewMrsPRTAWithMigrationCostAsIndividual;
import entity.Resource;
import entity.SporadicTask;
import generatorTools.SystemGenerator;

public class MigCostTest {

	public static int TOTAL_NUMBER_OF_SYSTEMS = 99999999;
	public static int TOTAL_PARTITIONS = 8;
	public static int MIN_PERIOD = 1;
	public static int MAX_PERIOD = 1000;
	public static int NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION = 16;
	public static int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 5;
	public static double RESOURCE_SHARING_FACTOR = .5;

	public static void main(String[] args) {
		
		NewMrsPRTA mrsp = new NewMrsPRTA();
		NewMrsPRTAWithMigrationCostAsIndividual s_mrsp = new NewMrsPRTAWithMigrationCostAsIndividual();
//		long[][] r1, r2;

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, 0.1 * (double) NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION,
				TOTAL_PARTITIONS, NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION, true, SchedulabilityTest.CS_LENGTH_RANGE.MEDIUM_CS_LEN,
				SchedulabilityTest.RESOURCES_RANGE.PARTITIONS, RESOURCE_SHARING_FACTOR, NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);

		for (int i = 0; i < 1000000; i++) {
			ArrayList<ArrayList<SporadicTask>> tasks = generator.generateTasks();
			ArrayList<Resource> resources = generator.generateResources();
			generator.generateResourceUsage(tasks, resources);
//			generator.testifyGeneratedTasksetAndResource(tasks, resources);
			System.out.println(i);
			
			
			s_mrsp.NewMrsPRTATest(tasks, resources,1,false);
			s_mrsp.NewMrsPRTATest(tasks, resources,10,false);
			s_mrsp.NewMrsPRTATest(tasks, resources,50,false);
			s_mrsp.NewMrsPRTATest(tasks, resources,100,false);
			
			
			
			mrsp.NewMrsPRTATest(tasks, resources, false);
		}

	}

	public static boolean isEqual(long[][] r1, long[][] r2) {
		for (int i = 0; i < r1.length; i++) {
			for (int j = 0; j < r1[i].length; j++) {
				if (r1[i][j] != r2[i][j]) {
					System.out.println("not equal at:  i=" + i + "  j=" + j + "   r1: " + r1[i][j] + "   r2:" + r2[i][j]);

					return false;
				}
			}
		}
		return true;
	}

}
