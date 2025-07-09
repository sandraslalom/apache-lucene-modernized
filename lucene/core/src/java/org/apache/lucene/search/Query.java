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
package org.apache.lucene.search;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

/**
 * The abstract base class for queries.
 *
 * <p>This class is sealed to ensure type safety and enable pattern matching.
 * Permitted subclasses include:
 *
 * <ul>
 *   <li>{@link TermQuery}
 *   <li>{@link BooleanQuery}
 *   <li>{@link WildcardQuery}
 *   <li>{@link PhraseQuery}
 *   <li>{@link PrefixQuery}
 *   <li>{@link MultiPhraseQuery}
 *   <li>{@link FuzzyQuery}
 *   <li>{@link RegexpQuery}
 *   <li>{@link TermRangeQuery}
 *   <li>{@link PointRangeQuery}
 *   <li>{@link ConstantScoreQuery}
 *   <li>{@link DisjunctionMaxQuery}
 *   <li>{@link MatchAllDocsQuery}
 * </ul>
 *
 * <p>See also additional queries available in the <a
 * href="{@docRoot}/../queries/overview-summary.html">Queries module</a>.
 */
public abstract sealed class Query 
    permits TermQuery, BooleanQuery, WildcardQuery, PhraseQuery, PrefixQuery, 
            MultiPhraseQuery, FuzzyQuery, RegexpQuery, TermRangeQuery, PointRangeQuery,
            ConstantScoreQuery, DisjunctionMaxQuery, MatchAllDocsQuery {

  /**
   * Prints a query to a string, with <code>field</code> assumed to be the default field and
   * omitted.
   */
  public abstract String toString(String field);

  /** Prints a query to a string. */
  @Override
  public final String toString() {
    return toString("");
  }

  /**
   * Expert: Constructs an appropriate Weight implementation for this query.
   *
   * <p>Only implemented by primitive queries, which re-write to themselves.
   *
   * @param scoreMode How the produced scorers will be consumed.
   * @param boost The boost that is propagated by the parent queries.
   */
  public Weight createWeight(IndexSearcher searcher, ScoreMode scoreMode, float boost)
      throws IOException {
    throw new UnsupportedOperationException("Query " + this + " does not implement createWeight");
  }

  /**
   * Expert: called to re-write queries into primitive queries. For example, a PrefixQuery will be
   * rewritten into a BooleanQuery that consists of TermQuerys.
   *
   * <p>Callers are expected to call <code>rewrite</code> multiple times if necessary, until the
   * rewritten query is the same as the original query.
   *
   * <p>The rewrite process may be able to make use of IndexSearcher's executor and be executed in
   * parallel if the executor is provided.
   *
   * @see IndexSearcher#rewrite(Query)
   */
  public Query rewrite(IndexSearcher indexSearcher) throws IOException {
    return this;
  }

  /**
   * Attempts to rewrite this query and returns an Optional containing the rewritten query,
   * or empty if no rewriting was performed.
   *
   * @param indexSearcher the index searcher to use for rewriting
   * @return Optional containing the rewritten query, or empty if no rewriting occurred
   * @throws IOException if an I/O error occurs during rewriting
   */
  public Optional<Query> tryRewrite(IndexSearcher indexSearcher) throws IOException {
    Query rewritten = rewrite(indexSearcher);
    return this.equals(rewritten) ? Optional.empty() : Optional.of(rewritten);
  }

  /**
   * Recurse through the query tree, visiting any child queries.
   *
   * @param visitor a QueryVisitor to be called by each query in the tree
   */
  public abstract void visit(QueryVisitor visitor);

  /**
   * Override and implement query instance equivalence properly in a subclass. This is required so
   * that {@link QueryCache} works properly.
   *
   * <p>Typically a query will be equal to another only if it's an instance of the same class and
   * its document-filtering properties are identical to those of the other instance. Utility methods
   * are provided for certain repetitive code.
   *
   * @see #sameClassAs(Object)
   * @see #classHash()
   */
  @Override
  public abstract boolean equals(Object obj);

  /**
   * Override and implement query hash code properly in a subclass. This is required so that {@link
   * QueryCache} works properly.
   *
   * @see #equals(Object)
   */
  @Override
  public abstract int hashCode();

  /**
   * Utility method to check whether <code>other</code> is not null and is exactly of the same class
   * as this object's class.
   *
   * <p>When this method is used in an implementation of {@link #equals(Object)}, consider using
   * {@link #classHash()} in the implementation of {@link #hashCode} to differentiate different
   * class.
   */
  protected final boolean sameClassAs(Object other) {
    return other != null && getClass() == other.getClass();
  }

  private final int CLASS_NAME_HASH = getClass().getName().hashCode();

  /**
   * Provides a constant integer for a given class, derived from the name of the class. The
   * rationale for not using just {@link Class#hashCode()} is that classes may be assigned different
   * hash codes for each execution and we want hashes to be possibly consistent to facilitate
   * debugging.
   */
  protected final int classHash() {
    return CLASS_NAME_HASH;
  }

  /**
   * Pattern matching helper for query type checking and casting.
   * 
   * @param queryType the expected query type
   * @return Optional containing the query cast to the specified type, or empty if not a match
   */
  @SuppressWarnings("unchecked")
  public final <T extends Query> Optional<T> as(Class<T> queryType) {
    return queryType.isInstance(this) ? Optional.of((T) this) : Optional.empty();
  }

  /**
   * Checks if this query is of the specified type.
   * 
   * @param queryType the query type to check
   * @return true if this query is of the specified type, false otherwise
   */
  public final boolean isInstanceOf(Class<? extends Query> queryType) {
    return queryType.isInstance(this);
  }

  /**
   * Returns the simple class name of this query for debugging purposes.
   * 
   * @return the simple class name
   */
  public final String getQueryType() {
    return getClass().getSimpleName();
  }

  /**
   * Creates a ConstantScoreQuery wrapping this query with the specified boost.
   * 
   * @param boost the boost value
   * @return a new ConstantScoreQuery
   */
  public final Query boost(float boost) {
    if (boost == 1.0f) {
      return this;
    }
    return new BoostQuery(this, boost);
  }

  /**
   * Validates that the query is in a consistent state.
   * 
   * @throws IllegalStateException if the query is in an invalid state
   */
  protected void validate() {
    // Default implementation does nothing
    // Subclasses can override to add validation logic
  }

  /**
   * Returns a detailed description of this query for debugging purposes.
   * 
   * @return a detailed string representation
   */
  public String toDetailedString() {
    return """
        Query Type: %s
        String Representation: %s
        Hash Code: %d
        Class Hash: %d
        """.formatted(getQueryType(), toString(), hashCode(), classHash());
  }
}