/**
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.search.strategy;

import solver.search.strategy.selectors.VariableSelector;
import solver.search.strategy.selectors.variables.InputOrder;
import solver.search.strategy.strategy.set.SetSearchStrategy;
import solver.search.strategy.strategy.set.SetValSelector;
import solver.search.strategy.strategy.set.SetVarSelector;
import solver.variables.SetVar;

/**
 * Strategies over set variables
 * Just there to simplify strategies creation.
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @since 02/2013
 */
public final class SetStrategyFactory {

	private SetStrategyFactory() {
	}

	/**
	 * Generic strategy to branch on set variables
	 * @param sets		SetVar array to branch on
	 * @param varS			variable selection strategy
	 * @param valS			integer  selection strategy
	 * @param enforceFirst	branching order true = enforce first; false = remove first
	 * @return a strategy to instantiate sets
	 */
	public static SetSearchStrategy generic(SetVar[] sets, VariableSelector<SetVar> varS, SetValSelector valS, boolean enforceFirst){
		return new SetSearchStrategy(varS,valS, enforceFirst);
	}

	/**
	 * strategy to branch on sets by choosing the first unfixed variable and forcing its first unfixed value
	 * @param sets variables to branch on
	 * @return a strategy to instantiate sets
	 */
	public static SetSearchStrategy force_first(SetVar[] sets){
		return new SetSearchStrategy(new InputOrder<>(sets), new SetValSelector.FirstVal(),true);
	}

	/**
	 * strategy to branch on sets by choosing the first unfixed variable and removing its first unfixed value
	 * @param sets variables to branch on
	 * @return a strategy to instantiate sets
	 */
	public static SetSearchStrategy remove_first(SetVar[] sets){
		return new SetSearchStrategy(new InputOrder<>(sets), new SetValSelector.FirstVal(),false);
	}

	/**
	 * strategy to branch on sets
	 * by choosing the unfixed variable of minimum delta (envSize-kerSize),
	 * and forcing its first unfixed value
	 * @param sets variables to branch on
	 * @return a strategy to instantiate sets
	 */
	public static SetSearchStrategy force_minDelta_first(SetVar[] sets){
		return new SetSearchStrategy(new SetVarSelector.MinDelta(sets), new SetValSelector.FirstVal(),true);
	}

	/**
	 * strategy to branch on sets
	 * by choosing the unfixed variable of maximum delta (envSize-kerSize),
	 * and forcing its first unfixed value
	 * @param sets variables to branch on
	 * @return a strategy to instantiate sets
	 */
	public static SetSearchStrategy force_maxDelta_first(SetVar[] sets){
		return new SetSearchStrategy(new SetVarSelector.MaxDelta(sets), new SetValSelector.FirstVal(),true);
	}
}
