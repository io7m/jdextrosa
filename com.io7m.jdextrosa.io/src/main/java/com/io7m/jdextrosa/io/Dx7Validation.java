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

package com.io7m.jdextrosa.io;

import com.io7m.junreachable.UnreachableCodeException;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import io.vavr.control.Validation;

import java.util.function.Function;

/**
 * Extra methods that {@link Validation} should have but
 * doesn't.
 */

final class Dx7Validation
{
  private Dx7Validation()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Flatten the given error sequence into a single flat sequence.
   *
   * @param v   The validation value
   * @param <E> The type of errors
   * @param <T> The type of values
   *
   * @return A flattened error sequence
   */

  public static <E, T> Validation<Seq<E>, T> errorsFlatten(
    final Validation<Seq<Seq<E>>, T> v)
  {
    return v.mapError(errors -> errors.fold(Vector.empty(), Seq::appendAll));
  }

  /**
   * Safely cast a validation of elements of type {@code A} to a validation of
   * elements of type {@code B}, assuming {@code B <: A}.
   *
   * @param v   The input validation
   * @param <A> The type of input elements
   * @param <B> The type of output elements
   *
   * @return {@code v}
   */

  @SuppressWarnings("unchecked")
  public static <E, A, B extends A> Validation<Seq<E>, A> cast(
    final Validation<Seq<E>, B> v)
  {
    return (Validation<Seq<E>, A>) v;
  }

  /**
   * Execute {@code f} for each element of {@code xs}. If {@code f} returns a
   * valid value for every {@code x} in {@code xs}, the function returns a list
   * of the results. Otherwise, it returns a list of every error encountered
   * whilst processing {@code xs}.
   *
   * @param xs  The sequence of values
   * @param f   A processing function
   * @param <E> The type of errors
   * @param <A> The type of input values
   * @param <B> The type of output values
   *
   * @return A processed vector, or a sequence of errors
   */

  public static <E, A, B> Validation<Seq<E>, Vector<B>>
  sequence(
    final Iterable<A> xs,
    final Function<A, Validation<Seq<E>, B>> f)
  {
    Vector<B> results = Vector.empty();
    Vector<E> errors = Vector.empty();

    for (final A x : xs) {
      final Validation<Seq<E>, B> rr = f.apply(x);
      if (rr.isValid()) {
        results = results.append(rr.get());
      } else {
        errors = errors.appendAll(rr.getError());
      }
    }

    if (!errors.isEmpty()) {
      return Validation.invalid(errors);
    }

    return Validation.valid(results);
  }

  /**
   * Create a validation typed as a sequence of errors, based on the given
   * initial error value.
   *
   * @param error The initial error
   * @param <E>   The type of errors
   * @param <T>   The type of result values
   *
   * @return A new sequence-typed validation
   */

  public static <E, T> Validation<Seq<E>, T> invalid(
    final E error)
  {
    return Validation.invalid(Vector.of(error));
  }
}
