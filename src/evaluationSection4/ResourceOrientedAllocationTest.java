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
import entity.Resource;
import entity.SporadicTask;
import generatorTools.AllocationGeneator;
import generatorTools.SystemGenerator;
import utils.AnalysisUtils.CS_LENGTH_RANGE;
import utils.AnalysisUtils.RESOURCES_RANGE;
import utils.ResultReader;

public class ResourceOrientedAllocationTest {
	public static int TOTAL_NUMBER_OF_SYSTEMS = 1000;

	public static int MAX_PERIOD = 1000;
	public static int MIN_PERIOD = 1;
	public static int TOTAL_PARTITIONS = 16;
	public static boolean useRi = true;
	public static boolean btbHit = true;

	static CS_LENGTH_RANGE range = CS_LENGTH_RANGE.LONG_CSLEN;
	static int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 3;
	static int NUMBER_OF_TASKS_ON_EACH_PARTITION = 4;
	static double RESOURCE_SHARING_FACTOR = 0.3;

	AllocationGeneator allocGeneator = new AllocationGeneator();

	public static void main(String[] args) throws Exception {
		ResourceOrientedAllocationTest test = new ResourceOrientedAllocationTest();

		// test.experimentIncreasingCriticalSectionLength(3);

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

		// final CountDownLatch accesscountdown = new CountDownLatch(9);
		// for (int i = 1; i < 42; i = i + 5) {
		// final int access = i;
		// new Thread(new Runnable() {
		// @Override
		// public void run() {
		// test.experimentIncreasingContention(access);
		// accesscountdown.countDown();
		// }
		// }).start();
		// }
		// accesscountdown.await();

		// final CountDownLatch Taskcountdown = new CountDownLatch(9);
		// for (int i = 1; i < 10; i++) {
		// final int NoT = i;
		// new Thread(new Runnable() {
		// @Override
		// public void run() {
		// test.experimentIncreasingWorkLoad(NoT);
		// Taskcountdown.countDown();
		// }
		// }).start();
		// }
		// Taskcountdown.await();
		//
		// final CountDownLatch Processorcountdown = new CountDownLatch(11);
		// for (int i = 4; i < 25; i = i + 2) {
		// final int NoP = i;
		// new Thread(new Runnable() {
		// @Override
		// public void run() {
		// test.experimentIncreasingParallel(NoP);
		// Processorcountdown.countDown();
		// }
		// }).start();
		// }
		// Processorcountdown.await();

		ResultReader.schedreader();
	}

	public void experimentIncreasingCriticalSectionLength(int cs_len) {
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

		long[][] Ris;
		String result = "";
		int wfsfnp = 0, ffsfnp = 0, bfsfnp = 0, nfsfnp = 0, rrfsfnp = 0, rlfsfnp = 0, rldfsfnp = 0, rlifsfnp = 0;
		int wfsmrsp = 0, ffsmrsp = 0, bfsmrsp = 0, nfsmrsp = 0, rrfsmrsp = 0, rlfsmrsp = 0, rldfsmrsp = 0, rlifsmrsp = 0;
		int wfsfp = 0, ffsfp = 0, bfsfp = 0, nfsfp = 0, rrfsfp = 0, rlfsfp = 0, rldfsfp = 0, rlifsfp = 0;

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, true, TOTAL_PARTITIONS, NUMBER_OF_TASKS_ON_EACH_PARTITION * TOTAL_PARTITIONS,
				RESOURCE_SHARING_FACTOR, cs_range, RESOURCES_RANGE.PARTITIONS, NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE, false);

		MSRPIO fnp = new MSRPIO();
		MrsPIO mrsp = new MrsPIO();
		PWLPIO fp = new PWLPIO();

		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
			ArrayList<SporadicTask> tasksToAlloc = null;
			ArrayList<Resource> resources = null;
			while (tasksToAlloc == null) {
				tasksToAlloc = generator.generateTasks(true);
				resources = generator.generateResources();

				generator.generateResourceUsage(tasksToAlloc, resources);

				int allocOK = 0;

				for (int a = 0; a < 8; a++) {
					if (allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, a) != null)
						allocOK++;
				}

