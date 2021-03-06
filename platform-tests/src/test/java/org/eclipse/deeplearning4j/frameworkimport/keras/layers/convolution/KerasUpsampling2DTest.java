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
package org.eclipse.deeplearning4j.frameworkimport.keras.layers.convolution;

import org.deeplearning4j.nn.conf.layers.Upsampling2D;
import org.deeplearning4j.BaseDL4JTest;
import org.deeplearning4j.frameworkimport.keras.keras.config.Keras1LayerConfiguration;
import org.deeplearning4j.frameworkimport.keras.keras.config.Keras2LayerConfiguration;
import org.deeplearning4j.frameworkimport.keras.keras.config.KerasLayerConfiguration;
import org.deeplearning4j.frameworkimport.keras.keras.layers.convolutional.KerasUpsampling2D;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.nd4j.common.tests.tags.NativeTag;
import org.nd4j.common.tests.tags.TagNames;

/**
 * @author Max Pumperla
 */
@DisplayName("Keras Upsampling 2 D Test")
@Tag(TagNames.FILE_IO)
@Tag(TagNames.KERAS)
@NativeTag
class KerasUpsampling2DTest extends BaseDL4JTest {

    private final String LAYER_NAME = "upsampling_2D_layer";

    private int[] size = new int[] { 2, 2 };

    private Integer keras1 = 1;

    private Integer keras2 = 2;

    private Keras1LayerConfiguration conf1 = new Keras1LayerConfiguration();

    private Keras2LayerConfiguration conf2 = new Keras2LayerConfiguration();

    @Test
    @DisplayName("Test Upsampling 2 D Layer")
    void testUpsampling2DLayer() throws Exception {
        buildUpsampling2DLayer(conf1, keras1);
        buildUpsampling2DLayer(conf2, keras2);
    }

    private void buildUpsampling2DLayer(KerasLayerConfiguration conf, Integer kerasVersion) throws Exception {
        Map<String, Object> layerConfig = new HashMap<>();
        layerConfig.put(conf.getLAYER_FIELD_CLASS_NAME(), conf.getLAYER_CLASS_NAME_UPSAMPLING_2D());
        Map<String, Object> config = new HashMap<>();
        List<Integer> sizeList = new ArrayList<>();
        sizeList.add(size[0]);
        sizeList.add(size[1]);
        config.put(conf.getLAYER_FIELD_UPSAMPLING_2D_SIZE(), sizeList);
        config.put(conf.getLAYER_FIELD_NAME(), LAYER_NAME);
        layerConfig.put(conf.getLAYER_FIELD_CONFIG(), config);
        layerConfig.put(conf.getLAYER_FIELD_KERAS_VERSION(), kerasVersion);
        Upsampling2D layer = new KerasUpsampling2D(layerConfig).getUpsampling2DLayer();
        assertEquals(LAYER_NAME, layer.getLayerName());
        assertEquals(size[0], layer.getSize()[0]);
        assertEquals(size[1], layer.getSize()[1]);
    }
}
