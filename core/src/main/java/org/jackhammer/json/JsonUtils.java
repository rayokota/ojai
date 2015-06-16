/**
 * Copyright (c) 2015 MapR, Inc.
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
package org.jackhammer.json;

import org.jackhammer.RecordReader;
import org.jackhammer.RecordReader.EventType;
import org.jackhammer.RecordWriter;
import org.jackhammer.annotation.API;

@API.Internal
public class JsonUtils {
  static void addToMap(RecordReader r, RecordWriter w) {
    EventType e;
    String currentFieldName = null;
    while((e = r.next()) != null) {
      switch (e) {
      case START_MAP:
        if (currentFieldName != null) {
          w.putNewMap(currentFieldName);
        }
        addToMap(r, w);
        break;
      case END_MAP:
        w.endMap();
        return;
      case FIELD_NAME:
        currentFieldName = r.getFieldName();
        break;
      case STRING:
        w.put(currentFieldName, r.getString());
        break;
      case BOOLEAN:
        w.put(currentFieldName, r.getBoolean());
        break;
      case LONG:
        w.put(currentFieldName, r.getLong());
        break;
      case DOUBLE:
        w.put(currentFieldName, r.getDouble());
        break;
      case DATE:
        w.put(currentFieldName, r.getDate());
        break;
      case TIME:
        w.put(currentFieldName, r.getTime());
        break;
      case NULL:
        w.putNull(currentFieldName);
        break;
      case BINARY:
        w.put(currentFieldName, r.getBinary());
        break;
      case START_ARRAY:
        w.putNewArray(currentFieldName);
        addToArray(r, w);
        break;
      case END_ARRAY:
        w.endArray();
        break;
      default:
        System.out.println(e);
        break;
      }
    }
  }

  private static void addToArray(RecordReader r, RecordWriter w) {
    EventType e;
    String currentFieldName = null;
    while((e = r.next()) != null) {
      switch (e) {
      case START_MAP:
        w.addNewMap();
        addToMap(r, w);
        break;
      case END_MAP:
        w.endMap();
        break;
      case FIELD_NAME:
        assert false;
        break;
      case STRING:
        w.add(r.getString());
        break;
      case BOOLEAN:
        w.add(r.getBoolean());
        break;
      case LONG:
        w.add(r.getLong());
        break;
      case DOUBLE:
        w.add(r.getDouble());
        break;
      case DATE:
        w.add(r.getDate());
        break;
      case TIME:
        w.add(r.getTime());
        break;
      case START_ARRAY:
        w.putNewArray(currentFieldName);
        addToArray(r, w);
        break;
      case END_ARRAY:
        w.endArray();
        return;
      default:
        System.out.println(e);
        break;
      }
    }
  }

}
