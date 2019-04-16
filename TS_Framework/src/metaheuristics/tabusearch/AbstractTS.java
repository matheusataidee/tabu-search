/**
 * 
 */
package metaheuristics.tabusearch;
import static utils.Utils.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import problems.Evaluator;
import solutions.Solution;
import utils.ProibitedTuple;
import utils.Recency;
import utils.RecencySorter;
import utils.Utils;

/**
 * Abstract class for metaheuristic Tabu Search. It consider a minimization problem.
 * 
 * @author ccavellucci, fusberti
 * @param <E>
 *            Generic type of the candidate to enter the solution.
 */
public abstract class AbstractTS<E> {

	/**
	 * flag that indicates whether the code should print more information on
	 * screen
	 */
	public static boolean verbose = true;

	/**
	 * a random number generator
	 */
	static Random rng = new Random(0);

	/**
	 * the objective function being optimized
	 */
	protected Evaluator<E> ObjFunction;

	/**
	 * the best solution cost
	 */
	protected Double bestCost;

	/**
	 * the incumbent solution cost
	 */
	protected Double incumbentCost;

	/**
	 * the best solution
	 */
	protected Solution<E> bestSol;

	/**
	 * the incumbent solution
	 */
	protected Solution<E> incumbentSol;

	/**
	 * the number of iterations the TS main loop executes.
	 */
	protected Integer iterations;
	
	protected Integer searchMethod;
	
	protected Integer method;
	
	protected Integer intensification_max_iterations;
	
	protected Integer how_many_recency_elements_to_take;
	
	protected Boolean is_feasible;
	
	protected Integer number_of_iterations_infeasible;
	
	protected List<Recency> listOfRecency = new LinkedList<>();
	/**
	 * the tabu tenure.
	 */
	protected Integer tenure;

	/**
	 * the Candidate List of elements to enter the solution.
	 */
	protected ArrayList<E> CL;

	/**
	 * the Restricted Candidate List of elements to enter the solution.
	 */
	protected ArrayList<E> RCL;
	
	/**
	 * the Tabu List of elements to enter the solution.
	 */
	protected ArrayDeque<E> TL;
	
	/**
	 * the list of penalties for each element when in a infeasible solution.
	 */
	protected ArrayList<Double> violationPenalties;
	
	protected List<ProibitedTuple> listOfProibitedTuples;
	
	protected Logger logger;

	/**
	 * Creates the Candidate List, which is an ArrayList of candidate elements
	 * that can enter a solution.
	 * 
	 * @return The Candidate List.
	 */
	public abstract ArrayList<E> makeCL();

	/**
	 * Creates the Restricted Candidate List, which is an ArrayList of the best
	 * candidate elements that can enter a solution. 
	 * 
	 * @return The Restricted Candidate List.
	 */
	public abstract ArrayList<E> makeRCL();
	
	/**
	 * Creates the Tabu List, which is an ArrayDeque of the Tabu
	 * candidate elements. The number of iterations a candidate
	 * is considered tabu is given by the Tabu Tenure {@link #tenure}
	 * 
	 * @return The Tabu List.
	 */
	public abstract ArrayDeque<E> makeTL();

	/**
	 * Updates the Candidate List according to the incumbent solution
	 * {@link #incumbentSol}. In other words, this method is responsible for
	 * updating the costs of the candidate solution elements.
	 */
	public abstract void updateCL();

	/**
	 * Creates the List of penalties for violating restrictions
	 * 
	 * @return The Violation Penalties List.
	 */
	public abstract ArrayList<Double> makeViolationPenaltiesList();
	
	/**
	 * Creates a new solution which is empty, i.e., does not contain any
	 * candidate solution element.
	 * 
	 * @return An empty solution.
	 */
	public abstract Solution<E> createEmptySol();

	/**
	 * The TS local search phase is responsible for repeatedly applying a
	 * neighborhood operation while the solution is getting improved, i.e.,
	 * until a local optimum is attained. When a local optimum is attained
	 * the search continues by exploring moves which can make the current 
	 * solution worse. Cycling is prevented by not allowing forbidden
	 * (tabu) moves that would otherwise backtrack to a previous solution.
	 * 
	 * @return An local optimum solution.
	 */
	public abstract void neighborhoodMove();
	
