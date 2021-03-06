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

package org.deeplearning4j.frameworkimport.keras.keras.config;

import lombok.Data;


@Data
public class KerasModelConfiguration {

    /* Model meta information fields */
    private final String fieldClassName = "class_name";
    private final String fieldClassNameSequential = "Sequential";
    private final String fieldClassNameModel = "Model";
    private final String fieldNameClassFunctional = "Functional";
    private final String fieldKerasVersion = "keras_version";
    private final String fieldBackend = "backend";


    /* Model configuration field. */
    private final String modelFieldConfig = "config";
    private final String modelFieldLayers = "layers";
    private final String modelFieldInputLayers = "input_layers";
    private final String modelFieldOutputLayers = "output_layers";

    /* Training configuration field. */
    private final String trainingLoss = "loss";
    private final String trainingWeightsRoot = "model_weights";
    private final String trainingModelConfigAttribute = "model_config";
    private final String trainingTrainingConfigAttribute = "training_config";
    private final String optimizerConfig = "optimizer_config";
    //The model weight values as dictionaries. Introduced with keras 2.7.0
    public final  static String topLevelModelWeights = "top_level_model_weights";

}
