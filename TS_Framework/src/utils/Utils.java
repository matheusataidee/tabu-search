package utils;

import java.util.ArrayList;
import java.util.List;

public class Utils {
	public static int BEST_IMPROVEMENT = 0;
	public static int FIRST_IMPROVEMENT = 1;
	public static int DEFAULT_METHOD = 0;
	public static int INTENSIFICATION_METHOD = 1;
	public static int OSCILATION_METHOD = 2;
	
	public static List<ProibitedTuple> getProibitedTuples(int size)
	{
		List<ProibitedTuple> myList = new ArrayList<ProibitedTuple>();
		int pg1 = 131;
		int pg2 = 1031;
		int ph1 = 193;
		int ph2 = 1093;

		for (int n = 1; n <= size; n++) 
		{
			Integer temp_x0 = 0, temp_x1 = 0, temp_x2 = 0;
			temp_x0 = n - 1;
			int g = 1 + ((pg1 * (n - 1) + pg2) % size);

			if (n == g)g += 1;
			temp_x1 = g -1;

			int h = 1 + ((ph1 * (n - 1) + ph2) % size);

			if (!(h != g && h != n)) {
				if ((1 + (h % size)) != n && (1 + (h % size)) != g) {
					h = 1 + (h % size);
				} else {
					h = 1 + ((h + 1) % size);
				}
			}
			temp_x2 = h - 1;
			
			myList.add(new ProibitedTuple(temp_x0, temp_x1, temp_x2));
		}
		
		return myList;
	}
	
	
}
