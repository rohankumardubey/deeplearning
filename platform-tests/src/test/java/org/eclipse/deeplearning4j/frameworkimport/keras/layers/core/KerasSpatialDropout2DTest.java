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
package org.eclipse.deeplearning4j.frameworkimport.keras.layers.core;

import org.deeplearning4j.frameworkimport.keras.keras.layers.core.KerasSpatialDropout;
import org.deeplearning4j.nn.conf.dropout.SpatialDropout;
import org.deeplearning4j.nn.conf.layers.DropoutLayer;
import org.deeplearning4j.BaseDL4JTest;
import org.deeplearning4j.frameworkimport.keras.keras.config.Keras1LayerConfiguration;
import org.deeplearning4j.frameworkimport.keras.keras.config.Keras2LayerConfiguration;
import org.deeplearning4j.frameworkimport.keras.keras.config.KerasLayerConfiguration;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.nd4j.common.tests.tags.NativeTag;
import org.nd4j.common.tests.tags.TagNames;

/**
 * @author Max Pumperla
 */
@DisplayName("Keras Spatial Dropout 2 D Test")
@Tag(TagNames.FILE_IO)
@Tag(TagNames.KERAS)
@NativeTag
class KerasSpatialDropout2DTest extends BaseDL4JTest {

    String LAYER_NAME = "spatial_dropout_2d";

    private final double RATE_KERAS = 0.3;

    private final double RATE_DL4J = 1 - RATE_KERAS;

    private Integer keras1 = 1;

    private Integer keras2 = 2;

    private Keras1LayerConfiguration conf1 = new Keras1LayerConfiguration();

    private Keras2LayerConfiguration conf2 = new Keras2LayerConfiguration();

    @Test
    @DisplayName("Test Spatial Dropout Layer")
    void testSpatialDropoutLayer() throws Exception {
        buildSpatialDropoutLayer(conf1, keras1);
        buildSpatialDropoutLayer(conf2, keras2);
    }

    private void buildSpatialDropoutLayer(KerasLayerConfiguration conf, Integer kerasVersion) throws Exception {
        Map<String, Object> layerConfig = new HashMap<>();
        layerConfig.put(conf.getLAYER_FIELD_CLASS_NAME(), conf.getLAYER_CLASS_NAME_SPATIAL_DROPOUT_2D());
        Map<String, Object> config = new HashMap<>();
        config.put(conf.getLAYER_FIELD_NAME(), LAYER_NAME);
        config.put(conf.getLAYER_FIELD_RATE(), RATE_KERAS);
        layerConfig.put(conf.getLAYER_FIELD_CONFIG(), config);
        layerConfig.put(conf.getLAYER_FIELD_KERAS_VERSION(), kerasVersion);
        DropoutLayer layer = new KerasSpatialDropout(layerConfig).getSpatialDropoutLayer();
        assertEquals(LAYER_NAME, layer.getLayerName());
        assertEquals(new SpatialDropout(RATE_DL4J), layer.getIDropout());
    }
}
