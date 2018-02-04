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
import analysisILP.FIFOLinearC;
import analysisNew.MSRPNew;
import analysisNew.MrsPNew;
import analysisNew.PWLPNew;
import analysisNewIO.MSRPIO;
import analysisNewIO.MrsPIO;
import analysisNewIO.PWLPIO;
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

		for (int i = 1; i < 7; i++)
			experimentIncreasingCriticalSectionLength(i);

		for (int i = 1; i < 23; i = i + 2)
			experimentIncreasingParallelism(i);

		for (int i = 1; i < 42; i = 1 + 5)
			experimentIncreasingContention(i);
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

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, TOTAL_PARTITIONS, NoT * TOTAL_PARTITIONS, true,
				CS_LENGTH_RANGE.VERY_SHORT_CS_LEN, RESOURCES_RANGE.PARTITIONS, RESOURCE_SHARING_FACTOR, NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);

		String result = "";
		int smrsp = 0;
		int smrspOld = 0;
		int smsrpOld = 0;
		int sNoBlocking = 0;
		int sfnp = 0;
		int sfp = 0;

		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
			ArrayList<SporadicTask> tasksToAlloc = generator.generateTasks();
			ArrayList<Resource> resources = generator.generateResources();
			ArrayList<ArrayList<SporadicTask>> tasks = generator.generateResourceUsage(tasksToAlloc, resources);

			Ris = noblocking.getResponseTime(tasks, resources, false);
			if (isSystemSchedulable(tasks, Ris)) {
				sNoBlocking++;

				Ris = original_mrsp.getResponseTime(tasks, resources, false);
				if (isSystemSchedulable(tasks, Ris)) {
					smrspOld++;
					smrsp++;
				} else {
					Ris = new_mrsp.getResponseTime(tasks, resources, false);
					if (isSystemSchedulable(tasks, Ris))
						smrsp++;
				}

				Ris = msrp.getResponseTime(tasks, resources, false);
				if (isSystemSchedulable(tasks, Ris)) {
					smsrpOld++;
					sfnp++;
				} else {
					Ris = fnp.getResponseTime(tasks, resources, false);
					if (isSystemSchedulable(tasks, Ris))
						sfnp++;
				}

				Ris = fp.getResponseTime(tasks, resources, false);
				if (isSystemSchedulable(tasks, Ris))
					sfp++;

			}

			System.out.println(1 + "" + 1 + " " + NoT + " times: " + i);

		}

		result += (double) smrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) smrspOld / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) smsrpOld / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) sNoBlocking / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) sfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) sfp / (double) TOTAL_NUMBER_OF_SYSTEMS + "\n";

		writeSystem((1 + " " + 1 + " " + NoT), result);
	}

	public static void experimentIncreasingParallelism(int NoP) {
		int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 2;
		double RESOURCE_SHARING_FACTOR = 0.4;
		int NoT = 5;

		long[][] Ris;
		MrsPNew new_mrsp = new MrsPNew();
		MrsPOriginal original_mrsp = new MrsPOriginal();
		MSRPOriginal msrp = new MSRPOriginal();
		RTAWithoutBlocking noblocking = new RTAWithoutBlocking();
		PWLPNew fp = new PWLPNew();
		MSRPNew fnp = new MSRPNew();

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, NoP, NoT * NoP, true, CS_LENGTH_RANGE.VERY_SHORT_CS_LEN,
				RESOURCES_RANGE.PARTITIONS, RESOURCE_SHARING_FACTOR, NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);

		String result = "";
		int smrsp = 0;
		int smrspOld = 0;
		int smsrpOld = 0;
		int sNoBlocking = 0;
		int sfnp = 0;
		int sfp = 0;

		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
			ArrayList<SporadicTask> tasksToAlloc = generator.generateTasks();
			ArrayList<Resource> resources = generator.generateResources();
			ArrayList<ArrayList<SporadicTask>> tasks = generator.generateResourceUsage(tasksToAlloc, resources);

			Ris = noblocking.getResponseTime(tasks, resources, false);
			if (isSystemSchedulable(tasks, Ris)) {
				sNoBlocking++;

				Ris = original_mrsp.getResponseTime(tasks, resources, false);
				if (isSystemSchedulable(tasks, Ris)) {
					smrspOld++;
					smrsp++;
				} else {
					Ris = new_mrsp.getResponseTime(tasks, resources, false);
					if (isSystemSchedulable(tasks, Ris))
						smrsp++;
				}

				Ris = msrp.getResponseTime(tasks, resources, false);
				if (isSystemSchedulable(tasks, Ris)) {
					smsrpOld++;
					sfnp++;
				} else {
					Ris = fnp.getResponseTime(tasks, resources, false);
					if (isSystemSchedulable(tasks, Ris))
						sfnp++;
				}

				Ris = fp.getResponseTime(tasks, resources, false);
				if (isSystemSchedulable(tasks, Ris))
					sfp++;

			}

			System.out.println(4 + "" + 1 + " " + NoP + " times: " + i);

		}

		result += (double) smrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) smrspOld / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) smsrpOld / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) sNoBlocking / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) sfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) sfp / (double) TOTAL_NUMBER_OF_SYSTEMS + "\n";

		writeSystem((4 + " " + 1 + " " + NoP), result);
	}

	public static void experimentIncreasingCriticalSectionLength(int cs_len) {
		int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 3;
		int NUMBER_OF_TASKS_ON_EACH_PARTITION = 4;
		double RESOURCE_SHARING_FACTOR = 0.4;

		final CS_LENGTH_RANGE cs_range;
		switch (cs_len) {
		case 1:
			cs_range = CS_LENGTH_RANGE.VERY_SHORT_CS_LEN;
			break;
		case 2:
			cs_range = CS_LENGTH_RANGE.SHORT_CS_LEN;
			break;
		case 3:
			cs_range = CS_LENGTH_RANGE.MEDIUM_CS_LEN;
			break;
		case 4:
			cs_range = CS_LENGTH_RANGE.LONG_CSLEN;
			break;
		case 5:
			cs_range = CS_LENGTH_RANGE.VERY_LONG_CSLEN;
			break;
		case 6:
			cs_range = CS_LENGTH_RANGE.Random;
			break;
		default:
			cs_range = null;
			break;
		}

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, TOTAL_PARTITIONS,
				NUMBER_OF_TASKS_ON_EACH_PARTITION * TOTAL_PARTITIONS, true, cs_range, RESOURCES_RANGE.PARTITIONS, RESOURCE_SHARING_FACTOR, NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);

		long[][] Ris;
		MSRPNew fnp = new MSRPNew();
		PWLPNew fp = new PWLPNew();
		MrsPNew mrsp = new MrsPNew();

		MSRPIO fnpIO = new MSRPIO();
		PWLPIO fpIO = new PWLPIO();
		MrsPIO mrspIO = new MrsPIO();

		String result = "";
		int sfnpIO = 0;
		int sfpIO = 0;
		int smrspIONP = 0;
		int smrspIO = 0;
		int sfnp = 0;
		int sfp = 0;
		int smrsp = 0;

		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
			ArrayList<SporadicTask> tasksToAlloc = generator.generateTasks();
			ArrayList<Resource> resources = generator.generateResources();
			ArrayList<ArrayList<SporadicTask>> tasks = generator.generateResourceUsage(tasksToAlloc, resources);

			Ris = fnpIO.getResponseTime(tasks, resources, false);
			if (isSystemSchedulable(tasks, Ris))
				sfnpIO++;

			Ris = fpIO.getResponseTime(tasks, resources, false);
			if (isSystemSchedulable(tasks, Ris))
				sfpIO++;

			Ris = mrspIO.getResponseTime(tasks, resources, true, true, false);
			if (isSystemSchedulable(tasks, Ris))
				smrspIONP++;

			Ris = mrspIO.getResponseTime(tasks, resources, true, false, false);
			if (isSystemSchedulable(tasks, Ris))
				smrspIO++;

			Ris = fnp.getResponseTime(tasks, resources, false);
			if (isSystemSchedulable(tasks, Ris))
				sfnp++;

			Ris = fp.getResponseTime(tasks, resources, false);
			if (isSystemSchedulable(tasks, Ris))
				sfp++;

			Ris = mrsp.getResponseTime(tasks, resources, false);
			if (isSystemSchedulable(tasks, Ris))
				smrsp++;

			System.out.println(2 + " " + 1 + " " + cs_len + " times: " + i);
		}

		result += (double) sfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) sfnpIO / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) sfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) sfpIO / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) smrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) smrspIO / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) smrspIONP / (double) TOTAL_NUMBER_OF_SYSTEMS + "\n";

		writeSystem(("ioa " + 2 + " " + 1 + " " + cs_len), result);
	}

	public static void experimentIncreasingContention(int smallSet) {
		double RESOURCE_SHARING_FACTOR = 0.4;
		int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 1 + 5 * (smallSet - 1);
		int NUMBER_OF_TASKS_ON_EACH_PARTITION = 5;

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, TOTAL_PARTITIONS,
				NUMBER_OF_TASKS_ON_EACH_PARTITION * TOTAL_PARTITIONS, true, CS_LENGTH_RANGE.VERY_SHORT_CS_LEN, RESOURCES_RANGE.PARTITIONS, RESOURCE_SHARING_FACTOR,
				NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);
		long[][] Ris;

		MrsPNew new_mrsp = new MrsPNew();
		MrsPOriginal original_mrsp = new MrsPOriginal();
		MSRPOriginal msrp = new MSRPOriginal();
		RTAWithoutBlocking noblocking = new RTAWithoutBlocking();
		FIFOLinearC fp = new FIFOLinearC();
		MSRPNew fnp = new MSRPNew();

		String result = "";

		int schedulableSystem_New_MrsP_Analysis2 = 0;
		int schedulableSystem_Original_MrsP_Analysis = 0;
		int schedulableSystem_MSRP_Analysis = 0;
		int schedulableSystem_No_Blocking = 0;
		int sfnp = 0;
		int sfp = 0;

		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {

			ArrayList<SporadicTask> tasksToAlloc = generator.generateTasks();
			ArrayList<Resource> resources = generator.generateResources();
			ArrayList<ArrayList<SporadicTask>> tasks = generator.generateResourceUsage(tasksToAlloc, resources);

			Ris = noblocking.getResponseTime(tasks, resources, false);
			if (isSystemSchedulable(tasks, Ris)) {
				schedulableSystem_No_Blocking++;

				Ris = original_mrsp.getResponseTime(tasks, resources, false);
				if (isSystemSchedulable(tasks, Ris)) {
					schedulableSystem_Original_MrsP_Analysis++;
					schedulableSystem_New_MrsP_Analysis2++;
				} else {
					Ris = new_mrsp.getResponseTime(tasks, resources, false);
					if (isSystemSchedulable(tasks, Ris))
						schedulableSystem_New_MrsP_Analysis2++;
				}

				Ris = msrp.getResponseTime(tasks, resources, false);
				if (isSystemSchedulable(tasks, Ris)) {
					schedulableSystem_MSRP_Analysis++;
					sfnp++;
				} else {
					Ris = fnp.getResponseTime(tasks, resources, false);
					if (isSystemSchedulable(tasks, Ris))
						sfnp++;
				}

				Ris = fp.getResponseTime(tasks, resources, true, false);
				if (isSystemSchedulable(tasks, Ris))
					sfp++;
			}

			System.out.println(3 + "" + 1 + " " + smallSet + " times: " + i);
		}

		result += (double) schedulableSystem_New_MrsP_Analysis2 / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) schedulableSystem_Original_MrsP_Analysis / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) schedulableSystem_MSRP_Analysis / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) schedulableSystem_No_Blocking / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) sfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) sfp / (double) TOTAL_NUMBER_OF_SYSTEMS + "\n";

		writeSystem((3 + " " + 1 + " " + smallSet), result);
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
