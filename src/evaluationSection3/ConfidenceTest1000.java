package evaluationSection3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import analysisBasic.MSRPOriginal;
import analysisBasic.MrsPOriginal;
import analysisBasic.RTAWithoutBlocking;
import analysisNew.MSRPNew;
import analysisNew.MrsPNew;
import analysisNew.PWLPNew;
import analysisNewIO.MSRPIO;
import analysisNewIO.MrsPIO;
import analysisNewIO.PWLPIO;
import analysisNewIO.RuntimeCostAnalysis;
import entity.Resource;
import entity.SporadicTask;
import generatorTools.AllocationGeneator;
import generatorTools.SimpleSystemGenerator;
import generatorTools.SystemGenerator;
import utils.AnalysisUtils.CS_LENGTH_RANGE;
import utils.AnalysisUtils.RESOURCES_RANGE;

public class ConfidenceTest1000 {

	public static int NUMBER_OF_TIMES = 1000;
	public static int TOTAL_NUMBER_OF_SYSTEMS = 1000;

	public static int TOTAL_PARTITIONS = 16;
	public static int MIN_PERIOD = 1;
	public static int MAX_PERIOD = 1000;

	AllocationGeneator allocGeneator = new AllocationGeneator();

	public static void main(String[] args) throws InterruptedException {
		ConfidenceTest1000 test = new ConfidenceTest1000();
		final CountDownLatch downLatch = new CountDownLatch(12);

		// Thread t1 = new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		// test.experimentIncreasingWorkLoad(4);
		// downLatch.countDown();
		// }
		// });
		// Thread t2 = new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		// test.experimentIncreasingWorkLoad(6);
		// downLatch.countDown();
		// }
		// });
		//
		// Thread l1 = new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		// test.experimentIncreasingCriticalSectionLength(3);
		// downLatch.countDown();
		// }
		// });
		// Thread l2 = new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		// test.experimentIncreasingCriticalSectionLength(5);
		// downLatch.countDown();
		// }
		// });
		//
		// Thread n1 = new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		// test.experimentIncreasingContention(21);
		// downLatch.countDown();
		// }
		// });
		// Thread n2 = new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		// test.experimentIncreasingContention(31);
		// downLatch.countDown();
		// }
		// });
		//
		// Thread p1 = new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		// test.experimentIncreasingParallelism(20);
		// downLatch.countDown();
		// }
		// });
		// Thread p2 = new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		// test.experimentIncreasingParallelism(12);
		// downLatch.countDown();
		// }
		// });

		// Thread btb1 = new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		// test.BTBH(11);
		// downLatch.countDown();
		// }
		// });
		// Thread btb2 = new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		// test.BTBH(26);
		// downLatch.countDown();
		// }
		// });
		//
		// Thread overheads1 = new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		// test.overheads(2);
		// downLatch.countDown();
		// }
		// });
		//
		// Thread overheads2 = new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		// test.overheads(4);
		// downLatch.countDown();
		// }
		// });

		// t1.start();
		// t2.start();
		//
		// l1.start();
		// l2.start();
		//
		// n1.start();
		// n2.start();
		//
		// p1.start();
		// p2.start();

		// btb1.start();
		// btb2.start();
		//
		// overheads1.start();
		// overheads2.start();

		// new Thread(new Runnable() {
		// @Override
		// public void run() {
		// test.allocationcslenmsrp(3);
		// downLatch.countDown();
		// }
		// }).start();
		//
		// new Thread(new Runnable() {
		// @Override
		// public void run() {
		// test.allocationcslenmsrp(5);
		// downLatch.countDown();
		// }
		// }).start();

//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				test.taskallocationaccessmsrp(6);
//				downLatch.countDown();
//			}
//		}).start();
//
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				test.taskallocationaccessmsrp(16);
//				downLatch.countDown();
//			}
//		}).start();

		// MSRPIO msrp = new MSRPIO();
		PWLPIO pwlp = new PWLPIO();

		// new Thread(new Runnable() {
		// @Override
		// public void run() {
		// test.PriorityOrder(msrp, 1, "MSRP");
		// downLatch.countDown();
		// }
		// }).start();
		//
		// new Thread(new Runnable() {
		// @Override
		// public void run() {
		// test.PriorityOrder(msrp, 6, "MSRP");
		// downLatch.countDown();
		// }
		// }).start();

//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				test.PriorityOrder(pwlp, 2, "PWLP");
//				downLatch.countDown();
//			}
//		}).start();

		new Thread(new Runnable() {
			@Override
			public void run() {
				test.PriorityOrder(pwlp, 5, "PWLP");
				downLatch.countDown();
			}
		}).start();

		downLatch.await();
		System.out.println("Down");
	}

