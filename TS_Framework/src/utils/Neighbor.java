package utils;

public class Neighbor {
	private Integer bestCandIn;
    private Integer bestCandOut;
    private Double minDeltaCost;
	
	public Neighbor()
	{
        this.bestCandIn = null;
        this.bestCandOut = null;
        this.minDeltaCost = Double.POSITIVE_INFINITY;
	}
	
	public Integer getBestCandIn()
	{
		return bestCandIn;
	}
	
	public Integer getBestCandOut()
	{
		return bestCandOut;
	}
	
	public Double getMinDeltaCost()
	{
		return minDeltaCost;
	}
	
	public void setBestCandIn( Integer a)
	{
		bestCandIn = a;
	}
	
	public void setBestCandOut(Integer a)
	{
		bestCandOut = a;
	}
	
	public void setMinDeltaCost(Double a)
	{
		minDeltaCost = a;
	}
}
