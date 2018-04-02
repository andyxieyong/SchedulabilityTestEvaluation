package evaluationSection3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import analysisNewIO.MSRPIO;
import analysisNewIO.MrsPIO;
import analysisNewIO.PWLPIO;
import entity.Resource;
import entity.SporadicTask;
import generatorTools.AllocationGeneator;
import generatorTools.SimpleSystemGenerator;
import utils.AnalysisUtils.CS_LENGTH_RANGE;
import utils.AnalysisUtils.RESOURCES_RANGE;
import utils.ResultReader;

public class dominance {

	public static int MAX_PERIOD = 1000;
	public static int MIN_PERIOD = 1;
	public static int TOTAL_NUMBER_OF_SYSTEMS = 1000;
	public static int TOTAL_PARTITIONS = 16;

	public static void main(String[] args) throws InterruptedException {
		dominance test = new dominance();

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
		double rsf = 0.3;
		int accesses = 3;
		int Not = 4;

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
		MSRPIO fnpIO = new MSRPIO();
		PWLPIO fpIO = new PWLPIO();
		MrsPIO mrspIO = new MrsPIO();

		String result = "";
		int sfnp = 0;
		int sfp = 0;
		int smrsp = 0;

		int fnpcanfpcannot = 0;
		int fnpcannotfpcan = 0;
		int fnpcanmrspcannot = 0;
		int fnpcannotmrspcan = 0;
		int fpcanmrspcannot = 0;
		int fpcannotmrspcan = 0;

		for (int count = 0; count < TOTAL_NUMBER_OF_SYSTEMS; count++) {
			ArrayList<SporadicTask> tasksToAlloc = generator.generateTasks();
			ArrayList<Resource> resources = generator.generateResources();
			generator.generateResourceUsage(tasksToAlloc, resources);

			boolean fnpOK = false, fpOK = false, mrspOK = false;
			for (int i = 0; i < 8; i++) {
				ArrayList<ArrayList<SporadicTask>> allocTask = new AllocationGeneator().allocateTasks(tasksToAlloc, resources, generator.total_partitions, i);

				if (!fnpOK) {
					Ris = fnpIO.getResponseTimeDM(allocTask, resources, true, true, false);
					if (isSystemSchedulable(allocTask, Ris)) {
						sfnp++;
						fnpOK = true;
					}
				}

				if (!fpOK) {
					Ris = fpIO.getResponseTimeDM(allocTask, resources, true, true, false);
					if (isSystemSchedulable(allocTask, Ris)) {
						sfp++;
						fpOK = true;
					}
				}

				if (!mrspOK) {
					Ris = mrspIO.getResponseTimeDM(allocTask, resources, true, true, true, true, false);
					if (isSystemSchedulable(allocTask, Ris)) {
						smrsp++;
						mrspOK = true;
					}
				}

				if (fnpOK && fpOK && mrspOK)
					break;
			}

			if (fnpOK && !fpOK) {
				fnpcanfpcannot++;
			}
			if (!fnpOK && fpOK) {
				fnpcannotfpcan++;
			}
			if (fnpOK && !mrspOK) {
				fnpcanmrspcannot++;
			}
			if (!fnpOK && mrspOK) {
				fnpcannotmrspcan++;
			}
			if (fpOK && !mrspOK) {
				fpcanmrspcannot++;
			}
			if (!fpOK && mrspOK) {
				fpcannotmrspcan++;
			}

			System.out.println(2 + " " + 1 + " " + cs_len + " times: " + count);
		}

		result += (double) sfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) sfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) smrsp / (double) TOTAL_NUMBER_OF_SYSTEMS;

		result += "    MSRPcan PWLPcannot " + fnpcanfpcannot + "    MSRPcannot PWLPcan " + fnpcannotfpcan + "    MSRPcan MrsPcannot " + fnpcanmrspcannot
				+ "    MSRPcannot MrsPcan " + fnpcannotmrspcan + "    PWLPcan MrsPcannot " + fpcanmrspcannot + "    PWLPcannot MrsPcan " + fpcannotmrspcan;

		writeSystem((2 + " " + 1 + " " + cs_len), result);
	}

	public boolean isSystemSchedulable(ArrayList<ArrayList<SporadicTask>> tasks, long[][] Ris) {
		if (tasks == null)
			return false;
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
