/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.lucene.document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.StoredFields; // for javadoc
import org.apache.lucene.search.ScoreDoc; // for javadoc
import org.apache.lucene.util.BytesRef;

/**
 * Documents are the unit of indexing and search.
 *
 * <p>A Document is a set of fields. Each field has a name and a textual value. A field may be
 * {@link org.apache.lucene.index.IndexableFieldType#stored() stored} with the document, in which
 * case it is returned with search hits on the document. Thus each document should typically contain
 * one or more stored fields which uniquely identify it.
 *
 * <p>Note that fields which are <i>not</i> {@link
 * org.apache.lucene.index.IndexableFieldType#stored() stored} are <i>not</i> available in documents
 * retrieved from the index, e.g. with {@link ScoreDoc#doc} or {@link StoredFields#document(int)}.
 */
public final class Document implements Iterable<IndexableField> {

  private final List<IndexableField> fields = new ArrayList<>();

  /** Constructs a new document with no fields. */
  public Document() {}

  @Override
  public Iterator<IndexableField> iterator() {
    return fields.iterator();
  }

  /**
   * Returns a stream of all fields in this document.
   * 
   * @return a Stream of IndexableField objects
   */
  public Stream<IndexableField> stream() {
    return fields.stream();
  }

  /**
   * Adds a field to a document. Several fields may be added with the same name. In this case, if
   * the fields are indexed, their text is treated as though appended for the purposes of search.
   *
   * <p>Note that add like the removeField(s) methods only makes sense prior to adding a document to
   * an index. These methods cannot be used to change the content of an existing index! In order to
   * achieve this, a document has to be deleted from an index and a new changed version of that
   * document has to be added.
   */
  public final void add(IndexableField field) {
    fields.add(field);
  }

  /**
   * Removes field with the specified name from the document. If multiple fields exist with this
   * name, this method removes the first field that has been added. If there is no field with the
   * specified name, the document remains unchanged.
   *
   * <p>Note that the removeField(s) methods like the add method only make sense prior to adding a
   * document to an index. These methods cannot be used to change the content of an existing index!
   * In order to achieve this, a document has to be deleted from an index and a new changed version
   * of that document has to be added.
   */
  public final void removeField(String name) {
    fields.removeIf(field -> field.name().equals(name));
  }

  /**
   * Removes all fields with the given name from the document. If there is no field with the
   * specified name, the document remains unchanged.
   *
   * <p>Note that the removeField(s) methods like the add method only make sense prior to adding a
   * document to an index. These methods cannot be used to change the content of an existing index!
   * In order to achieve this, a document has to be deleted from an index and a new changed version
   * of that document has to be added.
   */
  public final void removeFields(String name) {
    fields.removeIf(field -> field.name().equals(name));
  }

  /**
   * Returns an array of byte arrays for of the fields that have the name specified as the method
   * parameter. This method returns an empty array when there are no matching fields. It never
   * returns null.
   *
   * @param name the name of the field
   * @return a <code>BytesRef[]</code> of binary field values
   */
  public final BytesRef[] getBinaryValues(String name) {
    return fields.stream()
        .filter(field -> field.name().equals(name))
        .map(IndexableField::binaryValue)
        .filter(bytes -> bytes != null)
        .toArray(BytesRef[]::new);
  }

  /**
   * Returns an Optional containing the bytes for the first field that has the name specified as the
   * method parameter. This method will return an empty Optional if no binary fields with the
   * specified name are available. There may be non-binary fields with the same name.
   *
   * @param name the name of the field.
   * @return an Optional containing the binary field value
   */
  public final Optional<BytesRef> getBinaryValueOptional(String name) {
    return fields.stream()
        .filter(field -> field.name().equals(name))
        .map(IndexableField::binaryValue)
        .filter(bytes -> bytes != null)
        .findFirst();
  }

  /**
   * Returns an array of bytes for the first (or only) field that has the name specified as the
   * method parameter. This method will return <code>null</code> if no binary fields with the
   * specified name are available. There may be non-binary fields with the same name.
   *
   * @param name the name of the field.
   * @return a <code>BytesRef</code> containing the binary field value or <code>null</code>
   * @deprecated Use {@link #getBinaryValueOptional(String)} instead
   */
  @Deprecated(since = "10.0", forRemoval = true)
  public final BytesRef getBinaryValue(String name) {
    return getBinaryValueOptional(name).orElse(null);
  }

