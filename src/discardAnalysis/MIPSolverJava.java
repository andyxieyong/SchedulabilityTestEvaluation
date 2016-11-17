package discardAnalysis;
//package analysis;
//
//import java.util.ArrayList;
//
//import org.gnu.glpk.GLPK;
//import org.gnu.glpk.GLPKConstants;
//import org.gnu.glpk.SWIGTYPE_p_double;
//import org.gnu.glpk.SWIGTYPE_p_int;
//import org.gnu.glpk.glp_iocp;
//import org.gnu.glpk.glp_prob;
//
//public class MIPSolverJava {
//
//	glp_prob lp;
//	glp_iocp iocp;
//	SWIGTYPE_p_int ind = null;
//	SWIGTYPE_p_double val = null;
//	ArrayList<SWIGTYPE_p_int> inds = new ArrayList<>();
//	ArrayList<SWIGTYPE_p_double> vals = new ArrayList<>();
//	int ret;
//	
//	public ArrayList<String> variables = new ArrayList<>();
//	public ArrayList<Integer> binary_index = new ArrayList<>();
//	public ArrayList<ArrayList<Integer>> constraints_neq = new ArrayList<>();
//	public ArrayList<Integer> values_neq = new ArrayList<>();
//	public ArrayList<ArrayList<Integer>> constraints_eq = new ArrayList<>();
//	public ArrayList<Integer> values_eq = new ArrayList<>();
//	public ArrayList<Integer> objective = new ArrayList<>();
//	public ArrayList<Integer> coefficient = new ArrayList<>();
//
//	public MIPSolverJava() {
//		variables.add(null);
//	}
//
//	public void setParameters(boolean printDebug) {
//		/** Create problem **/
//		lp = GLPK.glp_create_prob();
//		GLPK.glp_set_prob_name(lp, "my MIP locking analysis solver");
//
//		/** Define columns **/
//		GLPK.glp_add_cols(lp, variables.size() - 1);
//
//		for (int i = 1; i < variables.size(); i++) {
//			if (!binary_index.contains(i)) {
//				GLPK.glp_set_col_name(lp, i, variables.get(i));
//				GLPK.glp_set_col_kind(lp, i, GLPKConstants.GLP_IV);
//				GLPK.glp_set_col_bnds(lp, i, GLPKConstants.GLP_LO, 0, 0);
//			} else {
//				GLPK.glp_set_col_name(lp, i, variables.get(i));
//				GLPK.glp_set_col_kind(lp, i, GLPKConstants.GLP_BV);
//				GLPK.glp_set_col_bnds(lp, i, GLPKConstants.GLP_DB, 0, 1);
//			}
//		}
//
//		/** Define constraints **/
//		GLPK.glp_add_rows(lp, constraints_neq.size() + constraints_eq.size());
//
//		int constrain_num = 0;
//		for (; constrain_num < constraints_neq.size(); constrain_num++) {
//			GLPK.glp_set_row_name(lp, constrain_num + 1, "neq " + (constrain_num + 1));
//			GLPK.glp_set_row_bnds(lp, constrain_num + 1, GLPKConstants.GLP_UP, 0, values_neq.get(constrain_num));
//
//			ind = GLPK.new_intArray(constraints_neq.get(constrain_num).size() + 1);
//			val = GLPK.new_doubleArray(constraints_neq.get(constrain_num).size() + 1);
//
//			inds.add(ind);
//			vals.add(val);
//			for (int j = 0; j < constraints_neq.get(constrain_num).size(); j++) {
//				int original_variable_index = constraints_neq.get(constrain_num).get(j);
//				int variable_index = 0;
//				int value = 0;
//				if (original_variable_index >= 0) {
//					variable_index = original_variable_index;
//					value = 1;
//				} else {
//					variable_index = -original_variable_index;
//					value = -1;
//				}
//
//				GLPK.intArray_setitem(ind, j + 1, variable_index);
//				GLPK.doubleArray_setitem(val, j + 1, value);
//			}
//			int len = constraints_neq.get(constrain_num).size();
//			GLPK.glp_set_mat_row(lp, constrain_num + 1, len, ind, val);
//		}
//
//		for (int i = 0; i < constraints_eq.size(); i++) {
//			GLPK.glp_set_row_name(lp, constrain_num + 1, "eq " + (constrain_num + 1));
//			GLPK.glp_set_row_bnds(lp, constrain_num + 1, GLPKConstants.GLP_FX, 0, values_eq.get(i));
//
//			ind = GLPK.new_intArray(constraints_eq.get(i).size() + 1);
//			val = GLPK.new_doubleArray(constraints_eq.get(i).size() + 1);
//
//			inds.add(ind);
//			vals.add(val);
//			for (int j = 0; j < constraints_eq.get(i).size(); j++) {
//				int original_variable_index = constraints_eq.get(i).get(j);
//				int variable_index = 0;
//				int value = 0;
//				if (original_variable_index >= 0) {
//					variable_index = original_variable_index;
//					value = 1;
//				} else {
//					variable_index = -original_variable_index;
//					value = -1;
//				}
//
//				GLPK.intArray_setitem(ind, j + 1, variable_index);
//				GLPK.doubleArray_setitem(val, j + 1, value);
//			}
//			GLPK.glp_set_mat_row(lp, constrain_num + 1, constraints_eq.get(i).size(), ind, val);
//			constrain_num++;
//		}
//
//		/** Define Objective Function **/
//		GLPK.glp_set_obj_name(lp, "objective function");
//		GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MAX);
//
//		GLPK.glp_set_obj_coef(lp, 0, 0);
//		for (int i = 0; i < objective.size(); i++) {
//			GLPK.glp_set_obj_coef(lp, objective.get(i), coefficient.get(i));
//		}
//	}
//
//	public void changeParameter(ArrayList<Integer> values, int index) {
//		for (int i = 0; i < values.size(); i++) {
//			GLPK.glp_set_row_bnds(lp, index, GLPKConstants.GLP_UP, 0, values.get(i));
//			index++;
//		}
//
//	}
//
//	public double[] solve(boolean printDebug) {
//		/** Solve the model **/
////		if (printDebug)
////			GLPK.glp_term_out(GLPKConstants.GLP_ON);
////		else
//			GLPK.glp_term_out(GLPKConstants.GLP_OFF);
//
//		iocp = new glp_iocp();
//		GLPK.glp_init_iocp(iocp);
//		iocp.setPresolve(GLPKConstants.GLP_ON);
//		ret = GLPK.glp_intopt(lp, iocp);
//
//		double[] blocking = new double[3];
//		blocking[0] = 0;
//		blocking[1] = 0;
//		blocking[2] = 0;
//
//		if (ret == 0 && GLPK.glp_mip_status(lp) == GLPK.GLP_OPT) {
//			blocking[0] = GLPK.glp_mip_obj_val(lp);
//			int i;
//			double value;
//			for (i = 0; i < objective.size(); i++) {
//				int variable_index = objective.get(i);
//				value = GLPK.glp_mip_col_val(lp, variable_index);
////				System.out.println("index: " + (i+1) + " value: " + value);
//
//				if (objective.get(i) % 2 == 1) {
//					blocking[1] += value * coefficient.get(i);
//				} else
//					blocking[2] += value * coefficient.get(i);
//			}
//		}
//
////		System.out.println("total blocking: " + blocking[0]);
//		return blocking;
//	}
//
//	public void delete() {
//		/** Free Memory **/
//		GLPK.glp_delete_prob(lp);
//		for (int i = 0; i < inds.size(); i++) {
//			GLPK.delete_intArray(inds.get(i));
//			GLPK.delete_doubleArray(vals.get(i));
//		}
//	}
//}
