package evaluationSection3;

import java.util.ArrayList;
import java.util.Random;

public class LogUniformTest {

	public static void main(String[] args) {
		Random random = new Random();
		ArrayList<Integer> uniform = new ArrayList<>();
		ArrayList<Integer> loguniform = new ArrayList<>();

		for (int i = 0; i < 10; i++) {
			int period = random.nextInt(999) + 1;
			uniform.add(period);

			double a1 = Math.log(1);
			double a2 = Math.log(1001);
			// System.out.println("a1: " + a1 + " a2: " + a2);
			double scaled = random.nextDouble() * (a2 - a1);
			double shifted = scaled + a1;

			// System.out.println(shifted);

			double exp = Math.exp(shifted);
			// System.out.println("exp: " + exp);

			int result = (int) exp;
			result = Math.max(1, result);
			result = Math.min(1000, result);
			// System.out.println("result: " + result);
			loguniform.add(result);
			// System.out.println();
		}

		System.out.println("uniform");
		for (int i = 0; i < uniform.size(); i++) {
			System.out.print(uniform.get(i) + "  ");
		}
		System.out.println();
		System.out.println("log uniform");
		for (int i = 0; i < loguniform.size(); i++) {
			System.out.print(loguniform.get(i) + "  ");
		}
	}

}
