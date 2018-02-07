package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import analysisBasic.MSRPOriginal;
import analysisBasic.MrsPOriginal;
import analysisBasic.RTAWithoutBlocking;
import analysisNew.MSRPNew;
import analysisNew.MrsPNew;
import analysisNew.PWLPNew;
import entity.Resource;
import entity.SporadicTask;
import generatorTools.SystemGenerator;
import generatorTools.SystemGenerator.CS_LENGTH_RANGE;
import generatorTools.SystemGenerator.RESOURCES_RANGE;

public class SchedulabilityTest {

	public static int TOTAL_NUMBER_OF_SYSTEMS = 1000;
	public static int TOTAL_PARTITIONS = 16;
	public static int MIN_PERIOD = 1;
	public static int MAX_PERIOD = 1000;

	public static void main(String[] args) throws InterruptedException {
		for (int i = 1; i < 10; i++)
			experimentIncreasingWorkLoad(i);
	}

	public static void experimentIncreasingWorkLoad(int NoT) {
		int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 2;
		double RESOURCE_SHARING_FACTOR = 0.4;

		long[][] Ris;
		MrsPNew new_mrsp = new MrsPNew();
		MrsPOriginal original_mrsp = new MrsPOriginal();
		MSRPOriginal msrp = new MSRPOriginal();
		RTAWithoutBlocking noblocking = new RTAWithoutBlocking();
		PWLPNew fp = new PWLPNew();
		MSRPNew fnp = new MSRPNew();

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, TOTAL_PARTITIONS, NoT * TOTAL_PARTITIONS, true, CS_LENGTH_RANGE.SHORT_CS_LEN,
				RESOURCES_RANGE.PARTITIONS, RESOURCE_SHARING_FACTOR, NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);

		String result = "", results = "";
		int smrsp = 0;
		int smrspOld = 0;
		int smsrpOld = 0;
		int sNoBlocking = 0;
		int sfnp = 0;
		int sfp = 0;

		int mrsps, mrspOlds, msrpOlds, fnps, fps, nos = 0;

		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
			ArrayList<SporadicTask> tasksToAlloc = generator.generateTasks();
			ArrayList<Resource> resources = generator.generateResources();
			ArrayList<ArrayList<SporadicTask>> tasks = generator.generateResourceUsage(tasksToAlloc, resources);

			mrsps = mrspOlds = msrpOlds = fnps = fps = nos = 0;

			Ris = noblocking.getResponseTime(tasks, resources, false);
			if (isSystemSchedulable(tasks, Ris)) {
				sNoBlocking++;
				nos = 1;

				Ris = original_mrsp.getResponseTime(tasks, resources, false);
				if (isSystemSchedulable(tasks, Ris)) {
					smrspOld++;
					smrsp++;
					mrsps = mrspOlds = 1;

				} else {
					Ris = new_mrsp.getResponseTime(tasks, resources, false);
					if (isSystemSchedulable(tasks, Ris)) {
						smrsp++;
						mrsps = 1;
					}
				}

				Ris = msrp.getResponseTime(tasks, resources, false);
				if (isSystemSchedulable(tasks, Ris)) {
					smsrpOld++;
					sfnp++;

					mrspOlds = fnps = 1;

				} else {
					Ris = fnp.getResponseTime(tasks, resources, false);
					if (isSystemSchedulable(tasks, Ris)) {
						sfnp++;
						fnps = 1;
					}
				}

				Ris = fp.getResponseTime(tasks, resources, false);
				if (isSystemSchedulable(tasks, Ris)) {
					sfp++;
					fps = 1;
				}

			}

			System.out.println(1 + " " + 1 + " " + NoT + " times: " + i + " " + fnps + " " + fps + " " + mrsps + " " + msrpOlds + " " + mrspOlds + " " + nos);
			results += 1 + " " + 1 + " " + NoT + " times: " + i + " " + fnps + " " + fps + " " + mrsps + " " + msrpOlds + " " + mrspOlds + " " + nos + "\n";
		}

		result += (double) sfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) sfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) smrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) smsrpOld / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) smrspOld / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) sNoBlocking / (double) TOTAL_NUMBER_OF_SYSTEMS + "\n";

		writeSystem((1 + " " + 1 + " " + NoT), result);
		writeSystem((1 + " " + 2 + " " + NoT), results);
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