  /**
   * Returns an Optional containing a field with the given name if any exist in this document.
   * If multiple fields exist with this name, this method returns the first value added.
   * 
   * @param name the name of the field
   * @return an Optional containing the field, or empty if not found
   */
  public final Optional<IndexableField> getFieldOptional(String name) {
    return fields.stream()
        .filter(field -> field.name().equals(name))
        .findFirst();
  }

  /**
   * Returns a field with the given name if any exist in this document, or null. If multiple fields
   * exists with this name, this method returns the first value added.
   * 
   * @deprecated Use {@link #getFieldOptional(String)} instead
   */
  @Deprecated(since = "10.0", forRemoval = true)
  public final IndexableField getField(String name) {
    return getFieldOptional(name).orElse(null);
  }

  /**
   * Returns an array of {@link IndexableField}s with the given name. This method returns an empty
   * array when there are no matching fields. It never returns null.
   *
   * @param name the name of the field
   * @return a <code>Field[]</code> array
   */
  public IndexableField[] getFields(String name) {
    return fields.stream()
        .filter(field -> field.name().equals(name))
        .toArray(IndexableField[]::new);
  }

  /**
   * Returns a List of all the fields in a document.
   *
   * <p>Note that fields which are <i>not</i> stored are <i>not</i> available in documents retrieved
   * from the index, e.g. {@link StoredFields#document(int)}.
   *
   * @return an immutable <code>List&lt;Field&gt;</code>
   */
  public final List<IndexableField> getFields() {
    return Collections.unmodifiableList(fields);
  }

  private static final String[] NO_STRINGS = new String[0];

  /**
   * Returns an array of values of the field specified as the method parameter. This method returns
   * an empty array when there are no matching fields. It never returns null. For a numeric {@link
   * StoredField} it returns the string value of the number. If you want the actual numeric field
   * instances back, use {@link #getFields}.
   *
   * @param name the name of the field
   * @return a <code>String[]</code> of field values
   */
  public final String[] getValues(String name) {
    var result = fields.stream()
        .filter(field -> field.name().equals(name) && field.stringValue() != null)
        .map(IndexableField::stringValue)
        .toArray(String[]::new);
    
    return result.length == 0 ? NO_STRINGS : result;
  }

  /**
   * Returns an Optional containing the string value of the field with the given name if any exist 
   * in this document. If multiple fields exist with this name, this method returns the first value added. 
   * If only binary fields with this name exist, returns empty Optional. For a numeric {@link StoredField} 
   * it returns the string value of the number.
   * 
   * @param name the name of the field
   * @return an Optional containing the string value
   */
  public final Optional<String> getOptional(String name) {
    return fields.stream()
        .filter(field -> field.name().equals(name) && field.stringValue() != null)
        .map(IndexableField::stringValue)
        .findFirst();
  }

  /**
   * Returns the string value of the field with the given name if any exist in this document, or
   * null. If multiple fields exist with this name, this method returns the first value added. If
   * only binary fields with this name exist, returns null. For a numeric {@link StoredField} it
   * returns the string value of the number. If you want the actual numeric field instance back, use
   * {@link #getField}.
   * 
   * @deprecated Use {@link #getOptional(String)} instead
   */
  @Deprecated(since = "10.0", forRemoval = true)
  public final String get(String name) {
    return getOptional(name).orElse(null);
  }

  /** Prints the fields of a document for human consumption. */
  @Override
  public final String toString() {
    return fields.stream()
        .map(IndexableField::toString)
        .collect(java.util.stream.Collectors.joining(" ", "Document<", ">"));
  }

  /** Removes all the fields from document. */
  public void clear() {
    fields.clear();
  }

  /**
   * Returns the number of fields in this document.
   * 
   * @return the field count
   */
  public int size() {
    return fields.size();
  }

  /**
   * Returns true if this document contains no fields.
   * 
   * @return true if empty, false otherwise
   */
  public boolean isEmpty() {
    return fields.isEmpty();
  }

  /**
   * Returns true if this document contains a field with the specified name.
   * 
   * @param name the field name to check
   * @return true if the field exists, false otherwise
   */
  public boolean hasField(String name) {
    return fields.stream().anyMatch(field -> field.name().equals(name));
  }
}