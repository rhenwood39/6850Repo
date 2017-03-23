package researchReport6850;

public class simulation {

	private static final double CONST = 8;
	
	public static void main(String[] args) {
		int nPeople = 1000;
		int nLeft = 0;
		int nRight = 0;
		int nCenter = 0;
		
		for (int p = 0; p < nPeople; p++) {
			double leftUnits = 1;
			double rightUnits = 1;
			
			for (int i = 0; i < 1000; i++) {
				double draw = Math.random();
				double ratio = leftUnits / (leftUnits + rightUnits);
				if (draw < ratio)
					leftUnits += CONST;
				else
					rightUnits += CONST;
			}
			
			double ratio = leftUnits / (leftUnits + rightUnits);
			if (ratio < 0.1)
				nRight++;
			else if (ratio > 0.9)
				nLeft++;
			else
				nCenter++;
		}
		
		System.out.println("Left: " + 1.0*nLeft / nPeople);
		System.out.println("Center: " + 1.0*nCenter / nPeople);
		System.out.println("Right: " + 1.0*nRight / nPeople);
	}
}
