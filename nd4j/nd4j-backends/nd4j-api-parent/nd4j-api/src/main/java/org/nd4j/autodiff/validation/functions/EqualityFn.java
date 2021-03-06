/*
 *  ******************************************************************************
 *  *
 *  *
 *  * This program and the accompanying materials are made available under the
 *  * terms of the Apache License, Version 2.0 which is available at
 *  * https://www.apache.org/licenses/LICENSE-2.0.
 *  *
 *  *  See the NOTICE file distributed with this work for additional
 *  *  information regarding copyright ownership.
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  * License for the specific language governing permissions and limitations
 *  * under the License.
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *  *****************************************************************************
 */

package org.nd4j.autodiff.validation.functions;

import lombok.AllArgsConstructor;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.common.function.Function;

@AllArgsConstructor
public class EqualityFn implements Function<INDArray,String> {
    private final INDArray expected;
    private double eps = 1e-6;

    @Override
    public String apply(INDArray actual) {
        if(expected.equalsWithEps(actual,eps)) {
            return null;
        }
        return "INDArray equality failed:\nExpected:\n" + expected + "\nActual:\n" + actual;
    }
}