	private void createRecencyList()
	{
		for (E el : incumbentSol) 
		{
			listOfRecency.add(new Recency<E>(el));
		}
	}

	/**
	 * Constructor for the AbstractTS class.
	 * 
	 * @param objFunction
	 *            The objective function being minimized.
	 * @param tenure
	 *            The Tabu tenure parameter. 
	 * @param iterations
	 *            The number of iterations which the TS will be executed.
	 */
	public AbstractTS(Logger logger, Evaluator<E> objFunction, Integer tenure, Integer method, Integer searchMethod, Integer iterations) {
		this.logger = logger;
		this.ObjFunction = objFunction;
		this.tenure = tenure;
		this.iterations = iterations;
		this.method = method;
		this.searchMethod = searchMethod;
		this.listOfProibitedTuples =  Utils.getProibitedTuples(ObjFunction.getSize());
	}
	
	public AbstractTS(Logger logger, Evaluator<E> objFunction, Integer tenure, Integer method, Integer searchMethod, Integer iterations, Integer intensification_max_iterations, Integer how_many_recency_elements_to_take) {
		this.logger = logger;
		this.ObjFunction = objFunction;
		this.tenure = tenure;
		this.iterations = iterations;
		this.method = method;
		this.searchMethod = searchMethod;
		this.intensification_max_iterations = intensification_max_iterations;
		this.how_many_recency_elements_to_take = how_many_recency_elements_to_take;
		this.listOfProibitedTuples =  Utils.getProibitedTuples(ObjFunction.getSize());
	}
	
	private Integer int2ProibitedTupleElement(int i, ProibitedTuple p)
	{
		switch(i)
		{
		case 0:
			return p.getX0();
		case 1:
			return p.getX1();
		default:
			return p.getX2();
		}
	}

	protected Recency<E> findOnRecencyList(Integer val)
	{
		return this.listOfRecency.stream().filter(element -> val == element.getValue()).findAny().orElse(null);
	}
	
	protected void repairSolution()
	{
		for(ProibitedTuple proibitedTuple : listOfProibitedTuples)
		{
			if(incumbentSol.indexOf(proibitedTuple.getX0()) != -1 && incumbentSol.indexOf(proibitedTuple.getX1()) != -1 && incumbentSol.indexOf(proibitedTuple.getX2()) != -1 ){
                Random gerador = new Random(); 
                Integer candToRemove = int2ProibitedTupleElement(gerador.nextInt(3), proibitedTuple);
                incumbentSol.remove(incumbentSol.indexOf(candToRemove));
                                    
                if(this.method == INTENSIFICATION_METHOD)
    			{
    				Recency<E> element = findOnRecencyList(candToRemove);
    				if(null != element)  listOfRecency.remove(listOfRecency.indexOf(element));
    			}
                
                ObjFunction.evaluate(incumbentSol);
			}
		}
	}
	
	protected void updateViolationPenalties() {
		is_feasible = true;
		ArrayList<Double> updatedPenalties = makeViolationPenaltiesList(); 
		for(ProibitedTuple proibitedTuple : listOfProibitedTuples) {
			if(incumbentSol.indexOf(proibitedTuple.getX0()) != -1 && incumbentSol.indexOf(proibitedTuple.getX1()) != -1 && incumbentSol.indexOf(proibitedTuple.getX2()) != -1 ) {
				if (is_feasible) {
					is_feasible = false;
					number_of_iterations_infeasible++;
				}
				updatedPenalties.set(proibitedTuple.getX0(), violationPenalties.get(proibitedTuple.getX0()) + number_of_iterations_infeasible);
				updatedPenalties.set(proibitedTuple.getX1(), violationPenalties.get(proibitedTuple.getX1()) + number_of_iterations_infeasible);
				updatedPenalties.set(proibitedTuple.getX2(), violationPenalties.get(proibitedTuple.getX2()) + number_of_iterations_infeasible);
			}
		}
		violationPenalties = updatedPenalties;
		if (is_feasible) {
			number_of_iterations_infeasible = 0;
		}
	}
	
