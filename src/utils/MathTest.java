package utils;

import java.util.Random;

public class MathTest {

	public static void main(String args[]) {
		Random ran = new Random(1000);
		
		for(int i=0; i<10000000; i++) {
			double a = ran.nextInt(1000)+1;
			double b = ran.nextInt(1000)+1;
			double c = ran.nextInt(1000)+1;
			
			Double ceilingLeft = Math.ceil(a/c);
			Double ceilingRight = Math.ceil((a+b)/c);
			Double x = Math.ceil((a%c+b)/c)-Math.ceil((a%c)/c);
			
			int leftRes = ceilingLeft.intValue() + x.intValue();
			int rightRes = ceilingRight.intValue();
			
			if(leftRes != rightRes) {
				System.out.println("!!!");
				System.out.println("!!!");
				System.out.println("!!!");
			}
		}
		
		
		
		
	}
}
