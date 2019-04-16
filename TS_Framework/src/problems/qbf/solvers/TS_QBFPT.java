package problems.qbf.solvers;

import static utils.Utils.*;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import metaheuristics.tabusearch.AbstractTS;
import problems.qbf.QBF_Inverse;
import solutions.Solution;
import utils.Neighbor;
import utils.ProibitedTuple;
import utils.Recency;
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
	public TS_QBFPT(Logger logger, Integer tenure, Integer iterations, Integer method, Integer searchMethod, String filename) throws IOException {
		super(logger, new QBF_Inverse(filename), tenure, method, searchMethod, iterations);
	}
	
	//Intensification constructor
	public TS_QBFPT(Logger logger, Integer tenure, Integer iterations, Integer method, Integer searchMethod, String filename, Integer intensification_max_iterations, Integer how_many_recency_elements_to_take) throws IOException {
		super(logger, new QBF_Inverse(filename), tenure, method, searchMethod, iterations, intensification_max_iterations, how_many_recency_elements_to_take);
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
	
	private Neighbor localSearch()
	{
		Neighbor n = new Neighbor();
		for (Integer candIn : CL) {
			Double deltaCost = ObjFunction.evaluateInsertionCost(candIn, incumbentSol);
			if (!TL.contains(candIn) || incumbentSol.cost+deltaCost < bestSol.cost) {
				if (deltaCost < n.getMinDeltaCost()) {
					n.setMinDeltaCost(deltaCost);
					n.setBestCandIn(candIn);
					n.setBestCandOut(null);
					
					if(this.searchMethod == FIRST_IMPROVEMENT) return n;
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
					
					if(this.searchMethod == FIRST_IMPROVEMENT) return n;
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
						
						if(this.searchMethod == FIRST_IMPROVEMENT) return n;
					}
				}
			}
		}
		
		return n;
	}
	
	private void updateRecencyList()
	{
		for(Integer val : incumbentSol)
		{
			Recency<Integer> element = findOnRecencyList(val);
			if(element == null)  listOfRecency.add(new Recency<Integer>(val));
			else element.increment();
		}
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
		
		Neighbor n = localSearch();
		
		// Implement the best non-tabu move
		TL.poll();
		if (n.getBestCandOut() != null) {
			incumbentSol.remove(n.getBestCandOut());
			
			//remove element from recency
			if(this.method == INTENSIFICATION_METHOD)
			{
				Recency<Integer> element = findOnRecencyList(n.getBestCandOut());
				if(null != element)  listOfRecency.remove(listOfRecency.indexOf(element));
			}
			
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
		
		if(this.method == INTENSIFICATION_METHOD) updateRecencyList();
		
		ObjFunction.evaluate(incumbentSol);
	}
	
	public static Logger setUpLogger(String addr)
	{
		Logger logger = Logger.getLogger("MyLog");
		try {  
			FileHandler fh = new FileHandler(addr);  
		    logger.addHandler(fh);
		    SimpleFormatter formatter = new SimpleFormatter();  
		    fh.setFormatter(formatter);   
		    
		    logger.setUseParentHandlers(false);
		    } catch (SecurityException e) {  
		        e.printStackTrace();  
		    } catch (IOException e) {  
		        e.printStackTrace();  
		    }  
		
		return logger;
	}

	/**
	 * A main method used for testing the TS metaheuristic.
	 * 
	 */
	public static void main(String[] args) throws IOException {
		
		Logger logger = setUpLogger("results\\DEFAULT_METHOD_final.txt");
		
		Integer tenures[] = {2, 18};
		Integer localSearchMethods[] = {FIRST_IMPROVEMENT, BEST_IMPROVEMENT};
		
		String instances[] = {
								"instances/qbf200",
								"instances/qbf400"};
		Integer intensification_qtds_run[] = {500};
		Integer intensification_qtds_to_tabu[] = {18};
		
		for (String instance : instances)
		for(Integer localSearchMethod : localSearchMethods)
		for(Integer tenure:tenures)
		//for(Integer intensification_qtd_run : intensification_qtds_run)
		//for(Integer intensification_qtd_to_tabu : intensification_qtds_to_tabu)
		{
			logger.info("----------------------------------------------------------------");
			logger.info("Going to start a new parameter configuration");
			logger.info("Execution for "+instance);
			logger.info("Local search Method = "+(localSearchMethod==FIRST_IMPROVEMENT?"FIRST_IMPROVEMENT":"BEST_IMPROVEMENT"));
			logger.info("Tenure = "+tenure);
			//logger.info("Intensification run quantity = "+intensification_qtd_run);
			//logger.info("Intensification to tabu qtd = "+intensification_qtd_to_tabu);
			long startTime = System.currentTimeMillis();
			TS_QBFPT tabusearch = new TS_QBFPT(logger, tenure, 300, DEFAULT_METHOD, localSearchMethod, instance);
			Solution<Integer> bestSol = tabusearch.solve();
			logger.info("maxVal = " + bestSol);
			long endTime   = System.currentTimeMillis();
			long totalTime = endTime - startTime;
			logger.info("Time = "+(double)totalTime/(double)1000+" seg");
			logger.info("\n\n\n");
		}
		
		
		

	}

}
