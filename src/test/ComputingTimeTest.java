package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;

import ILPBasedAnalysis.FIFOLinearC;
import basicAnalysis.MSRPRTA;
import basicAnalysis.OriginalMrsPRTA;
import entity.Resource;
import entity.SporadicTask;
import generatorTools.SystemGenerator;
import generatorTools.SystemGenerator.CS_LENGTH_RANGE;
import generatorTools.SystemGenerator.RESOURCES_RANGE;
import newAnalysis.FIFONP;
import newAnalysis.FIFOP;
import newAnalysisOverheads.NewMrsPRTAWithMCNP;

public class ComputingTimeTest {

	public static int TOTAL_NUMBER_OF_SYSTEMS = 1000;
	public static int TOTAL_PARTITIONS = 16;
	public static int MIN_PERIOD = 1;
	public static int MAX_PERIOD = 1000;

	public static void main(String[] args) throws InterruptedException {
		// int experiment = 0;
		// int parameter = 0;
		//
		// //experimentIncreasingCriticalSectionLength(2);
		//
		// if (args.length == 2) {
		// experiment = Integer.parseInt(args[0]);
		// parameter = Integer.parseInt(args[1]);
		//
		// switch (experiment) {
		// case 1:
		// experimentIncreasingWorkLoad(parameter);
		// break;
		// case 2:
		// experimentIncreasingCriticalSectionLength(parameter);
		// break;
		// case 3:
		// experimentIncreasingContention(parameter);
		// break;
		// case 4:
		// experimentIncreasingParallel(parameter);
		// break;
		// default:
		// break;
		// }
		//
		// } else
		// System.err.println("wrong parameter.");

//		for (int i = 5; i < 8; i++) {
//			experimentIncreasingWorkLoad(i);
//		}
//
//		for (int i = 14; i < 21; i=i+2) {
//			experimentIncreasingParallel(i);
//		}
		
		experimentIncreasingParallel(16);
	}

	public static void experimentIncreasingWorkLoad(int smallSet) {
		int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 2;
		double RESOURCE_SHARING_FACTOR = 0.4;
		int NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION = smallSet;

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, 0.1 * (double) NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION, TOTAL_PARTITIONS,
				NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION, true, CS_LENGTH_RANGE.SHORT_CS_LEN, RESOURCES_RANGE.PARTITIONS, RESOURCE_SHARING_FACTOR,
				NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);

		long[][] computingTime = new long[7][];

		for (int i = 0; i < 7; i++) {
			computingTime[i] = new long[TOTAL_NUMBER_OF_SYSTEMS];
		}

		String result = "";

		long start, end = 0;

		FIFONP fnp = new FIFONP();
		FIFOP fp = new FIFOP();
		FIFOLinearC fifo = new FIFOLinearC();
		NewMrsPRTAWithMCNP new_mrsp = new NewMrsPRTAWithMCNP();
		OriginalMrsPRTA original_mrsp = new OriginalMrsPRTA();
		MSRPRTA msrp = new MSRPRTA();

		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
			ArrayList<ArrayList<SporadicTask>> tasks = generator.generateTasks();
			ArrayList<Resource> resources = generator.generateResources();
			generator.generateResourceUsage(tasks, resources);

			start = getTime();
			new_mrsp.NewMrsPRTATest(tasks, resources, 8,16, false);
			end = getTime() - start;
			computingTime[0][i] = end;

			start = getTime();
			original_mrsp.NewMrsPRTATest(tasks, resources, false);
			end = getTime() - start;
			computingTime[1][i] = end;

			start = getTime();
			msrp.NewMrsPRTATest(tasks, resources, false);
			end = getTime() - start;
			computingTime[2][i] = end;

			start = getTime();
			fnp.NewMrsPRTATest(tasks, resources, false);
			end = getTime() - start;
			computingTime[3][i] = end;

			start = getTime();
			fp.NewMrsPRTATest(tasks, resources, false);
			end = getTime() - start;
			computingTime[4][i] = end;

			start = getTime();
			fifo.NewMrsPRTATest(tasks, resources, false, false);
			end = getTime() - start;
			computingTime[5][i] = end;

			start = getTime();
			fifo.NewMrsPRTATest(tasks, resources, true, false);
			end = getTime() - start;
			computingTime[6][i] = end;

			System.out.println(1 + " " + 1 + " " + smallSet + " times: " + i);

		}

		result += "New MrsP" + "    " + "Original MrsP" + "    " + "MSRP" + "    " + "FIFONP-JAVA" + "    " + "FIFOP-JAVA" + "    " + "FIFONP-C" + "    "
				+ "FIFOP-C" + "\n";

		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
			result += computingTime[0][i] + "    " + computingTime[1][i] + "    " + computingTime[2][i] + "    " + computingTime[3][i] + "    "
					+ computingTime[4][i] + "    " + computingTime[5][i] + "    " + computingTime[6][i] + "\n";
		}

		