	/**
	 * The TS constructive heuristic, which is responsible for building a
	 * feasible solution by selecting in a greedy fashion, candidate
	 * elements to enter the solution.
	 * 
	 * @return A feasible solution to the problem being minimized.
	 */
	public Solution<E> constructiveHeuristic() {

		CL = makeCL();
		RCL = makeRCL();
		incumbentSol = createEmptySol();
		incumbentCost = Double.POSITIVE_INFINITY;

		/* Main loop, which repeats until the stopping criteria is reached. */
		while (!constructiveStopCriteria()) {

			Double maxCost = Double.NEGATIVE_INFINITY, minCost = Double.POSITIVE_INFINITY;
			incumbentCost = incumbentSol.cost;
			repairSolution();

			/*
			 * Explore all candidate elements to enter the solution, saving the
			 * highest and lowest cost variation achieved by the candidates.
			 */
			for (E c : CL) {
				Double deltaCost = ObjFunction.evaluateInsertionCost(c, incumbentSol);
				if (deltaCost < minCost)
					minCost = deltaCost;
				if (deltaCost > maxCost)
					maxCost = deltaCost;
			}

			/*
			 * Among all candidates, insert into the RCL those with the highest
			 * performance.
			 */
			for (E c : CL) {
				Double deltaCost = ObjFunction.evaluateInsertionCost(c, incumbentSol);
				if (deltaCost <= minCost) {
					RCL.add(c);
				}
			}
		
			/* Choose a candidate randomly from the RCL */
			int rndIndex = rng.nextInt(RCL.size());
			E inCand = RCL.get(rndIndex);
			CL.remove(inCand);
			incumbentSol.add(inCand);
			ObjFunction.evaluate(incumbentSol);
			RCL.clear();

		}

		if(this.method == INTENSIFICATION_METHOD) createRecencyList();
		
		return incumbentSol;
	}

	private void intensificateBestSolution()
	{
		//assume the incument solution to the best solution
		incumbentSol = new Solution<E>(bestSol);
		
		//sort recency list
		Collections.sort(this.listOfRecency, new RecencySorter());
		
		//clear TL
		TL = makeTL();
		
		//create tabu of most recent elements
		int qtd = 0;
		for(Recency<E> el : listOfRecency)
		{			
			if(qtd++ >= how_many_recency_elements_to_take) break;
			TL.add(el.getValue());
		}
		//clear recency list
		listOfRecency = new LinkedList<>();		
	}
	
	/**
	 * The TS mainframe. It consists of a constructive heuristic followed by
	 * a loop, in which each iteration a neighborhood move is performed on
	 * the current solution. The best solution is returned as result.
	 * 
	 * @return The best feasible solution obtained throughout all iterations.
	 */
	public Solution<E> solve() {

		bestSol = createEmptySol();
		constructiveHeuristic();
		TL = makeTL();
		violationPenalties = makeViolationPenaltiesList();
		
		is_feasible = true;
		number_of_iterations_infeasible = 0;		
		int howManyIterationsWithoutImprovement = 0;
		
		for (int i = 0; i < iterations; i++) {
			neighborhoodMove();
			
			howManyIterationsWithoutImprovement++;
			if (is_feasible && bestSol.cost > incumbentSol.cost) {
				howManyIterationsWithoutImprovement = 0;
				bestSol = new Solution<E>(incumbentSol);
				if (verbose)
					logger.info("(Iter. " + i + ") BestSol = " + bestSol);
			}

			if(this.method == INTENSIFICATION_METHOD && howManyIterationsWithoutImprovement >= intensification_max_iterations)
			{
				intensificateBestSolution();
				howManyIterationsWithoutImprovement = 0;
			}
		}

		return bestSol;
	}

	/**
	 * A standard stopping criteria for the constructive heuristic is to repeat
	 * until the incumbent solution improves by inserting a new candidate
	 * element.
	 * 
	 * @return true if the criteria is met.
	 */
	public Boolean constructiveStopCriteria() {
		return (incumbentCost > incumbentSol.cost) ? false : true;
	}

}
