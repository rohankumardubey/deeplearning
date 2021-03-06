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

package org.eclipse.deeplearning4j.nd4j.linalg.util;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import org.nd4j.common.tests.tags.NativeTag;
import org.nd4j.common.tests.tags.TagNames;
import org.nd4j.linalg.BaseNd4jTestWithBackends;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.shape.Shape;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.factory.Nd4jBackend;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Adam Gibson
 */
@Tag(TagNames.NDARRAY_INDEXING)
@NativeTag
public class ShapeTest extends BaseNd4jTestWithBackends {



    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testToOffsetZero(Nd4jBackend backend) {
        INDArray matrix = Nd4j.rand(3, 5);
        INDArray rowOne = matrix.getRow(1);
        INDArray row1Copy = Shape.toOffsetZero(rowOne);
        assertEquals(rowOne, row1Copy);
        INDArray rows = matrix.getRows(1, 2);
        INDArray rowsOffsetZero = Shape.toOffsetZero(rows);
        assertEquals(rows, rowsOffsetZero);

        INDArray tensor = Nd4j.rand(new int[] {3, 3, 3});
        INDArray getTensor = tensor.slice(1).slice(1);
        INDArray getTensorZero = Shape.toOffsetZero(getTensor);
        assertEquals(getTensor, getTensorZero);



    }


    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testDupLeadingTrailingZeros(Nd4jBackend backend) {
        testDupHelper(1, 10);
        testDupHelper(10, 1);
        testDupHelper(1, 10, 1);
        testDupHelper(1, 10, 1, 1);
        testDupHelper(1, 10, 2);
        testDupHelper(2, 10, 1, 1);
        testDupHelper(1, 1, 1, 10);
        testDupHelper(10, 1, 1, 1);
        testDupHelper(1, 1);

    }

    private void testDupHelper(int... shape) {
        INDArray arr = Nd4j.ones(shape);
        INDArray arr2 = arr.dup();
        assertArrayEquals(arr.shape(), arr2.shape());
        assertTrue(arr.equals(arr2));
    }

    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testLeadingOnes(Nd4jBackend backend) {
        INDArray arr = Nd4j.create(1, 5, 5);
        assertEquals(1, arr.getLeadingOnes());
        INDArray arr2 = Nd4j.create(2, 2);
        assertEquals(0, arr2.getLeadingOnes());
        INDArray arr4 = Nd4j.create(1, 1, 5, 5);
        assertEquals(2, arr4.getLeadingOnes());
    }

    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testTrailingOnes(Nd4jBackend backend) {
        INDArray arr2 = Nd4j.create(5, 5, 1);
        assertEquals(1, arr2.getTrailingOnes());
        INDArray arr4 = Nd4j.create(5, 5, 1, 1);
        assertEquals(2, arr4.getTrailingOnes());
    }

    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testElementWiseCompareOnesInMiddle(Nd4jBackend backend) {
        INDArray arr = Nd4j.linspace(1, 6, 6).reshape(2, 3);
        INDArray onesInMiddle = Nd4j.linspace(1, 6, 6).reshape(2, 1, 3);
        for (int i = 0; i < arr.length(); i++) {
            double val = arr.getDouble(i);
            double middleVal = onesInMiddle.getDouble(i);
            assertEquals(val, middleVal, 1e-1);
        }
    }


    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testSumLeadingTrailingZeros(Nd4jBackend backend) {
        testSumHelper(1, 5, 5);
        testSumHelper(5, 5, 1);
        testSumHelper(1, 5, 1);

        testSumHelper(1, 5, 5, 5);
        testSumHelper(5, 5, 5, 1);
        testSumHelper(1, 5, 5, 1);

        testSumHelper(1, 5, 5, 5, 5);
        testSumHelper(5, 5, 5, 5, 1);
        testSumHelper(1, 5, 5, 5, 1);

        testSumHelper(1, 5, 5, 5, 5, 5);
        testSumHelper(5, 5, 5, 5, 5, 1);
        testSumHelper(1, 5, 5, 5, 5, 1);
    }

    private void testSumHelper(int... shape) {
        INDArray array = Nd4j.ones(shape);
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < array.vectorsAlongDimension(i); j++) {
                INDArray vec = array.vectorAlongDimension(j, i);
            }
            array.sum(i);
        }
    }


    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testEqualsWithSqueeze(){

        assertTrue(Shape.shapeEqualWithSqueeze(null, null));
        assertTrue(Shape.shapeEqualWithSqueeze(new long[0], new long[0]));
        assertTrue(Shape.shapeEqualWithSqueeze(new long[0], new long[]{1}));
        assertFalse(Shape.shapeEqualWithSqueeze(new long[0], new long[]{1,2}));
        assertFalse(Shape.shapeEqualWithSqueeze(new long[0], new long[]{2,1}));
        assertTrue(Shape.shapeEqualWithSqueeze(new long[]{1}, new long[0]));
        assertTrue(Shape.shapeEqualWithSqueeze(new long[0], new long[]{1,1,1,1,1}));
        assertTrue(Shape.shapeEqualWithSqueeze(new long[]{1,1,1,1,1}, new long[0]));
        assertTrue(Shape.shapeEqualWithSqueeze(new long[]{1}, new long[]{1,1,1}));

        assertTrue(Shape.shapeEqualWithSqueeze(new long[]{2,3}, new long[]{2,3}));
        assertFalse(Shape.shapeEqualWithSqueeze(new long[]{2,3}, new long[]{3,2}));
        assertTrue(Shape.shapeEqualWithSqueeze(new long[]{1,2,2}, new long[]{2,2}));
        assertTrue(Shape.shapeEqualWithSqueeze(new long[]{1,2,3}, new long[]{2,1,1,3}));
        assertFalse(Shape.shapeEqualWithSqueeze(new long[]{1,2,3}, new long[]{2,1,1,4}));

    }

    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testShapeOrder(){
        long[] shape = {2,2};
        long[] stride = {1,8};  //Ascending strides -> F order

        char order = Shape.getOrder(shape, stride, 1);
        assertEquals('f', order);
    }

    @Override
    public char ordering() {
        return 'f';
    }
}
