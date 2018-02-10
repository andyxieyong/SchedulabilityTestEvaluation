package evaluationSection3;

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
import generatorTools.SimpleSystemGenerator;
import utils.AnalysisUtils.CS_LENGTH_RANGE;
import utils.AnalysisUtils.RESOURCES_RANGE;
import utils.ResultReader;

public class BTBTest {

	public static int TOTAL_NUMBER_OF_SYSTEMS = 1000;
	public static int TOTAL_PARTITIONS = 16;
	public static int MIN_PERIOD = 1;
	public static int MAX_PERIOD = 1000;

	public static void main(String[] args) throws InterruptedException {

		for (int i = 1; i < 42; i = i + 5)
			experimentIncreasingContention(i);

		ResultReader.schedreader();
	}

	public static void experimentIncreasingWorkLoad(int NoT) {
		int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 2;
		double RESOURCE_SHARING_FACTOR = 0.4;

		long[][] Ris;
		MrsPNew new_mrsp = new MrsPNew();
		PWLPNew fp = new PWLPNew();
		MSRPNew fnp = new MSRPNew();

		SimpleSystemGenerator generator = new SimpleSystemGenerator(MIN_PERIOD, MAX_PERIOD, TOTAL_PARTITIONS, NoT * TOTAL_PARTITIONS, true,
				CS_LENGTH_RANGE.SHORT_CS_LEN, RESOURCES_RANGE.PARTITIONS, RESOURCE_SHARING_FACTOR, NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);

		String result = "";
		int smrsp = 0;
		int sfnp = 0;
		int sfp = 0;
		int smrsp1 = 0;
		int sfnp1 = 0;
		int sfp1 = 0;

		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
			ArrayList<SporadicTask> tasksToAlloc = generator.generateTasks();
			ArrayList<Resource> resources = generator.generateResources();
			ArrayList<ArrayList<SporadicTask>> tasks = generator.generateResourceUsage(tasksToAlloc, resources);

			Ris = new_mrsp.getResponseTime(tasks, resources, false);
			if (isSystemSchedulable(tasks, Ris))
				smrsp++;

			Ris = fp.getResponseTime(tasks, resources, false);
			if (isSystemSchedulable(tasks, Ris))
				sfp++;

			Ris = fnp.getResponseTime(tasks, resources, false);
			if (isSystemSchedulable(tasks, Ris))
				sfnp++;

			System.out.println(1 + " " + 1 + " " + NoT + " times: " + i);

		}

		result += (double) sfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) sfnp1 / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) sfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) sfp1 / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) smrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) smrsp1 / (double) TOTAL_NUMBER_OF_SYSTEMS + "\n";

		writeSystem((1 + " " + 1 + " " + NoT), result);
	}

	public static void experimentIncreasingCriticalSectionLength(int cs_len) {
		int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 2;
		int NUMBER_OF_TASKS_ON_EACH_PARTITION = 3;
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

		SimpleSystemGenerator generator = new SimpleSystemGenerator(MIN_PERIOD, MAX_PERIOD, TOTAL_PARTITIONS,
				NUMBER_OF_TASKS_ON_EACH_PARTITION * TOTAL_PARTITIONS, true, cs_range, RESOURCES_RANGE.PARTITIONS, RESOURCE_SHARING_FACTOR,
				NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);

		long[][] Ris;
		MrsPNew new_mrsp = new MrsPNew();
		MrsPOriginal original_mrsp = new MrsPOriginal();
		MSRPOriginal msrp = new MSRPOriginal();
		RTAWithoutBlocking noblocking = new RTAWithoutBlocking();
		PWLPNew fp = new PWLPNew();
		MSRPNew fnp = new MSRPNew();

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

			System.out.println(2 + " " + 1 + " " + cs_len + " times: " + i);
		}

		result += (double) sfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) sfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) smrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) smsrpOld / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) smrspOld / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) sNoBlocking / (double) TOTAL_NUMBER_OF_SYSTEMS + "\n";

		writeSystem((2 + " " + 1 + " " + cs_len), result);
	}

	public static void experimentIncreasingContention(int NoA) {
		double RESOURCE_SHARING_FACTOR = 0.4;
		int NUMBER_OF_TASKS_ON_EACH_PARTITION = 4;

		SimpleSystemGenerator generator = new SimpleSystemGenerator(MIN_PERIOD, MAX_PERIOD, TOTAL_PARTITIONS,
				NUMBER_OF_TASKS_ON_EACH_PARTITION * TOTAL_PARTITIONS, true, CS_LENGTH_RANGE.SHORT_CS_LEN, RESOURCES_RANGE.PARTITIONS, RESOURCE_SHARING_FACTOR,
				NoA);
		long[][] Ris;

		MrsPNew new_mrsp = new MrsPNew();
		PWLPNew fp = new PWLPNew();
		MSRPNew fnp = new MSRPNew();

		String result = "";
		int smrsp = 0;
		int sfnp = 0;
		int sfp = 0;
		int smrsp1 = 0;
		int sfnp1 = 0;
		int sfp1 = 0;

		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
			ArrayList<SporadicTask> tasksToAlloc = generator.generateTasks();
			ArrayList<Resource> resources = generator.generateResources();
			ArrayList<ArrayList<SporadicTask>> tasks = generator.generateResourceUsage(tasksToAlloc, resources);

			Ris = new_mrsp.getResponseTime(tasks, resources, false);
			if (isSystemSchedulable(tasks, Ris))
				smrsp++;

			Ris = fp.getResponseTime(tasks, resources, false);
			if (isSystemSchedulable(tasks, Ris))
				sfp++;

			Ris = fnp.getResponseTime(tasks, resources, false);
			if (isSystemSchedulable(tasks, Ris))
				sfnp++;

			Ris = new_mrsp.getResponseTime(tasks, resources, false, false);
			if (isSystemSchedulable(tasks, Ris))
				smrsp1++;

			Ris = fp.getResponseTime(tasks, resources, false, false);
			if (isSystemSchedulable(tasks, Ris))
				sfp1++;

			Ris = fnp.getResponseTime(tasks, resources, false, false);
			if (isSystemSchedulable(tasks, Ris))
				sfnp1++;

			System.out.println(3 + " " + 1 + " " + NoA + " times: " + i);
		}

		result += (double) sfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) sfnp1 / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) sfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) sfp1 / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) smrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) smrsp1 / (double) TOTAL_NUMBER_OF_SYSTEMS + "\n";

		writeSystem((3 + " " + 1 + " " + NoA), result);
	}

	public static void experimentIncreasingParallelism(int NoP) {
		int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 3;
		double RESOURCE_SHARING_FACTOR = 0.4;
		int NoT = 4;

		long[][] Ris;
		MrsPNew new_mrsp = new MrsPNew();
		MrsPOriginal original_mrsp = new MrsPOriginal();
		MSRPOriginal msrp = new MSRPOriginal();
		RTAWithoutBlocking noblocking = new RTAWithoutBlocking();
		PWLPNew fp = new PWLPNew();
		MSRPNew fnp = new MSRPNew();

		SimpleSystemGenerator generator = new SimpleSystemGenerator(MIN_PERIOD, MAX_PERIOD, NoP, NoT * NoP, true, CS_LENGTH_RANGE.SHORT_CS_LEN,
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

			System.out.println(4 + " " + 1 + " " + NoP + " times: " + i);

		}

		result += (double) sfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) sfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) smrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) smsrpOld / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) smrspOld / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) sNoBlocking / (double) TOTAL_NUMBER_OF_SYSTEMS + "\n";

		writeSystem((4 + " " + 1 + " " + NoP), result);
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
