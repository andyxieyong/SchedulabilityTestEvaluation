package test;

import java.util.ArrayList;

import analysis.NewMrsPRTA;
import analysis.NewMrsPRTAWithMCNP;
import entity.Resource;
import entity.SporadicTask;

public class FixedTaskSet {

	public static void main(String args[]) {
		fixed_system_1();
		fixed_system_2();
		hard_system1();
	}

	public static void fixed_system_1() {
		/* Generate tasks */
		ArrayList<ArrayList<SporadicTask>> tasks = new ArrayList<>();

		ArrayList<SporadicTask> ts = new ArrayList<>();
		ts.add(new SporadicTask(999, 1000, 1, 0, 1));
		ts.add(new SporadicTask(900, 1000, 1, 0, 2));
		ts.add(new SporadicTask(800, 1000, 1, 0, 3));
		tasks.add(ts);

		ArrayList<SporadicTask> ts1 = new ArrayList<>();
		ts1.add(new SporadicTask(988, 1000, 1, 1, 4));
		tasks.add(ts1);

		/* Generate resource */
		ArrayList<Resource> resources = new ArrayList<>();
		resources.add(new Resource(1, 2));
		// resources.add(new Resource(2, 10));
		// resources.add(new Resource(3, 5));

		tasks.get(0).get(0).resource_required_index.add(0);
		tasks.get(0).get(0).number_of_access_in_one_release.add(2);
		tasks.get(0).get(0).pure_resource_execution_time = 2 * 2;

		tasks.get(0).get(1).resource_required_index.add(0);
		tasks.get(0).get(1).number_of_access_in_one_release.add(3);
		tasks.get(0).get(1).pure_resource_execution_time = 2 * 3;

		tasks.get(0).get(2).resource_required_index.add(0);
		tasks.get(0).get(2).number_of_access_in_one_release.add(3);
		tasks.get(0).get(2).pure_resource_execution_time = 2 * 3;

		// tasks.get(0).get(2).resource_required_index.add(1);
		// tasks.get(0).get(2).number_of_access_in_one_release.add(3);
		// tasks.get(0).get(2).pure_resource_execution_time = 2 * 3 + 10 * 3;
		//
		// tasks.get(0).get(2).resource_required_index.add(2);
		// tasks.get(0).get(2).number_of_access_in_one_release.add(1);
		// tasks.get(0).get(2).pure_resource_execution_time = 2 * 3 + 10 * 3 +
		// 5;

		tasks.get(1).get(0).resource_required_index.add(0);
		tasks.get(1).get(0).number_of_access_in_one_release.add(5);
		tasks.get(1).get(0).pure_resource_execution_time = 2 * 5;

		// tasks.get(1).get(0).resource_required_index.add(1);
		// tasks.get(1).get(0).number_of_access_in_one_release.add(5);
		// tasks.get(1).get(0).pure_resource_execution_time = 2 * 5 + 10 * 5;

		resources.get(0).requested_tasks.add(tasks.get(0).get(0));
		resources.get(0).requested_tasks.add(tasks.get(0).get(1));
		resources.get(0).requested_tasks.add(tasks.get(0).get(2));
		resources.get(0).requested_tasks.add(tasks.get(1).get(0));

		resources.get(0).ceiling.add(999);
		resources.get(0).ceiling.add(999);

		resources.get(0).partitions.add(0);
		resources.get(0).partitions.add(1);
		resources.get(0).isGlobal = true;

		for (int i = 0; i < tasks.size(); i++) {
			for (int j = 0; j < tasks.get(i).size(); j++) {
				SporadicTask task = tasks.get(i).get(j);
				task.hasResource = 1;
				task.resource_required_index_cpoy = new int[task.resource_required_index.size()];
				task.number_of_access_in_one_release_copy = new int[task.resource_required_index.size()];
				for (int resource_index = 0; resource_index < task.resource_required_index.size(); resource_index++) {
					task.resource_required_index_cpoy[resource_index] = task.resource_required_index.get(resource_index);
					task.number_of_access_in_one_release_copy[resource_index] = task.number_of_access_in_one_release.get(resource_index);
				}
			}
		}

		tasks.get(0).get(0).Ri = 1 + 4;
		tasks.get(0).get(1).Ri = 1 + 4;
		tasks.get(0).get(2).Ri = 1 + 6;
		tasks.get(1).get(0).Ri = 1 + 10;

		// resources.get(1).requested_tasks.add(tasks.get(0).get(2));
		// resources.get(1).requested_tasks.add(tasks.get(1).get(0));
		//
		// resources.get(1).ceiling.add(800);
		// resources.get(1).ceiling.add(999);
		//
		// resources.get(1).partitions.add(0);
		// resources.get(1).partitions.add(1);
		// resources.get(1).isGlobal = true;
		//
		// resources.get(2).requested_tasks.add(tasks.get(0).get(2));
		// resources.get(2).ceiling.add(800);
		// resources.get(2).partitions.add(0);
		// resources.get(2).isGlobal = false;

		// new OriginalMrsPRTA().NewMrsPRTATest(tasks, resources, true);
		// new MSRPRTA().NewMrsPRTATest(tasks, resources, true);

		// new FIFONonPreemptiveLinear().NewMrsPRTATest(tasks, resources, true,
		// false);

		// long[][] Ris = {{30,30,30},{30}};
		// FIFONonPreemptiveLinear np = new FIFONonPreemptiveLinear();
		// np.generateFixedConstrains(tasks.get(0).get(0), tasks, resources,
		// true);
		// double[] b = np.solveMILPForOneTask(tasks.get(0).get(0), tasks,
		// resources, Ris, 0, true);
		// System.out.println(b[1] + " " + b[2]);

		// new NewFIFONP().NewMrsPRTATest(tasks, resources, true);
		// new FIFOPreemptiveLinear().NewMrsPRTATest(tasks, resources, true,
		// false);

		// new NewMrsPRTA().NewMrsPRTATest(tasks, resources, true);
		// new FIFONonPreemptiveLinear().NewMrsPRTATest(tasks, resources, true,
		// true);

		// MIPSolverC solver = new MIPSolverC();
		// solver.solveMIP(tasks, resources, 4);
		// // System.out.println("blocking: " + blocking);
		//
		// for(int i=0;i<tasks.size();i++){
		// for(int j=0;j<tasks.get(i).size();j++){
		// SporadicTask task = tasks.get(i).get(j);
		// System.out.println("T" + task.id + " total blocking: " +
		// task.total_blocking + " spin: " + task.spin + " local: " +
		// task.local);
		// }
		// }

		// new NewMrsPRTA().NewMrsPRTATest(tasks, resources, true);
		// new FIFONonPreemptiveLinearC().NewMrsPRTATest(tasks,
		// resources,true,true);
		new NewMrsPRTA().NewMrsPRTATest(tasks, resources, true);
		new NewMrsPRTAWithMCNP().NewMrsPRTATest(tasks, resources, 1,2,true);
		System.out.println();
		// new FIFONonPreemptiveLinear().NewMrsPRTATest(tasks, resources, true,
		// false);
	}

