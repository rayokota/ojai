/**
 * Copyright (c) 2016 MapR, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ojai.store.cdps;

import org.ojai.Value.Type;
import org.ojai.annotation.API;

/**
 * This enumeration identifies the change event associated
 * with the current change node.
 */
@API.Public
public enum ChangeEvent {

  /**
   * The reader arrived at a scalar field or a field that was deleted.
   */
  NODE(1),

  /**
   * The reader arrived at beginning of a {@link Type#MAP MAP}.
   */
  START_MAP(2),

  /**
   * The reader reached the end of a map.
   */
  END_MAP(3),

  /**
   * The reader arrived at beginning of a {@link Type#ARRAY ARRAY}.
   */
  START_ARRAY(4),

  /**
   * The reader reached the end of an array.
   */
  END_ARRAY(5);

  private final byte code;

  private ChangeEvent(int code) {
    this.code = (byte) code;
  }

  public byte getCode() {
    return code;
  }

}
