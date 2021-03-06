/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.search.strategy.selectors;

import solver.variables.Variable;

import java.io.Serializable;

/**
 * A variable selector specifies which variable should be selected at a fix point. It is based specifications
 * (ex: smallest domain, most constrained, etc.).
 * <br/> Basically, the variable selected should not be already instantiated to a singleton (although it is allowed).
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 2 juil. 2010
 */
public interface VariableSelector<V extends Variable> extends Serializable {

    /**
     * Returns the set of variables involved in this.
     *
     * @return a collection of variables.
     */
    public V[] getScope();

    /**
     * Checks if there is at least one variable not yet instantiated fitting specifications
     *
     * @return <code>true</code> if at least one not instantiated variable fitting the specifications,
     *         <code>false</code> otherwise
     */
    public boolean hasNext();

    /**
     * Moves the selector to the next not instantiated variable fitting the specifications.
     * Note that you must <code>advance()</code> at least once before invoking <code>getVariable()</code>.
     */
    public void advance();

    /**
     * Provides access to the current selected variable.
     * Note that you must <code>advance()</code> at least once before invoking <code>getVariable()</code>.
     *
     * @return the current selected variable
     */
    public V getVariable();
}
