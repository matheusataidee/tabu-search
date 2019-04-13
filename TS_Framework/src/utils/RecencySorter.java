package utils;

import java.util.Comparator;

public class RecencySorter implements Comparator<Recency>{
	public int compare(Recency a, Recency b)
	{
		return b.getQtd() - a.getQtd();
	}
}
