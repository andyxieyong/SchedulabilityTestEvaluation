package discardFiles;
// package discardAnalysis;
//
// import java.util.ArrayList;
//
// import analysis.MIPSolverJava;
// import analysis.Utils;
// import entity.Resource;
// import entity.SporadicTask;
// import javaToC.MIPSolverC;
//
// public class FIFONonPreemptiveLinear {
// long count = 0;
// private static boolean use_deadline_insteadof_Ri = false;
// MIPSolverC solverC = new MIPSolverC();
//
// public long[][] NewMrsPRTATest(ArrayList<ArrayList<SporadicTask>> tasks,
// ArrayList<Resource> resources, boolean printBebug,
// boolean printLIP) {
// long[][] init_Ri = new Utils().initResponseTime(tasks);
//
// for (int i = 0; i < init_Ri.length; i++) {
// for (int j = 0; j < init_Ri[i].length; j++) {
// if (init_Ri[i][j] > tasks.get(i).get(j).deadline)
// return init_Ri;
// }
// }
//
// long[][] response_time = new long[tasks.size()][];
// boolean isEqual = false, missDeadline = false;
// count = 0;
//
// for (int i = 0; i < init_Ri.length; i++)
// response_time[i] = new long[init_Ri[i].length];
// new Utils().cloneList(init_Ri, response_time);
//
// /** generate variables, binary variables, and fixed constrains. **/
// for (int i = 0; i < tasks.size(); i++) {
// for (int j = 0; j < tasks.get(i).size(); j++) {
// SporadicTask task = tasks.get(i).get(j);
// task.solver = new MIPSolverJava();
// }
// }
// for (int i = 0; i < tasks.size(); i++) {
// for (int j = 0; j < tasks.get(i).size(); j++) {
// SporadicTask task = tasks.get(i).get(j);
// generateFixedConstrains(task, tasks, resources, response_time,
// response_time[i][j], printLIP);
// }
// }
//
// /* a huge busy window to get a fixed Ri */
// while (!isEqual && !missDeadline) {
// isEqual = true;
// long[][] response_time_plus = busyWindow(tasks, resources, response_time,
// printLIP);
//
// for (int i = 0; i < response_time_plus.length; i++) {
// for (int j = 0; j < response_time_plus[i].length; j++) {
// if (response_time[i][j] != response_time_plus[i][j])
// isEqual = false;
// if (response_time_plus[i][j] > tasks.get(i).get(j).deadline)
// missDeadline = true;
// }
// }
//
// count++;
// new Utils().cloneList(response_time_plus, response_time);
// }
//
// /** free solver memory. **/
// for (int i = 0; i < tasks.size(); i++) {
// for (int j = 0; j < tasks.get(i).size(); j++) {
// SporadicTask task = tasks.get(i).get(j);
// task.solver.delete();
// task.solver = null;
// }
// }
//
// if (printBebug) {
// if (missDeadline)
// System.out.println("LIP-FIFO-NP after " + count + " tims of recursion, the
// tasks miss the deadline.");
// else
// System.out.println("LIP-FIFO-NP after " + count + " tims of recursion, we got
// the response time.");
// new Utils().printResponseTime(response_time, tasks);
// }
//
// return response_time;
// }
//
// private long[][] busyWindow(ArrayList<ArrayList<SporadicTask>> tasks,
// ArrayList<Resource> resources, long[][] response_time,
// boolean printBebug) {
// long[][] response_time_plus = new long[tasks.size()][];
// for (int i = 0; i < response_time.length; i++)
// response_time_plus[i] = new long[response_time[i].length];
//
// for (int i = 0; i < tasks.size(); i++) {
// for (int j = 0; j < tasks.get(i).size(); j++) {
// SporadicTask task = tasks.get(i).get(j);
// task.interference = highPriorityInterference(task, tasks,
// response_time[i][j]);
//
// double blocking[] = solveMILPForOneTask(task, tasks, resources,
// response_time, response_time[i][j], printBebug);
//
// task.total_blocking = (long) blocking[0];
// task.spin = (long) blocking[1];
// task.local = (long) blocking[2];
//
// response_time_plus[i][j] = task.Ri = task.WCET +
// task.pure_resource_execution_time + task.interference + task.spin
// + task.local;
// if (task.Ri > task.deadline)
// return response_time_plus;
// }
// }
// return response_time_plus;
//
// }
//
// /*
// * Calculate the local high priority tasks' interference for a given task t.
// CI is a set of computation time of local tasks, including
// * spin delay.
// */
// private long highPriorityInterference(SporadicTask t,
// ArrayList<ArrayList<SporadicTask>> allTasks, long Ri) {
// long interference = 0;
// int partition = t.partition;
// ArrayList<SporadicTask> tasks = allTasks.get(partition);
//
// for (int i = 0; i < tasks.size(); i++) {
// if (tasks.get(i).priority > t.priority) {
// SporadicTask hpTask = tasks.get(i);
// interference += Math.ceil((double) (Ri) / (double) hpTask.period) *
// (hpTask.WCET + hpTask.pure_resource_execution_time);
// }
// }
// return interference;
// }
//
// /******************************************
// ****** Linear Programming Generator *********
// ******************************************/
// public void generateFixedConstrains(SporadicTask task,
// ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources,
// long[][] Ris, long Ri, boolean printDebug) {
//
// generateObjectiveandC1C2C5C6(task, tasks, resources);
// c2c3c4(task, tasks, resources);
//
// constrainSeven(task, tasks, resources);
// constrainNine(task, tasks, resources);
// task.index = task.solver.constraints_neq.size() + 1;
// constrainEight(task, tasks, resources, Ris, Ri);
//
// printLIP(task, printDebug);
//
// task.solver.setParameters(printDebug);
//
// }
//
// public double[] solveMILPForOneTask(SporadicTask task,
// ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources,
// long[][] Ris, long Ri, boolean printDebug) {
// ArrayList<Integer> values = getNewValues(task, tasks, resources, Ris, Ri);
//
// task.solver.changeParameter(values, task.index);
// double[] blocking = task.solver.solve(printDebug);
//
// return blocking;
// }
//
// public void generateObjectiveandC1C2C5C6(SporadicTask task,
// ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources) {
// ArrayList<Integer> constrain5 = new ArrayList<>();
// ArrayList<Integer> constrain6 = new ArrayList<>();
//
// for (int i = 0; i < tasks.size(); i++) {
// for (int j = 0; j < tasks.get(i).size(); j++) {
// SporadicTask other_task = tasks.get(i).get(j);
//
// if (other_task != task) {
// for (int k = 0; k < other_task.number_of_access_in_one_release.size(); k++) {
// for (int l = 0; l < other_task.number_of_access_in_one_release.get(k); l++) {
// Resource resource = resources.get(other_task.resource_required_index.get(k));
//
// int variable1 = addGetVariable(task.solver.variables,
// (other_task.id - 1) + "|" + other_task.resource_required_index.get(k) + "|" +
// l + "|" + "SD");
// int variable2 = addGetVariable(task.solver.variables,
// (other_task.id - 1) + "|" + other_task.resource_required_index.get(k) + "|" +
// l + "|" + "LB");
//
// task.solver.objective.add(variable1);
// task.solver.objective.add(variable2);
//
// task.solver.coefficient.add((int) resource.csl);
// task.solver.coefficient.add((int) resource.csl);
//
// ArrayList<Integer> constrain1 = new ArrayList<>();
// constrain1.add(variable1);
// constrain1.add(variable2);
//
// task.solver.constraints_neq.add(constrain1);
// task.solver.values_neq.add(1);
//
// if (other_task.partition == task.partition && other_task.priority >
// task.priority) {
// constrain5.add(variable2);
// }
//
// if (other_task.partition == task.partition) {
// constrain6.add(variable1);
// }
// }
// }
// }
//
// }
// }
//
// if (constrain5.size() > 0) {
// task.solver.constraints_neq.add(constrain5);
// task.solver.values_neq.add(0);
// }
//
// if (constrain6.size() > 0) {
// task.solver.constraints_neq.add(constrain6);
// task.solver.values_neq.add(0);
// }
// }
//
// public void c2c3c4(SporadicTask task, ArrayList<ArrayList<SporadicTask>>
// tasks, ArrayList<Resource> resources) {
// ArrayList<Integer> constrain2 = new ArrayList<>();
// ArrayList<Integer> constrain3 = new ArrayList<>();
// ArrayList<Integer> constrain4 = new ArrayList<>();
//
// for (int i = 0; i < resources.size(); i++) {
// Resource resource = resources.get(i);
// int variable_index = addGetVariable(task.solver.variables, 0 + "|" +
// (resource.id - 1) + "|" + 0 + "|" + "PR");
//
// if (resource.partitions.size() == 1 && resource.partitions.get(0) ==
// task.partition && resource.ceiling.get(0) < task.priority)
// constrain2.add(variable_index);
//
// constrain3.add(variable_index);
//
// boolean isRequestByLLtasks = false;
// for (int j = 0; j < resource.requested_tasks.size(); j++) {
// SporadicTask other_task = resource.requested_tasks.get(j);
// if (other_task.partition == task.partition && other_task.priority <
// task.priority) {
// isRequestByLLtasks = true;
// break;
// }
// }
//
// if (!task.solver.binary_index.contains(variable_index))
// task.solver.binary_index.add(variable_index);
//
// if (!isRequestByLLtasks) {
// constrain4.add(variable_index);
// }
// }
//
// if (constrain2.size() > 0) {
// task.solver.constraints_eq.add(constrain2);
// task.solver.values_eq.add(0);
// }
//
// if (constrain3.size() > 0) {
// task.solver.constraints_neq.add(constrain3);
// task.solver.values_neq.add(1);
// }
//
// if (constrain4.size() > 0) {
// task.solver.constraints_neq.add(constrain4);
// task.solver.values_neq.add(0);
// }
// }
//
// public void constrainSeven(SporadicTask task,
// ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources) {
// for (int i = 0; i < resources.size(); i++) {
// ArrayList<Integer> constrain = new ArrayList<>();
// Resource resource = resources.get(i);
//
// for (int j = 0; j < resource.requested_tasks.size(); j++) {
// SporadicTask other_task = resource.requested_tasks.get(j);
// if (other_task.partition == task.partition && other_task.priority <
// task.priority) {
// for (int k = 0; k < other_task.number_of_access_in_one_release
// .get(other_task.resource_required_index.indexOf(resource.id - 1)); k++) {
// String variable = other_task.id - 1 + "|" + (resource.id - 1) + "|" + k + "|"
// + "LB";
// constrain.add(addGetVariable(task.solver.variables, variable));
// }
// }
// }
//
// if (constrain.size() > 0) {
// int variable_index = addGetVariable(task.solver.variables, 0 + "|" +
// (resource.id - 1) + "|" + 0 + "|" + "PR");
// constrain.add(variable_index * -1);
//
// task.solver.constraints_neq.add(constrain);
// task.solver.values_neq.add(0);
// }
// }
// }
//
// public void constrainNine(SporadicTask task,
// ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources) {
//
// for (int i = 0; i < tasks.size(); i++) {
// if (i == task.partition)
// continue;
//
// for (int k = 0; k < resources.size(); k++) {
// Resource resource = resources.get(k);
// ArrayList<Integer> constrain = new ArrayList<>();
//
// for (int j = 0; j < tasks.get(i).size(); j++) {
// SporadicTask other_task = tasks.get(i).get(j);
// if (other_task.resource_required_index.contains(resource.id - 1)) {
// for (int l = 0; l < other_task.number_of_access_in_one_release
// .get(other_task.resource_required_index.indexOf(resource.id - 1)); l++) {
// String variable = other_task.id - 1 + "|" + (resource.id - 1) + "|" + l + "|"
// + "LB";
// constrain.add(addGetVariable(task.solver.variables, variable));
// }
// }
// }
//
// if (constrain.size() > 0) {
// int variable_index = addGetVariable(task.solver.variables, 0 + "|" +
// (resource.id - 1) + "|" + 0 + "|" + "PR");
// constrain.add(variable_index * -1);
//
// task.solver.constraints_neq.add(constrain);
// task.solver.values_neq.add(0);
//
// if (!task.solver.binary_index.contains(variable_index))
// task.solver.binary_index.add(variable_index);
// }
// }
//
// }
// }
//
// public void constrainEight(SporadicTask task,
// ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources,
// long[][] Ris,
// long Ri) {
//
// for (int i = 0; i < resources.size(); i++) {
// Resource resource = resources.get(i);
//
// boolean requested_by_Task = task.resource_required_index.contains(resource.id
// - 1);
// int ncs = getNoRFromHP(resource, task, tasks.get(task.partition),
// Ris[task.partition], Ri) + (requested_by_Task
// ?
// task.number_of_access_in_one_release.get(task.resource_required_index.indexOf(resource.id
// - 1)) : 0);
//
// for (int j = 0; j < tasks.size(); j++) {
// if (j == task.partition)
// continue;
//
// ArrayList<Integer> constrain = new ArrayList<>();
// for (int k = 0; k < tasks.get(j).size(); k++) {
// SporadicTask other_task = tasks.get(j).get(k);
// if (other_task.resource_required_index.contains(resource.id - 1)) {
// for (int l = 0; l < other_task.number_of_access_in_one_release
// .get(other_task.resource_required_index.indexOf(resource.id - 1)); l++) {
// String variable = other_task.id - 1 + "|" + (resource.id - 1) + "|" + l + "|"
// + "SD";
// constrain.add(addGetVariable(task.solver.variables, variable));
// }
// }
// }
// if (constrain.size() > 0) {
// task.solver.constraints_neq.add(constrain);
// task.solver.values_neq.add(ncs);
// }
// }
//
// }
// }
//
// public ArrayList<Integer> getNewValues(SporadicTask task,
// ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources,
// long[][] Ris, long Ri) {
// ArrayList<Integer> values = new ArrayList<>();
//
// for (int i = 0; i < resources.size(); i++) {
// Resource resource = resources.get(i);
//
// boolean requested_by_Task = task.resource_required_index.contains(resource.id
// - 1);
// int ncs = getNoRFromHP(resource, task, tasks.get(task.partition),
// Ris[task.partition], Ri) + (requested_by_Task
// ?
// task.number_of_access_in_one_release.get(task.resource_required_index.indexOf(resource.id
// - 1)) : 0);
//
//// System.out.println("ncs: " + ncs);
// for (int j = 0; j < tasks.size(); j++) {
// if (j == task.partition)
// continue;
//
// boolean account = false;
// for (int k = 0; k < tasks.get(j).size(); k++) {
// SporadicTask other_task = tasks.get(j).get(k);
// if (other_task.resource_required_index.contains(resource.id - 1)) {
// account = true;
// }
// }
// if (account) {
// values.add(ncs);
// }
// }
//
// }
// return values;
// }
//
// public int addGetVariable(ArrayList<String> variables, String variable) {
// if (!variables.contains(variable))
// variables.add(variable);
//
// int index = variables.indexOf(variable);
// return index;
//
// }
//
// /*
// * gives that number of requests from HP local tasks for a resource that is
// required by the given task.
// */
// private int getNoRFromHP(Resource resource, SporadicTask task,
// ArrayList<SporadicTask> tasks, long[] Ris, long Ri) {
// int number_of_request_by_HP = 0;
// int priority = task.priority;
//
// for (int i = 0; i < tasks.size(); i++) {
// if (tasks.get(i).priority > priority &&
// tasks.get(i).resource_required_index.contains(resource.id - 1)) {
// SporadicTask hpTask = tasks.get(i);
// int indexR = tasks.get(i).resource_required_index.indexOf(resource.id - 1);
// number_of_request_by_HP += Math
// .ceil((double) (Ri + (use_deadline_insteadof_Ri ? hpTask.deadline : Ris[i]))
// / (double) hpTask.period)
// * hpTask.number_of_access_in_one_release.get(indexR);
// }
// }
// return number_of_request_by_HP;
// }
//
// public void printLIP(SporadicTask task, boolean printDebug) {
// if (printDebug) {
// System.out.println("variables: " + task.solver.variables.size());
// for (int i = 0; i < task.solver.variables.size(); i++) {
// System.out.println("variable index: " + i + " variable: " +
// task.solver.variables.get(i));
// }
//
// System.out.println("binrays: " + task.solver.binary_index.size());
// for (int i = 0; i < task.solver.binary_index.size(); i++) {
// System.out.println("variable index: " + task.solver.binary_index.get(i) + "
// variable: "
// + task.solver.variables.get(task.solver.binary_index.get(i)));
// }
//
// System.out.println("objective function: " + task.solver.objective.size());
// for (int i = 0; i < task.solver.objective.size(); i++) {
// System.out.print(task.solver.coefficient.get(i) + " * " +
// task.solver.objective.get(i) + " ");
// }
// System.out.println();
//
// System.out.println("not equal constrains: " + task.solver.values_neq.size());
// for (int i = 0; i < task.solver.values_neq.size(); i++) {
// for (int j = 0; j < task.solver.constraints_neq.get(i).size(); j++) {
// System.out.print(task.solver.constraints_neq.get(i).get(j) + " ");
// }
// System.out.println("<= " + task.solver.values_neq.get(i));
// }
//
// System.out.println("equal constrains: " + task.solver.values_eq.size());
// for (int i = 0; i < task.solver.values_eq.size(); i++) {
// for (int j = 0; j < task.solver.constraints_eq.get(i).size(); j++) {
// System.out.print(task.solver.constraints_eq.get(i).get(j) + " ");
// }
// System.out.println("= " + task.solver.values_eq.get(i));
// }
// }
//
// }
// }
