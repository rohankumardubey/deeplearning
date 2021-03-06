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
package org.nd4j.imports;

import lombok.val;

public class VariableUtils {

    /**
     * Remove the ":1" etc suffix for a variable name to get the op name
     *
     * @param varName Variable name
     * @return Variable name without any number suffix
     */
    public static String stripVarSuffix(String varName) {
        if(varName == null)
            return null;
        if (varName.matches(".*:\\d+")) {
            val idx = varName.lastIndexOf(':');
            return varName.substring(0, idx);
        }
        return varName;
    }


}
