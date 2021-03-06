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
package org.eclipse.deeplearning4j.dl4jcore.datasets.iterator;

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.BaseDL4JTest;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.datasets.iterator.ExistingDataSetIterator;
import org.deeplearning4j.datasets.iterator.MultipleEpochsIterator;
import org.eclipse.deeplearning4j.dl4jcore.nn.util.TestDataSetConsumer;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import org.nd4j.common.tests.tags.NativeTag;
import org.nd4j.common.tests.tags.TagNames;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.common.io.ClassPathResource;
import org.nd4j.common.resources.Resources;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;

@DisplayName("Multiple Epochs Iterator Test")
@NativeTag
@Tag(TagNames.FILE_IO)
class MultipleEpochsIteratorTest extends BaseDL4JTest {

    @Test
    @DisplayName("Test Next And Reset")
    void testNextAndReset() throws Exception {
        int epochs = 3;
        RecordReader rr = new CSVRecordReader();
        rr.initialize(new FileSplit(Resources.asFile("iris.txt")));
        DataSetIterator iter = new RecordReaderDataSetIterator(rr, 150);
        MultipleEpochsIterator multiIter = new MultipleEpochsIterator(epochs, iter);
        assertTrue(multiIter.hasNext());
        while (multiIter.hasNext()) {
            DataSet path = multiIter.next();
            assertFalse(path == null);
        }
        assertEquals(epochs, multiIter.getEpochs());
    }

    @Test
    @DisplayName("Test Load Full Data Set")
    void testLoadFullDataSet() throws Exception {
        int epochs = 3;
        RecordReader rr = new CSVRecordReader();
        rr.initialize(new FileSplit(Resources.asFile("iris.txt")));
        DataSetIterator iter = new RecordReaderDataSetIterator(rr, 150);
        DataSet ds = iter.next(50);
        assertEquals(50, ds.getFeatures().size(0));
        MultipleEpochsIterator multiIter = new MultipleEpochsIterator(epochs, ds);
        assertTrue(multiIter.hasNext());
        int count = 0;
        while (multiIter.hasNext()) {
            DataSet path = multiIter.next();
            assertNotNull(path);
            assertEquals(50, path.numExamples(), 0);
            count++;
        }
        assertEquals(epochs, count);
        assertEquals(epochs, multiIter.getEpochs());
    }

    @Test
    @DisplayName("Test Load Batch Data Set")
    void testLoadBatchDataSet() throws Exception {
        int epochs = 2;
        RecordReader rr = new CSVRecordReader();
        rr.initialize(new FileSplit(new ClassPathResource("iris.txt").getFile()));
        DataSetIterator iter = new RecordReaderDataSetIterator(rr, 150, 4, 3);
        DataSet ds = iter.next(20);
        assertEquals(20, ds.getFeatures().size(0));
        MultipleEpochsIterator multiIter = new MultipleEpochsIterator(epochs, ds);
        while (multiIter.hasNext()) {
            DataSet path = multiIter.next(10);
            assertNotNull(path);
            assertEquals(10, path.numExamples(), 0.0);
        }
        assertEquals(epochs, multiIter.getEpochs());
    }

    @Test
    @DisplayName("Test MEDI With Load 1")
    void testMEDIWithLoad1() throws Exception {
        ExistingDataSetIterator iter = new ExistingDataSetIterator(new IterableWithoutException(100));
        MultipleEpochsIterator iterator = new MultipleEpochsIterator(10, iter, 24);
        TestDataSetConsumer consumer = new TestDataSetConsumer(iterator, 1);
        long num = consumer.consumeWhileHasNext(true);
        assertEquals(10 * 100, num);
    }

    @Test
    @DisplayName("Test MEDI With Load 2")
    void testMEDIWithLoad2() throws Exception {
        ExistingDataSetIterator iter = new ExistingDataSetIterator(new IterableWithoutException(100));
        MultipleEpochsIterator iterator = new MultipleEpochsIterator(10, iter, 24);
        TestDataSetConsumer consumer = new TestDataSetConsumer(iterator, 2);
        long num1 = 0;
        for (; num1 < 150; num1++) {
            consumer.consumeOnce(iterator.next(), true);
        }
        iterator.reset();
        long num2 = consumer.consumeWhileHasNext(true);
        assertEquals((10 * 100) + 150, num1 + num2);
    }

    @Test
    @DisplayName("Test MEDI With Load 3")
    void testMEDIWithLoad3() throws Exception {
        ExistingDataSetIterator iter = new ExistingDataSetIterator(new IterableWithoutException(10000));
        MultipleEpochsIterator iterator = new MultipleEpochsIterator(iter, 24, 136);
        TestDataSetConsumer consumer = new TestDataSetConsumer(iterator, 2);
        long num1 = 0;
        while (iterator.hasNext()) {
            consumer.consumeOnce(iterator.next(), true);
            num1++;
        }
        assertEquals(136, num1);
    }

    @DisplayName("Iterable Without Exception")
    private class IterableWithoutException implements Iterable<DataSet> {

        private final AtomicLong counter = new AtomicLong(0);

        private final int datasets;

        public IterableWithoutException(int datasets) {
            this.datasets = datasets;
        }

        @Override
        public Iterator<DataSet> iterator() {
            counter.set(0);
            return new Iterator<DataSet>() {

                @Override
                public boolean hasNext() {
                    return counter.get() < datasets;
                }

                @Override
                public DataSet next() {
                    counter.incrementAndGet();
                    return new DataSet(Nd4j.create(100), Nd4j.create(10));
                }

                @Override
                public void remove() {
                }
            };
        }
    }
}
