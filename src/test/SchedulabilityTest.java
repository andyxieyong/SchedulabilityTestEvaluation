package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import analysis.FIFONPLinearJava;
import analysis.FIFOLinearC;
import analysis.MSRPRTA;
import analysis.NewMrsPRTA;
import analysis.NewMrsPRTAWithMigrationCostAsIndividual;
import analysis.OriginalMrsPRTA;
import analysis.RTAWithoutBlocking;
import entity.Resource;
import entity.SporadicTask;
import generatorTools.SystemGenerator;

public class SchedulabilityTest {

	/* define how long the critical section can be */
	public static enum CS_LENGTH_RANGE {
		VERY_LONG_CSLEN, LONG_CSLEN, MEDIUM_CS_LEN, SHORT_CS_LEN, VERY_SHORT_CS_LEN
	};

	/* define how many resources in the system */
	public static enum RESOURCES_RANGE {
		HALF_PARITIONS, /* partitions / 2 us */
		PARTITIONS, /* partitions us */
		DOUBLE_PARTITIONS, /* partitions * 2 us */
	};

	public static int TOTAL_NUMBER_OF_SYSTEMS = 100;
	public static int TOTAL_PARTITIONS = 16;
	public static int MIN_PERIOD = 1;
	public static int MAX_PERIOD = 1000;

	public static void main(String[] args) throws InterruptedException {
		int experiment = 0;
		int bigSet = 0;
		int smallSet = 0;

		if (args.length == 3) {
			experiment = Integer.parseInt(args[0]);
			bigSet = Integer.parseInt(args[1]);
			smallSet = Integer.parseInt(args[2]);

			switch (experiment) {
			case 1:
				experimentIncreasingWorkLoad(bigSet, smallSet);
				break;
			case 2:
				experimentIncreasingCriticalSectionLength(bigSet, smallSet);
				break;
			case 3:
				experimentIncreasingContention(bigSet, smallSet);
				break;
			default:
				break;
			}

		} else
			System.err.println("wrong parameter.");

	}

	public static void experimentIncreasingWorkLoad(int bigSet, int smallSet) {
		int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 2 + (2 * (bigSet - 1));
		double RESOURCE_SHARING_FACTOR = 0.2 + 0.2 * (double) (bigSet - 1);

		int NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION = smallSet;

		long[][] Ris;
		NewMrsPRTA new_mrsp = new NewMrsPRTA();
		OriginalMrsPRTA original_mrsp = new OriginalMrsPRTA();
		MSRPRTA msrp = new MSRPRTA();
		RTAWithoutBlocking noblocking = new RTAWithoutBlocking();
		FIFOLinearC fp = new FIFOLinearC();
		FIFONPLinearJava fnp = new FIFONPLinearJava();
		NewMrsPRTAWithMigrationCostAsIndividual new_mrsp_mig = new NewMrsPRTAWithMigrationCostAsIndividual();

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, 0.1 * (double) NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION,
				TOTAL_PARTITIONS, NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION, true, CS_LENGTH_RANGE.VERY_SHORT_CS_LEN, RESOURCES_RANGE.PARTITIONS,
				RESOURCE_SHARING_FACTOR, NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);

		String result = "";
		int mrsp_mig_1 = 0;
		int mrsp_mig_10 = 0;
		int mrsp_mig_50 = 0;
		int mrsp_mig_100 = 0;
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

