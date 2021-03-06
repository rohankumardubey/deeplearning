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
package org.eclipse.deeplearning4j.dl4jcore.optimize.solver.accumulation;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.RandomUtils;
import org.deeplearning4j.BaseDL4JTest;
import org.deeplearning4j.optimize.solvers.accumulation.SmartFancyBlockingQueue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.nd4j.common.util.ThreadUtils;
import org.nd4j.linalg.factory.Nd4j;
import java.util.ArrayList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.assertTimeout;

@Slf4j
@Disabled("AB 2019/05/21 - Failing (stuck, causing timeouts) - Issue #7657")
@DisplayName("Smart Fancy Blocking Queue Test")
class SmartFancyBlockingQueueTest extends BaseDL4JTest {

    @Test
    @DisplayName("Test SFBQ _ 1")
    void testSFBQ_1() {
        assertTimeout(ofMillis(120000), () -> {
            val queue = new SmartFancyBlockingQueue(8, Nd4j.create(5, 5));
            val array = Nd4j.create(5, 5);
            for (int e = 0; e < 6; e++) {
                queue.put(Nd4j.create(5, 5).assign(e));
            }
            ;
            assertEquals(6, queue.size());
            for (int e = 6; e < 10; e++) {
                queue.put(Nd4j.create(5, 5).assign(e));
            }
            assertEquals(1, queue.size());
        });
    }

