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

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jackhammer.Record;
import org.jackhammer.RecordReader;
import org.jackhammer.RecordReader.EventType;
import org.jackhammer.Value.Type;
import org.jackhammer.json.JsonRecord;
import org.junit.Test;

public class TestJsonRecord {

  @Test
  public void testAllTypes() {

    JsonRecord rec = new JsonRecord();
    rec.set("map.field1", (byte) 100);
    rec.set("map.field2", (short) 10000);
    rec.set("map.longfield2verylongverylong", 12.345678);
    rec.set("FIELD2", "VERY LONG STRING IS THIS YOU KNOW");

    rec.set("map2.field1", (byte) 100);
    rec.set("map2.field2", (short) 10000);
    rec.set("map2.longfield2verylongverylong", 12.345678);
    rec.set("FIELD3", "VERY LONG STRING IS THIS YOU KNOW");
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("Name", "Anurag");
    map.put("Age", 20);
    rec.set("newmap.map", map);

    rec.set("map.boolean", false);
    rec.set("map.string", "string");
    rec.set("map.byte", (byte) 100);
    rec.set("map.short", (short) 10000);
    rec.set("map.int", 50000);
    rec.set("map.long", 12345678999L);
    rec.set("map.float", 10.1234f);
    rec.set("map.double", 10.12345678910d);
    // rec.set("map.interval", new Interval(1000, 2000));
    rec.set("map.decimal", new BigDecimal("1000000000.11111111111111111111"));
    byte[] bytes = new byte[5];
    for (int i = 0; i < bytes.length; ++i) {
      bytes[i] = (byte) i;
    }
    rec.set("map.binary1", bytes);
    rec.set("map.binary2", bytes, 1, 3);
    ByteBuffer bbuf = ByteBuffer.allocate(100);
    for (int i = 0; i < bbuf.capacity(); ++i) {
      bbuf.put((byte) i);
    }
    rec.set("map.binary3", bbuf);

    map = new HashMap<String, Object>();
    map.put("Name", "Anurag");
    map.put("Age", 20);
    List<Integer> scores = new ArrayList<Integer>();
    scores.add(100);
    scores.add(200);
    scores.add(300);
    scores.add(400);
    // map.put("Score", scores);
    rec.set("map.map", map);

    List<Object> values = new ArrayList<Object>();
    values.add("Field1");
    values.add(new Integer(500));
    values.add(new Double(5555.5555));
    rec.set("map.list", values);

    assertEquals(rec.getValue("map").getType(), Type.MAP);
    assertEquals(rec.getBoolean("map.boolean"), false);
    assertEquals(rec.getString("map.string"), "string");
    assertEquals(rec.getByte("map.byte"), (byte) 100);
    assertEquals(rec.getShort("map.short"), (short) 10000);
    assertEquals(rec.getInt("map.int"), 50000);
    assertEquals(rec.getLong("map.long"), 12345678999L);
    assertEquals(rec.getFloat("map.float"), (float) 10.1234, 0.0);
    assertEquals(rec.getDouble("map.double"), 10.12345678910d, 0.0);
    // assertEquals(rec.getInterval("map.interval"), new Interval(1000, 2000));
    assertEquals(rec.getDecimal("map.decimal"), new BigDecimal(
        "1000000000.11111111111111111111"));

    java.nio.ByteBuffer readBuf;
    readBuf = rec.getBinary("map.binary1");
    for (int i = 0; i < bytes.length; ++i) {
      assertEquals(readBuf.get(i), bytes[i]);
    }

    readBuf = rec.getBinary("map.binary2");
    for (int i = 0; i < 3; ++i) {
      assertEquals(readBuf.get(), bytes[1 + i]);
    }
    readBuf = rec.getBinary("map.binary3");
    for (int i = 0; i < bbuf.capacity(); ++i) {
      assertEquals(readBuf.get(i), bbuf.get(i));
    }

    try {
      List<Object> l = rec.getValue("map.list").getList();
      assertEquals(values, l);
    } catch (Exception e) {
      System.out.println("Exception from list test " + e.getMessage());
    }
  }

  @Test
  public void testAsReaderFull() {
    JsonRecord record = new JsonRecord();
    record.set("map.byte", (byte)127);
    record.set("map.long", 123456789);
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("first","John");
    map.put("first", "Doe");
    record.set("map.name", map);
    List<Object> mylist = new ArrayList<Object>();
    mylist.add(true);
    mylist.add("string");
    mylist.add(123.456);
    record.set("map.array", mylist);

    RecordReader myReader = record.asReader();
    EventType et ;
    String fieldName = null;
    while ((et = myReader.next()) != null) {
      if (et == EventType.FIELD_NAME) {
        fieldName = myReader.getFieldName();
      }
      if ((et == EventType.BYTE) && (fieldName.equals("byte"))) {

        assertEquals((byte)127, myReader.getByte());
      }
      if ((et == EventType.STRING) && (fieldName.equals("array"))) {

        assertEquals("string", myReader.getString());
      }
    }

  }

  @Test
  public void testAsReaderPartial() {
    JsonRecord record = new JsonRecord();
    record.set("map.byte", (byte)127);
    record.set("map.num", 12345);
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("first","John");
    map.put("last", "Doe");
    map.put("id", (long)123456789);
    record.set("map.name", map);
    List<Object> mylist = new ArrayList<Object>();
    mylist.add(true);
    mylist.add("string");
    mylist.add(123.456);
    record.set("map.array", mylist);
    RecordReader myReader = record.asReader("map.name");
    EventType event;
    String fieldName = null;
    while ((event = myReader.next()) != null) {
      if (event == EventType.FIELD_NAME) {
        fieldName = myReader.getFieldName();
      }

      if (event == EventType.LONG) {
        assertEquals("id", fieldName);
        assertEquals(123456789, myReader.getLong());
      }
    }

  }

  /*
   * Unit test for asReader created on a leaf node of DOM tree.
   */
  @Test
  public void testAsReaderLeaf() {
    JsonRecord record = new JsonRecord();
    record.set("map.byte", (byte)127);
    record.set("map.num", 12345);
    Map<String, Object> m = new HashMap<String, Object>();
    m.put("first", "John");
    m.put("last", "Doe");
    m.put("age", (short)45);
    record.set("map.info", m);
    RecordReader myReader = record.asReader("map.info.age");
    EventType event;
    int numtokens = 0;
    while ((event = myReader.next()) != null) {
      if (event == EventType.SHORT) {
        numtokens++;
        assertEquals((short)45, myReader.getShort());
      }
    }
    assertEquals(1, numtokens);
  }

  @Test
  public void testGetMap() {
    Record record = new JsonRecord();
    Map<String, Object> map = new HashMap();
    map.put("a", 1);
    map.put("b", "A");
    record.set("map", map);
    assertEquals(map, record.getMap("map"));
  }

  @Test
  public void testSetBooleanArray() {
    Record record = new JsonRecord();
    record.set("map.int", 111);
    boolean[] boolArray = new boolean[3];
    boolArray[0] = true;
    boolArray[1] = false;
    boolArray[2] = true;
    record.setArray("map.boolarray", boolArray);

    assertEquals(false, record.getBoolean("map.boolarray[1]"));
    assertEquals(true, record.getBoolean("map.boolarray[2]"));

  }

}
