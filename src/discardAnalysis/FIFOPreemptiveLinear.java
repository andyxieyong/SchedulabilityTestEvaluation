//package discardAnalysis;
//
//import java.util.ArrayList;
//
//import analysis.Utils;
//import entity.Resource;
//import entity.SporadicTask;
//
//public class FIFOPreemptiveLinear {
//	long count = 0;
//	private static boolean use_deadline_insteadof_Ri = false;
//
//	public long[][] NewMrsPRTATest(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, boolean printBebug, boolean printLIP) {
//		long[][] init_Ri = new Utils().initResponseTime(tasks);
//		long[][] response_time = new long[tasks.size()][];
//		boolean isEqual = false, missDeadline = false;
//		count = 0;
//
//		for (int i = 0; i < init_Ri.length; i++)
//			response_time[i] = new long[init_Ri[i].length];
//		new Utils().cloneList(init_Ri, response_time);
//
//		/** generate variables, binary variables, and fixed constrains. **/
//		for (int i = 0; i < tasks.size(); i++) {
//			for (int j = 0; j < tasks.get(i).size(); j++) {
//				SporadicTask task = tasks.get(i).get(j);
//				generateFixedConstrains(task, tasks, resources, printLIP);
//			}
//		}
//
//		/* a huge busy window to get a fixed Ri */
//		while (!isEqual) {
//			isEqual = true;
//			long[][] response_time_plus = busyWindow(tasks, resources, response_time, printLIP);
//
//			for (int i = 0; i < response_time_plus.length; i++) {
//				for (int j = 0; j < response_time_plus[i].length; j++) {
//					if (response_time[i][j] != response_time_plus[i][j])
//						isEqual = false;
//					if (response_time_plus[i][j] > tasks.get(i).get(j).deadline)
//						missDeadline = true;
//				}
//			}
//
//			count++;
//			new Utils().cloneList(response_time_plus, response_time);
//			if (missDeadline)
//				break;
//		}
//
//		if (printBebug) {
//			if (missDeadline)
//				System.out.println("LIPFIFONP    after " + count + " tims of recursion, the tasks miss the deadline.");
//			else
//				System.out.println("LIPFIFONP    after " + count + " tims of recursion, we got the response time.");
//			new Utils().printResponseTime(response_time, tasks);
//		}
//
//		return response_time;
//	}
//
//	private long[][] busyWindow(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, long[][] response_time, boolean printBebug) {
//		long[][] response_time_plus = new long[tasks.size()][];
//		for (int i = 0; i < response_time.length; i++)
//			response_time_plus[i] = new long[response_time[i].length];
//
//		for (int i = 0; i < tasks.size(); i++) {
//			for (int j = 0; j < tasks.get(i).size(); j++) {
//				SporadicTask task = tasks.get(i).get(j);
//				task.interference = highPriorityInterference(task, tasks, response_time[i][j]);
//				double blocking[] = solveMILPForOneTask(task, tasks, resources, response_time, response_time[i][j], printBebug);
//				task.spin = (long) blocking[1];
//				task.local = (long) blocking[2];
//				response_time_plus[i][j] = task.Ri = task.WCET + task.pure_resource_execution_time + task.interference + task.spin + task.local;
//				if (task.Ri > task.deadline)
//					return response_time_plus;
//			}
//		}
//		return response_time_plus;
//
//	}
//
//	/*
//	 * Calculate the local high priority tasks' interference for a given task t. CI is a set of computation time of local tasks, including spin delay.
//	 */
//	private long highPriorityInterference(SporadicTask t, ArrayList<ArrayList<SporadicTask>> allTasks, long Ri) {
//		long interference = 0;
//		int partition = t.partition;
//		ArrayList<SporadicTask> tasks = allTasks.get(partition);
//
//		for (int i = 0; i < tasks.size(); i++) {
//			if (tasks.get(i).priority > t.priority) {
//				SporadicTask hpTask = tasks.get(i);
//				interference += Math.ceil((double) (Ri) / (double) hpTask.period) * (hpTask.WCET + hpTask.pure_resource_execution_time);
//			}
//		}
//		return interference;
//	}
//
//	/******************************************
//	 ****** Linear Programming Generator *********
//	 ******************************************/
//	public void generateFixedConstrains(SporadicTask task, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, boolean printDebug) {
//		generateObjective(task, tasks, resources, task.variables, task.objective, task.coefficient);
//
//		constraintOne(task, tasks, resources, task.fixed_constraints_neq, task.fixed_values_neq, task.variables);
//		constraintTwo(task, tasks, resources, task.fixed_constraints_eq, task.fixed_values_eq, task.variables);
//		constrainThree(task, tasks, resources, task.fixed_constraints_neq, task.fixed_values_neq, task.variables);
//		constrainFour(task, tasks, resources, task.fixed_constraints_neq, task.fixed_values_neq, task.variables, task.binary_index);
//		constrainFive(task, tasks, resources, task.fixed_constraints_neq, task.fixed_values_neq, task.variables);
//		constrainSix(task, tasks, resources, task.fixed_constraints_neq, task.fixed_values_neq, task.variables);
//		constrainSeven(task, tasks, resources, task.fixed_constraints_neq, task.fixed_values_neq, task.variables);
//		constrainTen(task, tasks, resources, task.fixed_constraints_neq, task.fixed_values_neq, task.variables);
//	}
//
//	public double[] solveMILPForOneTask(SporadicTask task, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, long[][] Ris, long Ri, boolean printDebug) {
//		ArrayList<ArrayList<Integer>> constraints_neq = new ArrayList<>();
//		ArrayList<Integer> values_neq = new ArrayList<>();
//
//		constraints_neq.addAll(task.fixed_constraints_neq);
//		values_neq.addAll(task.fixed_values_neq);
//
//		constrainElevenTwelve(task, tasks, resources, constraints_neq, values_neq, task.variables, Ris, Ri);
//		constrainThirteen(task, tasks, resources, constraints_neq, values_neq, task.variables, Ris, Ri);
//
//		printLIP(task.objective, task.coefficient, constraints_neq, values_neq, task.fixed_constraints_eq, task.fixed_values_eq, task.variables, task.binary_index, printDebug);
//
////		double[] blocking = new Utils().sloveMIP(task.objective, task.coefficient, constraints_neq, values_neq, task.fixed_constraints_eq, task.fixed_values_eq, task.variables,
////				task.binary_index, printDebug);
//
//		return null;
//	}
//
//	public void generateObjective(SporadicTask task, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, ArrayList<String> variables,
//			ArrayList<Integer> objective, ArrayList<Integer> coefficient) {
//
//		for (int i = 0; i < tasks.size(); i++) {
//			for (int j = 0; j < tasks.get(i).size(); j++) {
//				SporadicTask other_task = tasks.get(i).get(j);
//
//				if (other_task != task) {
//					for (int k = 0; k < other_task.number_of_access_in_one_release.size(); k++) {
//						for (int l = 0; l < other_task.number_of_access_in_one_release.get(k); l++) {
//							Resource resource = resources.get(other_task.resource_required_index.get(k));
//
//							String variable1 = other_task.id - 1 + "|" + other_task.resource_required_index.get(k) + "|" + l + "|" + "SD";
//							String variable2 = other_task.id - 1 + "|" + other_task.resource_required_index.get(k) + "|" + l + "|" + "LB";
//
//							objective.add(addGetVariable(variables, variable1));
//							objective.add(addGetVariable(variables, variable2));
//
//							coefficient.add((int) resource.csl);
//							coefficient.add((int) resource.csl);
//						}
//					}
//				}
//
//			}
//		}
//	}
//
//	public void constraintOne(SporadicTask task, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, ArrayList<ArrayList<Integer>> constraints_neq,
//			ArrayList<Integer> values_neq, ArrayList<String> variables) {
//
//		for (int i = 0; i < tasks.size(); i++) {
//			for (int j = 0; j < tasks.get(i).size(); j++) {
//				SporadicTask other_task = tasks.get(i).get(j);
//
//				if (other_task != task) {
//					for (int k = 0; k < other_task.number_of_access_in_one_release.size(); k++) {
//						for (int l = 0; l < other_task.number_of_access_in_one_release.get(k); l++) {
//							ArrayList<Integer> constrain = new ArrayList<>();
//
//							String variable1 = other_task.id - 1 + "|" + other_task.resource_required_index.get(k) + "|" + l + "|" + "SD";
//							String variable2 = other_task.id - 1 + "|" + other_task.resource_required_index.get(k) + "|" + l + "|" + "LB";
//
//							constrain.add(addGetVariable(variables, variable1));
//							constrain.add(addGetVariable(variables, variable2));
//
//							constraints_neq.add(constrain);
//							values_neq.add(1);
//						}
//					}
//				}
//
//			}
//		}
//
//	}
//
//	public void constraintTwo(SporadicTask task, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, ArrayList<ArrayList<Integer>> constraints_eq,
//			ArrayList<Integer> values_eq, ArrayList<String> variables) {
//		ArrayList<Resource> non_conflicting_local_resources = new ArrayList<>();
//
//		for (int i = 0; i < resources.size(); i++) {
//			Resource resource = resources.get(i);
//			if (resource.partitions.size() == 1 && resource.partitions.get(0) == task.partition && resource.ceiling.get(0) < task.priority) {
//				non_conflicting_local_resources.add(resource);
//			}
//		}
//
//		ArrayList<Integer> constrain = new ArrayList<>();
//
//		for (int i = 0; i < non_conflicting_local_resources.size(); i++) {
//			Resource resource = non_conflicting_local_resources.get(i);
//			String variable1 = 0 + "|" + (resource.id - 1) + "|" + 0 + "|" + "PR";
//			constrain.add(addGetVariable(variables, variable1));
//		}
//
//		if (constrain.size() > 0) {
//			constraints_eq.add(constrain);
//			values_eq.add(0);
//		}
//	}
//
//	public void constrainThree(SporadicTask task, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, ArrayList<ArrayList<Integer>> constraints_neq,
//			ArrayList<Integer> values_neq, ArrayList<String> variables) {
//
//		ArrayList<Integer> constrain = new ArrayList<>();
//		for (int i = 0; i < resources.size(); i++) {
//			Resource resource = resources.get(i);
//			String variable1 = 0 + "|" + (resource.id - 1) + "|" + 0 + "|" + "PR";
//			constrain.add(addGetVariable(variables, variable1));
//		}
//		if (constrain.size() > 0) {
//			constraints_neq.add(constrain);
//			values_neq.add(1);
//		}
//
//	}
//
//	public void constrainFour(SporadicTask task, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, ArrayList<ArrayList<Integer>> constraints_neq,
//			ArrayList<Integer> values_neq, ArrayList<String> variables, ArrayList<Integer> binary_index) {
//
//		ArrayList<Integer> constrain = new ArrayList<>();
//
//		for (int i = 0; i < resources.size(); i++) {
//			Resource resource = resources.get(i);
//			boolean isRequestByLLtasks = false;
//			for (int j = 0; j < resource.requested_tasks.size(); j++) {
//				SporadicTask other_task = resource.requested_tasks.get(j);
//				if (other_task.partition == task.partition && other_task.priority < task.priority) {
//					isRequestByLLtasks = true;
//					break;
//				}
//			}
//
//			int variable_index = addGetVariable(variables, 0 + "|" + (resource.id - 1) + "|" + 0 + "|" + "PR");
//			if (!binary_index.contains(variable_index))
//				binary_index.add(variable_index);
//
//			if (!isRequestByLLtasks) {
//				constrain.add(variable_index);
//			}
//		}
//
//		if (constrain.size() > 0) {
//			constraints_neq.add(constrain);
//			values_neq.add(0);
//		}
//	}
//
//	public void constrainFive(SporadicTask task, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, ArrayList<ArrayList<Integer>> constraints_neq,
//			ArrayList<Integer> values_neq, ArrayList<String> variables) {
//		ArrayList<Integer> constrain = new ArrayList<>();
//
//		for (int i = 0; i < tasks.get(task.partition).size(); i++) {
//			SporadicTask lhp_task = tasks.get(task.partition).get(i);
//			if (lhp_task.priority > task.priority) {
//				for (int j = 0; j < lhp_task.number_of_access_in_one_release.size(); j++) {
//					for (int k = 0; k < lhp_task.number_of_access_in_one_release.get(j); k++) {
//						String variable = lhp_task.id - 1 + "|" + lhp_task.resource_required_index.get(j) + "|" + k + "|" + "LB";
//						constrain.add(addGetVariable(variables, variable));
//					}
//				}
//			}
//		}
//
//		if (constrain.size() > 0) {
//			constraints_neq.add(constrain);
//			values_neq.add(0);
//		}
//
//	}
//
//	public void constrainSix(SporadicTask task, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, ArrayList<ArrayList<Integer>> constraints_neq,
//			ArrayList<Integer> values_neq, ArrayList<String> variables) {
//		ArrayList<Integer> constrain = new ArrayList<>();
//
//		for (int i = 0; i < tasks.get(task.partition).size(); i++) {
//			SporadicTask other_task = tasks.get(task.partition).get(i);
//			if (other_task != task) {
//				for (int j = 0; j < other_task.number_of_access_in_one_release.size(); j++) {
//					for (int k = 0; k < other_task.number_of_access_in_one_release.get(j); k++) {
//						String variable = other_task.id - 1 + "|" + other_task.resource_required_index.get(j) + "|" + k + "|" + "SD";
//						constrain.add(addGetVariable(variables, variable));
//					}
//				}
//			}
//		}
//
//		if (constrain.size() > 0) {
//			constraints_neq.add(constrain);
//			values_neq.add(0);
//		}
//	}
//
//	public void constrainSeven(SporadicTask task, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, ArrayList<ArrayList<Integer>> constraints_neq,
//			ArrayList<Integer> values_neq, ArrayList<String> variables) {
//		for (int i = 0; i < resources.size(); i++) {
//			ArrayList<Integer> constrain = new ArrayList<>();
//			Resource resource = resources.get(i);
//
//			for (int j = 0; j < resource.requested_tasks.size(); j++) {
//				SporadicTask other_task = resource.requested_tasks.get(j);
//				if (other_task.partition == task.partition && other_task.priority < task.priority) {
//					for (int k = 0; k < other_task.number_of_access_in_one_release.get(other_task.resource_required_index.indexOf(resource.id - 1)); k++) {
//						String variable = other_task.id - 1 + "|" + (resource.id - 1) + "|" + k + "|" + "LB";
//						constrain.add(addGetVariable(variables, variable));
//					}
//				}
//			}
//
//			if (constrain.size() > 0) {
//				int variable_index = addGetVariable(variables, 0 + "|" + (resource.id - 1) + "|" + 0 + "|" + "PR");
//				constrain.add(variable_index * -1);
//
//				constraints_neq.add(constrain);
//				values_neq.add(0);
//			}
//		}
//	}
//
//	public void constrainTen(SporadicTask task, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, ArrayList<ArrayList<Integer>> constraints_neq,
//			ArrayList<Integer> values_neq, ArrayList<String> variables) {
//		for (int k = 0; k < resources.size(); k++) {
//			Resource resource = resources.get(k);
//			for (int i = 0; i < tasks.size(); i++) {
//				for (int j = 0; j < tasks.get(i).size(); j++) {
//					SporadicTask other_task = tasks.get(i).get(j);
//					ArrayList<Integer> constrain = new ArrayList<>();
//
//					if (other_task.partition != task.partition) {
//						if (other_task.resource_required_index.contains(resource.id - 1)) {
//							for (int l = 0; l < other_task.number_of_access_in_one_release.get(other_task.resource_required_index.indexOf(resource.id - 1)); l++) {
//								String variable = other_task.id - 1 + "|" + (resource.id - 1) + "|" + l + "|" + "LB";
//								constrain.add(addGetVariable(variables, variable));
//							}
//						}
//					}
//
//					if (constrain.size() > 0) {
//						constraints_neq.add(constrain);
//						values_neq.add(0);
//					}
//				}
//			}
//		}
//	}
//
//	public void constrainElevenTwelve(SporadicTask task, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, ArrayList<ArrayList<Integer>> constraints_neq,
//			ArrayList<Integer> values_neq, ArrayList<String> variables, long[][] Ris, long Ri) {
//
//		ArrayList<Integer> constrain = new ArrayList<>();
//		for (int k = 0; k < resources.size(); k++) {
//			Resource resource = resources.get(k);
//			int variable_index = addGetVariable(variables, 0 + "|" + (resource.id - 1) + "|" + 0 + "|" + "OT");
//			constrain.add(variable_index);
//
//			boolean requested_by_Task = task.resource_required_index.contains(resource.id - 1);
//			int ncs = getNoRFromHP(resource, task, tasks.get(task.partition), Ris[task.partition], Ri)
//					+ (requested_by_Task ? task.number_of_access_in_one_release.get(task.resource_required_index.indexOf(resource.id - 1)) : 0);
//
//			if (ncs == 0) {
//				ArrayList<Integer> constrain1 = new ArrayList<>();
//				constrain1.add(variable_index);
//				constraints_neq.add(constrain1);
//				values_neq.add(0);
//			}
//		}
//
//		if (constrain.size() > 0) {
//			int max_preempt = getMaxPreemption(task, tasks.get(task.partition), Ri);
//			constraints_neq.add(constrain);
//			values_neq.add(max_preempt);
//		}
//	}
//
//	public void constrainThirteen(SporadicTask task, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, ArrayList<ArrayList<Integer>> constraints_neq,
//			ArrayList<Integer> values_neq, ArrayList<String> variables, long[][] Ris, long Ri) {
//
//		for (int i = 0; i < resources.size(); i++) {
//			Resource resource = resources.get(i);
//
//			boolean requested_by_Task = task.resource_required_index.contains(resource.id - 1);
//			int ncs = getNoRFromHP(resource, task, tasks.get(task.partition), Ris[task.partition], Ri)
//					+ (requested_by_Task ? task.number_of_access_in_one_release.get(task.resource_required_index.indexOf(resource.id - 1)) : 0);
//
//			for (int j = 0; j < tasks.size(); j++) {
//				if (j == task.partition)
//					continue;
//
//				ArrayList<Integer> constrain = new ArrayList<>();
//				for (int k = 0; k < tasks.get(j).size(); k++) {
//					SporadicTask other_task = tasks.get(j).get(k);
//					if (other_task.resource_required_index.contains(resource.id - 1)) {
//						for (int l = 0; l < other_task.number_of_access_in_one_release.get(other_task.resource_required_index.indexOf(resource.id - 1)); l++) {
//							String variable = other_task.id - 1 + "|" + (resource.id - 1) + "|" + l + "|" + "SD";
//							constrain.add(addGetVariable(variables, variable));
//						}
//					}
//				}
//				if (constrain.size() > 0) {
//					int variable_index = addGetVariable(variables, 0 + "|" + (resource.id - 1) + "|" + 0 + "|" + "OT");
//					constrain.add(variable_index * -1);
//
//					constraints_neq.add(constrain);
//					values_neq.add(ncs);
//				}
//			}
//
//		}
//	}
//
//	public int addGetVariable(ArrayList<String> variables, String variable) {
//		if (!variables.contains(variable))
//			variables.add(variable);
//
//		int index = variables.indexOf(variable);
//		return index;
//
//	}
//
//	public int getMaxPreemption(SporadicTask task, ArrayList<SporadicTask> tasks, long Ri) {
//		int max_preemption = 0;
//
//		for (int i = 0; i < tasks.size(); i++) {
//			SporadicTask other_task = tasks.get(i);
//			if (other_task.priority > task.priority) {
//				max_preemption += Math.ceil((double) (Ri) / (double) other_task.period);
//			}
//		}
//
//		return max_preemption;
//	}
//
//	/*
//	 * gives that number of requests from HP local tasks for a resource that is required by the given task.
//	 */
//	private int getNoRFromHP(Resource resource, SporadicTask task, ArrayList<SporadicTask> tasks, long[] Ris, long Ri) {
//		int number_of_request_by_HP = 0;
//		int priority = task.priority;
//
//		for (int i = 0; i < tasks.size(); i++) {
//			if (tasks.get(i).priority > priority && tasks.get(i).resource_required_index.contains(resource.id - 1)) {
//				SporadicTask hpTask = tasks.get(i);
//				int indexR = tasks.get(i).resource_required_index.indexOf(resource.id - 1);
//				number_of_request_by_HP += Math.ceil((double) (Ri + (use_deadline_insteadof_Ri ? hpTask.deadline : Ris[i])) / (double) hpTask.period)
//						* hpTask.number_of_access_in_one_release.get(indexR);
//			}
//		}
//		return number_of_request_by_HP;
//	}
//
//	public void printLIP(ArrayList<Integer> objective, ArrayList<Integer> coefficient, ArrayList<ArrayList<Integer>> constraints_neq, ArrayList<Integer> values_neq,
//			ArrayList<ArrayList<Integer>> constraints_eq, ArrayList<Integer> values_eq, ArrayList<String> variables, ArrayList<Integer> binary_index, boolean printDebug) {
//		if (printDebug) {
//			System.out.println("variables: " + variables.size());
//			for (int i = 0; i < variables.size(); i++) {
//				System.out.println("variable index: " + i + " variable: " + variables.get(i));
//			}
//
//			System.out.println("binrays: " + binary_index.size());
//			for (int i = 0; i < binary_index.size(); i++) {
//				System.out.println("variable index: " + binary_index.get(i) + " variable: " + variables.get(binary_index.get(i)));
//			}
//
//			System.out.println("objective function: " + objective.size());
//			for (int i = 0; i < objective.size(); i++) {
//				System.out.print(coefficient.get(i) + " * " + objective.get(i) + "      ");
//			}
//			System.out.println();
//
//			System.out.println("not equal constrains: " + values_neq.size());
//			for (int i = 0; i < values_neq.size(); i++) {
//				for (int j = 0; j < constraints_neq.get(i).size(); j++) {
//					System.out.print(constraints_neq.get(i).get(j) + "      ");
//				}
//				System.out.println("<= " + values_neq.get(i));
//			}
//
//			System.out.println("equal constrains: " + values_eq.size());
//			for (int i = 0; i < values_eq.size(); i++) {
//				for (int j = 0; j < constraints_eq.get(i).size(); j++) {
//					System.out.print(constraints_eq.get(i).get(j) + "      ");
//				}
//				System.out.println("= " + values_eq.get(i));
//			}
//		}
//
//	}
//}
