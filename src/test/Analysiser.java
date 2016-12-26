package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import analysis.FIFONonPreemptiveLinearC;
import analysis.MSRPRTA;
import analysis.NewMrsPRTA;
import analysis.OriginalMrsPRTA;
import analysis.RTAWithoutBlocking;
import entity.Resource;
import entity.SporadicTask;
import generatorTools.SystemGenerator;

public class Analysiser {

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

	public static int TOTAL_NUMBER_OF_SYSTEMS = 1000;
	public static int TOTAL_PARTITIONS = 16;
	public static int MIN_PERIOD = 1;
	public static int MAX_PERIOD = 1000;

	public static void main(String[] args) throws InterruptedException {
		int experiment = 0;
		int bigSet = 0;
		int smallSet = 0;

		if (args.length == 0) {
			experimentIncreasingWorkLoad();
			experimentIncreasingCriticalSectionLength();
			experimentIncreasingContention();
		}

		if (args.length == 1) {
			if (args[0].equals("1"))
				experimentIncreasingWorkLoad();
			if (args[0].equals("2"))
				experimentIncreasingCriticalSectionLength();
			if (args[0].equals("3"))
				experimentIncreasingContention();
		}

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

		}

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
		FIFONonPreemptiveLinearC fnp = new FIFONonPreemptiveLinearC();

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, 0.1 * (double) NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION,
				TOTAL_PARTITIONS, NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION, true, CS_LENGTH_RANGE.VERY_SHORT_CS_LEN, RESOURCES_RANGE.PARTITIONS,
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
					Ris = new_mrsp.NewMrsPRTATest(tasks, resources, false);
					if (isSystemSchedulable(tasks, Ris))
						schedulableSystem_New_MrsP_Analysis2++;
				}

				Ris = msrp.NewMrsPRTATest(tasks, resources, false);
				if (isSystemSchedulable(tasks, Ris)) {
					schedulableSystem_MSRP_Analysis++;
					sfnp++;
				} else {
					Ris = fnp.NewMrsPRTATest(tasks, resources, false, false);
					if (isSystemSchedulable(tasks, Ris))
						sfnp++;
				}

				Ris = fnp.NewMrsPRTATest(tasks, resources, true, false);
				if (isSystemSchedulable(tasks, Ris))
					sfp++;
			}

			System.out.println(1 + "" + bigSet + " " + smallSet + " times: " + i);

		}

		result += "number of tasks: " + " ; System number: " + TOTAL_NUMBER_OF_SYSTEMS + " ; New MrsP: "
				+ (double) schedulableSystem_New_MrsP_Analysis2 / (double) TOTAL_NUMBER_OF_SYSTEMS + "  Original MrsP: "
				+ (double) schedulableSystem_Original_MrsP_Analysis / (double) TOTAL_NUMBER_OF_SYSTEMS + "  MSRP: "
				+ (double) schedulableSystem_MSRP_Analysis / (double) TOTAL_NUMBER_OF_SYSTEMS + " No Blocking: "
				+ (double) schedulableSystem_No_Blocking / (double) TOTAL_NUMBER_OF_SYSTEMS + " fifo np lp: "
				+ (double) sfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " fifo p lp: " + (double) sfp / (double) TOTAL_NUMBER_OF_SYSTEMS + "\n";

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
		FIFONonPreemptiveLinearC fnp = new FIFONonPreemptiveLinearC();
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
					Ris = fnp.NewMrsPRTATest(tasks, resources, false, false);
					if (isSystemSchedulable(tasks, Ris))
						sfnp++;
				}

				Ris = fnp.NewMrsPRTATest(tasks, resources, true, false);
				if (isSystemSchedulable(tasks, Ris))
					sfp++;
			}

			System.out.println(2 + "" + tasksNumConfig + " " + csLenConfig + " times: " + i);
		}

		result += "cs _len: " + range.toString() + " ; New MrsP: " + (double) schedulableSystem_New_MrsP_Analysis2 / (double) TOTAL_NUMBER_OF_SYSTEMS
				+ "  Original MrsP: " + (double) schedulableSystem_Original_MrsP_Analysis / (double) TOTAL_NUMBER_OF_SYSTEMS + "  MSRP: "
				+ (double) schedulableSystem_MSRP_Analysis / (double) TOTAL_NUMBER_OF_SYSTEMS + " NO BLOCKING: "
				+ (double) schedulableSystem_No_Blocking / (double) TOTAL_NUMBER_OF_SYSTEMS + " fifo np lp: "
				+ (double) sfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " fifo p lp: " + (double) sfp / (double) TOTAL_NUMBER_OF_SYSTEMS + "\n";

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
		FIFONonPreemptiveLinearC fnp = new FIFONonPreemptiveLinearC();

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
					Ris = fnp.NewMrsPRTATest(tasks, resources, false, false);
					if (isSystemSchedulable(tasks, Ris))
						sfnp++;
				}

				Ris = fnp.NewMrsPRTATest(tasks, resources, true, false);
				if (isSystemSchedulable(tasks, Ris))
					sfp++;
			}

			System.out.println(3 + "" + bigSet + " " + smallSet + " times: " + i);
		}

		result += "number of access: " + NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE + " ; New MrsP: "
				+ (double) schedulableSystem_New_MrsP_Analysis2 / (double) TOTAL_NUMBER_OF_SYSTEMS + "  Original MrsP: "
				+ (double) schedulableSystem_Original_MrsP_Analysis / (double) TOTAL_NUMBER_OF_SYSTEMS + "  MSRP: "
				+ (double) schedulableSystem_MSRP_Analysis / (double) TOTAL_NUMBER_OF_SYSTEMS + " NO BLOCKING: "
				+ (double) schedulableSystem_No_Blocking / (double) TOTAL_NUMBER_OF_SYSTEMS + " fifo np lp: "
				+ (double) sfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " fifo p lp: " + (double) sfp / (double) TOTAL_NUMBER_OF_SYSTEMS + "\n";

		writeSystem((3 + " " + bigSet + " " + smallSet), result);
	}

	public static void experimentIncreasingWorkLoad() {
		int NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION = 9;
		int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 2;
		double RESOURCE_SHARING_FACTOR = .2;

		long[][] Ris;
		NewMrsPRTA new_mrsp = new NewMrsPRTA();
		OriginalMrsPRTA original_mrsp = new OriginalMrsPRTA();
		MSRPRTA msrp = new MSRPRTA();
		RTAWithoutBlocking noblocking = new RTAWithoutBlocking();
		FIFONonPreemptiveLinearC fnp = new FIFONonPreemptiveLinearC();

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, 0.1 * (double) 1, TOTAL_PARTITIONS, 1, true,
				CS_LENGTH_RANGE.VERY_SHORT_CS_LEN, RESOURCES_RANGE.PARTITIONS, RESOURCE_SHARING_FACTOR, NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);

		String result = "";
		System.out.println("experimentIncreasingWorkLoad");
		result += "experiment Increasing WorkLoad \n";

		for (int k = 0; k < 3; k++) {
			System.out.println("max access: " + NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE + " rsf: " + RESOURCE_SHARING_FACTOR);
			result += "max access: " + NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE + " rsf: " + RESOURCE_SHARING_FACTOR + "\n";

			generator.rsf = RESOURCE_SHARING_FACTOR;
			generator.number_of_max_access = NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE;

			for (int j = 1; j <= NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION; j++) {
				int schedulableSystem_New_MrsP_Analysis2 = 0;
				int schedulableSystem_Original_MrsP_Analysis = 0;
				int schedulableSystem_MSRP_Analysis = 0;
				int schedulableSystem_No_Blocking = 0;
				int sfnp = 0;
				int sfp = 0;

				generator.number_of_tasks_per_processor = j;
				generator.util = 0.1 * (double) j;
				for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
					ArrayList<ArrayList<SporadicTask>> tasks = generator.generateTasks();
					ArrayList<Resource> resources = generator.generateResources();
					generator.generateResourceUsage(tasks, resources);

					Ris = noblocking.NewMrsPRTATest(tasks, resources, false);
					if (isSystemSchedulable(tasks, Ris)) {
						schedulableSystem_No_Blocking++;
					}

					Ris = original_mrsp.NewMrsPRTATest(tasks, resources, false);
					if (isSystemSchedulable(tasks, Ris))
						schedulableSystem_Original_MrsP_Analysis++;

					Ris = msrp.NewMrsPRTATest(tasks, resources, false);
					if (isSystemSchedulable(tasks, Ris)) {
						schedulableSystem_MSRP_Analysis++;
						sfnp++;
						sfp++;
						schedulableSystem_New_MrsP_Analysis2++;
					} else {
						Ris = new_mrsp.NewMrsPRTATest(tasks, resources, false);
						if (isSystemSchedulable(tasks, Ris)) {
							schedulableSystem_New_MrsP_Analysis2++;

							Ris = fnp.NewMrsPRTATest(tasks, resources, true, false);
							if (isSystemSchedulable(tasks, Ris)) {
								sfp++;
								sfnp++;
							} else {
								Ris = fnp.NewMrsPRTATest(tasks, resources, false, false);
								if (isSystemSchedulable(tasks, Ris))
									sfnp++;
							}

						}
					}

					// System.out.println(i);
				}

				System.err.println("number of tasks: " + TOTAL_PARTITIONS * j + " ; System number: " + TOTAL_NUMBER_OF_SYSTEMS + " ; New MrsP: "
						+ (double) schedulableSystem_New_MrsP_Analysis2 / (double) TOTAL_NUMBER_OF_SYSTEMS + "  Original MrsP: "
						+ (double) schedulableSystem_Original_MrsP_Analysis / (double) TOTAL_NUMBER_OF_SYSTEMS + "  MSRP: "
						+ (double) schedulableSystem_MSRP_Analysis / (double) TOTAL_NUMBER_OF_SYSTEMS + " No Blocking: "
						+ (double) schedulableSystem_No_Blocking / (double) TOTAL_NUMBER_OF_SYSTEMS + " fifo np lp: "
						+ (double) sfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " fifo p lp: " + (double) sfp / (double) TOTAL_NUMBER_OF_SYSTEMS);
				result += "number of tasks: " + TOTAL_PARTITIONS * j + " ; System number: " + TOTAL_NUMBER_OF_SYSTEMS + " ; New MrsP: "
						+ (double) schedulableSystem_New_MrsP_Analysis2 / (double) TOTAL_NUMBER_OF_SYSTEMS + "  Original MrsP: "
						+ (double) schedulableSystem_Original_MrsP_Analysis / (double) TOTAL_NUMBER_OF_SYSTEMS + "  MSRP: "
						+ (double) schedulableSystem_MSRP_Analysis / (double) TOTAL_NUMBER_OF_SYSTEMS + " No Blocking: "
						+ (double) schedulableSystem_No_Blocking / (double) TOTAL_NUMBER_OF_SYSTEMS + " fifo np lp: "
						+ (double) sfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " fifo p lp: " + (double) sfp / (double) TOTAL_NUMBER_OF_SYSTEMS + "\n";

			}

			NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE += 3;
			RESOURCE_SHARING_FACTOR += 0.3;

		}

		writeSystem("" + 1, result);

	}

	public static void experimentIncreasingContention() {
		double RESOURCE_SHARING_FACTOR = 0.4;

		int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 50;
		int NUMBER_OF_TASKS_ON_EACH_PARTITION = 3;

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, 0.1 * (double) NUMBER_OF_TASKS_ON_EACH_PARTITION, TOTAL_PARTITIONS,
				NUMBER_OF_TASKS_ON_EACH_PARTITION, true, CS_LENGTH_RANGE.SHORT_CS_LEN, RESOURCES_RANGE.PARTITIONS, RESOURCE_SHARING_FACTOR,
				NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);
		long[][] Ris;

		NewMrsPRTA new_mrsp = new NewMrsPRTA();
		OriginalMrsPRTA original_mrsp = new OriginalMrsPRTA();
		MSRPRTA msrp = new MSRPRTA();
		RTAWithoutBlocking noblocking = new RTAWithoutBlocking();
		FIFONonPreemptiveLinearC fnp = new FIFONonPreemptiveLinearC();

		String result = "";

		System.out.println("experimentIncreasingContention");
		result += "experiment Increasing Contention \n";

		for (int k = 0; k < 3; k++) {
			System.out.println("number of tasks: " + NUMBER_OF_TASKS_ON_EACH_PARTITION * TOTAL_PARTITIONS);
			result += "number of tasks: " + NUMBER_OF_TASKS_ON_EACH_PARTITION * TOTAL_PARTITIONS + "\n";

			generator.number_of_tasks_per_processor = NUMBER_OF_TASKS_ON_EACH_PARTITION;
			generator.util = 0.1 * (double) NUMBER_OF_TASKS_ON_EACH_PARTITION;

			for (int j = 1; j <= NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE;) {
				int schedulableSystem_New_MrsP_Analysis2 = 0;
				int schedulableSystem_Original_MrsP_Analysis = 0;
				int schedulableSystem_MSRP_Analysis = 0;
				int schedulableSystem_No_Blocking = 0;
				int sfnp = 0;
				int sfp = 0;
				generator.number_of_max_access = j;

				for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {

					ArrayList<ArrayList<SporadicTask>> tasks = generator.generateTasks();
					ArrayList<Resource> resources = generator.generateResources();
					generator.generateResourceUsage(tasks, resources);

					Ris = noblocking.NewMrsPRTATest(tasks, resources, false);
					if (isSystemSchedulable(tasks, Ris)) {
						schedulableSystem_No_Blocking++;
					}

					Ris = original_mrsp.NewMrsPRTATest(tasks, resources, false);
					if (isSystemSchedulable(tasks, Ris))
						schedulableSystem_Original_MrsP_Analysis++;

					Ris = fnp.NewMrsPRTATest(tasks, resources, true, false);
					if (isSystemSchedulable(tasks, Ris))
						sfp++;

					Ris = msrp.NewMrsPRTATest(tasks, resources, false);
					if (isSystemSchedulable(tasks, Ris)) {
						schedulableSystem_MSRP_Analysis++;
						sfnp++;
						schedulableSystem_New_MrsP_Analysis2++;
					} else {
						Ris = new_mrsp.NewMrsPRTATest(tasks, resources, false);
						if (isSystemSchedulable(tasks, Ris)) {
							schedulableSystem_New_MrsP_Analysis2++;

							Ris = fnp.NewMrsPRTATest(tasks, resources, false, false);
							if (isSystemSchedulable(tasks, Ris))
								sfnp++;
						}
					}
				}

				System.err.println("number of access: " + j + " ; System number: " + TOTAL_NUMBER_OF_SYSTEMS + " ; New MrsP: "
						+ (double) schedulableSystem_New_MrsP_Analysis2 / (double) TOTAL_NUMBER_OF_SYSTEMS + "  Original MrsP: "
						+ (double) schedulableSystem_Original_MrsP_Analysis / (double) TOTAL_NUMBER_OF_SYSTEMS + "  MSRP: "
						+ (double) schedulableSystem_MSRP_Analysis / (double) TOTAL_NUMBER_OF_SYSTEMS + " NO BLOCKING: "
						+ (double) schedulableSystem_No_Blocking / (double) TOTAL_NUMBER_OF_SYSTEMS + " fifo np lp: "
						+ (double) sfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " fifo p lp: " + (double) sfp / (double) TOTAL_NUMBER_OF_SYSTEMS);
				result += "number of access: " + j + " ; System number: " + TOTAL_NUMBER_OF_SYSTEMS + " ; New MrsP: "
						+ (double) schedulableSystem_New_MrsP_Analysis2 / (double) TOTAL_NUMBER_OF_SYSTEMS + "  Original MrsP: "
						+ (double) schedulableSystem_Original_MrsP_Analysis / (double) TOTAL_NUMBER_OF_SYSTEMS + "  MSRP: "
						+ (double) schedulableSystem_MSRP_Analysis / (double) TOTAL_NUMBER_OF_SYSTEMS + " NO BLOCKING: "
						+ (double) schedulableSystem_No_Blocking / (double) TOTAL_NUMBER_OF_SYSTEMS + " fifo np lp: "
						+ (double) sfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " fifo p lp: " + (double) sfp / (double) TOTAL_NUMBER_OF_SYSTEMS + "\n";

				j = j + 5;
			}

			NUMBER_OF_TASKS_ON_EACH_PARTITION += 2;
		}
		writeSystem("" + 2, result);
	}

	public static void experimentIncreasingCriticalSectionLength() {
		int TOTAL_PARTITIONS = 16;
		int MIN_PERIOD = 1;
		int MAX_PERIOD = 1000;
		// int TOTAL_NUMBER_OF_SYSTEMS = 1000;
		// double UTIL_PER_PARTITION = 0.5;

		double RESOURCE_SHARING_FACTOR = 0.4;
		int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 4;

		int NUMBER_OF_TASKS_ON_EACH_PARTITION = 3;

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, 0.1 * (double) NUMBER_OF_TASKS_ON_EACH_PARTITION, TOTAL_PARTITIONS,
				NUMBER_OF_TASKS_ON_EACH_PARTITION, true, CS_LENGTH_RANGE.SHORT_CS_LEN, RESOURCES_RANGE.PARTITIONS, RESOURCE_SHARING_FACTOR,
				NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);
		long[][] Ris;

		NewMrsPRTA new_mrsp = new NewMrsPRTA();
		OriginalMrsPRTA original_mrsp = new OriginalMrsPRTA();
		MSRPRTA msrp = new MSRPRTA();
		RTAWithoutBlocking noblocking = new RTAWithoutBlocking();
		FIFONonPreemptiveLinearC fnp = new FIFONonPreemptiveLinearC();

		String result = "";

		System.out.println("experimentIncreasingCriticalSectionLength");
		result += "experiment Increasing Critical Section Length \n";

		for (int k = 0; k < 3; k++) {
			System.out.println("number of tasks: " + NUMBER_OF_TASKS_ON_EACH_PARTITION * TOTAL_PARTITIONS);
			result += "number of tasks: " + NUMBER_OF_TASKS_ON_EACH_PARTITION * TOTAL_PARTITIONS + "\n";

			generator.number_of_tasks_per_processor = NUMBER_OF_TASKS_ON_EACH_PARTITION;
			generator.util = 0.1 * NUMBER_OF_TASKS_ON_EACH_PARTITION;

			for (int j = 0; j < 5; j++) {
				int schedulableSystem_New_MrsP_Analysis2 = 0;
				int schedulableSystem_Original_MrsP_Analysis = 0;
				int schedulableSystem_MSRP_Analysis = 0;
				int schedulableSystem_No_Blocking = 0;
				int sfnp = 0;
				int sfp = 0;

				CS_LENGTH_RANGE range = null;
				switch (j) {
				case 0:
					range = CS_LENGTH_RANGE.VERY_SHORT_CS_LEN;
					break;
				case 1:
					range = CS_LENGTH_RANGE.SHORT_CS_LEN;
					break;
				case 2:
					range = CS_LENGTH_RANGE.MEDIUM_CS_LEN;
					break;
				case 3:
					range = CS_LENGTH_RANGE.LONG_CSLEN;
					break;
				case 4:
					range = CS_LENGTH_RANGE.VERY_LONG_CSLEN;
					break;
				default:
					break;
				}
				generator.cs_len_range = range;

				for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {

					ArrayList<ArrayList<SporadicTask>> tasks = generator.generateTasks();
					ArrayList<Resource> resources = generator.generateResources();
					generator.generateResourceUsage(tasks, resources);

					Ris = noblocking.NewMrsPRTATest(tasks, resources, false);
					if (isSystemSchedulable(tasks, Ris)) {
						schedulableSystem_No_Blocking++;
					}

					Ris = original_mrsp.NewMrsPRTATest(tasks, resources, false);
					if (isSystemSchedulable(tasks, Ris))
						schedulableSystem_Original_MrsP_Analysis++;

					Ris = fnp.NewMrsPRTATest(tasks, resources, true, false);
					if (isSystemSchedulable(tasks, Ris))
						sfp++;

					Ris = msrp.NewMrsPRTATest(tasks, resources, false);
					if (isSystemSchedulable(tasks, Ris)) {
						schedulableSystem_MSRP_Analysis++;
						sfnp++;
						schedulableSystem_New_MrsP_Analysis2++;
					} else {
						Ris = new_mrsp.NewMrsPRTATest(tasks, resources, false);
						if (isSystemSchedulable(tasks, Ris)) {
							schedulableSystem_New_MrsP_Analysis2++;

							Ris = fnp.NewMrsPRTATest(tasks, resources, false, false);
							if (isSystemSchedulable(tasks, Ris))
								sfnp++;
						}
					}
					// System.out.println(i);
				}

				System.err.println("cs _len " + (j + 1) * 100 + " ; System number: " + TOTAL_NUMBER_OF_SYSTEMS + " ; New MrsP: "
						+ (double) schedulableSystem_New_MrsP_Analysis2 / (double) TOTAL_NUMBER_OF_SYSTEMS + "  Original MrsP: "
						+ (double) schedulableSystem_Original_MrsP_Analysis / (double) TOTAL_NUMBER_OF_SYSTEMS + "  MSRP: "
						+ (double) schedulableSystem_MSRP_Analysis / (double) TOTAL_NUMBER_OF_SYSTEMS + " NO BLOCKING: "
						+ (double) schedulableSystem_No_Blocking / (double) TOTAL_NUMBER_OF_SYSTEMS + " fifo np lp: "
						+ (double) sfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " fifo p lp: " + (double) sfp / (double) TOTAL_NUMBER_OF_SYSTEMS);
				result += "cs _len " + (j + 1) * 100 + " ; System number: " + TOTAL_NUMBER_OF_SYSTEMS + " ; New MrsP: "
						+ (double) schedulableSystem_New_MrsP_Analysis2 / (double) TOTAL_NUMBER_OF_SYSTEMS + "  Original MrsP: "
						+ (double) schedulableSystem_Original_MrsP_Analysis / (double) TOTAL_NUMBER_OF_SYSTEMS + "  MSRP: "
						+ (double) schedulableSystem_MSRP_Analysis / (double) TOTAL_NUMBER_OF_SYSTEMS + " NO BLOCKING: "
						+ (double) schedulableSystem_No_Blocking / (double) TOTAL_NUMBER_OF_SYSTEMS + " fifo np lp: "
						+ (double) sfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " fifo p lp: " + (double) sfp / (double) TOTAL_NUMBER_OF_SYSTEMS + "\n";
			}

			NUMBER_OF_TASKS_ON_EACH_PARTITION += 2;
		}
		writeSystem("" + 3, result);
	}

	public static boolean isResponseTimeEqual(long[][] oldRi, long[][] newRi) {
		for (int i = 0; i < oldRi.length; i++) {
			for (int j = 0; j < oldRi[i].length; j++) {
				if (oldRi[i][j] != newRi[i][j]) {
					System.out.println("P" + i + " T" + j);

					return false;

				}
			}
		}
		return true;
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

	public static void runTest() {
		int TOTAL_PARTITIONS = 2;
		double UTIL_PER_PARTITION = 0.9;
		int MIN_PERIOD = 1;
		int MAX_PERIOD = 1000;
		int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 3; // 1,2,5,10,15
		double RESOURCE_SHARING_FACTOR = 0.8; // 0.1, 0.25, 0.4, 0.75
		int NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION = 2; // 1,2,5,10,15
		int TOTAL_NUMBER_OF_SYSTEMS = 1;

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, UTIL_PER_PARTITION, TOTAL_PARTITIONS,
				NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION, true, CS_LENGTH_RANGE.SHORT_CS_LEN, RESOURCES_RANGE.PARTITIONS, RESOURCE_SHARING_FACTOR,
				NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);
		long[][] Ris, Ris1;

		OriginalMrsPRTA original_mrsp = new OriginalMrsPRTA();
		MSRPRTA msrp = new MSRPRTA();

		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS;) {
			ArrayList<ArrayList<SporadicTask>> tasks = generator.generateTasks();
			ArrayList<Resource> resources = generator.generateResources();
			generator.generateResourceUsage(tasks, resources);

			Ris1 = original_mrsp.NewMrsPRTATest(tasks, resources, false);
			Ris = msrp.NewMrsPRTATest(tasks, resources, false);

			if (!isResponseTimeEqual(Ris, Ris1)) {
				System.out.println("not equal");
				break;
			}
		}
	}

	public static void runTest1() {
		int TOTAL_PARTITIONS = 16;
		double UTIL_PER_PARTITION = 1;
		int MIN_PERIOD = 1;
		int MAX_PERIOD = 1000;
		int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 50; // 1,2,5,10,15
		double RESOURCE_SHARING_FACTOR = .8; // 0.1, 0.25, 0.4, 0.75
		int NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION = 10; // 1,2,5,10,15

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, UTIL_PER_PARTITION, TOTAL_PARTITIONS,
				NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION, true, CS_LENGTH_RANGE.SHORT_CS_LEN, RESOURCES_RANGE.PARTITIONS, RESOURCE_SHARING_FACTOR,
				NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);

		int count = 0;
		while (count < 10000) {
			ArrayList<ArrayList<SporadicTask>> tasks = generator.generateTasks();
			ArrayList<Resource> resources = generator.generateResources();

			int fails = generator.generateResourceUsage(tasks, resources);
			System.out.println("fails : " + fails);
			// generator.testifyGeneratedTasksetAndResource(tasks, resources);
			count++;
		}

	}

	public static void runTest3() {
		int TOTAL_PARTITIONS = 16;
		double UTIL_PER_PARTITION = 0.9;
		int MIN_PERIOD = 1;
		int MAX_PERIOD = 1000;
		int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 8; // 1,2,5,10,15
		double RESOURCE_SHARING_FACTOR = .8; // 0.1, 0.25, 0.4, 0.75
		int NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION = 9; // 1,2,5,10,15

		FIFONonPreemptiveLinearC lip_fifo_np = new FIFONonPreemptiveLinearC();

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, UTIL_PER_PARTITION, TOTAL_PARTITIONS,
				NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION, true, CS_LENGTH_RANGE.SHORT_CS_LEN, RESOURCES_RANGE.PARTITIONS, RESOURCE_SHARING_FACTOR,
				NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);

		ArrayList<ArrayList<SporadicTask>> tasks = generator.generateTasks();
		ArrayList<Resource> resources = generator.generateResources();
		generator.generateResourceUsage(tasks, resources);
		// generator.testifyGeneratedTasksetAndResource(tasks, resources);

		for (int i = 0; i < 100; i++) {
			lip_fifo_np.NewMrsPRTATest(tasks, resources, false, false);
			lip_fifo_np.NewMrsPRTATest(tasks, resources, true, false);
			System.out.println(i);
		}

	}

	public static void runTest2() {
		int TOTAL_PARTITIONS = 2;
		double UTIL_PER_PARTITION = 0.5;
		int MIN_PERIOD = 1;
		int MAX_PERIOD = 1000;
		int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 3; // 1,2,5,10,15
		double RESOURCE_SHARING_FACTOR = 1; // 0.1, 0.25, 0.4, 0.75
		int NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION = 2; // 1,2,5,10,15

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, UTIL_PER_PARTITION, TOTAL_PARTITIONS,
				NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION, true, CS_LENGTH_RANGE.SHORT_CS_LEN, RESOURCES_RANGE.PARTITIONS, RESOURCE_SHARING_FACTOR,
				NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);
		FIFONonPreemptiveLinearC lip_fifo_np = new FIFONonPreemptiveLinearC();

		ArrayList<ArrayList<SporadicTask>> tasks = generator.generateTasks();
		ArrayList<Resource> resources = generator.generateResources();
		generator.generateResourceUsage(tasks, resources);

		for (int i = 0; i < 100; i++) {
			System.out.println("aaa");
			lip_fifo_np.NewMrsPRTATest(tasks, resources, true, false);
			System.out.println(i);
		}

	}

}
