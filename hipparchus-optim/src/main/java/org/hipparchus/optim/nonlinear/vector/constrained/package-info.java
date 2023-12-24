/*
 * Licensed to the Hipparchus project under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The Hipparchus project licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * This package provides algorithms that minimize the residuals
 * between observations and model values.
 * The {@link org.hipparchus.optim.nonlinear.vector.leastsquares.LeastSquaresOptimizer
 * least-squares optimizers} minimize the distance (called
 * <em>cost</em> or <em>&chi;<sup>2</sup></em>) between model and
 * observations.
 *
 * <br>
 * Algorithms in this category need access to a <em>problem</em>
 * (represented by a {@link org.hipparchus.optim.nonlinear.vector.leastsquares.LeastSquaresProblem
 * LeastSquaresProblem}).
 * Such a model predicts a set of values which the algorithm tries to match
 * with a set of given set of observed values.
 * <br>
 * The problem can be created progressively using a {@link
 * org.hipparchus.optim.nonlinear.vector.leastsquares.LeastSquaresBuilder builder} or it can
 * be created at once using a {@link org.hipparchus.optim.nonlinear.vector.leastsquares.LeastSquaresFactory
 * factory}.
 * @since 3.1
 */
package org.hipparchus.optim.nonlinear.vector.constrained;
