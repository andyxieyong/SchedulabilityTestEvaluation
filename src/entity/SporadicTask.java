package entity;

import java.util.ArrayList;

public class SporadicTask {
	public int priority;
	public long period;
	public long deadline;
	public long WCET;
	public int partition;
	public int id;

	public long pure_resource_execution_time = 0;
	public long Ri = 0, spin = 0, interference = 0, local = 0, total_blocking = 0;

	public ArrayList<Integer> resource_required_index;
	public ArrayList<Integer> number_of_access_in_one_release;
	
	public int hasResource = 0;
	public int[] resource_required_index_cpoy = null;
	public int[] number_of_access_in_one_release_copy = null;

//	public MIPSolverJava solver;
	public int index = 0;

	public SporadicTask(int priority, long t, long c, int partition, int id) {
		this.priority = priority;
		this.period = t;
		this.WCET = c;
		this.deadline = t;
		this.partition = partition;
		this.id = id;

		resource_required_index = new ArrayList<>();
		number_of_access_in_one_release = new ArrayList<>();
		
		resource_required_index_cpoy = null;
		number_of_access_in_one_release_copy = null;

		Ri = 0;
		spin = 0;
		interference = 0;
		local = 0;
	}

	@Override
	public String toString() {
		return "T" + this.id + " : T = " + this.period + ", C = " + this.WCET + ", PRET: "
				+ this.pure_resource_execution_time + ", D = " + this.deadline + ", Priority = " + this.priority
				+ ", Partition = " + this.partition;
	}

	public String RTA() {
		return "T" + this.id + " : R = " + this.Ri + ", S = " + this.spin + ", I = " + this.interference + ", A = "
				+ this.local + ". is schedulable: " + (Ri <= deadline);
	}

//	public void initTaskVariables() {
//		variables.clear();
//		binary_index.clear();
//		fixed_constraints_neq.clear();
//		fixed_values_neq.clear();
//		fixed_constraints_eq.clear();
//		fixed_values_eq.clear();
//		objective.clear();
//		coefficient.clear();
//	}

	// public void generateVariables(ArrayList<String> variables){
	// for(int i=0;i<number_of_access_in_one_release.size();i++){
	// ArrayList<Integer> both = new ArrayList<>();
	// ArrayList<Integer> sd = new ArrayList<>();
	// ArrayList<Integer> lb = new ArrayList<>();
	// for(int j=0;j<number_of_access_in_one_release.get(i);j++){
	// String var1 = (this.id - 1) + "|" + this.resource_required_index.get(i) + "|" + j + "|" + "SD";
	// String var2 = (this.id - 1) + "|" + this.resource_required_index.get(i) + "|" + j + "|" + "LB";
	//
	// if (!variables.contains(var1))
	// variables.add(var1);
	//
	// int index1 = variables.indexOf(var1);
	//
	// if (!variables.contains(var2))
	// variables.add(var2);
	//
	// int index2 = variables.indexOf(var2);
	//
	// both.add(index1);
	// both.add(index2);
	//
	// sd.add(index1);
	// lb.add(index2);
	// }
	//
	// LBvariables.add(lb);
	// SDvariables.add(sd);
	// Bothvariables.add(both);
	// }
	// }

}
