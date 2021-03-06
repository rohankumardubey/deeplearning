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

package org.eclipse.deeplearning4j.frameworkimport.keras.preprocessing.sequence;

import org.deeplearning4j.BaseDL4JTest;
import org.deeplearning4j.frameworkimport.keras.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.frameworkimport.keras.keras.preprocessing.sequence.TimeSeriesGenerator;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.nd4j.common.tests.tags.NativeTag;
import org.nd4j.common.tests.tags.TagNames;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.common.primitives.Pair;

import static org.junit.jupiter.api.Assertions.assertEquals;
@Tag(TagNames.FILE_IO)
@Tag(TagNames.KERAS)
@NativeTag
public class TimeSeriesGeneratorTest extends BaseDL4JTest {

    @Test
    public void tsGeneratorTest() throws InvalidKerasConfigurationException {
        INDArray data = Nd4j.create(50, 10);
        INDArray targets = Nd4j.create(50, 10);


        int length = 10;
        int samplingRate = 2;
        int stride = 1;
        int startIndex = 0;
        int endIndex = 49;
        int batchSize = 1;

        boolean shuffle = false;
        boolean reverse = false;

        TimeSeriesGenerator gen = new TimeSeriesGenerator(data, targets, length,
                samplingRate, stride, startIndex, endIndex, shuffle, reverse, batchSize);

        assertEquals(length, gen.getLength());
        assertEquals(startIndex + length, gen.getStartIndex());
        assertEquals(reverse, gen.isReverse());
        assertEquals(shuffle, gen.isShuffle());
        assertEquals(endIndex, gen.getEndIndex());
        assertEquals(batchSize, gen.getBatchSize());
        assertEquals(samplingRate, gen.getSamplingRate());
        assertEquals(stride, gen.getStride());

        Pair<INDArray, INDArray> next = gen.next(0);
    }
}