				if (allocOK != 8) {
					tasksToAlloc = null;
				}
			}

			/**
			 * WORST FIT
			 */
			ArrayList<ArrayList<SporadicTask>> tasksWF = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 0);
			Ris = fnp.getResponseTimeDM(tasksWF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksWF, Ris))
				wfsfnp++;

			Ris = fp.getResponseTimeDM(tasksWF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksWF, Ris))
				wfsfp++;

			Ris = mrsp.getResponseTimeDM(tasksWF, resources, true, true, btbHit, useRi, false);
			if (isSystemSchedulable(tasksWF, Ris))
				wfsmrsp++;

			/**
			 * BEST FIT
			 */
			ArrayList<ArrayList<SporadicTask>> tasksBF = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 1);

			Ris = fnp.getResponseTimeDM(tasksBF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksBF, Ris))
				bfsfnp++;

			Ris = fp.getResponseTimeDM(tasksBF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksBF, Ris))
				bfsfp++;

			Ris = mrsp.getResponseTimeDM(tasksBF, resources, true, true, btbHit, useRi, false);
			if (isSystemSchedulable(tasksBF, Ris))
				bfsmrsp++;

			/**
			 * FIRST FIT
			 */
			ArrayList<ArrayList<SporadicTask>> tasksFF = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 2);
			Ris = fnp.getResponseTimeDM(tasksFF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksFF, Ris))
				ffsfnp++;

			Ris = fp.getResponseTimeDM(tasksFF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksFF, Ris))
				ffsfp++;

			Ris = mrsp.getResponseTimeDM(tasksFF, resources, true, true, btbHit, useRi, false);
			if (isSystemSchedulable(tasksFF, Ris))
				ffsmrsp++;

			/**
			 * NEXT FIT
			 */
			ArrayList<ArrayList<SporadicTask>> tasksNF = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 3);
			Ris = fnp.getResponseTimeDM(tasksNF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksNF, Ris))
				nfsfnp++;

			Ris = fp.getResponseTimeDM(tasksNF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksNF, Ris))
				nfsfp++;

			Ris = mrsp.getResponseTimeDM(tasksNF, resources, true, true, btbHit, useRi, false);
			if (isSystemSchedulable(tasksNF, Ris))
				nfsmrsp++;

			/**
			 * RESOURCE LOCAL FIT
			 */

			ArrayList<ArrayList<SporadicTask>> tasksSPA = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 4);
			Ris = fnp.getResponseTimeDM(tasksSPA, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksSPA, Ris))
				rlfsfnp++;

			Ris = fp.getResponseTimeDM(tasksSPA, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksSPA, Ris))
				rlfsfp++;

			Ris = mrsp.getResponseTimeDM(tasksSPA, resources, true, true, btbHit, useRi, false);
			if (isSystemSchedulable(tasksSPA, Ris))
				rlfsmrsp++;

			/**
			 * RESOURCE REQUEST TASKS FIT
			 */

			ArrayList<ArrayList<SporadicTask>> tasksRCF = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 5);
			Ris = fnp.getResponseTimeDM(tasksRCF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksRCF, Ris))
				rrfsfnp++;

			Ris = fp.getResponseTimeDM(tasksRCF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksRCF, Ris))
				rrfsfp++;

			Ris = mrsp.getResponseTimeDM(tasksRCF, resources, true, true, btbHit, useRi, false);
			if (isSystemSchedulable(tasksRCF, Ris))
				rrfsmrsp++;

			/**
			 * RESOURCE LENGTH DECREASE FIT
			 */

			ArrayList<ArrayList<SporadicTask>> tasksRLFL = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 6);
			Ris = fnp.getResponseTimeDM(tasksRLFL, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksRLFL, Ris))
				rldfsfnp++;

			Ris = fp.getResponseTimeDM(tasksRLFL, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksRLFL, Ris))
				rldfsfp++;

			Ris = mrsp.getResponseTimeDM(tasksRLFL, resources, true, true, btbHit, useRi, false);
			if (isSystemSchedulable(tasksRLFL, Ris))
				rldfsmrsp++;

			/**
			 * RESOURCE LENGTH INCREASE FIT
			 */
			ArrayList<ArrayList<SporadicTask>> tasksRLFS = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 7);
			Ris = fnp.getResponseTimeDM(tasksRLFS, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksRLFS, Ris))
				rlifsfnp++;

			Ris = fp.getResponseTimeDM(tasksRLFS, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksRLFS, Ris))
				rlifsfp++;

			Ris = mrsp.getResponseTimeDM(tasksRLFS, resources, true, true, btbHit, useRi, false);
			if (isSystemSchedulable(tasksRLFS, Ris))
				rlifsmrsp++;

			System.out.println(2 + " " + 3 + " " + cs_len + " times: " + i);
		}

		result += "WF: " + (double) wfsfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) wfsfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) wfsmrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + "  ";

		result += "BF: " + (double) bfsfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) bfsfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) bfsmrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + "  ";

		result += "FF: " + (double) ffsfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) ffsfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) ffsmrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + "  ";

		result += "NF: " + (double) nfsfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) nfsfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) nfsmrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + "  ";

		result += "SPA: " + (double) rlfsfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) rlfsfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) rlfsmrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + "  ";

		result += "RCF: " + (double) rrfsfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) rrfsfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) rrfsmrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + "  ";

		result += "RLFL: " + (double) rldfsfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) rldfsfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) rldfsmrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + " ";

		result += "RLFS: " + (double) rlifsfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) rlifsfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) rlifsmrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + " ";

		writeSystem((2 + " " + 3 + " " + cs_len), result);
	}

	public void experimentIncreasingContention(int NoA) {
		long[][] Ris;
		String result = "";
		int wfsfnp = 0, ffsfnp = 0, bfsfnp = 0, nfsfnp = 0, rrfsfnp = 0, rlfsfnp = 0, rldfsfnp = 0, rlifsfnp = 0;
		int wfsmrsp = 0, ffsmrsp = 0, bfsmrsp = 0, nfsmrsp = 0, rrfsmrsp = 0, rlfsmrsp = 0, rldfsmrsp = 0, rlifsmrsp = 0;
		int wfsfp = 0, ffsfp = 0, bfsfp = 0, nfsfp = 0, rrfsfp = 0, rlfsfp = 0, rldfsfp = 0, rlifsfp = 0;

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, true, TOTAL_PARTITIONS, NUMBER_OF_TASKS_ON_EACH_PARTITION * TOTAL_PARTITIONS,
				RESOURCE_SHARING_FACTOR, range, RESOURCES_RANGE.PARTITIONS, NoA, false);

		MSRPIO fnp = new MSRPIO();
		PWLPIO fp = new PWLPIO();
		MrsPIO mrsp = new MrsPIO();

		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
			ArrayList<SporadicTask> tasksToAlloc = null;
			ArrayList<Resource> resources = null;
			while (tasksToAlloc == null) {
				tasksToAlloc = generator.generateTasks(true);
				resources = generator.generateResources();

				generator.generateResourceUsage(tasksToAlloc, resources);

				int allocOK = 0;

				for (int a = 0; a < 8; a++) {
					if (allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, a) != null)
						allocOK++;
				}

				if (allocOK != 8) {
					tasksToAlloc = null;
				}
			}

			/**
			 * WORST FIT
			 */
			ArrayList<ArrayList<SporadicTask>> tasksWF = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 0);
			Ris = fnp.getResponseTimeDM(tasksWF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksWF, Ris))
				wfsfnp++;

			Ris = fp.getResponseTimeDM(tasksWF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksWF, Ris))
				wfsfp++;

			Ris = mrsp.getResponseTimeDM(tasksWF, resources, true, true, btbHit, useRi, false);
			if (isSystemSchedulable(tasksWF, Ris))
				wfsmrsp++;

			/**
			 * BEST FIT
			 */
			ArrayList<ArrayList<SporadicTask>> tasksBF = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 1);

			Ris = fnp.getResponseTimeDM(tasksBF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksBF, Ris))
				bfsfnp++;

			Ris = fp.getResponseTimeDM(tasksBF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksBF, Ris))
				bfsfp++;

			Ris = mrsp.getResponseTimeDM(tasksBF, resources, true, true, btbHit, useRi, false);
			if (isSystemSchedulable(tasksBF, Ris))
				bfsmrsp++;

			/**
			 * FIRST FIT
			 */
			ArrayList<ArrayList<SporadicTask>> tasksFF = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 2);
			Ris = fnp.getResponseTimeDM(tasksFF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksFF, Ris))
				ffsfnp++;

			Ris = fp.getResponseTimeDM(tasksFF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksFF, Ris))
				ffsfp++;

			Ris = mrsp.getResponseTimeDM(tasksFF, resources, true, true, btbHit, useRi, false);
			if (isSystemSchedulable(tasksFF, Ris))
				ffsmrsp++;

			/**
			 * NEXT FIT
			 */
			ArrayList<ArrayList<SporadicTask>> tasksNF = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 3);
			Ris = fnp.getResponseTimeDM(tasksNF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksNF, Ris))
				nfsfnp++;

			Ris = fp.getResponseTimeDM(tasksNF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksNF, Ris))
				nfsfp++;

			Ris = mrsp.getResponseTimeDM(tasksNF, resources, true, true, btbHit, useRi, false);
			if (isSystemSchedulable(tasksNF, Ris))
				nfsmrsp++;

			/**
			 * RESOURCE LOCAL FIT
			 */

			ArrayList<ArrayList<SporadicTask>> tasksSPA = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 4);
			Ris = fnp.getResponseTimeDM(tasksSPA, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksSPA, Ris))
				rlfsfnp++;

			Ris = fp.getResponseTimeDM(tasksSPA, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksSPA, Ris))
				rlfsfp++;

			Ris = mrsp.getResponseTimeDM(tasksSPA, resources, true, true, btbHit, useRi, false);
			if (isSystemSchedulable(tasksSPA, Ris))
				rlfsmrsp++;

			/**
			 * RESOURCE REQUEST TASKS FIT
			 */

			ArrayList<ArrayList<SporadicTask>> tasksRCF = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 5);
			Ris = fnp.getResponseTimeDM(tasksRCF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksRCF, Ris))
				rrfsfnp++;

			Ris = fp.getResponseTimeDM(tasksRCF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksRCF, Ris))
				rrfsfp++;

			Ris = mrsp.getResponseTimeDM(tasksRCF, resources, true, true, btbHit, useRi, false);
			if (isSystemSchedulable(tasksRCF, Ris))
				rrfsmrsp++;

			/**
			 * RESOURCE LENGTH DECREASE FIT
			 */

			ArrayList<ArrayList<SporadicTask>> tasksRLFL = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 6);
			Ris = fnp.getResponseTimeDM(tasksRLFL, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksRLFL, Ris))
				rldfsfnp++;

			Ris = fp.getResponseTimeDM(tasksRLFL, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksRLFL, Ris))
				rldfsfp++;

			Ris = mrsp.getResponseTimeDM(tasksRLFL, resources, true, true, btbHit, useRi, false);
			if (isSystemSchedulable(tasksRLFL, Ris))
				rldfsmrsp++;

			/**
			 * RESOURCE LENGTH INCREASE FIT
			 */
			ArrayList<ArrayList<SporadicTask>> tasksRLFS = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 7);
			Ris = fnp.getResponseTimeDM(tasksRLFS, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksRLFS, Ris))
				rlifsfnp++;

			Ris = fp.getResponseTimeDM(tasksRLFS, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksRLFS, Ris))
				rlifsfp++;

			Ris = mrsp.getResponseTimeDM(tasksRLFS, resources, true, true, btbHit, useRi, false);
			if (isSystemSchedulable(tasksRLFS, Ris))
				rlifsmrsp++;

			System.out.println(3 + " " + 3 + " " + NoA + " times: " + i);
		}

		result += "WF: " + (double) wfsfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) wfsfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) wfsmrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + "  ";

		result += "BF: " + (double) bfsfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) bfsfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) bfsmrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + "  ";

		result += "FF: " + (double) ffsfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) ffsfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) ffsmrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + "  ";

		result += "NF: " + (double) nfsfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) nfsfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) nfsmrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + "  ";

		result += "SPA: " + (double) rlfsfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) rlfsfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) rlfsmrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + "  ";

		result += "RCF: " + (double) rrfsfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) rrfsfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) rrfsmrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + "  ";

		result += "RLFL: " + (double) rldfsfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) rldfsfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) rldfsmrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + " ";

		result += "RLFS: " + (double) rlifsfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) rlifsfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) rlifsmrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + " ";

		writeSystem((3 + " " + 3 + " " + NoA), result);
	}

	public void experimentIncreasingParallel(int NoP) {
		long[][] Ris;
		String result = "";
		int wfsfnp = 0, ffsfnp = 0, bfsfnp = 0, nfsfnp = 0, rrfsfnp = 0, rlfsfnp = 0, rldfsfnp = 0, rlifsfnp = 0;
		int wfsmrsp = 0, ffsmrsp = 0, bfsmrsp = 0, nfsmrsp = 0, rrfsmrsp = 0, rlfsmrsp = 0, rldfsmrsp = 0, rlifsmrsp = 0;
		int wfsfp = 0, ffsfp = 0, bfsfp = 0, nfsfp = 0, rrfsfp = 0, rlfsfp = 0, rldfsfp = 0, rlifsfp = 0;

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, true, NoP, NUMBER_OF_TASKS_ON_EACH_PARTITION * NoP, RESOURCE_SHARING_FACTOR,
				range, RESOURCES_RANGE.PARTITIONS, NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE, false);

		MSRPIO fnp = new MSRPIO();
		PWLPIO fp = new PWLPIO();
		MrsPIO mrsp = new MrsPIO();

		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
			ArrayList<SporadicTask> tasksToAlloc = null;
			ArrayList<Resource> resources = null;
			while (tasksToAlloc == null) {
				tasksToAlloc = generator.generateTasks(true);
				resources = generator.generateResources();

				generator.generateResourceUsage(tasksToAlloc, resources);

				int allocOK = 0;

				for (int a = 0; a < 8; a++) {
					if (allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, a) != null)
						allocOK++;
				}

				if (allocOK != 8) {
					tasksToAlloc = null;
				}
			}

			/**
			 * WORST FIT
			 */
			ArrayList<ArrayList<SporadicTask>> tasksWF = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 0);
			Ris = fnp.getResponseTimeDM(tasksWF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksWF, Ris))
				wfsfnp++;

			Ris = fp.getResponseTimeDM(tasksWF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksWF, Ris))
				wfsfp++;

			Ris = mrsp.getResponseTimeDM(tasksWF, resources, true, true, btbHit, useRi, false);
			if (isSystemSchedulable(tasksWF, Ris))
				wfsmrsp++;

			/**
			 * BEST FIT
			 */
			ArrayList<ArrayList<SporadicTask>> tasksBF = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 1);

			Ris = fnp.getResponseTimeDM(tasksBF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksBF, Ris))
				bfsfnp++;

			Ris = fp.getResponseTimeDM(tasksBF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksBF, Ris))
				bfsfp++;

			Ris = mrsp.getResponseTimeDM(tasksBF, resources, true, true, btbHit, useRi, false);
			if (isSystemSchedulable(tasksBF, Ris))
				bfsmrsp++;

			/**
			 * FIRST FIT
			 */
			ArrayList<ArrayList<SporadicTask>> tasksFF = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 2);
			Ris = fnp.getResponseTimeDM(tasksFF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksFF, Ris))
				ffsfnp++;

			Ris = fp.getResponseTimeDM(tasksFF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksFF, Ris))
				ffsfp++;

			Ris = mrsp.getResponseTimeDM(tasksFF, resources, true, true, btbHit, useRi, false);
			if (isSystemSchedulable(tasksFF, Ris))
				ffsmrsp++;

			/**
			 * NEXT FIT
			 */
			ArrayList<ArrayList<SporadicTask>> tasksNF = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 3);
			Ris = fnp.getResponseTimeDM(tasksNF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksNF, Ris))
				nfsfnp++;

			Ris = fp.getResponseTimeDM(tasksNF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksNF, Ris))
				nfsfp++;

			Ris = mrsp.getResponseTimeDM(tasksNF, resources, true, true, btbHit, useRi, false);
			if (isSystemSchedulable(tasksNF, Ris))
				nfsmrsp++;

			/**
			 * RESOURCE LOCAL FIT
			 */

			ArrayList<ArrayList<SporadicTask>> tasksSPA = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 4);
			Ris = fnp.getResponseTimeDM(tasksSPA, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksSPA, Ris))
				rlfsfnp++;

			Ris = fp.getResponseTimeDM(tasksSPA, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksSPA, Ris))
				rlfsfp++;

			Ris = mrsp.getResponseTimeDM(tasksSPA, resources, true, true, btbHit, useRi, false);
			if (isSystemSchedulable(tasksSPA, Ris))
				rlfsmrsp++;

			/**
			 * RESOURCE REQUEST TASKS FIT
			 */

			ArrayList<ArrayList<SporadicTask>> tasksRCF = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 5);
			Ris = fnp.getResponseTimeDM(tasksRCF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksRCF, Ris))
				rrfsfnp++;

			Ris = fp.getResponseTimeDM(tasksRCF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksRCF, Ris))
				rrfsfp++;

			Ris = mrsp.getResponseTimeDM(tasksRCF, resources, true, true, btbHit, useRi, false);
			if (isSystemSchedulable(tasksRCF, Ris))
				rrfsmrsp++;

			/**
			 * RESOURCE LENGTH DECREASE FIT
			 */

			ArrayList<ArrayList<SporadicTask>> tasksRLFL = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 6);
			Ris = fnp.getResponseTimeDM(tasksRLFL, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksRLFL, Ris))
				rldfsfnp++;

			Ris = fp.getResponseTimeDM(tasksRLFL, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksRLFL, Ris))
				rldfsfp++;

			Ris = mrsp.getResponseTimeDM(tasksRLFL, resources, true, true, btbHit, useRi, false);
			if (isSystemSchedulable(tasksRLFL, Ris))
				rldfsmrsp++;

			/**
			 * RESOURCE LENGTH INCREASE FIT
			 */
			ArrayList<ArrayList<SporadicTask>> tasksRLFS = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 7);
			Ris = fnp.getResponseTimeDM(tasksRLFS, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksRLFS, Ris))
				rlifsfnp++;

			Ris = fp.getResponseTimeDM(tasksRLFS, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksRLFS, Ris))
				rlifsfp++;

			Ris = mrsp.getResponseTimeDM(tasksRLFS, resources, true, true, btbHit, useRi, false);
			if (isSystemSchedulable(tasksRLFS, Ris))
				rlifsmrsp++;

			System.out.println(4 + " " + 3 + " " + NoP + " times: " + i);
		}

		result += "WF: " + (double) wfsfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) wfsfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) wfsmrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + "  ";

		result += "BF: " + (double) bfsfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) bfsfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) bfsmrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + "  ";

		result += "FF: " + (double) ffsfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) ffsfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) ffsmrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + "  ";

		result += "NF: " + (double) nfsfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) nfsfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) nfsmrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + "  ";

		result += "SPA: " + (double) rlfsfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) rlfsfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) rlfsmrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + "  ";

		result += "RCF: " + (double) rrfsfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) rrfsfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) rrfsmrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + "  ";

		result += "RLFL: " + (double) rldfsfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) rldfsfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) rldfsmrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + " ";

		result += "RLFS: " + (double) rlifsfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) rlifsfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) rlifsmrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + " ";

		writeSystem((4 + " " + 3 + " " + NoP), result);
	}

	public void experimentIncreasingWorkLoad(int NoT) {
		long[][] Ris;

		String result = "";
		int wfsfnp = 0, ffsfnp = 0, bfsfnp = 0, nfsfnp = 0, rrfsfnp = 0, rlfsfnp = 0, rldfsfnp = 0, rlifsfnp = 0;
		int wfsfp = 0, ffsfp = 0, bfsfp = 0, nfsfp = 0, rrfsfp = 0, rlfsfp = 0, rldfsfp = 0, rlifsfp = 0;
		int wfsmrsp = 0, ffsmrsp = 0, bfsmrsp = 0, nfsmrsp = 0, rrfsmrsp = 0, rlfsmrsp = 0, rldfsmrsp = 0, rlifsmrsp = 0;

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, true, TOTAL_PARTITIONS, NoT * TOTAL_PARTITIONS, RESOURCE_SHARING_FACTOR, range,
				RESOURCES_RANGE.PARTITIONS, NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE, false);

		MSRPIO fnp = new MSRPIO();
		PWLPIO fp = new PWLPIO();
		MrsPIO mrsp = new MrsPIO();

		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
			ArrayList<SporadicTask> tasksToAlloc = null;
			ArrayList<Resource> resources = null;
			while (tasksToAlloc == null) {
				tasksToAlloc = generator.generateTasks(true);
				resources = generator.generateResources();

				generator.generateResourceUsage(tasksToAlloc, resources);

				int allocOK = 0;

				for (int a = 0; a < 8; a++) {
					if (allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, a) != null)
						allocOK++;
				}

				if (allocOK != 8) {
					tasksToAlloc = null;
				}
			}

			/**
			 * WORST FIT
			 */
			ArrayList<ArrayList<SporadicTask>> tasksWF = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 0);
			Ris = fnp.getResponseTimeDM(tasksWF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksWF, Ris))
				wfsfnp++;

			Ris = fp.getResponseTimeDM(tasksWF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksWF, Ris))
				wfsfp++;

			Ris = mrsp.getResponseTimeDM(tasksWF, resources, true, true, btbHit, useRi, false);
			if (isSystemSchedulable(tasksWF, Ris))
				wfsmrsp++;

			/**
			 * BEST FIT
			 */
			ArrayList<ArrayList<SporadicTask>> tasksBF = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 1);

			Ris = fnp.getResponseTimeDM(tasksBF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksBF, Ris))
				bfsfnp++;

			Ris = fp.getResponseTimeDM(tasksBF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksBF, Ris))
				bfsfp++;

			Ris = mrsp.getResponseTimeDM(tasksBF, resources, true, true, btbHit, useRi, false);
			if (isSystemSchedulable(tasksBF, Ris))
				bfsmrsp++;

			/**
			 * FIRST FIT
			 */
			ArrayList<ArrayList<SporadicTask>> tasksFF = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 2);
			Ris = fnp.getResponseTimeDM(tasksFF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksFF, Ris))
				ffsfnp++;

			Ris = fp.getResponseTimeDM(tasksFF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksFF, Ris))
				ffsfp++;

			Ris = mrsp.getResponseTimeDM(tasksFF, resources, true, true, btbHit, useRi, false);
			if (isSystemSchedulable(tasksFF, Ris))
				ffsmrsp++;

			/**
			 * NEXT FIT
			 */
			ArrayList<ArrayList<SporadicTask>> tasksNF = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 3);
			Ris = fnp.getResponseTimeDM(tasksNF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksNF, Ris))
				nfsfnp++;

			Ris = fp.getResponseTimeDM(tasksNF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksNF, Ris))
				nfsfp++;

			Ris = mrsp.getResponseTimeDM(tasksNF, resources, true, true, btbHit, useRi, false);
			if (isSystemSchedulable(tasksNF, Ris))
				nfsmrsp++;

			/**
			 * RESOURCE LOCAL FIT
			 */

			ArrayList<ArrayList<SporadicTask>> tasksSPA = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 4);
			Ris = fnp.getResponseTimeDM(tasksSPA, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksSPA, Ris))
				rlfsfnp++;

			Ris = fp.getResponseTimeDM(tasksSPA, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksSPA, Ris))
				rlfsfp++;

			Ris = mrsp.getResponseTimeDM(tasksSPA, resources, true, true, btbHit, useRi, false);
			if (isSystemSchedulable(tasksSPA, Ris))
				rlfsmrsp++;

			/**
			 * RESOURCE REQUEST TASKS FIT
			 */

			ArrayList<ArrayList<SporadicTask>> tasksRCF = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 5);
			Ris = fnp.getResponseTimeDM(tasksRCF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksRCF, Ris))
				rrfsfnp++;

			Ris = fp.getResponseTimeDM(tasksRCF, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksRCF, Ris))
				rrfsfp++;

			Ris = mrsp.getResponseTimeDM(tasksRCF, resources, true, true, btbHit, useRi, false);
			if (isSystemSchedulable(tasksRCF, Ris))
				rrfsmrsp++;

			/**
			 * RESOURCE LENGTH DECREASE FIT
			 */

			ArrayList<ArrayList<SporadicTask>> tasksRLFL = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 6);
			Ris = fnp.getResponseTimeDM(tasksRLFL, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksRLFL, Ris))
				rldfsfnp++;

			Ris = fp.getResponseTimeDM(tasksRLFL, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksRLFL, Ris))
				rldfsfp++;

			Ris = mrsp.getResponseTimeDM(tasksRLFL, resources, true, true, btbHit, useRi, false);
			if (isSystemSchedulable(tasksRLFL, Ris))
				rldfsmrsp++;

			/**
			 * RESOURCE LENGTH INCREASE FIT
			 */
			ArrayList<ArrayList<SporadicTask>> tasksRLFS = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 7);
			Ris = fnp.getResponseTimeDM(tasksRLFS, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksRLFS, Ris))
				rlifsfnp++;

			Ris = fp.getResponseTimeDM(tasksRLFS, resources, btbHit, useRi, false);
			if (isSystemSchedulable(tasksRLFS, Ris))
				rlifsfp++;

			Ris = mrsp.getResponseTimeDM(tasksRLFS, resources, true, true, btbHit, useRi, false);
			if (isSystemSchedulable(tasksRLFS, Ris))
				rlifsmrsp++;

			System.out.println(1 + " " + 3 + " " + NoT + " times: " + i);
		}

		result += "WF: " + (double) wfsfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) wfsfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) wfsmrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + "  ";

		result += "BF: " + (double) bfsfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) bfsfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) bfsmrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + "  ";

		result += "FF: " + (double) ffsfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) ffsfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) ffsmrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + "  ";

		result += "NF: " + (double) nfsfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) nfsfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) nfsmrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + "  ";

		result += "SPA: " + (double) rlfsfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) rlfsfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) rlfsmrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + "  ";

		result += "RCF: " + (double) rrfsfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) rrfsfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) rrfsmrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + "  ";

		result += "RLFL: " + (double) rldfsfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) rldfsfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) rldfsmrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + " ";

		result += "RLFS: " + (double) rlifsfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) rlifsfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) rlifsmrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + " ";

		writeSystem((1 + " " + 3 + " " + NoT), result);
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