				Ris = new_mrsp_mig.NewMrsPRTATest(tasks, resources, 100, false);
				if (isSystemSchedulable(tasks, Ris)) {
					mrsp_mig_100++;
				}
				Ris = new_mrsp_mig.NewMrsPRTATest(tasks, resources, 50, false);
				if (isSystemSchedulable(tasks, Ris)) {
					mrsp_mig_50++;
				}
				Ris = new_mrsp_mig.NewMrsPRTATest(tasks, resources, 10, false);
				if (isSystemSchedulable(tasks, Ris)) {
					mrsp_mig_10++;
				}
				Ris = new_mrsp_mig.NewMrsPRTATest(tasks, resources, 1, false);
				if (isSystemSchedulable(tasks, Ris)) {
					mrsp_mig_1++;
				}
			}

			System.out.println(1 + "" + bigSet + " " + smallSet + " times: " + i);

		}

		result += "number of tasks: " + smallSet + " ; System number: " + TOTAL_NUMBER_OF_SYSTEMS + " ; New MrsP: "
				+ (double) schedulableSystem_New_MrsP_Analysis2 / (double) TOTAL_NUMBER_OF_SYSTEMS + "  Original MrsP: "
				+ (double) schedulableSystem_Original_MrsP_Analysis / (double) TOTAL_NUMBER_OF_SYSTEMS + "  MSRP: "
				+ (double) schedulableSystem_MSRP_Analysis / (double) TOTAL_NUMBER_OF_SYSTEMS + " No Blocking: "
				+ (double) schedulableSystem_No_Blocking / (double) TOTAL_NUMBER_OF_SYSTEMS + " fifo np lp: "
				+ (double) sfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " fifo p lp: " + (double) sfp / (double) TOTAL_NUMBER_OF_SYSTEMS
				+ " MrsP-Mig:1: " + (double) mrsp_mig_1 / (double) TOTAL_NUMBER_OF_SYSTEMS + " MrsP-Mig:10: "
				+ (double) mrsp_mig_10 / (double) TOTAL_NUMBER_OF_SYSTEMS + " MrsP-Mig:50: " + (double) mrsp_mig_50 / (double) TOTAL_NUMBER_OF_SYSTEMS
				+ " MrsP-Mig:100: " + (double) mrsp_mig_100 / (double) TOTAL_NUMBER_OF_SYSTEMS + "\n";

		writeSystem((1 + " " + bigSet + " " + smallSet), result);
	}

	public static void experimentIncreasingCriticalSectionLength(int tasksNumConfig, int csLenConfig) {
		double RESOURCE_SHARING_FACTOR = 0.3;
		int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 3;
		int NUMBER_OF_TASKS_ON_EACH_PARTITION = 3 + 2 * (tasksNumConfig - 1);

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
		default:
			break;
		}

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, 0.1 * (double) NUMBER_OF_TASKS_ON_EACH_PARTITION, TOTAL_PARTITIONS,
				NUMBER_OF_TASKS_ON_EACH_PARTITION, true, range, RESOURCES_RANGE.HALF_PARITIONS, RESOURCE_SHARING_FACTOR,
				NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);
		long[][] Ris;

		NewMrsPRTA new_mrsp = new NewMrsPRTA();
		OriginalMrsPRTA original_mrsp = new OriginalMrsPRTA();
		MSRPRTA msrp = new MSRPRTA();
		RTAWithoutBlocking noblocking = new RTAWithoutBlocking();
		FIFOLinearC fp = new FIFOLinearC();
		FIFONPLinearJava fnp = new FIFONPLinearJava();
		NewMrsPRTAWithMigrationCostAsIndividual new_mrsp_mig = new NewMrsPRTAWithMigrationCostAsIndividual();

		String result = "";

		int mrsp_mig_1 = 0;
		int mrsp_mig_10 = 0;
		int mrsp_mig_50 = 0;
		int mrsp_mig_100 = 0;
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

				Ris = new_mrsp_mig.NewMrsPRTATest(tasks, resources, 100, false);
				if (isSystemSchedulable(tasks, Ris)) {
					mrsp_mig_100++;
				}
				Ris = new_mrsp_mig.NewMrsPRTATest(tasks, resources, 50, false);
				if (isSystemSchedulable(tasks, Ris)) {
					mrsp_mig_50++;
				}
				Ris = new_mrsp_mig.NewMrsPRTATest(tasks, resources, 10, false);
				if (isSystemSchedulable(tasks, Ris)) {
					mrsp_mig_10++;
				}
				Ris = new_mrsp_mig.NewMrsPRTATest(tasks, resources, 1, false);
				if (isSystemSchedulable(tasks, Ris)) {
					mrsp_mig_1++;
				}
			}

			System.out.println(2 + "" + tasksNumConfig + " " + csLenConfig + " times: " + i);
		}

		result += "cs _len: " + range.toString() + " ; New MrsP: " + (double) schedulableSystem_New_MrsP_Analysis2 / (double) TOTAL_NUMBER_OF_SYSTEMS
				+ "  Original MrsP: " + (double) schedulableSystem_Original_MrsP_Analysis / (double) TOTAL_NUMBER_OF_SYSTEMS + "  MSRP: "
				+ (double) schedulableSystem_MSRP_Analysis / (double) TOTAL_NUMBER_OF_SYSTEMS + " NO BLOCKING: "
				+ (double) schedulableSystem_No_Blocking / (double) TOTAL_NUMBER_OF_SYSTEMS + " fifo np lp: "
				+ (double) sfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " fifo p lp: " + (double) sfp / (double) TOTAL_NUMBER_OF_SYSTEMS
				+ " MrsP-Mig:1: " + (double) mrsp_mig_1 / (double) TOTAL_NUMBER_OF_SYSTEMS + " MrsP-Mig:10: "
				+ (double) mrsp_mig_10 / (double) TOTAL_NUMBER_OF_SYSTEMS + " MrsP-Mig:50: " + (double) mrsp_mig_50 / (double) TOTAL_NUMBER_OF_SYSTEMS
				+ " MrsP-Mig:100: " + (double) mrsp_mig_100 / (double) TOTAL_NUMBER_OF_SYSTEMS + "\n";

		writeSystem((2 + " " + tasksNumConfig + " " + csLenConfig), result);
	}

	public static void experimentIncreasingContention(int bigSet, int smallSet) {
		double RESOURCE_SHARING_FACTOR = 0.25;
		int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 1 + 5 * (smallSet - 1);
		int NUMBER_OF_TASKS_ON_EACH_PARTITION = 3 + 2 * (bigSet - 1);

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, 0.1 * (double) NUMBER_OF_TASKS_ON_EACH_PARTITION, TOTAL_PARTITIONS,
				NUMBER_OF_TASKS_ON_EACH_PARTITION, true, CS_LENGTH_RANGE.VERY_SHORT_CS_LEN, RESOURCES_RANGE.HALF_PARITIONS, RESOURCE_SHARING_FACTOR,
				NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);
		long[][] Ris;

		NewMrsPRTA new_mrsp = new NewMrsPRTA();
		OriginalMrsPRTA original_mrsp = new OriginalMrsPRTA();
		MSRPRTA msrp = new MSRPRTA();
		RTAWithoutBlocking noblocking = new RTAWithoutBlocking();
		FIFOLinearC fp = new FIFOLinearC();
		FIFONPLinearJava fnp = new FIFONPLinearJava();
		NewMrsPRTAWithMigrationCostAsIndividual new_mrsp_mig = new NewMrsPRTAWithMigrationCostAsIndividual();

		String result = "";

		int mrsp_mig_1 = 0;
		int mrsp_mig_10 = 0;
		int mrsp_mig_50 = 0;
		int mrsp_mig_100 = 0;
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

				Ris = new_mrsp_mig.NewMrsPRTATest(tasks, resources, 100, false);
				if (isSystemSchedulable(tasks, Ris)) {
					mrsp_mig_100++;
				}
				Ris = new_mrsp_mig.NewMrsPRTATest(tasks, resources, 50, false);
				if (isSystemSchedulable(tasks, Ris)) {
					mrsp_mig_50++;
				}
				Ris = new_mrsp_mig.NewMrsPRTATest(tasks, resources, 10, false);
				if (isSystemSchedulable(tasks, Ris)) {
					mrsp_mig_10++;
				}
				Ris = new_mrsp_mig.NewMrsPRTATest(tasks, resources, 1, false);
				if (isSystemSchedulable(tasks, Ris)) {
					mrsp_mig_1++;
				}
			}

			System.out.println(3 + "" + bigSet + " " + smallSet + " times: " + i);
		}

		result += "number of access: " + NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE + " ; New MrsP: "
				+ (double) schedulableSystem_New_MrsP_Analysis2 / (double) TOTAL_NUMBER_OF_SYSTEMS + "  Original MrsP: "
				+ (double) schedulableSystem_Original_MrsP_Analysis / (double) TOTAL_NUMBER_OF_SYSTEMS + "  MSRP: "
				+ (double) schedulableSystem_MSRP_Analysis / (double) TOTAL_NUMBER_OF_SYSTEMS + " NO BLOCKING: "
				+ (double) schedulableSystem_No_Blocking / (double) TOTAL_NUMBER_OF_SYSTEMS + " fifo np lp: "
				+ (double) sfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " fifo p lp: " + (double) sfp / (double) TOTAL_NUMBER_OF_SYSTEMS
				+ " MrsP-Mig:1: " + (double) mrsp_mig_1 / (double) TOTAL_NUMBER_OF_SYSTEMS + " MrsP-Mig:10: "
				+ (double) mrsp_mig_10 / (double) TOTAL_NUMBER_OF_SYSTEMS + " MrsP-Mig:50: " + (double) mrsp_mig_50 / (double) TOTAL_NUMBER_OF_SYSTEMS
				+ " MrsP-Mig:100: " + (double) mrsp_mig_100 / (double) TOTAL_NUMBER_OF_SYSTEMS + "\n";

		writeSystem((3 + " " + bigSet + " " + smallSet), result);
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

	public static void writeSystem(String filename, String result) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter(new File("result/" + filename + ".txt"), false));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		writer.println(result);
		writer.close();
	}
}
