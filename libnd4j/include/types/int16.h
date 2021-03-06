/* ******************************************************************************
 *
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ******************************************************************************/

//
// @author raver119@gmail.com
//

#ifndef LIBND4J_INT16_H
#define LIBND4J_INT16_H
#include <stdint.h>
#include <system/op_boilerplate.h>

namespace sd {

float SD_INLINE SD_HOST_DEVICE cpu_int162float(int16_t data);
int16_t SD_INLINE SD_HOST_DEVICE cpu_float2int16(float data);

struct int16 {
  int16_t data;

  SD_INLINE SD_HOST_DEVICE int16();
  SD_INLINE SD_HOST_DEVICE ~int16() = default;

  template <class T>
  SD_INLINE SD_HOST_DEVICE int16(const T& rhs);

  template <class T>
  SD_INLINE SD_HOST_DEVICE int16& operator=(const T& rhs);

  SD_INLINE SD_HOST_DEVICE operator float() const;

  SD_INLINE SD_HOST_DEVICE void assign(double rhs);

  SD_INLINE SD_HOST_DEVICE void assign(float rhs);
};

//////////////////////////////

float cpu_int162float(int16_t data) { return (float)((int)data); }

int16_t cpu_float2int16(float data) {
  auto t = static_cast<int>(data);
  if (t > 32767) t = 32767;
  if (t < -32768) t = -32768;

  return static_cast<int16_t>(t);
}

int16::int16() { data = cpu_float2int16(0.0f); }

template <class T>
int16::int16(const T& rhs) {
  assign(rhs);
}

template <class T>
int16& int16::operator=(const T& rhs) {
  assign(rhs);
  return *this;
}

int16::operator float() const { return cpu_int162float(data); }

void int16::assign(double rhs) { assign(static_cast<float>(rhs)); }

void int16::assign(float rhs) { data = cpu_float2int16(rhs); }

}  // namespace sd

#endif  // LIBND4J_INT16_H
