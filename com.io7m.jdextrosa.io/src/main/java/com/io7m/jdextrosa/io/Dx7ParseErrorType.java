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

import com.io7m.jdextrosa.core.DxImmutableStyleType;
import org.immutables.value.Value;

import java.net.URI;
import java.util.Optional;

/**
 * The type of parse errors.
 */

@DxImmutableStyleType
@Value.Immutable
public interface Dx7ParseErrorType
{
  /**
   * @return The file being parsed
   */

  @Value.Parameter
  URI file();

  /**
   * @return The byte offset of the error within the file
   */

  @Value.Parameter
  long offset();

  /**
   * @return The severity of the error
   */

  @Value.Parameter
  Severity severity();

  /**
   * @return The error message
   */

  @Value.Parameter
  String message();

  /**
   * @return The exception raised, if any
   */

  @Value.Parameter
  Optional<Exception> exception();

  /**
   * @return A human-readable form of the error
   */

  default String show()
  {
    final StringBuilder sb = new StringBuilder(128);
    sb.append(this.file());
    sb.append("[0x");
    sb.append(Long.toUnsignedString(this.offset(), 16));
    sb.append("]: ");
    sb.append(this.message());
    return sb.toString();
  }

  /**
   * The severity of the error.
   */

  enum Severity
  {
    /**
     * A warning.
     */

    WARNING,

    /**
     * An error.
     */

    ERROR
  }
}