	public void experimentIncreasingWorkLoad(int NoT) {
		int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 2;
		double RESOURCE_SHARING_FACTOR = 0.4;

		long[][] Ris;
		MrsPNew new_mrsp = new MrsPNew();
		MrsPOriginal original_mrsp = new MrsPOriginal();
		MSRPOriginal msrp = new MSRPOriginal();
		RTAWithoutBlocking noblocking = new RTAWithoutBlocking();
		PWLPNew fp = new PWLPNew();
		MSRPNew fnp = new MSRPNew();

		SimpleSystemGenerator generator = new SimpleSystemGenerator(MIN_PERIOD, MAX_PERIOD, TOTAL_PARTITIONS, NoT * TOTAL_PARTITIONS, true,
				CS_LENGTH_RANGE.SHORT_CS_LEN, RESOURCES_RANGE.PARTITIONS, RESOURCE_SHARING_FACTOR, NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);

		String result = "";
		int smrsp = 0;
		int smrspOld = 0;
		int smsrpOld = 0;
		int sNoBlocking = 0;
		int sfnp = 0;
		int sfp = 0;

		for (int j = 0; j < NUMBER_OF_TIMES; j++) {
			smrsp = smrspOld = smsrpOld = sNoBlocking = sfnp = sfp = 0;

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

				System.out.println(1 + " " + 1 + " " + NoT + " times: " + j + " numbers: " + i);

			}

			// result += (double) sfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
			// + (double) sfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
			// + (double) smrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " +
			// (double) smsrpOld / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
			// + (double) smrspOld / (double) TOTAL_NUMBER_OF_SYSTEMS + " " +
			// (double) sNoBlocking / (double) TOTAL_NUMBER_OF_SYSTEMS + "\n";

			result += sfnp + " " + sfp + " " + smrsp + " " + smsrpOld + " " + smrspOld + " " + sNoBlocking + "\n";
		}