	public static void fixed_system_2() {
		/* Generate tasks */
		ArrayList<ArrayList<SporadicTask>> tasks = new ArrayList<>();

		ArrayList<SporadicTask> ts = new ArrayList<>();
		ts.add(new SporadicTask(999, 1000, 1, 0, 1));
		ts.add(new SporadicTask(900, 1000, 1, 0, 2));
		ts.add(new SporadicTask(800, 1000, 1, 0, 3));
		ts.add(new SporadicTask(700, 1000, 1, 0, 4));
		tasks.add(ts);

		ArrayList<SporadicTask> ts1 = new ArrayList<>();
		ts1.add(new SporadicTask(999, 1000, 1, 1, 5));
		tasks.add(ts1);

		/* Generate resource */
		ArrayList<Resource> resources = new ArrayList<>();
		resources.add(new Resource(1, 2));
		resources.add(new Resource(2, 1));

		tasks.get(0).get(0).resource_required_index.add(0);
		tasks.get(0).get(0).resource_required_index.add(1);
		tasks.get(0).get(0).number_of_access_in_one_release.add(1);
		tasks.get(0).get(0).number_of_access_in_one_release.add(1);
		tasks.get(0).get(0).pure_resource_execution_time = 2 + 1;

		tasks.get(0).get(1).resource_required_index.add(0);
		tasks.get(0).get(1).number_of_access_in_one_release.add(1);
		tasks.get(0).get(1).pure_resource_execution_time = 2;

		tasks.get(0).get(2).resource_required_index.add(0);
		tasks.get(0).get(2).number_of_access_in_one_release.add(1);
		tasks.get(0).get(2).pure_resource_execution_time = 2;

		tasks.get(0).get(3).resource_required_index.add(0);
		tasks.get(0).get(3).resource_required_index.add(1);
		tasks.get(0).get(3).number_of_access_in_one_release.add(1);
		tasks.get(0).get(3).number_of_access_in_one_release.add(1);
		tasks.get(0).get(3).pure_resource_execution_time = 2 + 1;

		tasks.get(1).get(0).resource_required_index.add(0);
		tasks.get(1).get(0).number_of_access_in_one_release.add(1);
		tasks.get(1).get(0).pure_resource_execution_time = 2;

		resources.get(0).requested_tasks.add(tasks.get(0).get(0));
		resources.get(0).requested_tasks.add(tasks.get(0).get(1));
		resources.get(0).requested_tasks.add(tasks.get(0).get(2));
		resources.get(0).requested_tasks.add(tasks.get(0).get(3));
		resources.get(0).requested_tasks.add(tasks.get(1).get(0));

		resources.get(0).ceiling.add(999);
		resources.get(0).ceiling.add(999);

		resources.get(0).partitions.add(0);
		resources.get(0).partitions.add(1);
		resources.get(0).isGlobal = true;

		resources.get(1).requested_tasks.add(tasks.get(0).get(0));
		resources.get(1).requested_tasks.add(tasks.get(0).get(3));
		resources.get(1).ceiling.add(999);
		resources.get(1).partitions.add(0);

		for (int i = 0; i < tasks.size(); i++) {
			for (int j = 0; j < tasks.get(i).size(); j++) {
				SporadicTask task = tasks.get(i).get(j);
				task.hasResource = 1;
				task.resource_required_index_cpoy = new int[task.resource_required_index.size()];
				task.number_of_access_in_one_release_copy = new int[task.resource_required_index.size()];
				for (int resource_index = 0; resource_index < task.resource_required_index.size(); resource_index++) {
					task.resource_required_index_cpoy[resource_index] = task.resource_required_index.get(resource_index);
					task.number_of_access_in_one_release_copy[resource_index] = task.number_of_access_in_one_release.get(resource_index);
				}
			}
		}

		// new NewMrsPRTA().NewMrsPRTATest(tasks, resources, true);
		// FIFONonPreemptiveLinear np = new FIFONonPreemptiveLinear();
		// np.NewMrsPRTATest(tasks, resources, true, false);
		// new NewFIFONP().NewMrsPRTATest(tasks, resources, true);
		// new FIFOPreemptiveLinear().NewMrsPRTATest(tasks, resources, true,
		// false);
		// new FIFOPreemptiveLinear().NewMrsPRTATest(tasks, resources, true,
		// false);
		// new NewMrsPRTA().NewMrsPRTATest(tasks, resources, true);
		// new FIFONonPreemptiveLinearC().NewMrsPRTATest(tasks,
		// resources,true,true);
		new NewMrsPRTA().NewMrsPRTATest(tasks, resources, true);
		new NewMrsPRTAWithMCNP().NewMrsPRTATest(tasks, resources, 1,2, true);
		System.out.println();
		// new FIFONonPreemptiveLinear().NewMrsPRTATest(tasks, resources, true,
		// false);
	}

