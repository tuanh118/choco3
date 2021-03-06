/*
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
package solver.deprecatedPropagators;

import memory.IEnvironment;
import memory.IStateInt;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.BoolVar;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.delta.IIntDeltaMonitor;
import util.ESat;
import util.procedure.IntProcedure;
import util.tools.ArrayUtils;

/**
 * Note JG: has some bugs, is not efficient, dont use it!
 * Constraints that map the boolean assignments variables (bvars) with the standard assignment variables (var).
 * var = i -> bvars[i] = 1
 * <br/>
 *
 * @author Xavier Lorca
 * @author Hadrien Cambazard
 * @author Fabien Hermenier
 * @author Charles Prud'homme
 * @since 04/08/11
 */
@Deprecated
public class PropDomainChanneling extends Propagator<IntVar> {

    /**
     * Number of possible assignments.
     * ie, the number of boolean vars
     */
    private final int dsize;

    /**
     * The last lower bounds of the assignment var.
     */
    private final IStateInt oldinf;

    /**
     * The last upper bounds of the assignment var.
     */
    private final IStateInt oldsup;

    protected final RemProc rem_proc;

    protected final IIntDeltaMonitor[] idms;

    public PropDomainChanneling(BoolVar[] bvars, IntVar aVar) {
        super(ArrayUtils.append(bvars, new IntVar[]{aVar}), PropagatorPriority.LINEAR, true);
        this.idms = new IIntDeltaMonitor[this.vars.length];
        for (int i = 0; i < this.vars.length; i++) {
            idms[i] = this.vars[i].monitorDelta(this);
        }
        this.dsize = bvars.length;
		IEnvironment environment = solver.getEnvironment();
        oldinf = environment.makeInt();
        oldsup = environment.makeInt();
        this.rem_proc = new RemProc(this);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx < dsize) {
            return EventType.INSTANTIATE.mask;
        } else {
            return EventType.INT_ALL_MASK();
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        vars[dsize].updateLowerBound(0, aCause);
        vars[dsize].updateUpperBound(dsize - 1, aCause);

        int left = Integer.MIN_VALUE;
        int right = left;
        for (int i = 0; i < dsize; i++) {
            if (vars[i].isInstantiatedTo(0)) {
                if (i == right + 1) {
                    right = i;
                } else {
                    vars[dsize].removeInterval(left, right, aCause);
                    left = i;
                    right = i;
                }
//                vars[dsize].removeVal(i, this, false);
            } else if (vars[i].isInstantiatedTo(1)) {
                vars[dsize].instantiateTo(i, aCause);
                clearBooleanExcept(i);
            } else if (!vars[dsize].contains(i)) {
                clearBoolean(i);
            }
        }
        vars[dsize].removeInterval(left, right, aCause);
        if (vars[dsize].isInstantiated()) {
            final int value = vars[dsize].getValue();
            clearBooleanExcept(value);
            vars[value].instantiateTo(1, aCause);
        }

        //Set oldinf & oldsup equals to the nt bounds of the assignment var
        oldinf.set(vars[dsize].getLB());
        oldsup.set(vars[dsize].getUB());
        for (int i = 0; i < idms.length; i++) {
            idms[i].unfreeze();
        }
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        if (EventType.isInstantiate(mask)) {
            //val = the current value
            final int val = vars[varIdx].getValue();

            if (varIdx == dsize) {
                //We instantiate the assignment var
                //val = index to keep
                vars[val].instantiateTo(1, aCause);
                clearBooleanExcept(val);
            } else {
                //We instantiate a boolean var
                if (val == 1) {
                    //We report the instantiation to the associated assignment var
                    vars[dsize].instantiateTo(varIdx, aCause);
                    //Next line should be useless ?
                    clearBooleanExcept(varIdx);
                } else {
                    vars[dsize].removeValue(varIdx, aCause);
                    if (vars[dsize].isInstantiated()) {
                        vars[vars[dsize].getValue()].instantiateTo(1, aCause);
                    }
                }
            }
        } else {
            if (EventType.isInclow(mask)) {
                clearBoolean(oldinf.get(), vars[varIdx].getLB());
                oldinf.set(vars[varIdx].getLB());
            }
            if (EventType.isDecupp(mask)) {
                clearBoolean(vars[varIdx].getUB() + 1, oldsup.get() + 1);
                oldsup.set(vars[varIdx].getUB());
            }
            if (EventType.isRemove(mask)) {
                idms[varIdx].freeze();
                idms[varIdx].forEach(rem_proc, EventType.REMOVE);
                idms[varIdx].unfreeze();
            }
        }

    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            //TODO: ugly
            return constraint.isSatisfied();
        }
        return ESat.UNDEFINED;
    }


    private void clearBoolean(int val) throws ContradictionException {
        vars[val].instantiateTo(0, aCause);
    }


    private void clearBoolean(int begin, int end) throws ContradictionException {
        for (int i = begin; i < end; i++) {
            clearBoolean(i);
        }
    }

    /**
     * Instantiate all the boolean variable to 1 except one.
     *
     * @param val The index of the variable to keep
     * @throws ContradictionException if an error occured
     */
    private void clearBooleanExcept(int val) throws ContradictionException {
        clearBoolean(oldinf.get(), val);
        clearBoolean(val + 1, oldsup.get());
    }


    private static class RemProc implements IntProcedure {

        private final PropDomainChanneling p;

        public RemProc(PropDomainChanneling p) {
            this.p = p;
        }

        @Override
        public void execute(int i) throws ContradictionException {
            p.clearBoolean(i);
        }
    }
}
