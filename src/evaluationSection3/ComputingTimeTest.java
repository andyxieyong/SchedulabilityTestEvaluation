package evaluationSection3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;

import analysisILP.FIFOLinearC;
import analysisNewIO.MSRPIO;
import analysisNewIO.MrsPIO;
import analysisNewIO.PWLPIO;
import entity.Resource;
import entity.SporadicTask;
import generatorTools.SimpleSystemGenerator;
import utils.AnalysisUtils.CS_LENGTH_RANGE;
import utils.AnalysisUtils.RESOURCES_RANGE;

public class ComputingTimeTest {

	public static int TOTAL_NUMBER_OF_SYSTEMS = 1000;
	public static int TOTAL_PARTITIONS = 16;
	public static int MIN_PERIOD = 1;
	public static int MAX_PERIOD = 1000;

	public static void main(String[] args) throws InterruptedException {

		for (int i = 3; i < 8; i++) {
			experimentIncreasingWorkLoad(i);
		}

		for (int i = 4; i < 17; i = i + 4) {
			experimentIncreasingParallel(i);
		}

	}

	public static void experimentIncreasingWorkLoad(int smallSet) {
		int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 2;
		double RESOURCE_SHARING_FACTOR = 0.4;
		int NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION = smallSet;

		SimpleSystemGenerator generator = new SimpleSystemGenerator(MIN_PERIOD, MAX_PERIOD, TOTAL_PARTITIONS,
				NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION * TOTAL_PARTITIONS, true, CS_LENGTH_RANGE.SHORT_CS_LEN, RESOURCES_RANGE.PARTITIONS,
				RESOURCE_SHARING_FACTOR, NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);

		long[][] computingTime = new long[5][];

		for (int i = 0; i < 5; i++) {
			computingTime[i] = new long[TOTAL_NUMBER_OF_SYSTEMS];
		}

		String result = "";

		long start, end = 0;

		MSRPIO fnp = new MSRPIO();
		PWLPIO fp = new PWLPIO();
		FIFOLinearC fifo = new FIFOLinearC();
		MrsPIO new_mrsp = new MrsPIO();

		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
			ArrayList<SporadicTask> tasksToAlloc = generator.generateTasks();
			ArrayList<Resource> resources = generator.generateResources();
			ArrayList<ArrayList<SporadicTask>> tasks = generator.generateResourceUsage(tasksToAlloc, resources);

			start = getTime();
			fnp.getResponseTimeDM(tasks, resources, true, true, false);
			end = getTime() - start;
			computingTime[0][i] = end;

			start = getTime();
			new_mrsp.getResponseTimeDM(tasks, resources, true, true, true, true, false);
			end = getTime() - start;
			computingTime[1][i] = end;

			start = getTime();
			fp.getResponseTimeDM(tasks, resources, true, true, false);
			end = getTime() - start;
			computingTime[2][i] = end;

			start = getTime();
			fifo.getResponseTime(tasks, resources, false, false);
			end = getTime() - start;
			computingTime[3][i] = end;

			start = getTime();
			fifo.getResponseTime(tasks, resources, true, false);
			end = getTime() - start;
			computingTime[4][i] = end;

			System.out.println(1 + " " + 1 + " " + smallSet + " times: " + i);

		}

		result += "MSRP-new" + "    " + "MrsP-new" + "    " + "PWLP-new" + "    " + "MSRP-ilp" + "    " + "PWLP-ilp" + "\n";

		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
			result += computingTime[0][i] + "    " + computingTime[1][i] + "    " + computingTime[2][i] + "    " + computingTime[3][i] + "    "
					+ computingTime[4][i] + "\n";
		}

		writeSystem((1 + " " + 1 + " " + smallSet), result);
	}

	public static void experimentIncreasingParallel(int partitions) {
		double RESOURCE_SHARING_FACTOR = 0.4;
		int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 2;
		int NUMBER_OF_TASKS_ON_EACH_PARTITION = 5;
		int total_partitions = partitions;

		SimpleSystemGenerator generator = new SimpleSystemGenerator(MIN_PERIOD, MAX_PERIOD, total_partitions,
				NUMBER_OF_TASKS_ON_EACH_PARTITION * total_partitions, true, CS_LENGTH_RANGE.VERY_SHORT_CS_LEN, RESOURCES_RANGE.PARTITIONS,
				RESOURCE_SHARING_FACTOR, NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);

		long[][] computingTime = new long[5][];

		for (int i = 0; i < 5; i++) {
			computingTime[i] = new long[TOTAL_NUMBER_OF_SYSTEMS];
		}

		String result = "";

		long start, end = 0;

		MSRPIO fnp = new MSRPIO();
		PWLPIO fp = new PWLPIO();
		FIFOLinearC fifo = new FIFOLinearC();
		MrsPIO new_mrsp = new MrsPIO();

		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
			ArrayList<SporadicTask> tasksToAlloc = generator.generateTasks();
			ArrayList<Resource> resources = generator.generateResources();
			ArrayList<ArrayList<SporadicTask>> tasks = generator.generateResourceUsage(tasksToAlloc, resources);

			start = getTime();
			fnp.getResponseTimeDM(tasks, resources, true, true, false);
			end = getTime() - start;
			computingTime[0][i] = end;

			start = getTime();
			new_mrsp.getResponseTimeDM(tasks, resources, true, true, true, true, false);
			end = getTime() - start;
			computingTime[1][i] = end;

			start = getTime();
			fp.getResponseTimeDM(tasks, resources, true, true, false);
			end = getTime() - start;
			computingTime[2][i] = end;

			start = getTime();
			fifo.getResponseTime(tasks, resources, false, false);
			end = getTime() - start;
			computingTime[3][i] = end;

			start = getTime();
			fifo.getResponseTime(tasks, resources, true, false);
			end = getTime() - start;
			computingTime[4][i] = end;

			System.out.println(4 + " " + 1 + " " + total_partitions + " times: " + i);

		}

		result += "MSRP-new" + "    " + "MrsP-new" + "    " + "PWLP-new" + "    " + "MSRP-ilp" + "    " + "PWLP-ilp" + "\n";

		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
			result += computingTime[0][i] + "    " + computingTime[1][i] + "    " + computingTime[2][i] + "    " + computingTime[3][i] + "    "
					+ computingTime[4][i] + "\n";
		}

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