	public static void hard_system1() {
		/* Generate tasks */
		ArrayList<ArrayList<SporadicTask>> tasks = new ArrayList<>();

		ArrayList<SporadicTask> ts = new ArrayList<>();
		ts.add(new SporadicTask(998, 56000, 6558, 0, 1));
		ts.add(new SporadicTask(996, 522000, 62869, 0, 2));
		ts.add(new SporadicTask(994, 653000, 106070, 0, 3));
		tasks.add(ts);

		ArrayList<SporadicTask> ts1 = new ArrayList<>();
		ts1.add(new SporadicTask(998, 174000, 50173, 1, 4));
		ts1.add(new SporadicTask(996, 658000, 73465, 1, 5));
		tasks.add(ts1);

		/* Generate resource */
		ArrayList<Resource> resources = new ArrayList<>();
		resources.add(new Resource(1, 10));
		resources.add(new Resource(2, 14));
		resources.add(new Resource(3, 15));
		resources.add(new Resource(4, 12));

		SporadicTask t2 = tasks.get(0).get(1);
		t2.resource_required_index.add(1);
		t2.number_of_access_in_one_release.add(4);
		t2.resource_required_index.add(2);
		t2.number_of_access_in_one_release.add(3);
		t2.resource_required_index.add(3);
		t2.number_of_access_in_one_release.add(3);
		t2.pure_resource_execution_time = 137;
		t2.WCET = t2.WCET - 137;

		SporadicTask t3 = tasks.get(0).get(2);
		t3.resource_required_index.add(0);
		t3.resource_required_index.add(1);
		t3.resource_required_index.add(2);
		t3.resource_required_index.add(3);
		t3.number_of_access_in_one_release.add(1);
		t3.number_of_access_in_one_release.add(4);
		t3.number_of_access_in_one_release.add(3);
		t3.number_of_access_in_one_release.add(2);
		t3.pure_resource_execution_time = 135;
		t3.WCET = t3.WCET - 135;

		SporadicTask t4 = tasks.get(1).get(0);
		t4.resource_required_index.add(2);
		t4.number_of_access_in_one_release.add(5);
		t4.pure_resource_execution_time = 75;
		t4.WCET = t4.WCET - 75;

		SporadicTask t5 = tasks.get(1).get(1);
		t5.resource_required_index.add(2);
		t5.number_of_access_in_one_release.add(1);
		t5.pure_resource_execution_time = 15;
		t5.WCET = t5.WCET - 15;

		resources.get(0).requested_tasks.add(t3);
		resources.get(0).ceiling.add(994);
		resources.get(0).partitions.add(0);

		resources.get(1).requested_tasks.add(t2);
		resources.get(1).requested_tasks.add(t3);
		resources.get(1).ceiling.add(996);
		resources.get(1).partitions.add(0);

		resources.get(2).requested_tasks.add(t2);
		resources.get(2).requested_tasks.add(t3);
		resources.get(2).requested_tasks.add(t4);
		resources.get(2).requested_tasks.add(t5);
		resources.get(2).ceiling.add(996);
		resources.get(2).ceiling.add(998);
		resources.get(2).partitions.add(0);
		resources.get(2).partitions.add(1);
		resources.get(2).isGlobal = true;

		resources.get(3).requested_tasks.add(t2);
		resources.get(3).requested_tasks.add(t3);
		resources.get(3).ceiling.add(996);
		resources.get(3).partitions.add(0);

		for (int i = 0; i < tasks.size(); i++) {
			for (int j = 0; j < tasks.get(i).size(); j++) {
				SporadicTask task = tasks.get(i).get(j);
				task.hasResource = 1;
				task.resource_required_index_cpoy = new int[task.resource_required_index.size()];
				task.number_of_access_in_one_release_copy = new int[task.resource_required_index.size()];
				for (int resource_index = 0; resource_index < task.resource_required_index.size(); resource_index++) {
					task.resource_required_index_cpoy[resource_index] = task.resource_required_index.get(resource_index);
					task.number_of_access_in_one_release_copy[resource_index] = task.number_of_access_in_one_release.get(resource_index);
				}
			}
		}
		//
		// SystemGenerator gene = new SystemGenerator();
		// gene.testifyGeneratedTasksetAndResource(tasks, resources);

		// new NewMrsPRTA().NewMrsPRTATest(tasks, resources, true);
		// FIFONonPreemptiveLinear np = new FIFONonPreemptiveLinear();
		// np.NewMrsPRTATest(tasks, resources, true, false);
		// new NewFIFONP().NewMrsPRTATest(tasks, resources, true);
		// new FIFOPreemptiveLinear().NewMrsPRTATest(tasks, resources, true,
		// false);
		//// new FIFOPreemptiveLinear().NewMrsPRTATest(tasks, resources, true,
		// false);
		// new NewMrsPRTA().NewMrsPRTATest(tasks, resources, true);
		// new FIFONonPreemptiveLinearC().NewMrsPRTATest(tasks,
		// resources,true,true);
		new NewMrsPRTA().NewMrsPRTATest(tasks, resources, true);
		new NewMrsPRTAWithMCNP().NewMrsPRTATest(tasks, resources,1,2,true);
		System.out.println();
	}
}
