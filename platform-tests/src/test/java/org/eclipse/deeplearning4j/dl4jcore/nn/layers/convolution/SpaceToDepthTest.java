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
package org.eclipse.deeplearning4j.dl4jcore.nn.layers.convolution;

import org.deeplearning4j.BaseDL4JTest;
import org.deeplearning4j.nn.conf.layers.SpaceToDepthLayer;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.nd4j.common.tests.tags.NativeTag;
import org.nd4j.common.tests.tags.TagNames;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.deeplearning4j.nn.workspace.LayerWorkspaceMgr;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;

@DisplayName("Space To Depth Test")
@NativeTag
@Tag(TagNames.DL4J_OLD_API)
class SpaceToDepthTest extends BaseDL4JTest {

    private int mb = 1;

    private int inDepth = 2;

    private int inputWidth = 2;

    private int inputHeight = 2;

    private int blockSize = 2;

    private SpaceToDepthLayer.DataFormat dataFormat = SpaceToDepthLayer.DataFormat.NCHW;

    private int outDepth = inDepth * blockSize * blockSize;

    private int outputHeight = inputHeight / blockSize;

    private int outputWidth = inputWidth / blockSize;

    private INDArray getContainedData() {
        return Nd4j.create(new double[] { 1., 2., 3., 4., 5., 6., 7., 8. }, new int[] { mb, inDepth, inputHeight, inputWidth }, 'c');
    }

    private INDArray getContainedOutput() {
        return Nd4j.create(new double[] { 1., 5., 2., 6., 3., 7., 4., 8. }, new int[] { mb, outDepth, outputHeight, outputWidth }, 'c');
    }

    private Layer getSpaceToDepthLayer() {
        NeuralNetConfiguration conf = new NeuralNetConfiguration.Builder().gradientNormalization(GradientNormalization.RenormalizeL2PerLayer).seed(123).layer(new SpaceToDepthLayer.Builder(blockSize, dataFormat).build()).build();
        return conf.getLayer().instantiate(conf, null, 0, null, true, Nd4j.defaultFloatingPointType());
    }

    @Test
    @DisplayName("Test Space To Depth Forward")
    void testSpaceToDepthForward() throws Exception {
        INDArray containedInput = getContainedData();
        INDArray containedExpectedOut = getContainedOutput();
        Layer std = getSpaceToDepthLayer();
        INDArray containedOutput = std.activate(containedInput, false, LayerWorkspaceMgr.noWorkspaces());
        assertTrue(Arrays.equals(containedExpectedOut.shape(), containedOutput.shape()));
        assertEquals(containedExpectedOut, containedOutput);
    }

    @Test
    @DisplayName("Test Space To Depth Backward")
    void testSpaceToDepthBackward() throws Exception {
        INDArray containedInputEpsilon = getContainedOutput();
        INDArray containedExpectedOut = getContainedData();
        Layer std = getSpaceToDepthLayer();
        std.setInput(getContainedData(), LayerWorkspaceMgr.noWorkspaces());
        INDArray containedOutput = std.backpropGradient(containedInputEpsilon, LayerWorkspaceMgr.noWorkspaces()).getRight();
        assertTrue(Arrays.equals(containedExpectedOut.shape(), containedOutput.shape()));
        assertEquals(containedExpectedOut, containedOutput);
    }
}
