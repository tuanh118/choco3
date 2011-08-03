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
package samples;

import choco.kernel.ResolutionPolicy;
import org.kohsuke.args4j.Option;
import org.slf4j.LoggerFactory;
import solver.Solver;
import solver.constraints.binary.Absolute;
import solver.constraints.binary.GreaterOrEqualX_YC;
import solver.constraints.nary.AllDifferent;
import solver.constraints.nary.Sum;
import solver.constraints.reified.ReifiedConstraint;
import solver.search.strategy.StrategyFactory;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 03/08/11
 */
public class Photo extends AbstractProblem {

    @Option(name = "-l", aliases = "--large", usage = "enable Photo large preferences .", required = false)
    private boolean isLarge = false;

    // Preferences for small example
    private static final int[] small = {
            0, 2,
            1, 4,
            2, 3,
            2, 4,
            3, 0,
            4, 3,
            4, 0,
            4, 1
    };


    // Preferences for large example
    private static final int[] large = {
            0, 2, 0, 4, 0, 7, 1, 4, 1, 8, 2, 3, 2, 4, 3, 0, 3, 4,
            4, 5, 4, 0, 5, 0, 5, 8, 6, 2, 6, 7, 7, 8, 7, 6
    };

    int[] preferences = small;

    int nbPeople = 5, nbPrefsPerP = 8, nbPref = small.length;

    IntVar[] positions;
    IntVar violations;

    @Override
    public void buildModel() {
        if (isLarge) {
            nbPeople = 9;
            nbPrefsPerP = 17;
            nbPref = large.length;
            preferences = large;
        }

        solver = new Solver();
        positions = VariableFactory.boundedArray("pos", nbPeople, 0, nbPeople - 1, solver);
        violations = VariableFactory.bounded("viol", 0, nbPref, solver);

        BoolVar[] viol = VariableFactory.boolArray("b", nbPrefsPerP, solver);
        for (int i = 0; i < nbPrefsPerP; i++) {
            int pa = preferences[(2 * i)];
            int pb = preferences[2 * i + 1];
            IntVar tmp = VariableFactory.bounded("tmp" + nbPref, -50, 50, solver);
            solver.post(Sum.eq(
                    new IntVar[]{positions[pa], positions[pb], tmp},
                    new int[]{1, -1, -1}, 0, solver));
            IntVar abst = VariableFactory.bounded("abst" + nbPref, 0, 50, solver);
            solver.post(new Absolute(abst, tmp, solver));
            solver.post(new ReifiedConstraint(
                    viol[i],
                    Sum.geq(new IntVar[]{abst}, 2, solver),
                    Sum.leq(new IntVar[]{abst}, 1, solver),
                    solver));
        }
        solver.post(Sum.eq(viol, violations, solver));
        solver.post(new AllDifferent(positions, solver));
        solver.post(new GreaterOrEqualX_YC(positions[1], positions[0], 1, solver));
    }

    @Override
    public void configureSolver() {
        solver.set(StrategyFactory.minDomMinVal(positions, solver.getEnvironment()));
    }

    @Override
    public void solve() {
        solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, violations);
    }

    @Override
    public void prettyOut() {
        LoggerFactory.getLogger("bench").info("Photo -- {}", isLarge ? "large" : "small");
        StringBuilder st = new StringBuilder();
        st.append("\tPositions: ");
        for (int i = 0; i < nbPeople; i++) {
            st.append(String.format("%d ", positions[i].getValue()));
        }
        st.append(String.format("\n\tViolations: %d", violations.getValue()));
        LoggerFactory.getLogger("bench").info(st.toString());
    }

    public static void main(String[] args) {
        new Photo().execute(args);
    }
}