		writeSystem((1 + " " + 1 + " " + NoT), result);
	}

	public void experimentIncreasingCriticalSectionLength(int cs_len) {
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

		for (int j = 0; j < NUMBER_OF_TIMES; j++) {
			smrsp = smrspOld = smsrpOld = sNoBlocking = sfnp = sfp = 0;
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

				System.out.println(2 + " " + 1 + " " + cs_len + " times: " + j + " numbers: " + i);
			}

			// result += (double) sfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
			// + (double) sfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
			// + (double) smrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " +
			// (double) smsrpOld / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
			// + (double) smrspOld / (double) TOTAL_NUMBER_OF_SYSTEMS + " " +
			// (double) sNoBlocking / (double) TOTAL_NUMBER_OF_SYSTEMS + "\n";

			result += sfnp + " " + sfp + " " + smrsp + " " + smsrpOld + " " + smrspOld + " " + sNoBlocking + "\n";
		}

		writeSystem((2 + " " + 1 + " " + cs_len), result);
	}

	public void experimentIncreasingContention(int NoA) {
		double RESOURCE_SHARING_FACTOR = 0.4;
		int NUMBER_OF_TASKS_ON_EACH_PARTITION = 4;

		SimpleSystemGenerator generator = new SimpleSystemGenerator(MIN_PERIOD, MAX_PERIOD, TOTAL_PARTITIONS,
				NUMBER_OF_TASKS_ON_EACH_PARTITION * TOTAL_PARTITIONS, true, CS_LENGTH_RANGE.SHORT_CS_LEN, RESOURCES_RANGE.PARTITIONS, RESOURCE_SHARING_FACTOR,
				NoA);
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

		for (int j = 0; j < NUMBER_OF_TIMES; j++) {
			smrsp = smrspOld = smsrpOld = sNoBlocking = sfnp = sfp = 0;
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

				System.out.println(3 + " " + 1 + " " + NoA + " times: " + j + " numbers: " + i);
			}

			// result += (double) sfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
			// + (double) sfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
			// + (double) smrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " +
			// (double) smsrpOld / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
			// + (double) smrspOld / (double) TOTAL_NUMBER_OF_SYSTEMS + " " +
			// (double) sNoBlocking / (double) TOTAL_NUMBER_OF_SYSTEMS + "\n";

			result += sfnp + " " + sfp + " " + smrsp + " " + smsrpOld + " " + smrspOld + " " + sNoBlocking + "\n";
		}

		writeSystem((3 + " " + 1 + " " + NoA), result);
	}

	public void experimentIncreasingParallelism(int NoP) {
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

		for (int j = 0; j < NUMBER_OF_TIMES; j++) {
			smrsp = smrspOld = smsrpOld = sNoBlocking = sfnp = sfp = 0;
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

				System.out.println(4 + " " + 1 + " " + NoP + " times: " + j + " numbers: " + i);

			}

			// result += (double) sfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
			// + (double) sfp / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
			// + (double) smrsp / (double) TOTAL_NUMBER_OF_SYSTEMS + " " +
			// (double) smsrpOld / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
			// + (double) smrspOld / (double) TOTAL_NUMBER_OF_SYSTEMS + " " +
			// (double) sNoBlocking / (double) TOTAL_NUMBER_OF_SYSTEMS + "\n";

			result += sfnp + " " + sfp + " " + smrsp + " " + smsrpOld + " " + smrspOld + " " + sNoBlocking + "\n";
		}

		writeSystem((4 + " " + 1 + " " + NoP), result);
	}

	public void BTBH(int NoA) {
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

		for (int j = 0; j < NUMBER_OF_TIMES; j++) {
			smrsp = sfnp = sfp = smrsp1 = sfnp1 = sfp1 = 0;
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

				System.out.println(3 + " " + 2 + " " + NoA + " times: " + j + " numbers: " + i);
			}
			result += sfnp + " " + sfp + " " + smrsp + " " + sfnp1 + " " + sfp1 + " " + smrsp1 + "\n";
		}

		writeSystem((3 + " " + 2 + " " + NoA), result);
	}

	public void overheads(int cs_len) {
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

		for (int j = 0; j < NUMBER_OF_TIMES; j++) {
			sfnp = sfpIO = sfp = sfpIO = smrsp = smrspIO = smrspIONP = 0;
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

				System.out.println(2 + " " + 2 + " " + cs_len + " times: " + j + " numbers: " + i);
			}

			result += sfnp + " " + sfnpIO + " " + sfp + " " + sfpIO + " " + smrsp + " " + smrspIO + " " + smrspIONP + "\n";
		}

		writeSystem((2 + " " + 2 + " " + cs_len), result);
	}

	public void allocationcslenmsrp(int cs_len) {
		boolean useRi = true;
		boolean btbHit = true;

		int noa = 3;
		int not = 4;
		double rsf = 0.3;

		AllocationGeneator allocGeneator = new AllocationGeneator();

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

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, true, TOTAL_PARTITIONS, not * TOTAL_PARTITIONS, rsf, cs_range,
				RESOURCES_RANGE.PARTITIONS, noa, false);

		MSRPIO fnp = new MSRPIO();

		for (int num = 0; num < NUMBER_OF_TIMES; num++) {
			wfsfnp = ffsfnp = bfsfnp = nfsfnp = rrfsfnp = rlfsfnp = rldfsfnp = rlifsfnp = 0;
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

				/**
				 * BEST FIT
				 */
				ArrayList<ArrayList<SporadicTask>> tasksBF = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 1);

				Ris = fnp.getResponseTimeDM(tasksBF, resources, btbHit, useRi, false);
				if (isSystemSchedulable(tasksBF, Ris))
					bfsfnp++;

				/**
				 * FIRST FIT
				 */
				ArrayList<ArrayList<SporadicTask>> tasksFF = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 2);
				Ris = fnp.getResponseTimeDM(tasksFF, resources, btbHit, useRi, false);
				if (isSystemSchedulable(tasksFF, Ris))
					ffsfnp++;

				/**
				 * NEXT FIT
				 */
				ArrayList<ArrayList<SporadicTask>> tasksNF = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 3);
				Ris = fnp.getResponseTimeDM(tasksNF, resources, btbHit, useRi, false);
				if (isSystemSchedulable(tasksNF, Ris))
					nfsfnp++;

				/**
				 * RESOURCE LOCAL FIT
				 */

				ArrayList<ArrayList<SporadicTask>> tasksSPA = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 4);
				Ris = fnp.getResponseTimeDM(tasksSPA, resources, btbHit, useRi, false);
				if (isSystemSchedulable(tasksSPA, Ris))
					rlfsfnp++;

				/**
				 * RESOURCE REQUEST TASKS FIT
				 */

				ArrayList<ArrayList<SporadicTask>> tasksRCF = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 5);
				Ris = fnp.getResponseTimeDM(tasksRCF, resources, btbHit, useRi, false);
				if (isSystemSchedulable(tasksRCF, Ris))
					rrfsfnp++;

				/**
				 * RESOURCE LENGTH DECREASE FIT
				 */

				ArrayList<ArrayList<SporadicTask>> tasksRLFL = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 6);
				Ris = fnp.getResponseTimeDM(tasksRLFL, resources, btbHit, useRi, false);
				if (isSystemSchedulable(tasksRLFL, Ris))
					rldfsfnp++;

				/**
				 * RESOURCE LENGTH INCREASE FIT
				 */
				ArrayList<ArrayList<SporadicTask>> tasksRLFS = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 7);
				Ris = fnp.getResponseTimeDM(tasksRLFS, resources, btbHit, useRi, false);
				if (isSystemSchedulable(tasksRLFS, Ris))
					rlifsfnp++;

				System.out.println(2 + " " + 3 + " " + cs_len + " times: " + num + " numbers: " + i);
			}
			result += wfsfnp + " " + ffsfnp + " " + bfsfnp + " " + nfsfnp + " " + rrfsfnp + " " + rlfsfnp + " " + rldfsfnp + " " + rlifsfnp + "\n";
		}

		writeSystem((2 + " " + 3 + " " + cs_len), result);
	}

	public void taskallocationaccessmsrp(int NoA) {
		boolean useRi = true;
		boolean btbHit = true;

		CS_LENGTH_RANGE rangeee = CS_LENGTH_RANGE.LONG_CSLEN;
		int not = 4;
		double rsf = 0.3;

		AllocationGeneator allocGeneator = new AllocationGeneator();

		long[][] Ris;
		String result = "";
		int wfsmrsp = 0, ffsmrsp = 0, bfsmrsp = 0, nfsmrsp = 0, rrfsmrsp = 0, rlfsmrsp = 0, rldfsmrsp = 0, rlifsmrsp = 0;

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, true, TOTAL_PARTITIONS, not * TOTAL_PARTITIONS, rsf, rangeee,
				RESOURCES_RANGE.PARTITIONS, NoA, false);

		MSRPIO mrsp = new MSRPIO();

		for (int j = 0; j < NUMBER_OF_TIMES; j++) {
			wfsmrsp = ffsmrsp = bfsmrsp = nfsmrsp = rrfsmrsp = rlfsmrsp = rldfsmrsp = rlifsmrsp = 0;
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

				ArrayList<ArrayList<SporadicTask>> tasksWF = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 0);
				Ris = mrsp.getResponseTimeDM(tasksWF, resources, btbHit, useRi, false);
				if (isSystemSchedulable(tasksWF, Ris))
					wfsmrsp++;

				ArrayList<ArrayList<SporadicTask>> tasksBF = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 1);
				Ris = mrsp.getResponseTimeDM(tasksBF, resources, btbHit, useRi, false);
				if (isSystemSchedulable(tasksBF, Ris))
					bfsmrsp++;

				ArrayList<ArrayList<SporadicTask>> tasksFF = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 2);
				Ris = mrsp.getResponseTimeDM(tasksFF, resources, btbHit, useRi, false);
				if (isSystemSchedulable(tasksFF, Ris))
					ffsmrsp++;

				ArrayList<ArrayList<SporadicTask>> tasksNF = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 3);
				Ris = mrsp.getResponseTimeDM(tasksNF, resources, btbHit, useRi, false);
				if (isSystemSchedulable(tasksNF, Ris))
					nfsmrsp++;

				ArrayList<ArrayList<SporadicTask>> tasksSPA = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 4);
				Ris = mrsp.getResponseTimeDM(tasksSPA, resources, btbHit, useRi, false);
				if (isSystemSchedulable(tasksSPA, Ris))
					rlfsmrsp++;

				ArrayList<ArrayList<SporadicTask>> tasksRCF = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 5);
				Ris = mrsp.getResponseTimeDM(tasksRCF, resources, btbHit, useRi, false);
				if (isSystemSchedulable(tasksRCF, Ris))
					rrfsmrsp++;

				ArrayList<ArrayList<SporadicTask>> tasksRLFL = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 6);
				Ris = mrsp.getResponseTimeDM(tasksRLFL, resources, btbHit, useRi, false);
				if (isSystemSchedulable(tasksRLFL, Ris))
					rldfsmrsp++;

				ArrayList<ArrayList<SporadicTask>> tasksRLFS = allocGeneator.allocateTasks(tasksToAlloc, resources, generator.total_partitions, 7);
				Ris = mrsp.getResponseTimeDM(tasksRLFS, resources, btbHit, useRi, false);
				if (isSystemSchedulable(tasksRLFS, Ris))
					rlifsmrsp++;

				System.out.println(3 + " " + 3 + " " + NoA + " times: " + j + " numbers: " + i);
			}
			result += wfsmrsp + " " + bfsmrsp + " " + ffsmrsp + " " + nfsmrsp + " " + rlfsmrsp + " " + rrfsmrsp + " " + rldfsmrsp + " " + rlifsmrsp + "\n";
		}

		writeSystem((3 + " " + 3 + " " + NoA), result);
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

		for (int j = 0; j < NUMBER_OF_TIMES; j++) {
			DM = OPA = RPA = SBPO = 0;
			for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
				ArrayList<SporadicTask> tasksToAlloc = generator.generateTasks(true);
				ArrayList<Resource> resources = generator.generateResources();
				generator.generateResourceUsage(tasksToAlloc, resources);
				ArrayList<ArrayList<SporadicTask>> tasks = new AllocationGeneator().allocateTasks(tasksToAlloc, resources, generator.total_partitions, 0);

				Ris = analysis.getResponseTimeDM(tasks, resources, true, true, false);
				if (isSystemSchedulable(tasks, Ris)) {
					DM++;
				}

				Ris = analysis.getResponseTimeRPA(tasks, resources, false);
				if (isSystemSchedulable(tasks, Ris)) {
					RPA++;
				}

				Ris = analysis.getResponseTimeOPA(tasks, resources, false);
				if (isSystemSchedulable(tasks, Ris)) {
					OPA++;
				}

				Ris = analysis.getResponseTimeSBPO(tasks, resources, false);
				if (isSystemSchedulable(tasks, Ris)) {
					SBPO++;
				}

				System.out.println(name + " " + 2 + " " + 4 + " " + cs_len + " times: " + j + " numbers: " + i);

			}
			result += DM + " " + OPA + " " + RPA + " " + SBPO + "\n";
		}

		writeSystem((name + " " + 2 + " " + 4 + " " + cs_len), result);
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
