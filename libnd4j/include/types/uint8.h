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

#ifndef LIBND4J_UINT8_H
#define LIBND4J_UINT8_H
#include <stdint.h>
#include <system/op_boilerplate.h>

namespace sd {

float SD_INLINE SD_HOST_DEVICE cpu_uint82float(uint8_t data);
uint8_t SD_INLINE SD_HOST_DEVICE cpu_float2uint8(float data);

struct uint8 {
  uint8_t data;

  SD_INLINE SD_HOST_DEVICE uint8();
  SD_INLINE SD_HOST_DEVICE ~uint8() = default;

  template <class T>
  SD_INLINE SD_HOST_DEVICE uint8(const T& rhs);

  template <class T>
  SD_INLINE SD_HOST_DEVICE uint8& operator=(const T& rhs);

  SD_INLINE SD_HOST_DEVICE operator float() const;

  SD_INLINE SD_HOST_DEVICE void assign(double rhs);

  SD_INLINE SD_HOST_DEVICE void assign(float rhs);
};

///////////////////////////

float cpu_uint82float(uint8_t data) { return static_cast<float>(static_cast<int>(data)); }

uint8_t cpu_float2uint8(float data) {
  auto t = static_cast<int>(data);
  if (t > 255) t = 255;
  if (t < 0) t = 0;

  return static_cast<uint8_t>(t);
}

uint8::uint8() { data = cpu_float2uint8(0.0f); }

template <class T>
uint8::uint8(const T& rhs) {
  assign(rhs);
}

template <class T>
uint8& uint8::operator=(const T& rhs) {
  assign(rhs);
  return *this;
}

uint8::operator float() const { return cpu_uint82float(data); }

void uint8::assign(double rhs) { assign(static_cast<float>(rhs)); }

void uint8::assign(float rhs) { data = cpu_float2uint8(rhs); }
}  // namespace sd

#endif  // LIBND4J_UINT8_H
