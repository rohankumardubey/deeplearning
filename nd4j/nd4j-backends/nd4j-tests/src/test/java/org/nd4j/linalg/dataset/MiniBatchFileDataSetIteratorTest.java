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

package org.nd4j.linalg.dataset;


import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.io.TempDir;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.nd4j.linalg.BaseNd4jTest;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4jBackend;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;


@RunWith(Parameterized.class)
public class MiniBatchFileDataSetIteratorTest extends BaseNd4jTest {


    public MiniBatchFileDataSetIteratorTest(Nd4jBackend backend) {
        super(backend);
    }


    @Test
    public void testMiniBatches(@TempDir Path testDir) throws Exception {
        DataSet load = new IrisDataSetIterator(150, 150).next();
        final MiniBatchFileDataSetIterator iter = new MiniBatchFileDataSetIterator(load, 10, false, testDir.toFile());
        while (iter.hasNext())
            assertEquals(10, iter.next().numExamples());
        if (iter.getRootDir() == null)
            return;
        DataSetIterator existing = new ExistingMiniBatchDataSetIterator(iter.getRootDir());
        while (iter.hasNext())
            assertEquals(10, existing.next().numExamples());
    }

    @Override
    public char ordering() {
        return 'f';
    }
}