    @Test
    @DisplayName("Test SFBQ _ 2")
    void testSFBQ_2() {
        assertTimeout(ofMillis(120000), () -> {
            final val queue = new SmartFancyBlockingQueue(1285601, Nd4j.create(5, 5));
            final val barrier = new CyclicBarrier(4);
            val threads = new ArrayList<Thread>();
            for (int e = 0; e < 4; e++) {
                val f = e;
                val t = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        int cnt = 0;
                        while (true) {
                            while (cnt < 1000) {
                                if (!queue.isEmpty()) {
                                    if (cnt % 50 == 0)
                                        log.info("Thread {}: [{}]", f, cnt);
                                    val arr = queue.poll();
                                    assertNotNull(arr);
                                    val local = arr.unsafeDuplication(true);
                                    assertEquals(cnt, local.meanNumber().intValue());
                                    cnt++;
                                }
                                try {
                                    barrier.await();
                                    if (f == 0)
                                        queue.registerConsumers(4);
                                    barrier.await();
                                } catch (InterruptedException e1) {
                                    e1.printStackTrace();
                                } catch (BrokenBarrierException e1) {
                                    e1.printStackTrace();
                                }
                            }
                            break;
                        }
                    }
                });
                t.setName("reader thread " + f);
                t.start();
                threads.add(t);
            }
            for (int e = 0; e < 1000; e++) {
                queue.put(Nd4j.create(5, 5).assign(e));
                Nd4j.getExecutioner().commit();
            }
            for (val t : threads) t.join();
        });
    }

    @Test
    @DisplayName("Test SFBQ _ 3")
    void testSFBQ_3() {
        assertTimeout(ofMillis(120000), () -> {
            final val queue = new SmartFancyBlockingQueue(1285601, Nd4j.create(5, 5));
            val threads = new ArrayList<Thread>();
            for (int e = 0; e < 4; e++) {
                val f = e;
                val t = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        int cnt = 0;
                        while (true) {
                            while (cnt < 1000) {
                                if (!queue.isEmpty()) {
                                    if (cnt % 50 == 0)
                                        log.info("Thread {}: [{}]", f, cnt);
                                    val arr = queue.poll();
                                    assertNotNull(arr);
                                    val local = arr.unsafeDuplication(true);
                                    cnt++;
                                }
                            }
                            break;
                        }
                    }
                });
                t.start();
                threads.add(t);
            }
            val b = new Thread(new Runnable() {

                @Override
                public void run() {
                    while (true) {
                        queue.registerConsumers(4);
                        ThreadUtils.uncheckedSleep(30);
                    }
                }
            });
            b.setDaemon(true);
            b.start();
            val writers = new ArrayList<Thread>();
            for (int e = 0; e < 4; e++) {
                val t = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        for (int e = 0; e < 250; e++) {
                            try {
                                queue.put(Nd4j.createUninitialized(5, 5).assign(e));
                                Thread.sleep(30);
                            } catch (Exception ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    }
                });
                writers.add(t);
                t.start();
            }
            for (val t : writers) t.join();
            for (val t : threads) t.join();
        });
    }

    @Test
    @DisplayName("Test SFBQ _ 4")
    void testSFBQ_4() {
        assertTimeout(ofMillis(120000), () -> {
            final val queue = new SmartFancyBlockingQueue(16, Nd4j.create(5, 5));
            final val barrier = new CyclicBarrier(4);
            /*
        val m = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    queue.registerConsumers(4);
                    ThreadUtils.uncheckedSleep(100);
                }
            }
        });


        m.setName("master thread");
        m.setDaemon(true);
        m.start();
*/
            val threads = new ArrayList<Thread>();
            for (int e = 0; e < 4; e++) {
                val f = e;
                val t = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            for (int e = 0; e < 100; e++) {
                                log.info("[Thread {}]: fill phase {}", f, e);
                                val numUpdates = RandomUtils.nextInt(8, 128);
                                for (int p = 0; p < numUpdates; p++) {
                                    queue.put(Nd4j.createUninitialized(5, 5));
                                }
                                if (f == 0)
                                    queue.registerConsumers(4);
                                barrier.await();
                                log.info("[Thread {}]: read phase {}", f, e);
                                while (!queue.isEmpty()) {
                                    val arr = queue.poll();
                                    assertNotNull(arr);
                                }
                                barrier.await();
                            }
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        } catch (BrokenBarrierException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                t.setName("worker thread " + f);
                t.start();
                threads.add(t);
            }
            for (val t : threads) t.join();
        });
    }

    @Test
    @DisplayName("Test SFBQ _ 5")
    void testSFBQ_5() {
        assertTimeout(ofMillis(120000), () -> {
            final val queue = new SmartFancyBlockingQueue(16, Nd4j.create(5, 5));
            final val barrier = new CyclicBarrier(4);
            // writers are just spamming updates every X ms
            val writers = new ArrayList<Thread>();
            for (int e = 0; e < 4; e++) {
                val w = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        while (true) {
                            try {
                                val n = RandomUtils.nextInt(8, 64);
                                for (int i = 1; i < n + 1; i++) {
                                    val arr = Nd4j.createUninitialized(5, 5).assign(i);
                                    Nd4j.getExecutioner().commit();
                                    queue.put(arr);
                                }
                                ThreadUtils.uncheckedSleep(10);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                });
                w.setName("writer thread " + e);
                w.setDaemon(true);
                w.start();
                writers.add(w);
            }
            // each reader will read 250 updates. supposedly equal :)
            final long[] means = new long[4];
            val readers = new ArrayList<Thread>();
            for (int e = 0; e < 4; e++) {
                final int f = e;
                means[f] = 0;
                val t = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            int cnt = 0;
                            int fnt = 0;
                            while (cnt < 1000) {
                                if (!queue.isEmpty()) {
                                    while (!queue.isEmpty()) {
                                        val m = queue.poll();
                                        val arr = m.unsafeDuplication(true);
                                        val mean = arr.meanNumber().longValue();
                                        assertNotEquals(0, mean,"Failed at cycle: " + cnt);
                                        means[f] += mean;
                                        cnt++;
                                    }
                                    barrier.await();
                                }
                                barrier.await();
                                if (f == 0) {
                                    log.info("Read cycle finished");
                                    queue.registerConsumers(4);
                                }
                                barrier.await();
                            }
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        } catch (BrokenBarrierException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                t.setName("reader thread " + f);
                t.start();
                readers.add(t);
            }
            for (val t : readers) t.join();
            // all messages should be the same
            assertEquals(means[0], means[1]);
            assertEquals(means[0], means[2]);
            assertEquals(means[0], means[3]);
        });
    }
}
