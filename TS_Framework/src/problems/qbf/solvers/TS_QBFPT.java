package problems.qbf.solvers;

import static utils.Utils.*;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import metaheuristics.tabusearch.AbstractTS;
import problems.qbf.QBF_Inverse;
import solutions.Solution;
import utils.Neighbor;
import utils.ProibitedTuple;
import utils.Utils;



/**
 * Metaheuristic TS (Tabu Search) for obtaining an optimal solution to a QBF
 * (Quadractive Binary Function -- {@link #QuadracticBinaryFunction}).
 * Since by default this TS considers minimization problems, an inverse QBF
 *  function is adopted.
 * 
 * @author ccavellucci, fusberti
 */
public class TS_QBFPT extends AbstractTS<Integer> {
	
	private final Integer fake = new Integer(-1);
	
	/**
	 * Constructor for the TS_QBF class. An inverse QBF objective function is
	 * passed as argument for the superclass constructor.
	 * 
	 * @param tenure
	 *            The Tabu tenure parameter.
	 * @param iterations
	 *            The number of iterations which the TS will be executed.
	 * @param filename
	 *            Name of the file for which the objective function parameters
	 *            should be read.
	 * @throws IOException
	 *             necessary for I/O operations.
	 */
	public TS_QBFPT(Integer tenure, Integer iterations, Integer searchMethod, String filename) throws IOException {
		super(new QBF_Inverse(filename), tenure, searchMethod, iterations);
	}

	/* (non-Javadoc)
	 * @see metaheuristics.tabusearch.AbstractTS#makeCL()
	 */
	@Override
	public ArrayList<Integer> makeCL() {

		ArrayList<Integer> _CL = new ArrayList<Integer>();
		for (int i = 0; i < ObjFunction.getDomainSize(); i++) {
			Integer cand = new Integer(i);
			_CL.add(cand);
		}

		return _CL;

	}

	/* (non-Javadoc)
	 * @see metaheuristics.tabusearch.AbstractTS#makeRCL()
	 */
	@Override
	public ArrayList<Integer> makeRCL() {

		ArrayList<Integer> _RCL = new ArrayList<Integer>();

		return _RCL;

	}
	
	/* (non-Javadoc)
	 * @see metaheuristics.tabusearch.AbstractTS#makeTL()
	 */
	@Override
	public ArrayDeque<Integer> makeTL() {

		ArrayDeque<Integer> _TS = new ArrayDeque<Integer>(2*tenure);
		for (int i=0; i<2*tenure; i++) {
			_TS.add(fake);
		}

		return _TS;

	}

	/* (non-Javadoc)
	 * @see metaheuristics.tabusearch.AbstractTS#updateCL()
	 */
	@Override
	public void updateCL() {

		// do nothing

	}

	/**
	 * {@inheritDoc}
	 * 
	 * This createEmptySol instantiates an empty solution and it attributes a
	 * zero cost, since it is known that a QBF solution with all variables set
	 * to zero has also zero cost.
	 */
	@Override
	public Solution<Integer> createEmptySol() {
		Solution<Integer> sol = new Solution<Integer>();
		sol.cost = 0.0;
		return sol;
	}
	Neighbor firstSearch()
	{
		return new Neighbor();
	}

	private Neighbor bestSearch()
	{
		Neighbor n = new Neighbor();
		for (Integer candIn : CL) {
			Double deltaCost = ObjFunction.evaluateInsertionCost(candIn, incumbentSol);
			if (!TL.contains(candIn) || incumbentSol.cost+deltaCost < bestSol.cost) {
				if (deltaCost < n.getMinDeltaCost()) {
					n.setMinDeltaCost(deltaCost);
					n.setBestCandIn(candIn);
					n.setBestCandOut(null);
				}
			}
		}
		// Evaluate removals
		for (Integer candOut : incumbentSol) {
			Double deltaCost = ObjFunction.evaluateRemovalCost(candOut, incumbentSol);
			if (!TL.contains(candOut) || incumbentSol.cost+deltaCost < bestSol.cost) {
				if (deltaCost < n.getMinDeltaCost()) {
					n.setMinDeltaCost(deltaCost);
					n.setBestCandIn(null);
					n.setBestCandOut(candOut);
				}
			}
		}
		// Evaluate exchanges
		for (Integer candIn : CL) {
			for (Integer candOut : incumbentSol) {
				Double deltaCost = ObjFunction.evaluateExchangeCost(candIn, candOut, incumbentSol);
				if ((!TL.contains(candIn) && !TL.contains(candOut)) || incumbentSol.cost+deltaCost < bestSol.cost) {
					if (deltaCost < n.getMinDeltaCost()) {
						n.setMinDeltaCost(deltaCost);
						n.setBestCandIn(candIn);
						n.setBestCandOut(candOut);
					}
				}
			}
		}
		
		return n;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * The local search operator developed for the QBF objective function is
	 * composed by the neighborhood moves Insertion, Removal and 2-Exchange.
	 */
	@Override
	public void neighborhoodMove() 
	{
		repairSolution();
		
		Neighbor n = (this.searchMethod == FIRST_IMPROVEMENT) ? firstSearch():bestSearch();
		
		
		
		// Implement the best non-tabu move
		TL.poll();
		if (n.getBestCandOut() != null) {
			incumbentSol.remove(n.getBestCandOut());
			CL.add(n.getBestCandOut());
			TL.add(n.getBestCandOut());
		} else {
			TL.add(fake);
		}
		TL.poll();
		if (n.getBestCandIn() != null) {
			incumbentSol.add(n.getBestCandIn());
			CL.remove(n.getBestCandIn());
			TL.add(n.getBestCandIn());
		} else {
			TL.add(fake);
		}
		
		repairSolution();
		
		ObjFunction.evaluate(incumbentSol);
	}

	/**
	 * A main method used for testing the TS metaheuristic.
	 * 
	 */
	public static void main(String[] args) throws IOException {
		
		long startTime = System.currentTimeMillis();
		TS_QBFPT tabusearch = new TS_QBFPT(50, 10000, BEST_IMPROVEMENT, "instances/qbf020");
		Solution<Integer> bestSol = tabusearch.solve();
		System.out.println("maxVal = " + bestSol);
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Time = "+(double)totalTime/(double)1000+" seg");

	}

}
