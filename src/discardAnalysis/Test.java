// package discardAnalysis;
//
// import org.gnu.glpk.GLPK;
// import org.gnu.glpk.GLPKConstants;
// import org.gnu.glpk.GlpkException;
// import org.gnu.glpk.SWIGTYPE_p_double;
// import org.gnu.glpk.SWIGTYPE_p_int;
// import org.gnu.glpk.glp_iocp;
// import org.gnu.glpk.glp_prob;
// import org.gnu.glpk.glp_smcp;
//
// public class Test {
// public static void main(String[] args) {
// mipExample();
// }
//
// // Maximize z = 17 * x1 + 12* x2
// // subject to
// // 10 x1 + 7 x2 <= 40
// // x1 + x2 <= 5
// // where,
// // 0.0 <= x1 integer
// // 0.0 <= x2 integer
// public static void mipExample() {
// glp_prob lp;
// glp_iocp iocp;
// SWIGTYPE_p_int ind;
// SWIGTYPE_p_double val;
// int ret;
//
// // Create problem
// lp = GLPK.glp_create_prob();
// System.out.println("Problem created");
// GLPK.glp_set_prob_name(lp, "myProblem");
//
// // Define columns
// GLPK.glp_add_cols(lp, 2);
// GLPK.glp_set_col_name(lp, 1, "x1");
// GLPK.glp_set_col_kind(lp, 1, GLPKConstants.GLP_IV);
// GLPK.glp_set_col_bnds(lp, 1, GLPKConstants.GLP_LO, 0, 0);
// GLPK.glp_set_col_name(lp, 2, "x2");
// GLPK.glp_set_col_kind(lp, 2, GLPKConstants.GLP_IV);
// GLPK.glp_set_col_bnds(lp, 2, GLPKConstants.GLP_LO, 0, 0);
//
// // Create constraints
// GLPK.glp_add_rows(lp, 2);
// GLPK.glp_set_row_name(lp, 1, "c1");
// GLPK.glp_set_row_bnds(lp, 1, GLPKConstants.GLP_UP, 0, 40);
//
// ind = GLPK.new_intArray(3);
// GLPK.intArray_setitem(ind, 1, 1);
// GLPK.intArray_setitem(ind, 2, 2);
// val = GLPK.new_doubleArray(3);
// GLPK.doubleArray_setitem(val, 1, 10);
// GLPK.doubleArray_setitem(val, 2, 7);
// GLPK.glp_set_mat_row(lp, 1, 2, ind, val);
//
// ind = GLPK.new_intArray(3);
// GLPK.intArray_setitem(ind, 1, 1);
// GLPK.intArray_setitem(ind, 2, 2);
// val = GLPK.new_doubleArray(3);
// GLPK.glp_set_row_name(lp, 2, "c2");
// GLPK.glp_set_row_bnds(lp, 2, GLPKConstants.GLP_UP, 0, 5);
// GLPK.doubleArray_setitem(val, 1, 1);
// GLPK.doubleArray_setitem(val, 2, 1);
// GLPK.glp_set_mat_row(lp, 2, 2, ind, val);
//
// // Define objective
// GLPK.glp_set_obj_name(lp, "obj");
// GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MAX);
// GLPK.glp_set_obj_coef(lp, 0, 0);
// GLPK.glp_set_obj_coef(lp, 1, 17);
// GLPK.glp_set_obj_coef(lp, 2, 12);
//
// GLPK.glp_term_out(GLPKConstants.GLP_OFF);
// // solve model
// iocp = new glp_iocp();
// GLPK.glp_init_iocp(iocp);
// iocp.setPresolve(GLPKConstants.GLP_ON);
// // GLPK.glp_write_lp(lp, null, "yi.lp");
// ret = GLPK.glp_intopt(lp, iocp);
//
// // Retrieve solution
// if (ret == 0) {
// write_mip_solution(lp);
// } else {
// System.out.println("The problemcould not be solved");
// }
// ;
//
// // free memory
// GLPK.glp_delete_prob(lp);
// }
//
// static void write_mip_solution(glp_prob lp) {
// int i;
// int n;
// String name;
// double val;
//
// name = GLPK.glp_get_obj_name(lp);
// val = GLPK.glp_mip_obj_val(lp);
// System.out.print(name);
// System.out.print(" = ");
// System.out.println(val);
// n = GLPK.glp_get_num_cols(lp);
// for (i = 1; i <= n; i++) {
// name = GLPK.glp_get_col_name(lp, i);
// val = GLPK.glp_mip_col_val(lp, i);
// System.out.print(name);
// System.out.print(" = ");
// System.out.println(val);
// }
// }
//
// // Minimize z = (x1 -x2) /2 + (1 -(x1 -x2 )) = -.5 * x1 + .5 * x2 + 1
// //
// // subject to
// // 0.0 <= x1 - x2 <= 0.2
// // where ,
// // 0.0 <= x1 <= 0.5
// // 0.0 <= x2 <= 0.5
// public static void lpExample() {
// glp_prob lp;
// glp_smcp parm;
// SWIGTYPE_p_int ind;
// SWIGTYPE_p_double val;
// int ret;
// try {
// // Create problem
// lp = GLPK.glp_create_prob();
// System.out.println(" Problem created ");
// GLPK.glp_set_prob_name(lp, " myProblem ");
// // Define columns
// GLPK.glp_add_cols(lp, 2);
// GLPK.glp_set_col_name(lp, 1, "x1");
// GLPK.glp_set_col_kind(lp, 1, GLPKConstants.GLP_CV);
// GLPK.glp_set_col_bnds(lp, 1, GLPKConstants.GLP_DB, -1, 1);
// GLPK.glp_set_col_name(lp, 2, "x2");
// GLPK.glp_set_col_kind(lp, 2, GLPKConstants.GLP_CV);
// GLPK.glp_set_col_bnds(lp, 2, GLPKConstants.GLP_DB, -1, 1);
// // Create constraints
// GLPK.glp_add_rows(lp, 1);
// GLPK.glp_set_row_name(lp, 1, "c1");
// GLPK.glp_set_row_bnds(lp, 1, GLPKConstants.GLP_DB, 0, 1200);
// ind = GLPK.new_intArray(3);
// GLPK.intArray_setitem(ind, 1, 1);
// GLPK.intArray_setitem(ind, 2, 2);
// val = GLPK.new_doubleArray(3);
// GLPK.doubleArray_setitem(val, 1, -1.);
// GLPK.doubleArray_setitem(val, 2, 1.);
// GLPK.glp_set_mat_row(lp, 1, 2, ind, val);
// GLPK.delete_intArray(ind);
// GLPK.delete_doubleArray(val);
// // Define objective max z = (x1 -x2) /2 + (1 -(x1 -x2 )) = -.5 * x1 + .5 * x2
// + 1
// GLPK.glp_set_obj_name(lp, "z");
// GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MAX);
// GLPK.glp_set_obj_coef(lp, 0, 1);
// GLPK.glp_set_obj_coef(lp, 1, -0.5);
// // GLPK.glp_set_obj_coef(lp, 2, 0.5);
// // Solve model
// parm = new glp_smcp();
// GLPK.glp_init_smcp(parm);
// ret = GLPK.glp_simplex(lp, parm);
// // Retrieve solution
// if (ret == 0) {
// write_lp_solution(lp);
// } else {
// System.out.println("The problem could not be solved ");
// }
// // Free memory
// GLPK.glp_delete_prob(lp);
// } catch (GlpkException ex) {
// ex.printStackTrace();
// }
// }
//
// /**
// * write simplex solution
// *
// * @param lp
// * problem
// */
// static void write_lp_solution(glp_prob lp) {
// int i;
// int n;
// String name;
// double val;
// name = GLPK.glp_get_obj_name(lp);
// val = GLPK.glp_get_obj_val(lp);
// System.out.print(name);
// System.out.print(" = ");
// System.out.println(val);
// n = GLPK.glp_get_num_cols(lp);
// for (i = 1; i <= n; i++) {
// name = GLPK.glp_get_col_name(lp, i);
// val = GLPK.glp_get_col_prim(lp, i);
// System.out.print(name);
// System.out.print(" = ");
// System.out.println(val);
// }
// }
//
// }