package test;

import java.util.ArrayList;

import ILPBasedAnalysis.FIFOLinearC;
import basicAnalysis.MSRPRTA;
import basicAnalysis.OriginalMrsPRTA;
import basicAnalysis.RTAWithoutBlocking;
import entity.Resource;
import entity.SporadicTask;
import generatorTools.SystemGenerator;
import generatorTools.SystemGenerator.CS_LENGTH_RANGE;
import generatorTools.SystemGenerator.RESOURCES_RANGE;
import newAnalysis.FIFONP;
import newAnalysis.FIFOP;
import newAnalysis.NewMrsPRTA;
import newAnalysis.NewMrsPRTAWithMCNP;

public class MoreSchedulabilityTest {

	public static int TOTAL_NUMBER_OF_SYSTEMS = 1000;
	public static int TOTAL_PARTITIONS = 16;
	public static int MIN_PERIOD = 1;
	public static int MAX_PERIOD = 1000;

	public static void main(String[] args) throws InterruptedException {

		for (int i = 6; i < 7; i++)
			experimentIncreasingCriticalSectionLength(i);

	}

	public static void experimentIncreasingWorkLoad(int smallSet) {
		int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 2;
		double RESOURCE_SHARING_FACTOR = 0.3;

		int NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION = smallSet;

		long[][] Ris;
		NewMrsPRTAWithMCNP new_mrsp = new NewMrsPRTAWithMCNP();
		OriginalMrsPRTA original_mrsp = new OriginalMrsPRTA();
		MSRPRTA msrp = new MSRPRTA();
		RTAWithoutBlocking noblocking = new RTAWithoutBlocking();
		FIFOP fp = new FIFOP();
		FIFONP fnp = new FIFONP();

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, 0.1 * (double) NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION,
				TOTAL_PARTITIONS, NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION, true, CS_LENGTH_RANGE.MEDIUM_CS_LEN, RESOURCES_RANGE.PARTITIONS,
				RESOURCE_SHARING_FACTOR, NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);

		String result = "";
		int schedulableSystem_New_MrsP_Analysis2 = 0;
		int schedulableSystem_Original_MrsP_Analysis = 0;
		int schedulableSystem_MSRP_Analysis = 0;
		int schedulableSystem_No_Blocking = 0;
		int sfnp = 0;
		int sfp = 0;

		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
			ArrayList<ArrayList<SporadicTask>> tasks = generator.generateTasks();
			ArrayList<Resource> resources = generator.generateResources();
			generator.generateResourceUsage(tasks, resources);

