/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.math3.optimization.general;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.analysis.DifferentiableMultivariateVectorFunction;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.optimization.SimpleVectorValueChecker;
import org.apache.commons.math3.optimization.PointVectorValuePair;
import org.apache.commons.math3.util.Precision;
import org.apache.commons.math3.util.FastMath;
import org.junit.Assert;
import org.junit.Test;

/**
 * <p>Some of the unit tests are re-implementations of the MINPACK <a
 * href="http://www.netlib.org/minpack/ex/file17">file17</a> and <a
 * href="http://www.netlib.org/minpack/ex/file22">file22</a> test files.
 * The redistribution policy for MINPACK is available <a
 * href="http://www.netlib.org/minpack/disclaimer">here</a>, for
 * convenience, it is reproduced below.</p>

 * <table border="0" width="80%" cellpadding="10" align="center" bgcolor="#E0E0E0">
 * <tr><td>
 *    Minpack Copyright Notice (1999) University of Chicago.
 *    All rights reserved
 * </td></tr>
 * <tr><td>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * <ol>
 *  <li>Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.</li>
 * <li>The end-user documentation included with the redistribution, if any,
 *     must include the following acknowledgment:
 *     <code>This product includes software developed by the University of
 *           Chicago, as Operator of Argonne National Laboratory.</code>
 *     Alternately, this acknowledgment may appear in the software itself,
 *     if and wherever such third-party acknowledgments normally appear.</li>
 * <li><strong>WARRANTY DISCLAIMER. THE SOFTWARE IS SUPPLIED "AS IS"
 *     WITHOUT WARRANTY OF ANY KIND. THE COPYRIGHT HOLDER, THE
 *     UNITED STATES, THE UNITED STATES DEPARTMENT OF ENERGY, AND
 *     THEIR EMPLOYEES: (1) DISCLAIM ANY WARRANTIES, EXPRESS OR
 *     IMPLIED, INCLUDING BUT NOT LIMITED TO ANY IMPLIED WARRANTIES
 *     OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, TITLE
 *     OR NON-INFRINGEMENT, (2) DO NOT ASSUME ANY LEGAL LIABILITY
 *     OR RESPONSIBILITY FOR THE ACCURACY, COMPLETENESS, OR
 *     USEFULNESS OF THE SOFTWARE, (3) DO NOT REPRESENT THAT USE OF
 *     THE SOFTWARE WOULD NOT INFRINGE PRIVATELY OWNED RIGHTS, (4)
 *     DO NOT WARRANT THAT THE SOFTWARE WILL FUNCTION
 *     UNINTERRUPTED, THAT IT IS ERROR-FREE OR THAT ANY ERRORS WILL
 *     BE CORRECTED.</strong></li>
 * <li><strong>LIMITATION OF LIABILITY. IN NO EVENT WILL THE COPYRIGHT
 *     HOLDER, THE UNITED STATES, THE UNITED STATES DEPARTMENT OF
 *     ENERGY, OR THEIR EMPLOYEES: BE LIABLE FOR ANY INDIRECT,
 *     INCIDENTAL, CONSEQUENTIAL, SPECIAL OR PUNITIVE DAMAGES OF
 *     ANY KIND OR NATURE, INCLUDING BUT NOT LIMITED TO LOSS OF
 *     PROFITS OR LOSS OF DATA, FOR ANY REASON WHATSOEVER, WHETHER
 *     SUCH LIABILITY IS ASSERTED ON THE BASIS OF CONTRACT, TORT
 *     (INCLUDING NEGLIGENCE OR STRICT LIABILITY), OR OTHERWISE,
 *     EVEN IF ANY OF SAID PARTIES HAS BEEN WARNED OF THE
 *     POSSIBILITY OF SUCH LOSS OR DAMAGES.</strong></li>
 * <ol></td></tr>
 * </table>

 * @author Argonne National Laboratory. MINPACK project. March 1980 (original fortran minpack tests)
 * @author Burton S. Garbow (original fortran minpack tests)
 * @author Kenneth E. Hillstrom (original fortran minpack tests)
 * @author Jorge J. More (original fortran minpack tests)
 * @author Luc Maisonobe (non-minpack tests and minpack tests Java translation)
 */
public class LevenbergMarquardtOptimizerTest {

