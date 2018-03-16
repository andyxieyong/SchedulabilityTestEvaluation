package evaluationSection4;

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
import analysisNewIO.RuntimeCostAnalysis;
import entity.Resource;
import entity.SporadicTask;
import generatorTools.AllocationGeneator;
import generatorTools.SystemGenerator;
import utils.AnalysisUtils.CS_LENGTH_RANGE;
import utils.AnalysisUtils.RESOURCES_RANGE;
import utils.ResultReader;

public class PriorityOrderingTest {
	public static int MAX_PERIOD = 1000;
	public static int MIN_PERIOD = 1;
	public static int TOTAL_NUMBER_OF_SYSTEMS = 1000;
	public static int TOTAL_PARTITIONS = 16;

	public static void main(String[] args) throws Exception {
		PriorityOrderingTest test = new PriorityOrderingTest();

		MSRPIO msrp = new MSRPIO();
		PWLPIO pwlp = new PWLPIO();
		MrsPIO mrsp = new MrsPIO();

		final CountDownLatch msrpwork = new CountDownLatch(6);
		for (int i = 1; i < 7; i++) {
			final int cslen = i;
			new Thread(new Runnable() {
				@Override
				public void run() {
					test.PriorityOrder(msrp, cslen, "MSRP");
					msrpwork.countDown();
				}
			}).start();
		}
		msrpwork.await();

		final CountDownLatch pwlpwork = new CountDownLatch(6);
		for (int i = 1; i < 7; i++) {
			final int cslen = i;
			new Thread(new Runnable() {
				@Override
				public void run() {
					test.PriorityOrder(pwlp, cslen, "PWLP");
					pwlpwork.countDown();
				}
			}).start();
		}
		pwlpwork.await();

		final CountDownLatch mrspwork = new CountDownLatch(6);
		for (int i = 1; i < 2; i++) {
			final int cslen = i;
			new Thread(new Runnable() {
				@Override
				public void run() {
					test.PriorityOrder(mrsp, cslen, "MrsP");
					mrspwork.countDown();
				}
			}).start();
		}
		mrspwork.await();

		ResultReader.priorityReader();

		// for (int i = 0; i < 9999999; i++) {
		// test.experimentIncreasingCriticalSectionLength(msrp, 5, "MSRP");
		// test.experimentIncreasingCriticalSectionLength(pwlp, 5, "PWLP");
		// test.experimentIncreasingCriticalSectionLength(mrsp, 5, "MrsP");
		//
		// test.experimentIncreasingCriticalSectionLength(msrp, 6, "MSRP");
		// test.experimentIncreasingCriticalSectionLength(pwlp, 6, "PWLP");
		// test.experimentIncreasingCriticalSectionLength(mrsp, 6, "MrsP");
		// System.out.println(i);
		// }

	}

	public void PriorityOrder(RuntimeCostAnalysis analysis, int cs_len, String name) {
		int NoA = 2;
		int NoT = 3;
		double Rsf = 0.4;

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

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, true, TOTAL_PARTITIONS, TOTAL_PARTITIONS * NoT, Rsf, cs_range,
				RESOURCES_RANGE.PARTITIONS, NoA, false);

		long[][] Ris;
		String result = "";
		int DM = 0;
		int OPA = 0;
		int RPA = 0;
		int SBPO = 0;

		int DMcannotOPAcan = 0;
		int DMcanOPAcannot = 0;

		int DMcannotRPAcan = 0;
		int DMcanRPAcannot = 0;

		int DMcannotSBPOcan = 0;
		int DMcanSBPOcannot = 0;

		int OPAcanRPAcannot = 0;
		int OPAcannotRPAcan = 0;

		int OPAcanSBPOcannot = 0;
		int OPAcannotSBPOcan = 0;

		int RPAcanSBPOcannot = 0;
		int RPAcannotSBPOcan = 0;

		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
			ArrayList<SporadicTask> tasksToAlloc = generator.generateTasks(true);
			ArrayList<Resource> resources = generator.generateResources();
			generator.generateResourceUsage(tasksToAlloc, resources);
			ArrayList<ArrayList<SporadicTask>> tasks = new AllocationGeneator().allocateTasks(tasksToAlloc, resources, generator.total_partitions, 0);

			boolean DMok = false, OPAok = false, RPAok = false, SBPOok = false;
			Ris = analysis.getResponseTimeDM(tasks, resources, true, true, false);
			if (isSystemSchedulable(tasks, Ris)) {
				DM++;
				DMok = true;
			}

			Ris = analysis.getResponseTimeRPA(tasks, resources, false);
			if (isSystemSchedulable(tasks, Ris)) {
				RPA++;
				RPAok = true;
			}

			Ris = analysis.getResponseTimeOPA(tasks, resources, false);
			if (isSystemSchedulable(tasks, Ris)) {
				OPA++;
				OPAok = true;
			}

			Ris = analysis.getResponseTimeSBPO(tasks, resources, false);
			if (isSystemSchedulable(tasks, Ris)) {
				SBPO++;
				SBPOok = true;
			}

			if (!DMok && OPAok)
				DMcannotOPAcan++;

			if (DMok && !OPAok)
				DMcanOPAcannot++;

			if (!DMok && RPAok)
				DMcannotRPAcan++;

			if (DMok && !RPAok)
				DMcanRPAcannot++;

			if (!DMok && SBPOok)
				DMcannotSBPOcan++;

			if (DMok && !SBPOok)
				DMcanSBPOcannot++;

			if (OPAok && !RPAok) {
				OPAcanRPAcannot++;
			}

			if (!OPAok && RPAok)
				OPAcannotRPAcan++;

			if (OPAok && !SBPOok) {
				OPAcanSBPOcannot++;
			}

			if (!OPAok && SBPOok)
				OPAcannotSBPOcan++;

			if (RPAok && !SBPOok) {
				RPAcanSBPOcannot++;
			}

			if (!RPAok && SBPOok)
				RPAcannotSBPOcan++;

			System.out.println(name + " " + 2 + " " + 4 + " " + cs_len + " times: " + i);

		}

		result = name + "   DM: " + (double) DM / (double) TOTAL_NUMBER_OF_SYSTEMS + "    OPA: " + (double) OPA / (double) TOTAL_NUMBER_OF_SYSTEMS + "    RPA: "
				+ (double) RPA / (double) TOTAL_NUMBER_OF_SYSTEMS + "    SBPO: " + (double) SBPO / (double) TOTAL_NUMBER_OF_SYSTEMS;

		result += "    DMcannotOPAcan: " + DMcannotOPAcan + "    DMcanOPAcannot: " + DMcanOPAcannot + "    DMcannotRPAcan: " + DMcannotRPAcan
				+ "    DMcanRPAcannot: " + DMcanRPAcannot + "    DMcannotSBPOcan: " + DMcannotSBPOcan + "    DMcanSBPOcannot: " + DMcanSBPOcannot
				+ "    OPAcanRPAcannot: " + OPAcanRPAcannot + "    OPAcannotRPAcan: " + OPAcannotRPAcan + "   OPAcanSBPOcannot: " + OPAcanSBPOcannot
				+ "   OPAcannotSBPOcan: " + OPAcannotSBPOcan + "   RPAcanSBPOcannot: " + RPAcanSBPOcannot + "   RPAcannotSBPOcan: " + RPAcannotSBPOcan + "\n";

		writeSystem((name + " " + 2 + " " + 4 + " " + cs_len), result);
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
