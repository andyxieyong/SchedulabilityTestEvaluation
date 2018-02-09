package evaluationSection3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import analysisNew.MSRPNew;
import analysisNew.MrsPNew;
import entity.Resource;
import entity.SporadicTask;
import generatorTools.SimpleSystemGenerator;
import utils.AnalysisUtils.CS_LENGTH_RANGE;
import utils.AnalysisUtils.RESOURCES_RANGE;

public class ResponseTimeBoundingTest {

	public static int TOTAL_NUMBER_OF_SYSTEMS = 10000;

	public static int TOTAL_PARTITIONS = 5;
	public static int MIN_PERIOD = 1;
	public static int MAX_PERIOD = 1000;
	public static int NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION = 5;
	public static int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 25;
	public static double RESOURCE_SHARING_FACTOR = .4;

	public static CS_LENGTH_RANGE range = CS_LENGTH_RANGE.VERY_SHORT_CS_LEN;

	public static void main(String[] args) {

		run();
	}

	public static void run() {

		MrsPNew new_mrsp = new MrsPNew();
		MSRPNew fnp = new MSRPNew();

		SimpleSystemGenerator generator = new SimpleSystemGenerator(MIN_PERIOD, MAX_PERIOD, TOTAL_PARTITIONS,
				NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION * TOTAL_PARTITIONS, true, range, RESOURCES_RANGE.PARTITIONS, RESOURCE_SHARING_FACTOR,
				NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);

		long[][] r1, r2, diff;
		double[][] totaldiff = new double[TOTAL_PARTITIONS][NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION];
		double[][] diffs = new double[TOTAL_PARTITIONS * NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION][TOTAL_NUMBER_OF_SYSTEMS];

		int count = 0;
		int actual_count = 0;

		while (count < TOTAL_NUMBER_OF_SYSTEMS) {
			ArrayList<SporadicTask> tasksToAlloc = generator.generateTasks();
			ArrayList<Resource> resources = generator.generateResources();
			ArrayList<ArrayList<SporadicTask>> tasks = generator.generateResourceUsage(tasksToAlloc, resources);

			r1 = fnp.getResponseTime(tasks, resources, false);
			r2 = new_mrsp.getResponseTime(tasks, resources, false);

			if (isSystemSchedulable(tasks, r1) && isSystemSchedulable(tasks, r2)) {
				diff = diff(r1, r2);

				for (int j = 0; j < diff.length; j++) {
					for (int k = 0; k < diff[j].length; k++) {
						totaldiff[j][k] += diff[j][k];
						diffs[j * NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION + k][count] = ((double) diff[j][k] / (double) r2[j][k]) * 100;

					}
				}

				count++;
				System.out.println(count);
			}

			actual_count++;

		}

		System.out.println("vaild system number: " + count);
		System.out.println("totoal system number: " + actual_count);

		// "result/critical section length m5 n5 k4 A4 Rm/diff mrsp fifonp " +
		// range.toString() + ".txt"

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(
					new FileWriter(new File("result/access m5 n5 k4 Rm vshort/diff mrsp fifonp " + NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE + ".txt"), false));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		PrintWriter writer1 = null;
		try {
			writer1 = new PrintWriter(new FileWriter(new File("result/diffname.txt"), false));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < diffs.length; i++) {
			for (int j = 0; j < diffs[i].length; j++) {
				writer.println(diffs[i][j]);
				writer1.println("T" + (i + 1));
			}
		}

		for (int j = 0; j < totaldiff.length; j++) {

			for (int k = 0; k < totaldiff[j].length; k++) {
				totaldiff[j][k] = (double) totaldiff[j][k] / (double) TOTAL_NUMBER_OF_SYSTEMS;
				System.out.println("task id: " + (j * NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION + k + 1) + " diff: " + totaldiff[j][k]);
			}
		}

		writer.close();
		writer1.close();
	}

	public static long[][] diff(long[][] r1, long[][] r2) {
		long[][] diff = new long[r1.length][];

		for (int i = 0; i < r1.length; i++) {
			diff[i] = new long[r1[i].length];

			for (int j = 0; j < r1[i].length; j++) {
				diff[i][j] = r1[i][j] - r2[i][j];
				if (diff[i][j] < 0) {
					System.out.println("error");
					System.exit(0);
				}
			}
		}

		return diff;
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