			Ris = noblocking.NewMrsPRTATest(tasks, resources, false);
			if (isSystemSchedulable(tasks, Ris)) {
				schedulableSystem_No_Blocking++;

				Ris = original_mrsp.NewMrsPRTATest(tasks, resources, false);
				if (isSystemSchedulable(tasks, Ris)) {
					schedulableSystem_Original_MrsP_Analysis++;
					schedulableSystem_New_MrsP_Analysis2++;
				} else {
					Ris = new_mrsp.NewMrsPRTATest(tasks, resources, 6, 100, false);
					if (isSystemSchedulable(tasks, Ris))
						schedulableSystem_New_MrsP_Analysis2++;
				}

				Ris = msrp.NewMrsPRTATest(tasks, resources, false);
				if (isSystemSchedulable(tasks, Ris)) {
					schedulableSystem_MSRP_Analysis++;
					sfnp++;
				} else {
					Ris = fnp.NewMrsPRTATest(tasks, resources, false);
					if (isSystemSchedulable(tasks, Ris))
						sfnp++;
				}

				Ris = fp.NewMrsPRTATest(tasks, resources, false);
				if (isSystemSchedulable(tasks, Ris))
					sfp++;

			}

		}

		result += "number of tasks: " + " ; System number: " + TOTAL_NUMBER_OF_SYSTEMS + " ; New MrsP: "
				+ (double) schedulableSystem_New_MrsP_Analysis2 / (double) TOTAL_NUMBER_OF_SYSTEMS + "  Original MrsP: "
				+ (double) schedulableSystem_Original_MrsP_Analysis / (double) TOTAL_NUMBER_OF_SYSTEMS + "  MSRP: "
				+ (double) schedulableSystem_MSRP_Analysis / (double) TOTAL_NUMBER_OF_SYSTEMS + " No Blocking: "
				+ (double) schedulableSystem_No_Blocking / (double) TOTAL_NUMBER_OF_SYSTEMS + " fifo np lp: "
				+ (double) sfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " fifo p lp: " + (double) sfp / (double) TOTAL_NUMBER_OF_SYSTEMS + "\n";

		System.out.print(result);
	}

	public static void experimentIncreasingCriticalSectionLength(int csLenConfig) {
		double RESOURCE_SHARING_FACTOR = 0.4;
		int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 3;
		int NUMBER_OF_TASKS_ON_EACH_PARTITION = 4;

		CS_LENGTH_RANGE range = null;
		switch (csLenConfig) {
		case 1:
			range = CS_LENGTH_RANGE.VERY_SHORT_CS_LEN;
			break;
		case 2:
			range = CS_LENGTH_RANGE.SHORT_CS_LEN;
			break;
		case 3:
			range = CS_LENGTH_RANGE.MEDIUM_CS_LEN;
			break;
		case 4:
			range = CS_LENGTH_RANGE.LONG_CSLEN;
			break;
		case 5:
			range = CS_LENGTH_RANGE.VERY_LONG_CSLEN;
			break;
		case 6:
			range = CS_LENGTH_RANGE.RANDOM;
			break;
		default:
			break;
		}

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, 0.1 * (double) NUMBER_OF_TASKS_ON_EACH_PARTITION, TOTAL_PARTITIONS,
				NUMBER_OF_TASKS_ON_EACH_PARTITION, true, range, RESOURCES_RANGE.PARTITIONS, RESOURCE_SHARING_FACTOR,
				NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);
		long[][] Ris;

		NewMrsPRTA new_mrsp = new NewMrsPRTA();
		OriginalMrsPRTA original_mrsp = new OriginalMrsPRTA();
		MSRPRTA msrp = new MSRPRTA();
		RTAWithoutBlocking noblocking = new RTAWithoutBlocking();
		FIFOLinearC fp = new FIFOLinearC();
		FIFONP fnp = new FIFONP();
		String result = "";

		int schedulableSystem_New_MrsP_Analysis2 = 0;
		int schedulableSystem_Original_MrsP_Analysis = 0;
		int schedulableSystem_MSRP_Analysis = 0;
		int schedulableSystem_No_Blocking = 0;
		int sfnp = 0;
		int sfp = 0;

		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
			ArrayList<ArrayList<SporadicTask>> tasks = generator.generateTasks();
			ArrayList<Resource> resources = generator.generateResources();
			generator.generateResourceUsage(tasks, resources);

			Ris = noblocking.NewMrsPRTATest(tasks, resources, false);
			if (isSystemSchedulable(tasks, Ris)) {
				schedulableSystem_No_Blocking++;

				Ris = original_mrsp.NewMrsPRTATest(tasks, resources, false);
				if (isSystemSchedulable(tasks, Ris)) {
					schedulableSystem_Original_MrsP_Analysis++;
					schedulableSystem_New_MrsP_Analysis2++;
				} else {
					Ris = new_mrsp.NewMrsPRTATest(tasks, resources, false);
					if (isSystemSchedulable(tasks, Ris))
						schedulableSystem_New_MrsP_Analysis2++;
				}

				Ris = msrp.NewMrsPRTATest(tasks, resources, false);
				if (isSystemSchedulable(tasks, Ris)) {
					schedulableSystem_MSRP_Analysis++;
					sfnp++;
				} else {
					Ris = fnp.NewMrsPRTATest(tasks, resources, false);
					if (isSystemSchedulable(tasks, Ris))
						sfnp++;
				}

				Ris = fp.NewMrsPRTATest(tasks, resources, true, false);
				if (isSystemSchedulable(tasks, Ris))
					sfp++;
			}

			System.out.println(2 + "" + 1 + " " + csLenConfig + " times: " + i);
		}

		result += "cs _len: " + range.toString() + " ; New MrsP: " + (double) schedulableSystem_New_MrsP_Analysis2 / (double) TOTAL_NUMBER_OF_SYSTEMS
				+ "  Original MrsP: " + (double) schedulableSystem_Original_MrsP_Analysis / (double) TOTAL_NUMBER_OF_SYSTEMS + "  MSRP: "
				+ (double) schedulableSystem_MSRP_Analysis / (double) TOTAL_NUMBER_OF_SYSTEMS + " NO BLOCKING: "
				+ (double) schedulableSystem_No_Blocking / (double) TOTAL_NUMBER_OF_SYSTEMS + " fifo np lp: "
				+ (double) sfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " fifo p lp: " + (double) sfp / (double) TOTAL_NUMBER_OF_SYSTEMS + "\n";
		System.out.println(result);
	}

	public static void experimentIncreasingContention(int bigSet, int smallSet) {
		double RESOURCE_SHARING_FACTOR = 0.4;
		int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 1 + 5 * (smallSet - 1);
		int NUMBER_OF_TASKS_ON_EACH_PARTITION = 5;

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, 0.1 * (double) NUMBER_OF_TASKS_ON_EACH_PARTITION, TOTAL_PARTITIONS,
				NUMBER_OF_TASKS_ON_EACH_PARTITION, true, CS_LENGTH_RANGE.VERY_SHORT_CS_LEN, RESOURCES_RANGE.PARTITIONS, RESOURCE_SHARING_FACTOR,
				NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);
		long[][] Ris;

		NewMrsPRTA new_mrsp = new NewMrsPRTA();
		OriginalMrsPRTA original_mrsp = new OriginalMrsPRTA();
		MSRPRTA msrp = new MSRPRTA();
		RTAWithoutBlocking noblocking = new RTAWithoutBlocking();
		FIFOLinearC fp = new FIFOLinearC();
		FIFONP fnp = new FIFONP();

		String result = "";

		int schedulableSystem_New_MrsP_Analysis2 = 0;
		int schedulableSystem_Original_MrsP_Analysis = 0;
		int schedulableSystem_MSRP_Analysis = 0;
		int schedulableSystem_No_Blocking = 0;
		int sfnp = 0;
		int sfp = 0;

		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {

			ArrayList<ArrayList<SporadicTask>> tasks = generator.generateTasks();
			ArrayList<Resource> resources = generator.generateResources();
			generator.generateResourceUsage(tasks, resources);

			Ris = noblocking.NewMrsPRTATest(tasks, resources, false);
			if (isSystemSchedulable(tasks, Ris)) {
				schedulableSystem_No_Blocking++;

				Ris = original_mrsp.NewMrsPRTATest(tasks, resources, false);
				if (isSystemSchedulable(tasks, Ris)) {
					schedulableSystem_Original_MrsP_Analysis++;
					schedulableSystem_New_MrsP_Analysis2++;
				} else {
					Ris = new_mrsp.NewMrsPRTATest(tasks, resources, false);
					if (isSystemSchedulable(tasks, Ris))
						schedulableSystem_New_MrsP_Analysis2++;
				}

				Ris = msrp.NewMrsPRTATest(tasks, resources, false);
				if (isSystemSchedulable(tasks, Ris)) {
					schedulableSystem_MSRP_Analysis++;
					sfnp++;
				} else {
					Ris = fnp.NewMrsPRTATest(tasks, resources, false);
					if (isSystemSchedulable(tasks, Ris))
						sfnp++;
				}

				Ris = fp.NewMrsPRTATest(tasks, resources, true, false);
				if (isSystemSchedulable(tasks, Ris))
					sfp++;
			}

			// System.out.println(3 + "" + bigSet + " " + smallSet + " times: "
			// + i);
		}

		result += "number of access: " + NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE + " ; New MrsP: "
				+ (double) schedulableSystem_New_MrsP_Analysis2 / (double) TOTAL_NUMBER_OF_SYSTEMS + "  Original MrsP: "
				+ (double) schedulableSystem_Original_MrsP_Analysis / (double) TOTAL_NUMBER_OF_SYSTEMS + "  MSRP: "
				+ (double) schedulableSystem_MSRP_Analysis / (double) TOTAL_NUMBER_OF_SYSTEMS + " NO BLOCKING: "
				+ (double) schedulableSystem_No_Blocking / (double) TOTAL_NUMBER_OF_SYSTEMS + " fifo np lp: "
				+ (double) sfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " fifo p lp: " + (double) sfp / (double) TOTAL_NUMBER_OF_SYSTEMS + "\n";
		System.out.println(result);
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
