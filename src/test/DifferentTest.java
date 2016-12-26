package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import analysis.FIFONPLinearJava;
import analysis.NewMrsPRTA;
import entity.Resource;
import entity.SporadicTask;
import generatorTools.SystemGenerator;

public class DifferentTest {

	public static int TOTAL_NUMBER_OF_SYSTEMS = 1000;
	public static int TOTAL_PARTITIONS = 5;
	public static int MIN_PERIOD = 1;
	public static int MAX_PERIOD = 1000;
	public static int NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION = 5;
	public static int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 40;
	public static double RESOURCE_SHARING_FACTOR = .5;

	public static void main(String[] args) {

		NewMrsPRTA new_mrsp = new NewMrsPRTA();
		FIFONPLinearJava fnp = new FIFONPLinearJava();

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, 0.1 * (double) NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION,
				TOTAL_PARTITIONS, NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION, true, Analysiser.CS_LENGTH_RANGE.VERY_SHORT_CS_LEN,
				Analysiser.RESOURCES_RANGE.PARTITIONS, RESOURCE_SHARING_FACTOR, NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);

		long[][] r1, r2, diff;
		double[][] totaldiff;
		long[][] diffs = new long[TOTAL_PARTITIONS * NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION][TOTAL_NUMBER_OF_SYSTEMS];

		totaldiff = new double[TOTAL_PARTITIONS][NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION];

		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
			ArrayList<ArrayList<SporadicTask>> tasks = generator.generateTasks();
			ArrayList<Resource> resources = generator.generateResources();
			generator.generateResourceUsage(tasks, resources);

			r1 = fnp.NewMrsPRTATest(tasks, resources, false);
			System.out.println(i);
			r2 = new_mrsp.NewMrsPRTATest(tasks, resources, false);
			// System.out.println(i+"*");
			diff = diff(r1, r2);

			for (int j = 0; j < diff.length; j++) {
				for (int k = 0; k < diff[j].length; k++) {
					totaldiff[j][k] += diff[j][k];
					diffs[j * NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION + k][i] = diff[j][k];
				}
			}
		}

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter(new File("result/diff.txt"), false));
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

}
