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

package org.nd4j.linalg.dataset.api.iterator.fetcher;

import org.nd4j.linalg.dataset.DataSet;

import java.io.File;
import java.io.Serializable;

public interface DataSetFetcher extends Serializable {


    /**
     * The top level directory to download datasets from
     * This will usually be the default dl4j resources directory
     * at user home/.deeplearning4j
     * @return
     */
    File topLevelDir();

    /**
     * For file download datasets this sets a top level directory
     * outside the default
     * @param topLevelDir the top level directory to use
     */
    void setTopLevelDir(File topLevelDir);

    /**
     * Whether the dataset has more to load
     *
     * @return whether the data applyTransformToDestination has more to load
     */
    boolean hasMore();

    /**
     * Returns the next data applyTransformToDestination
     *
     * @return the next dataset
     */
    DataSet next();

    /**
     * Fetches the next dataset. You need to call this
     * to getFromOrigin a new dataset, otherwise {@link #next()}
     * just returns the last data applyTransformToDestination fetch
     *
     * @param numExamples the number of examples to fetch
     */
    void fetch(int numExamples);

    /**
     * The number of labels for a dataset
     *
     * @return the number of labels for a dataset
     */
    int totalOutcomes();

    /**
     * The length of a feature vector for an individual example
     *
     * @return the length of a feature vector for an individual example
     */
    int inputColumns();

    /**
     * The total number of examples
     *
     * @return the total number of examples
     */
    int totalExamples();

    /**
     * Returns the fetcher back to the beginning of the dataset
     */
    void reset();

    /**
     * Direct access to a number represenative of iterating through a dataset
     *
     * @return a cursor similar to an index
     */
    int cursor();
}
