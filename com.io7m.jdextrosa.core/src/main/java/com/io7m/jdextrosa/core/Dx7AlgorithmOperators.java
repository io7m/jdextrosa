/*
 * Copyright © 2017 Mark Raynsford <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.jdextrosa.core;

import com.io7m.junreachable.UnreachableCodeException;
import io.vavr.collection.SortedSet;
import io.vavr.collection.TreeSet;

import java.util.Objects;

/**
 * Functions to describe the operators of an algorithm.
 */

public final class Dx7AlgorithmOperators
{
  private Dx7AlgorithmOperators()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Return the set of operators that are carriers for the given algorithm.
   *
   * @param algorithm The algorithm
   *
   * @return The carriers
   */

  public static SortedSet<Dx7OperatorID> carriers(
    final Dx7AlgorithmID algorithm)
  {
    Objects.requireNonNull(algorithm, "Algorithm");

    switch (algorithm.id()) {
      case 1:
      case 2: {
        return TreeSet.of(
          Dx7OperatorID.of(1),
          Dx7OperatorID.of(3));
      }
      case 3:
      case 4: {
        return TreeSet.of(
          Dx7OperatorID.of(1),
          Dx7OperatorID.of(4));
      }
      case 5:
      case 6: {
        return TreeSet.of(
          Dx7OperatorID.of(1),
          Dx7OperatorID.of(3),
          Dx7OperatorID.of(5));
      }
      case 7:
      case 8:
      case 9: {
        return TreeSet.of(
          Dx7OperatorID.of(1),
          Dx7OperatorID.of(3));
      }
      case 10:
      case 11: {
        return TreeSet.of(
          Dx7OperatorID.of(1),
          Dx7OperatorID.of(4));
      }
      case 12:
      case 13:
      case 14:
      case 15: {
        return TreeSet.of(
          Dx7OperatorID.of(1),
          Dx7OperatorID.of(3));
      }
      case 16:
      case 17:
      case 18: {
        return TreeSet.of(
          Dx7OperatorID.of(1));
      }
      case 19: {
        return TreeSet.of(
          Dx7OperatorID.of(1),
          Dx7OperatorID.of(4),
          Dx7OperatorID.of(5));
      }
      case 20: {
        return TreeSet.of(
          Dx7OperatorID.of(1),
          Dx7OperatorID.of(2),
          Dx7OperatorID.of(4));
      }
      case 21: {
        return TreeSet.of(
          Dx7OperatorID.of(1),
          Dx7OperatorID.of(2),
          Dx7OperatorID.of(4),
          Dx7OperatorID.of(5));
      }
      case 22: {
        return TreeSet.of(
          Dx7OperatorID.of(1),
          Dx7OperatorID.of(3),
          Dx7OperatorID.of(4),
          Dx7OperatorID.of(5));
      }
      case 23: {
        return TreeSet.of(
          Dx7OperatorID.of(1),
          Dx7OperatorID.of(2),
          Dx7OperatorID.of(4),
          Dx7OperatorID.of(5));
      }
      case 24:
      case 25: {
        return TreeSet.of(
          Dx7OperatorID.of(1),
          Dx7OperatorID.of(2),
          Dx7OperatorID.of(3),
          Dx7OperatorID.of(4),
          Dx7OperatorID.of(5));
      }
      case 26:
      case 27: {
        return TreeSet.of(
          Dx7OperatorID.of(1),
          Dx7OperatorID.of(2),
          Dx7OperatorID.of(4));
      }
      case 28: {
        return TreeSet.of(
          Dx7OperatorID.of(1),
          Dx7OperatorID.of(3),
          Dx7OperatorID.of(6));
      }
      case 29: {
        return TreeSet.of(
          Dx7OperatorID.of(1),
          Dx7OperatorID.of(2),
          Dx7OperatorID.of(3),
          Dx7OperatorID.of(5));
      }
      case 30: {
        return TreeSet.of(
          Dx7OperatorID.of(1),
          Dx7OperatorID.of(2),
          Dx7OperatorID.of(3),
          Dx7OperatorID.of(6));
      }
      case 31: {
        return TreeSet.of(
          Dx7OperatorID.of(1),
          Dx7OperatorID.of(2),
          Dx7OperatorID.of(3),
          Dx7OperatorID.of(4),
          Dx7OperatorID.of(5));
      }
      case 32: {
        return TreeSet.of(
          Dx7OperatorID.of(1),
          Dx7OperatorID.of(2),
          Dx7OperatorID.of(3),
          Dx7OperatorID.of(4),
          Dx7OperatorID.of(5),
          Dx7OperatorID.of(6));
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  /**
   * Return the set of operators that are modulators for the given algorithm.
   *
   * @param algorithm The algorithm
   *
   * @return The carriers
   */

  public static SortedSet<Dx7OperatorID> modulators(
    final Dx7AlgorithmID algorithm)
  {
    Objects.requireNonNull(algorithm, "Algorithm");

    return TreeSet.of(
      Dx7OperatorID.of(1),
      Dx7OperatorID.of(2),
      Dx7OperatorID.of(3),
      Dx7OperatorID.of(4),
      Dx7OperatorID.of(5),
      Dx7OperatorID.of(6))
      .removeAll(carriers(algorithm));
  }
}
