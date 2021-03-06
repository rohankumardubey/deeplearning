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
package org.eclipse.deeplearning4j.dl4jcore.nn.layers;

import org.deeplearning4j.BaseDL4JTest;
import org.deeplearning4j.datasets.iterator.impl.IrisDataSetIterator;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.AutoEncoder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.nd4j.common.tests.tags.NativeTag;
import org.nd4j.common.tests.tags.TagNames;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.deeplearning4j.nn.workspace.LayerWorkspaceMgr;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;

/**
 */
@DisplayName("Seed Test")
@NativeTag
@Tag(TagNames.DL4J_OLD_API)
@Tag(TagNames.FILE_IO)
@Tag(TagNames.RNG)
class SeedTest extends BaseDL4JTest {

    private DataSetIterator irisIter = new IrisDataSetIterator(50, 50);

    private DataSet data = irisIter.next();

    @Test
    @DisplayName("Test Auto Encoder Seed")
    void testAutoEncoderSeed() {
        AutoEncoder layerType = new AutoEncoder.Builder().nIn(4).nOut(3).corruptionLevel(0.0).activation(Activation.SIGMOID).build();
        NeuralNetConfiguration conf = new NeuralNetConfiguration.Builder().layer(layerType).seed(123).build();
        long numParams = conf.getLayer().initializer().numParams(conf);
        INDArray params = Nd4j.create(1, numParams);
        Layer layer = conf.getLayer().instantiate(conf, null, 0, params, true, params.dataType());
        layer.setBackpropGradientsViewArray(Nd4j.create(1, numParams));
        layer.fit(data.getFeatures(), LayerWorkspaceMgr.noWorkspaces());
        layer.computeGradientAndScore(LayerWorkspaceMgr.noWorkspaces());
        double score = layer.score();
        INDArray parameters = layer.params();
        layer.setParams(parameters);
        layer.computeGradientAndScore(LayerWorkspaceMgr.noWorkspaces());
        double score2 = layer.score();
        assertEquals(parameters, layer.params());
        assertEquals(score, score2, 1e-4);
    }
}