//		result += "New MrsP" + "\n";
//
//		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
//			result += computingTime[0][i] + "\n";
//		}
		
		writeSystem((1 + " " + 1 + " " + smallSet), result);
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
		default:
			break;
		}

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, 0.1 * (double) NUMBER_OF_TASKS_ON_EACH_PARTITION, TOTAL_PARTITIONS,
				NUMBER_OF_TASKS_ON_EACH_PARTITION, true, range, RESOURCES_RANGE.PARTITIONS, RESOURCE_SHARING_FACTOR, NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);

		long[][] computingTime = new long[7][];

		for (int i = 0; i < 7; i++) {
			computingTime[i] = new long[TOTAL_NUMBER_OF_SYSTEMS];
		}

		String result = "";

		long start, end = 0;

		FIFONP fnp = new FIFONP();
		FIFOP fp = new FIFOP();
		FIFOLinearC fifo = new FIFOLinearC();
		NewMrsPRTAWithMCNP new_mrsp = new NewMrsPRTAWithMCNP();
		OriginalMrsPRTA original_mrsp = new OriginalMrsPRTA();
		MSRPRTA msrp = new MSRPRTA();

		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
			ArrayList<ArrayList<SporadicTask>> tasks = generator.generateTasks();
			ArrayList<Resource> resources = generator.generateResources();
			generator.generateResourceUsage(tasks, resources);

			start = getTime();
			new_mrsp.NewMrsPRTATest(tasks, resources, 8,16,false);
			end = getTime() - start;
			computingTime[0][i] = end;

			start = getTime();
			original_mrsp.NewMrsPRTATest(tasks, resources, false);
			end = getTime() - start;
			computingTime[1][i] = end;

			start = getTime();
			msrp.NewMrsPRTATest(tasks, resources, false);
			end = getTime() - start;
			computingTime[2][i] = end;

			start = getTime();
			fnp.NewMrsPRTATest(tasks, resources, false);
			end = getTime() - start;
			computingTime[3][i] = end;

			start = getTime();
			fp.NewMrsPRTATest(tasks, resources, false);
			end = getTime() - start;
			computingTime[4][i] = end;

			start = getTime();
			fifo.NewMrsPRTATest(tasks, resources, false, false);
			end = getTime() - start;
			computingTime[5][i] = end;

			start = getTime();
			fifo.NewMrsPRTATest(tasks, resources, true, false);
			end = getTime() - start;
			computingTime[6][i] = end;

			System.out.println(2 + " " + 1 + " " + csLenConfig + " times: " + i);
		}

		result += "New MrsP" + "    " + "Original MrsP" + "    " + "MSRP" + "    " + "FIFONP-JAVA" + "    " + "FIFOP-JAVA" + "    " + "FIFONP-C" + "    "
				+ "FIFOP-C" + "\n";

		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
			result += computingTime[0][i] + "    " + computingTime[1][i] + "    " + computingTime[2][i] + "    " + computingTime[3][i] + "    "
					+ computingTime[4][i] + "    " + computingTime[5][i] + "    " + computingTime[6][i] + "\n";
		}
		writeSystem((2 + " " + 4 + " " + csLenConfig), result);
	}

	public static void experimentIncreasingContention(int smallSet) {
		double RESOURCE_SHARING_FACTOR = 0.4;
		int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = smallSet;
		int NUMBER_OF_TASKS_ON_EACH_PARTITION = 5;

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, 0.1 * (double) NUMBER_OF_TASKS_ON_EACH_PARTITION, TOTAL_PARTITIONS,
				NUMBER_OF_TASKS_ON_EACH_PARTITION, true, CS_LENGTH_RANGE.VERY_SHORT_CS_LEN, RESOURCES_RANGE.PARTITIONS, RESOURCE_SHARING_FACTOR,
				NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);

		long[][] computingTime = new long[7][];

		for (int i = 0; i < 7; i++) {
			computingTime[i] = new long[TOTAL_NUMBER_OF_SYSTEMS];
		}

		String result = "";

		long start, end = 0;

		FIFONP fnp = new FIFONP();
		FIFOP fp = new FIFOP();
		FIFOLinearC fifo = new FIFOLinearC();
		NewMrsPRTAWithMCNP new_mrsp = new NewMrsPRTAWithMCNP();
		OriginalMrsPRTA original_mrsp = new OriginalMrsPRTA();
		MSRPRTA msrp = new MSRPRTA();

		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {

			ArrayList<ArrayList<SporadicTask>> tasks = generator.generateTasks();
			ArrayList<Resource> resources = generator.generateResources();
			generator.generateResourceUsage(tasks, resources);

			start = getTime();
			new_mrsp.NewMrsPRTATest(tasks, resources,8,16, false);
			end = getTime() - start;
			computingTime[0][i] = end;

			start = getTime();
			original_mrsp.NewMrsPRTATest(tasks, resources, false);
			end = getTime() - start;
			computingTime[1][i] = end;

			start = getTime();
			msrp.NewMrsPRTATest(tasks, resources, false);
			end = getTime() - start;
			computingTime[2][i] = end;

			start = getTime();
			fnp.NewMrsPRTATest(tasks, resources, false);
			end = getTime() - start;
			computingTime[3][i] = end;

			start = getTime();
			fp.NewMrsPRTATest(tasks, resources, false);
			end = getTime() - start;
			computingTime[4][i] = end;

			start = getTime();
			fifo.NewMrsPRTATest(tasks, resources, false, false);
			end = getTime() - start;
			computingTime[5][i] = end;

			start = getTime();
			fifo.NewMrsPRTATest(tasks, resources, true, false);
			end = getTime() - start;
			computingTime[6][i] = end;

			System.out.println(3 + " " + 1 + " " + smallSet + " times: " + i);
		}

		result += "New MrsP" + "    " + "Original MrsP" + "    " + "MSRP" + "    " + "FIFONP-JAVA" + "    " + "FIFOP-JAVA" + "    " + "FIFONP-C" + "    "
				+ "FIFOP-C" + "\n";

		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
			result += computingTime[0][i] + "    " + computingTime[1][i] + "    " + computingTime[2][i] + "    " + computingTime[3][i] + "    "
					+ computingTime[4][i] + "    " + computingTime[5][i] + "    " + computingTime[6][i] + "\n";
		}
		writeSystem((3 + " " + 1 + " " + smallSet), result);
	}

	public static void experimentIncreasingParallel(int partitions) {
		double RESOURCE_SHARING_FACTOR = 0.4;
		int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 2;
		int NUMBER_OF_TASKS_ON_EACH_PARTITION = 5;
		int total_partitions = partitions;

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, 0.1 * (double) NUMBER_OF_TASKS_ON_EACH_PARTITION, total_partitions,
				NUMBER_OF_TASKS_ON_EACH_PARTITION, true, CS_LENGTH_RANGE.VERY_SHORT_CS_LEN, RESOURCES_RANGE.PARTITIONS, RESOURCE_SHARING_FACTOR,
				NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);

		long[][] computingTime = new long[7][];

		for (int i = 0; i < 7; i++) {
			computingTime[i] = new long[TOTAL_NUMBER_OF_SYSTEMS];
		}

		String result = "";

		long start, end = 0;

		FIFONP fnp = new FIFONP();
		FIFOP fp = new FIFOP();
		FIFOLinearC fifo = new FIFOLinearC();
		NewMrsPRTAWithMCNP new_mrsp = new NewMrsPRTAWithMCNP();
		OriginalMrsPRTA original_mrsp = new OriginalMrsPRTA();
		MSRPRTA msrp = new MSRPRTA();

		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
			ArrayList<ArrayList<SporadicTask>> tasks = generator.generateTasks();
			ArrayList<Resource> resources = generator.generateResources();
			generator.generateResourceUsage(tasks, resources);

			start = getTime();
			new_mrsp.NewMrsPRTATest(tasks, resources,8,16, false);
			end = getTime() - start;
			computingTime[0][i] = end;

			start = getTime();
			original_mrsp.NewMrsPRTATest(tasks, resources, false);
			end = getTime() - start;
			computingTime[1][i] = end;

			start = getTime();
			msrp.NewMrsPRTATest(tasks, resources, false);
			end = getTime() - start;
			computingTime[2][i] = end;

			start = getTime();
			fnp.NewMrsPRTATest(tasks, resources, false);
			end = getTime() - start;
			computingTime[3][i] = end;

			start = getTime();
			fp.NewMrsPRTATest(tasks, resources, false);
			end = getTime() - start;
			computingTime[4][i] = end;

			start = getTime();
			fifo.NewMrsPRTATest(tasks, resources, false, false);
			end = getTime() - start;
			computingTime[5][i] = end;

			start = getTime();
			fifo.NewMrsPRTATest(tasks, resources, true, false);
			end = getTime() - start;
			computingTime[6][i] = end;

			System.out.println(4 + " " + 1 + " " + total_partitions + " times: " + i);

		}

		result += "New MrsP" + "    " + "Original MrsP" + "    " + "MSRP" + "    " + "FIFONP-JAVA" + "    " + "FIFOP-JAVA" + "    " + "FIFONP-C" + "    "
				+ "FIFOP-C" + "\n";

		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
			result += computingTime[0][i] + "    " + computingTime[1][i] + "    " + computingTime[2][i] + "    " + computingTime[3][i] + "    "
					+ computingTime[4][i] + "    " + computingTime[5][i] + "    " + computingTime[6][i] + "\n";
		}
		
//		result += "New MrsP" + "\n";
//
//		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
//			result += computingTime[0][i] + "\n";
//		}

		writeSystem(("4 " + 1 + " " + total_partitions), result);
	}

	public static long getTime() {
		return ManagementFactory.getThreadMXBean().getThreadCpuTime(Thread.currentThread().getId());
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