    @Test
    public void testTrivial() {
        LinearProblem problem =
            new LinearProblem(new double[][] { { 2 } }, new double[] { 3 });
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        PointVectorValuePair optimum =
            optimizer.optimize(100, problem, problem.target, new double[] { 1 }, new double[] { 0 });
        Assert.assertEquals(0, optimizer.getRMS(), 1.0e-10);
        try {
            optimizer.guessParametersErrors();
            Assert.fail("an exception should have been thrown");
        } catch (NumberIsTooSmallException ee) {
            // expected behavior
        }
        Assert.assertEquals(1.5, optimum.getPoint()[0], 1.0e-10);
        Assert.assertEquals(3.0, optimum.getValue()[0], 1.0e-10);
    }

    @Test
    public void testQRColumnsPermutation() {

        LinearProblem problem =
            new LinearProblem(new double[][] { { 1.0, -1.0 }, { 0.0, 2.0 }, { 1.0, -2.0 } },
                              new double[] { 4.0, 6.0, 1.0 });

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        PointVectorValuePair optimum =
            optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1 }, new double[] { 0, 0 });
        Assert.assertEquals(0, optimizer.getRMS(), 1.0e-10);
        Assert.assertEquals(7.0, optimum.getPoint()[0], 1.0e-10);
        Assert.assertEquals(3.0, optimum.getPoint()[1], 1.0e-10);
        Assert.assertEquals(4.0, optimum.getValue()[0], 1.0e-10);
        Assert.assertEquals(6.0, optimum.getValue()[1], 1.0e-10);
        Assert.assertEquals(1.0, optimum.getValue()[2], 1.0e-10);
    }

    @Test
    public void testNoDependency() {
        LinearProblem problem = new LinearProblem(new double[][] {
                { 2, 0, 0, 0, 0, 0 },
                { 0, 2, 0, 0, 0, 0 },
                { 0, 0, 2, 0, 0, 0 },
                { 0, 0, 0, 2, 0, 0 },
                { 0, 0, 0, 0, 2, 0 },
                { 0, 0, 0, 0, 0, 2 }
        }, new double[] { 0.0, 1.1, 2.2, 3.3, 4.4, 5.5 });
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        PointVectorValuePair optimum =
            optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1, 1, 1, 1 },
                               new double[] { 0, 0, 0, 0, 0, 0 });
        Assert.assertEquals(0, optimizer.getRMS(), 1.0e-10);
        for (int i = 0; i < problem.target.length; ++i) {
            Assert.assertEquals(0.55 * i, optimum.getPoint()[i], 1.0e-10);
        }
    }

    @Test
    public void testOneSet() {

        LinearProblem problem = new LinearProblem(new double[][] {
                {  1,  0, 0 },
                { -1,  1, 0 },
                {  0, -1, 1 }
        }, new double[] { 1, 1, 1});
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        PointVectorValuePair optimum =
            optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1 }, new double[] { 0, 0, 0 });
        Assert.assertEquals(0, optimizer.getRMS(), 1.0e-10);
        Assert.assertEquals(1.0, optimum.getPoint()[0], 1.0e-10);
        Assert.assertEquals(2.0, optimum.getPoint()[1], 1.0e-10);
        Assert.assertEquals(3.0, optimum.getPoint()[2], 1.0e-10);
    }

    @Test
    public void testTwoSets() {
        double epsilon = 1.0e-7;
        LinearProblem problem = new LinearProblem(new double[][] {
                {  2,  1,   0,  4,       0, 0 },
                { -4, -2,   3, -7,       0, 0 },
                {  4,  1,  -2,  8,       0, 0 },
                {  0, -3, -12, -1,       0, 0 },
                {  0,  0,   0,  0, epsilon, 1 },
                {  0,  0,   0,  0,       1, 1 }
        }, new double[] { 2, -9, 2, 2, 1 + epsilon * epsilon, 2});

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        PointVectorValuePair optimum =
            optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1, 1, 1, 1 },
                               new double[] { 0, 0, 0, 0, 0, 0 });
        Assert.assertEquals(0, optimizer.getRMS(), 1.0e-10);
        Assert.assertEquals( 3.0, optimum.getPoint()[0], 1.0e-10);
        Assert.assertEquals( 4.0, optimum.getPoint()[1], 1.0e-10);
        Assert.assertEquals(-1.0, optimum.getPoint()[2], 1.0e-10);
        Assert.assertEquals(-2.0, optimum.getPoint()[3], 1.0e-10);
        Assert.assertEquals( 1.0 + epsilon, optimum.getPoint()[4], 1.0e-10);
        Assert.assertEquals( 1.0 - epsilon, optimum.getPoint()[5], 1.0e-10);
    }

    @Test(expected=SingularMatrixException.class)
    public void testNonInvertible() {
        LinearProblem problem = new LinearProblem(new double[][] {
                {  1, 2, -3 },
                {  2, 1,  3 },
                { -3, 0, -9 }
        }, new double[] { 1, 1, 1 });

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.optimize(100, problem, problem.target,
                             new double[] { 1, 1, 1 },
                             new double[] { 0, 0, 0 });
        Assert.assertTrue(FastMath.sqrt(problem.target.length) * optimizer.getRMS() > 0.6);

        // The default singularity threshold (1e-14) does not trigger the
        // expected exception.
        double[][] cov = optimizer.getCovariances(1.5e-14);
    }

    @Test
    public void testIllConditioned() {
        LinearProblem problem1 = new LinearProblem(new double[][] {
                { 10.0, 7.0,  8.0,  7.0 },
                {  7.0, 5.0,  6.0,  5.0 },
                {  8.0, 6.0, 10.0,  9.0 },
                {  7.0, 5.0,  9.0, 10.0 }
        }, new double[] { 32, 23, 33, 31 });
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        PointVectorValuePair optimum1 =
            optimizer.optimize(100, problem1, problem1.target, new double[] { 1, 1, 1, 1 },
                               new double[] { 0, 1, 2, 3 });
        Assert.assertEquals(0, optimizer.getRMS(), 1.0e-10);
        Assert.assertEquals(1.0, optimum1.getPoint()[0], 1.0e-10);
        Assert.assertEquals(1.0, optimum1.getPoint()[1], 1.0e-10);
        Assert.assertEquals(1.0, optimum1.getPoint()[2], 1.0e-10);
        Assert.assertEquals(1.0, optimum1.getPoint()[3], 1.0e-10);

        LinearProblem problem2 = new LinearProblem(new double[][] {
                { 10.00, 7.00, 8.10, 7.20 },
                {  7.08, 5.04, 6.00, 5.00 },
                {  8.00, 5.98, 9.89, 9.00 },
                {  6.99, 4.99, 9.00, 9.98 }
        }, new double[] { 32, 23, 33, 31 });
        PointVectorValuePair optimum2 =
            optimizer.optimize(100, problem2, problem2.target, new double[] { 1, 1, 1, 1 },
                               new double[] { 0, 1, 2, 3 });
        Assert.assertEquals(0, optimizer.getRMS(), 1.0e-10);
        Assert.assertEquals(-81.0, optimum2.getPoint()[0], 1.0e-8);
        Assert.assertEquals(137.0, optimum2.getPoint()[1], 1.0e-8);
        Assert.assertEquals(-34.0, optimum2.getPoint()[2], 1.0e-8);
        Assert.assertEquals( 22.0, optimum2.getPoint()[3], 1.0e-8);
    }

    @Test
    public void testMoreEstimatedParametersSimple() {

        LinearProblem problem = new LinearProblem(new double[][] {
                { 3.0, 2.0,  0.0, 0.0 },
                { 0.0, 1.0, -1.0, 1.0 },
                { 2.0, 0.0,  1.0, 0.0 }
        }, new double[] { 7.0, 3.0, 5.0 });

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1 },
                new double[] { 7, 6, 5, 4 });
        Assert.assertEquals(0, optimizer.getRMS(), 1.0e-10);
    }

    @Test
    public void testMoreEstimatedParametersUnsorted() {
        LinearProblem problem = new LinearProblem(new double[][] {
                { 1.0, 1.0,  0.0,  0.0, 0.0,  0.0 },
                { 0.0, 0.0,  1.0,  1.0, 1.0,  0.0 },
                { 0.0, 0.0,  0.0,  0.0, 1.0, -1.0 },
                { 0.0, 0.0, -1.0,  1.0, 0.0,  1.0 },
                { 0.0, 0.0,  0.0, -1.0, 1.0,  0.0 }
       }, new double[] { 3.0, 12.0, -1.0, 7.0, 1.0 });

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        PointVectorValuePair optimum =
            optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1, 1, 1 },
                               new double[] { 2, 2, 2, 2, 2, 2 });
        Assert.assertEquals(0, optimizer.getRMS(), 1.0e-10);
        Assert.assertEquals(3.0, optimum.getPointRef()[2], 1.0e-10);
        Assert.assertEquals(4.0, optimum.getPointRef()[3], 1.0e-10);
        Assert.assertEquals(5.0, optimum.getPointRef()[4], 1.0e-10);
        Assert.assertEquals(6.0, optimum.getPointRef()[5], 1.0e-10);
    }

    @Test
    public void testRedundantEquations() {
        LinearProblem problem = new LinearProblem(new double[][] {
                { 1.0,  1.0 },
                { 1.0, -1.0 },
                { 1.0,  3.0 }
        }, new double[] { 3.0, 1.0, 5.0 });

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        PointVectorValuePair optimum =
            optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1 },
                               new double[] { 1, 1 });
        Assert.assertEquals(0, optimizer.getRMS(), 1.0e-10);
        Assert.assertEquals(2.0, optimum.getPointRef()[0], 1.0e-10);
        Assert.assertEquals(1.0, optimum.getPointRef()[1], 1.0e-10);
    }

    @Test
    public void testInconsistentEquations() {
        LinearProblem problem = new LinearProblem(new double[][] {
                { 1.0,  1.0 },
                { 1.0, -1.0 },
                { 1.0,  3.0 }
        }, new double[] { 3.0, 1.0, 4.0 });

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1 }, new double[] { 1, 1 });
        Assert.assertTrue(optimizer.getRMS() > 0.1);
    }

    @Test
    public void testInconsistentSizes() {
        LinearProblem problem =
            new LinearProblem(new double[][] { { 1, 0 }, { 0, 1 } }, new double[] { -1, 1 });
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

        PointVectorValuePair optimum =
            optimizer.optimize(100, problem, problem.target, new double[] { 1, 1 }, new double[] { 0, 0 });
        Assert.assertEquals(0, optimizer.getRMS(), 1.0e-10);
        Assert.assertEquals(-1, optimum.getPoint()[0], 1.0e-10);
        Assert.assertEquals(+1, optimum.getPoint()[1], 1.0e-10);

        try {
            optimizer.optimize(100, problem, problem.target,
                               new double[] { 1 },
                               new double[] { 0, 0 });
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException oe) {
            // expected behavior
        }

        try {
            optimizer.optimize(100, problem, new double[] { 1 },
                               new double[] { 1 },
                               new double[] { 0, 0 });
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException oe) {
            // expected behavior
        }
    }

    @Test
    public void testControlParameters() {
        CircleVectorial circle = new CircleVectorial();
        circle.addPoint( 30.0,  68.0);
        circle.addPoint( 50.0,  -6.0);
        circle.addPoint(110.0, -20.0);
        circle.addPoint( 35.0,  15.0);
        circle.addPoint( 45.0,  97.0);
        checkEstimate(circle, 0.1, 10, 1.0e-14, 1.0e-16, 1.0e-10, false);
        checkEstimate(circle, 0.1, 10, 1.0e-15, 1.0e-17, 1.0e-10, true);
        checkEstimate(circle, 0.1,  5, 1.0e-15, 1.0e-16, 1.0e-10, true);
        circle.addPoint(300, -300);
        checkEstimate(circle, 0.1, 20, 1.0e-18, 1.0e-16, 1.0e-10, true);
    }

    private void checkEstimate(DifferentiableMultivariateVectorFunction problem,
                               double initialStepBoundFactor, int maxCostEval,
                               double costRelativeTolerance, double parRelativeTolerance,
                               double orthoTolerance, boolean shouldFail) {
        try {
            LevenbergMarquardtOptimizer optimizer
                = new LevenbergMarquardtOptimizer(initialStepBoundFactor,
                                                  costRelativeTolerance,
                                                  parRelativeTolerance,
                                                  orthoTolerance,
                                                  Precision.SAFE_MIN);
            optimizer.optimize(maxCostEval, problem, new double[] { 0, 0, 0, 0, 0 },
                               new double[] { 1, 1, 1, 1, 1 },
                               new double[] { 98.680, 47.345 });
            Assert.assertTrue(!shouldFail);
        } catch (DimensionMismatchException ee) {
            Assert.assertTrue(shouldFail);
        } catch (TooManyEvaluationsException ee) {
            Assert.assertTrue(shouldFail);
        }
    }

    @Test
    public void testCircleFitting() {
        CircleVectorial circle = new CircleVectorial();
        circle.addPoint( 30.0,  68.0);
        circle.addPoint( 50.0,  -6.0);
        circle.addPoint(110.0, -20.0);
        circle.addPoint( 35.0,  15.0);
        circle.addPoint( 45.0,  97.0);
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        PointVectorValuePair optimum =
            optimizer.optimize(100, circle, new double[] { 0, 0, 0, 0, 0 }, new double[] { 1, 1, 1, 1, 1 },
                               new double[] { 98.680, 47.345 });
        Assert.assertTrue(optimizer.getEvaluations() < 10);
        Assert.assertTrue(optimizer.getJacobianEvaluations() < 10);
        double rms = optimizer.getRMS();
        Assert.assertEquals(1.768262623567235,  FastMath.sqrt(circle.getN()) * rms,  1.0e-10);
        Point2D.Double center = new Point2D.Double(optimum.getPointRef()[0], optimum.getPointRef()[1]);
        Assert.assertEquals(69.96016176931406, circle.getRadius(center), 1.0e-10);
        Assert.assertEquals(96.07590211815305, center.x,      1.0e-10);
        Assert.assertEquals(48.13516790438953, center.y,      1.0e-10);
        double[][] cov = optimizer.getCovariances();
        Assert.assertEquals(1.839, cov[0][0], 0.001);
        Assert.assertEquals(0.731, cov[0][1], 0.001);
        Assert.assertEquals(cov[0][1], cov[1][0], 1.0e-14);
        Assert.assertEquals(0.786, cov[1][1], 0.001);
        double[] errors = optimizer.guessParametersErrors();
        Assert.assertEquals(1.384, errors[0], 0.001);
        Assert.assertEquals(0.905, errors[1], 0.001);

        // add perfect measurements and check errors are reduced
        double  r = circle.getRadius(center);
        for (double d= 0; d < 2 * FastMath.PI; d += 0.01) {
            circle.addPoint(center.x + r * FastMath.cos(d), center.y + r * FastMath.sin(d));
        }
        double[] target = new double[circle.getN()];
        Arrays.fill(target, 0.0);
        double[] weights = new double[circle.getN()];
        Arrays.fill(weights, 2.0);
        optimizer.optimize(100, circle, target, weights, new double[] { 98.680, 47.345 });
        cov = optimizer.getCovariances();
        Assert.assertEquals(0.0016, cov[0][0], 0.001);
        Assert.assertEquals(3.2e-7, cov[0][1], 1.0e-9);
        Assert.assertEquals(cov[0][1], cov[1][0], 1.0e-14);
        Assert.assertEquals(0.0016, cov[1][1], 0.001);
        errors = optimizer.guessParametersErrors();
        Assert.assertEquals(0.004, errors[0], 0.001);
        Assert.assertEquals(0.004, errors[1], 0.001);
    }

    @Test
    public void testCircleFittingBadInit() {
        CircleVectorial circle = new CircleVectorial();
        double[][] points = new double[][] {
                {-0.312967,  0.072366}, {-0.339248,  0.132965}, {-0.379780,  0.202724},
                {-0.390426,  0.260487}, {-0.361212,  0.328325}, {-0.346039,  0.392619},
                {-0.280579,  0.444306}, {-0.216035,  0.470009}, {-0.149127,  0.493832},
                {-0.075133,  0.483271}, {-0.007759,  0.452680}, { 0.060071,  0.410235},
                { 0.103037,  0.341076}, { 0.118438,  0.273884}, { 0.131293,  0.192201},
                { 0.115869,  0.129797}, { 0.072223,  0.058396}, { 0.022884,  0.000718},
                {-0.053355, -0.020405}, {-0.123584, -0.032451}, {-0.216248, -0.032862},
                {-0.278592, -0.005008}, {-0.337655,  0.056658}, {-0.385899,  0.112526},
                {-0.405517,  0.186957}, {-0.415374,  0.262071}, {-0.387482,  0.343398},
                {-0.347322,  0.397943}, {-0.287623,  0.458425}, {-0.223502,  0.475513},
                {-0.135352,  0.478186}, {-0.061221,  0.483371}, { 0.003711,  0.422737},
                { 0.065054,  0.375830}, { 0.108108,  0.297099}, { 0.123882,  0.222850},
                { 0.117729,  0.134382}, { 0.085195,  0.056820}, { 0.029800, -0.019138},
                {-0.027520, -0.072374}, {-0.102268, -0.091555}, {-0.200299, -0.106578},
                {-0.292731, -0.091473}, {-0.356288, -0.051108}, {-0.420561,  0.014926},
                {-0.471036,  0.074716}, {-0.488638,  0.182508}, {-0.485990,  0.254068},
                {-0.463943,  0.338438}, {-0.406453,  0.404704}, {-0.334287,  0.466119},
                {-0.254244,  0.503188}, {-0.161548,  0.495769}, {-0.075733,  0.495560},
                { 0.001375,  0.434937}, { 0.082787,  0.385806}, { 0.115490,  0.323807},
                { 0.141089,  0.223450}, { 0.138693,  0.131703}, { 0.126415,  0.049174},
                { 0.066518, -0.010217}, {-0.005184, -0.070647}, {-0.080985, -0.103635},
                {-0.177377, -0.116887}, {-0.260628, -0.100258}, {-0.335756, -0.056251},
                {-0.405195, -0.000895}, {-0.444937,  0.085456}, {-0.484357,  0.175597},
                {-0.472453,  0.248681}, {-0.438580,  0.347463}, {-0.402304,  0.422428},
                {-0.326777,  0.479438}, {-0.247797,  0.505581}, {-0.152676,  0.519380},
                {-0.071754,  0.516264}, { 0.015942,  0.472802}, { 0.076608,  0.419077},
                { 0.127673,  0.330264}, { 0.159951,  0.262150}, { 0.153530,  0.172681},
                { 0.140653,  0.089229}, { 0.078666,  0.024981}, { 0.023807, -0.037022},
                {-0.048837, -0.077056}, {-0.127729, -0.075338}, {-0.221271, -0.067526}
        };
        double[] target = new double[points.length];
        Arrays.fill(target, 0.0);
        double[] weights = new double[points.length];
        Arrays.fill(weights, 2.0);
        for (int i = 0; i < points.length; ++i) {
            circle.addPoint(points[i][0], points[i][1]);
        }
        LevenbergMarquardtOptimizer optimizer
            = new LevenbergMarquardtOptimizer(new SimpleVectorValueChecker(1.0e-8, 1.0e-8));
        PointVectorValuePair optimum =
            optimizer.optimize(100, circle, target, weights, new double[] { -12, -12 });
        Point2D.Double center = new Point2D.Double(optimum.getPointRef()[0], optimum.getPointRef()[1]);
        Assert.assertTrue(optimizer.getEvaluations() < 25);
        Assert.assertTrue(optimizer.getJacobianEvaluations() < 20);
        Assert.assertEquals( 0.043, optimizer.getRMS(), 1.0e-3);
        Assert.assertEquals( 0.292235,  circle.getRadius(center), 1.0e-6);
        Assert.assertEquals(-0.151738,  center.x,      1.0e-6);
        Assert.assertEquals( 0.2075001, center.y,      1.0e-6);
    }

    @Test
    public void testMath199() {
        try {
            QuadraticProblem problem = new QuadraticProblem();
            problem.addPoint (0, -3.182591015485607);
            problem.addPoint (1, -2.5581184967730577);
            problem.addPoint (2, -2.1488478161387325);
            problem.addPoint (3, -1.9122489313410047);
            problem.addPoint (4, 1.7785661310051026);
            LevenbergMarquardtOptimizer optimizer
                = new LevenbergMarquardtOptimizer(100, 1e-10, 1e-10, 1e-10, 0);
            optimizer.optimize(100, problem,
                               new double[] { 0, 0, 0, 0, 0 },
                               new double[] { 0.0, 4.4e-323, 1.0, 4.4e-323, 0.0 },
                               new double[] { 0, 0, 0 });
            Assert.fail("an exception should have been thrown");
        } catch (ConvergenceException ee) {
            // expected behavior
        }
    }

    /**
     * Non-linear test case: fitting of decay curve (from Chapter 8 of
     * Bevington's textbook, "Data reduction and analysis for the physical sciences").
     * XXX The expected ("reference") values may not be accurate and the tolerance too
     * relaxed for this test to be currently really useful (the issue is under
     * investigation).
     */
    @Test
    public void testBevington() {
        final double[][] dataPoints = {
            // column 1 = times
            { 15, 30, 45, 60, 75, 90, 105, 120, 135, 150,
              165, 180, 195, 210, 225, 240, 255, 270, 285, 300,
              315, 330, 345, 360, 375, 390, 405, 420, 435, 450,
              465, 480, 495, 510, 525, 540, 555, 570, 585, 600,
              615, 630, 645, 660, 675, 690, 705, 720, 735, 750,
              765, 780, 795, 810, 825, 840, 855, 870, 885, },
            // column 2 = measured counts
            { 775, 479, 380, 302, 185, 157, 137, 119, 110, 89,
              74, 61, 66, 68, 48, 54, 51, 46, 55, 29,
              28, 37, 49, 26, 35, 29, 31, 24, 25, 35,
              24, 30, 26, 28, 21, 18, 20, 27, 17, 17,
              14, 17, 24, 11, 22, 17, 12, 10, 13, 16,
              9, 9, 14, 21, 17, 13, 12, 18, 10, },
        };

        final BevingtonProblem problem = new BevingtonProblem();

        final int len = dataPoints[0].length;
        final double[] weights = new double[len];
        for (int i = 0; i < len; i++) {
            problem.addPoint(dataPoints[0][i],
                             dataPoints[1][i]);

            weights[i] = 1 / dataPoints[1][i];
        }

        final LevenbergMarquardtOptimizer optimizer
            = new LevenbergMarquardtOptimizer();
        
        final PointVectorValuePair optimum =
            optimizer.optimize(100, problem, dataPoints[1], weights,
                               new double[] { 10, 900, 80, 27, 225 });
        
        final double chi2 = optimizer.getChiSquare();
        final double[] solution = optimum.getPoint();
        final double[] expectedSolution = { 10.4, 958.3, 131.4, 33.9, 205.0 };

        final double[][] covarMatrix = optimizer.getCovariances();
        final double[][] expectedCovarMatrix = {
            { 3.38, -3.69, 27.98, -2.34, -49.24 },
            { -3.69, 2492.26, 81.89, -69.21, -8.9 },
            { 27.98, 81.89, 468.99, -44.22, -615.44 },
            { -2.34, -69.21, -44.22, 6.39, 53.80 },
            { -49.24, -8.9, -615.44, 53.8, 929.45 }
        };

        final int numParams = expectedSolution.length;

        // Check that the computed solution is within the reference error range.
        for (int i = 0; i < numParams; i++) {
            final double error = FastMath.sqrt(expectedCovarMatrix[i][i]);
            Assert.assertEquals("Parameter " + i, expectedSolution[i], solution[i], error);
        }

        // Check that each entry of the computed covariance matrix is within 10%
        // of the reference matrix entry.
        for (int i = 0; i < numParams; i++) {
            for (int j = 0; j < numParams; j++) {
                Assert.assertEquals("Covariance matrix [" + i + "][" + j + "]",
                                    expectedCovarMatrix[i][j],
                                    covarMatrix[i][j],
                                    FastMath.abs(0.1 * expectedCovarMatrix[i][j]));
            }
        }
    }

    @Test
    public void testCircleFitting2() {
        final double xCenter = 123.456;
        final double yCenter = 654.321;
        final double xSigma = 10;
        final double ySigma = 15;
        final double radius = 111.111;
        final RandomCirclePointGenerator factory
            = new RandomCirclePointGenerator(xCenter, yCenter, radius,
                                             xSigma, ySigma,
                                             59421063L);
        final CircleProblem circle = new CircleProblem(xSigma, ySigma);

        final int numPoints = 10;
        for (Point2D.Double p : factory.generate(numPoints)) {
            circle.addPoint(p.x, p.y);
            // System.out.println(p.x + " " + p.y);
        }

        // First guess for the center's coordinates and radius.
        final double[] init = { 90, 659, 115 };

        final LevenbergMarquardtOptimizer optimizer
            = new LevenbergMarquardtOptimizer();
        final PointVectorValuePair optimum = optimizer.optimize(100, circle,
                                                                circle.target(), circle.weight(),
                                                                init);

        final double[] paramFound = optimum.getPoint();

        // Retrieve errors estimation.
        final double[][] covMatrix = optimizer.getCovariances();
        final double[] asymptoticStandardErrorFound = optimizer.guessParametersErrors();
        final double[] sigmaFound = new double[covMatrix.length];
        for (int i = 0; i < covMatrix.length; i++) {
            sigmaFound[i] = FastMath.sqrt(covMatrix[i][i]);
//             System.out.println("i=" + i + " value=" + paramFound[i]
//                                + " sigma=" + sigmaFound[i]
//                                + " ase=" + asymptoticStandardErrorFound[i]);
        }

        // System.out.println("chi2=" + optimizer.getChiSquare());

        // Check that the parameters are found within the assumed error bars.
        Assert.assertEquals(xCenter, paramFound[0], asymptoticStandardErrorFound[0]);
        Assert.assertEquals(yCenter, paramFound[1], asymptoticStandardErrorFound[1]);
        Assert.assertEquals(radius, paramFound[2], asymptoticStandardErrorFound[2]);
    }

    private static class LinearProblem implements DifferentiableMultivariateVectorFunction, Serializable {

        private static final long serialVersionUID = 703247177355019415L;
        final RealMatrix factors;
        final double[] target;
        public LinearProblem(double[][] factors, double[] target) {
            this.factors = new BlockRealMatrix(factors);
            this.target  = target;
        }

        public double[] value(double[] variables) {
            return factors.operate(variables);
        }

        public MultivariateMatrixFunction jacobian() {
            return new MultivariateMatrixFunction() {
                public double[][] value(double[] point) {
                    return factors.getData();
                }
            };
        }
    }

    private static class QuadraticProblem implements DifferentiableMultivariateVectorFunction, Serializable {

        private static final long serialVersionUID = 7072187082052755854L;
        private List<Double> x;
        private List<Double> y;

        public QuadraticProblem() {
            x = new ArrayList<Double>();
            y = new ArrayList<Double>();
        }

        public void addPoint(double x, double y) {
            this.x.add(x);
            this.y.add(y);
        }

        private double[][] jacobian(double[] variables) {
            double[][] jacobian = new double[x.size()][3];
            for (int i = 0; i < jacobian.length; ++i) {
                jacobian[i][0] = x.get(i) * x.get(i);
                jacobian[i][1] = x.get(i);
                jacobian[i][2] = 1.0;
            }
            return jacobian;
        }

        public double[] value(double[] variables) {
            double[] values = new double[x.size()];
            for (int i = 0; i < values.length; ++i) {
                values[i] = (variables[0] * x.get(i) + variables[1]) * x.get(i) + variables[2];
            }
            return values;
        }

        public MultivariateMatrixFunction jacobian() {
            return new MultivariateMatrixFunction() {
                public double[][] value(double[] point) {
                    return jacobian(point);
                }
            };
        }
    }

    private static class BevingtonProblem
        implements DifferentiableMultivariateVectorFunction {
        private List<Double> time;
        private List<Double> count;

        public BevingtonProblem() {
            time = new ArrayList<Double>();
            count = new ArrayList<Double>();
        }

        public void addPoint(double t, double c) {
            time.add(t);
            count.add(c);
        }

        private double[][] jacobian(double[] params) {
            double[][] jacobian = new double[time.size()][5];

            for (int i = 0; i < jacobian.length; ++i) {
                final double t = time.get(i);
                jacobian[i][0] = 1;
                
                final double p3 =  params[3];
                final double p4 =  params[4];
                final double tOp3 = t / p3;
                final double tOp4 = t / p4;
                jacobian[i][1] = Math.exp(-tOp3);
                jacobian[i][2] = Math.exp(-tOp4);
                jacobian[i][3] = params[1] * Math.exp(-tOp3) * tOp3 / p3;
                jacobian[i][4] = params[2] * Math.exp(-tOp4) * tOp4 / p4;
            }
            return jacobian;
        }

        public double[] value(double[] params) {
            double[] values = new double[time.size()];
            for (int i = 0; i < values.length; ++i) {
                final double t = time.get(i);
                values[i] = params[0]
                    + params[1] * Math.exp(-t / params[3])
                    + params[2] * Math.exp(-t / params[4]);
            }
            return values;
        }

        public MultivariateMatrixFunction jacobian() {
            return new MultivariateMatrixFunction() {
                public double[][] value(double[] point) {
                    return jacobian(point);
                }
            };
        }
    }
}
