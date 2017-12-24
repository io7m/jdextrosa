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
import io.vavr.collection.Vector;

/**
 * Missing functions over {@link Vector}.
 */

final class Dx7Vectors
{
  private Dx7Vectors()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Safely cast a vector of elements of type {@code A} to a vector of elements
   * of type {@code B}, assuming {@code B <: A}.
   *
   * @param v   The input vector
   * @param <A> The type of input elements
   * @param <B> The type of output elements
   *
   * @return {@code v}
   */

  @SuppressWarnings("unchecked")
  public static <A, B extends A> Vector<A> vectorCast(
    final Vector<B> v)
  {
    return (Vector<A>) v;
  }
}
