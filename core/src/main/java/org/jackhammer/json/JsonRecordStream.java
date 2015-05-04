/**
 * Copyright (c) 2014 MapR, Inc.
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import org.jackhammer.FieldPath;
import org.jackhammer.Record;
import org.jackhammer.RecordListener;
import org.jackhammer.RecordReader;
import org.jackhammer.RecordStream;
import org.jackhammer.Value.Type;
import org.jackhammer.exceptions.DecodingException;
import org.jackhammer.exceptions.StreamInUseException;
import org.jackhammer.json.DelegatingJsonRecordReader.EventDelegate;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;

public class JsonRecordStream implements RecordStream<Record> {

  private final InputStream inputStream;
  private JsonParser jsonParser;

  private final boolean readStarted;
  private volatile boolean iteratorOpened;

  private final Map<FieldPath, Type> fieldPathTypeMap;
  private final EventDelegate eventDelegate;

  public JsonRecordStream(InputStream in) {
    this(in, null, null);
  }

  public JsonRecordStream(InputStream in, Map<FieldPath, Type> fieldPathTypeMap) {
    this(in, fieldPathTypeMap, null);
  }

  public JsonRecordStream(InputStream in, EventDelegate eventDelegate) {
    this(in, null, eventDelegate);
  }

  JsonRecordStream(InputStream in,
      Map<FieldPath, Type> fieldPathTypeMap, EventDelegate eventDelegate) {
    inputStream = in;
    readStarted = false;
    iteratorOpened = false;
    this.eventDelegate = eventDelegate;
    this.fieldPathTypeMap = fieldPathTypeMap;
    try {
      JsonFactory jFactory = new JsonFactory();
      /* setting explicitly AUTO_CLOSE_SOURCE = false to ensure that
       * jsonParser.close() do not close the underlying inputstream.
       * It has to be closed by the owner of the stream.
       */
      jFactory.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
      jsonParser = jFactory.createParser(inputStream);
    } catch (IOException e) {
      throw new DecodingException(e);
    }
  }

  @Override
  public Iterable<RecordReader> recordReaders() {
    checkStateForIteration();
    iteratorOpened = true;
    return new JsonRecordReaderIterable(this);
  }


  @Override
  public synchronized Iterator<Record> iterator() {
    checkStateForIteration();
    return new JsonRecordIterator(this);
  }

  @Override
  public void streamTo(RecordListener l) {
    try {
      for (Record record : this) {
        l.recordArrived(record);
      }
    } catch (Exception e) {
      try {
        close();
      } catch (IOException e1) {}
      l.failed(e);
    }
  }

  @Override
  public void close() throws IOException {
    jsonParser.close();
  }

  JsonParser getParser() {
    return jsonParser;
  }

  Map<FieldPath, Type> getFieldPathTypeMap() {
    return fieldPathTypeMap;
  }

  EventDelegate getEventDelegate() {
    return eventDelegate;
  }

  private void checkStateForIteration() {
    if (readStarted) {
      throw new StreamInUseException("Can not create iterator after reading from the stream has started.");
    } else if (iteratorOpened) {
      throw new StreamInUseException("An iterator has already been opened on this record stream.");
    }
  }

}
