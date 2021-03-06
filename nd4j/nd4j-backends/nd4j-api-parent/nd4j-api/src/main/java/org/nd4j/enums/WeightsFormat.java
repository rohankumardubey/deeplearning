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

package org.nd4j.enums;

/**
 * WeightsFormat reflects the possible formats for a convolution weight matrix.
 * The following are available:
 *   YXIO: [kH, kW, iC, oC]
 *
 *   OIYX:  [oC, iC, kH, kW]
 *
 *   OYXI: [oC, kH, kW, iC]
 *
 */
public enum WeightsFormat {
  YXIO,

  OIYX,

  OYXI
}
