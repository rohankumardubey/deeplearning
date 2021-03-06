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
package org.eclipse.deeplearning4j.frameworkimport.keras.layers.embeddings;

import org.deeplearning4j.frameworkimport.keras.keras.layers.embeddings.KerasEmbedding;
import org.deeplearning4j.nn.conf.layers.EmbeddingSequenceLayer;
import org.deeplearning4j.BaseDL4JTest;
import org.deeplearning4j.frameworkimport.keras.keras.config.Keras1LayerConfiguration;
import org.deeplearning4j.frameworkimport.keras.keras.config.Keras2LayerConfiguration;
import org.deeplearning4j.frameworkimport.keras.keras.config.KerasLayerConfiguration;
import org.deeplearning4j.nn.params.DefaultParamInitializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.nd4j.common.tests.tags.NativeTag;
import org.nd4j.common.tests.tags.TagNames;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import java.util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;

/**
 * @author Max Pumperla
 */
@DisplayName("Keras Embedding Test")
@Tag(TagNames.FILE_IO)
@Tag(TagNames.KERAS)
@NativeTag
class KerasEmbeddingTest extends BaseDL4JTest {

    private final String LAYER_NAME = "embedding_sequence_layer";

    private final String INIT_KERAS = "glorot_normal";

    private final int[] INPUT_SHAPE = new int[] { 100, 20 };

    private static final boolean[] MASK_ZERO = new boolean[] { false, true };

    private Integer keras1 = 1;

    private Integer keras2 = 2;

    private Keras1LayerConfiguration conf1 = new Keras1LayerConfiguration();

    private Keras2LayerConfiguration conf2 = new Keras2LayerConfiguration();

    @Test
    @DisplayName("Test Embedding Layer")
    void testEmbeddingLayer() throws Exception {
        for (boolean mz : MASK_ZERO) {
            buildEmbeddingLayer(conf1, keras1, mz);
            buildEmbeddingLayer(conf2, keras2, mz);
        }
    }

    @Test
    @DisplayName("Test Embedding Layer Set Weights Mask Zero")
    void testEmbeddingLayerSetWeightsMaskZero() throws Exception {
        // GIVEN keras embedding with mask zero true
        KerasEmbedding embedding = buildEmbeddingLayer(conf1, keras1, true);
        // WHEN
        embedding.setWeights(Collections.singletonMap(conf1.getLAYER_FIELD_EMBEDDING_WEIGHTS(), Nd4j.ones(INPUT_SHAPE)));
        // THEN first row is set to zeros
        INDArray weights = embedding.getWeights().get(DefaultParamInitializer.WEIGHT_KEY);
        Assertions.assertEquals(embedding.getWeights().get(DefaultParamInitializer.WEIGHT_KEY).columns(), INPUT_SHAPE[1]);
    }

    private KerasEmbedding buildEmbeddingLayer(KerasLayerConfiguration conf, Integer kerasVersion, boolean maskZero) throws Exception {
        Map<String, Object> layerConfig = new HashMap<>();
        layerConfig.put(conf.getLAYER_FIELD_CLASS_NAME(), conf.getLAYER_CLASS_NAME_EMBEDDING());
        Map<String, Object> config = new HashMap<>();
        Integer inputDim = 10;
        Integer inputLength = 1;
        Integer outputDim = 10;
        config.put(conf.getLAYER_FIELD_INPUT_DIM(), inputDim);
        config.put(conf.getLAYER_FIELD_INPUT_LENGTH(), inputLength);
        config.put(conf.getLAYER_FIELD_OUTPUT_DIM(), outputDim);
        List<Integer> inputShape = new ArrayList<>(INPUT_SHAPE.length);
        for (int i : INPUT_SHAPE) {
            inputShape.add(i);
        }
        config.put(conf.getLAYER_FIELD_BATCH_INPUT_SHAPE(), inputShape);
        config.put(conf.getLAYER_FIELD_NAME(), LAYER_NAME);
        layerConfig.put(conf.getLAYER_FIELD_CONFIG(), config);
        layerConfig.put(conf.getLAYER_FIELD_KERAS_VERSION(), kerasVersion);
        if (kerasVersion == 1) {
            config.put(conf.getLAYER_FIELD_EMBEDDING_INIT(), INIT_KERAS);
        } else {
            Map<String, Object> init = new HashMap<>();
            init.put("class_name", conf.getINIT_GLOROT_NORMAL());
            config.put(conf.getLAYER_FIELD_EMBEDDING_INIT(), init);
        }
        config.put(conf.getLAYER_FIELD_MASK_ZERO(), maskZero);
        KerasEmbedding kerasEmbedding = new KerasEmbedding(layerConfig, false);
        assertEquals(kerasEmbedding.getNumParams(), 1);
        assertEquals(kerasEmbedding.isZeroMasking(), maskZero);
        EmbeddingSequenceLayer layer = kerasEmbedding.getEmbeddingLayer();
        assertEquals(LAYER_NAME, layer.getLayerName());
        return kerasEmbedding;
    }
}
