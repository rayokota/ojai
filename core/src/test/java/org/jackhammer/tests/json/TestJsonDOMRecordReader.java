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
package org.jackhammer.tests.json;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jackhammer.Record;
import org.jackhammer.RecordReader;
import org.jackhammer.RecordReader.EventType;
import org.jackhammer.json.Json;
import org.jackhammer.tests.BaseTest;
import org.junit.Before;
import org.junit.Test;

public class TestJsonDOMRecordReader extends BaseTest {

  Record record = Json.newRecord();

  @Before
  public void setUp() {
    record.set("map.num1", (byte)127);
    record.set("map.name.first", "John");
    record.set("map.name.last", "Doe");
    record.set("map.address.street.no", 350);
    record.set("map.address.street.name", "Front St");
    record.set("map.address.zip", (long)95134);
    List<Object> values = new ArrayList<Object>();
    values.add("Field1");
    values.add(new Short((short)500));
    values.add(new Double(5555.5555));
    Map<String, Object> m = new HashMap<String, Object>();
    m.put("key1", 100);
    m.put("key2", "xyz");
    values.add(m);
    List<Object> l = new ArrayList<Object>();
    l.add(123.4567);
    l.add(true);
    values.add(l);
    record.set("map.list", values);
  }

  @Test
  public void testDOMRecordReader() throws IOException {
    boolean isArray = false;

    RecordReader r = record.asReader();
    EventType et;
    String fieldName = null;
    while ((et = r.next()) != null) {
      if (et == EventType.FIELD_NAME) {
        fieldName = r.getFieldName();
      }
      if (isArray && et == EventType.SHORT) {
        assertEquals("list", fieldName);
        assertEquals((short)500, r.getShort());
      }
      if (et == EventType.BYTE) {
        assertEquals("num1", fieldName);
        assertEquals((byte)127, r.getByte());
      }
      if ((et == EventType.INT) && (!isArray)) {
        assertEquals("no", fieldName);
        assertEquals(350, r.getInt());
      }

      if ((et == EventType.LONG) && (!isArray)) {
        assertEquals("zip", fieldName);
        assertEquals(95134, r.getLong());
      }

      if (et == EventType.START_ARRAY) {
        isArray = true;
      }

      if (et == EventType.END_ARRAY) {
        isArray = false;
      }
    }

  }

  @Test
  public void testDOMReader() {
    RecordReader reader = record.asReader("map.list");
    EventType et;

    while ((et = reader.next()) != null) {
      if (et == EventType.BOOLEAN) {
        assertEquals(true, reader.getBoolean());
      }
    }
  }

  @Test
  public void testScalarTypeReader() {
    RecordReader reader = record.asReader("map.num1");
    EventType et;
    while ((et = reader.next()) != null) {
      assertEquals(EventType.BYTE, et);
      assertEquals((byte)127, reader.getByte());
    }
  }

}
