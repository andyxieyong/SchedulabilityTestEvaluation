package evaluationSection3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import analysisNew.MSRPNew;
import analysisNew.MrsPNew;
import analysisNew.PWLPNew;
import analysisNewIO.MSRPIO;
import analysisNewIO.MrsPIO;
import analysisNewIO.PWLPIO;
import entity.Resource;
import entity.SporadicTask;
import generatorTools.SimpleSystemGenerator;
import utils.AnalysisUtils.CS_LENGTH_RANGE;
import utils.AnalysisUtils.RESOURCES_RANGE;
import utils.ResultReader;

public class RunTimeOverheadsTest {

	public static int MAX_PERIOD = 1000;
	public static int MIN_PERIOD = 1;
	public static int TOTAL_NUMBER_OF_SYSTEMS = 1000;
	public static int TOTAL_PARTITIONS = 16;

	public static void main(String[] args) throws InterruptedException {
		RunTimeOverheadsTest test = new RunTimeOverheadsTest();

		final CountDownLatch cslencountdown = new CountDownLatch(6);
		for (int i = 1; i < 7; i++) {
			final int cslen = i;
			new Thread(new Runnable() {
				@Override
				public void run() {
					test.experimentIncreasingCriticalSectionLength(cslen);
					cslencountdown.countDown();
				}
			}).start();
		}

		cslencountdown.await();
		ResultReader.schedreader();

	}

	public void experimentIncreasingCriticalSectionLength(int cs_len) {
		double rsf = 0.4;
		int accesses = 3;
		int Not = 3;

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

		SimpleSystemGenerator generator = new SimpleSystemGenerator(MIN_PERIOD, MAX_PERIOD, TOTAL_PARTITIONS, Not * TOTAL_PARTITIONS, true, cs_range,
				RESOURCES_RANGE.PARTITIONS, rsf, accesses);

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

			Ris = fnpIO.getResponseTimeDM(tasks, resources, true, true, false);
			if (isSystemSchedulable(tasks, Ris))
				sfnpIO++;

			Ris = fpIO.getResponseTimeDM(tasks, resources, true, true, false);
			if (isSystemSchedulable(tasks, Ris))
				sfpIO++;

			Ris = mrspIO.getResponseTimeDM(tasks, resources, true, true, true, true, false);
			if (isSystemSchedulable(tasks, Ris))
				smrspIONP++;

			Ris = mrspIO.getResponseTimeDM(tasks, resources, true, false, true, true, false);
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

		writeSystem((2 + " " + 1 + " " + cs_len), result);
	}

	public boolean isSystemSchedulable(ArrayList<ArrayList<SporadicTask>> tasks, long[][] Ris) {
		for (int i = 0; i < tasks.size(); i++) {
			for (int j = 0; j < tasks.get(i).size(); j++) {
				if (tasks.get(i).get(j).deadline < Ris[i][j])
					return false;
			}
		}
		return true;
	}

	public void writeSystem(String filename, String result) {
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
