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

package com.io7m.jdextrosa.io.xml.spi;

import com.io7m.immutables.styles.ImmutablesStyleType;
import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.jlexing.core.LexicalType;
import org.immutables.value.Value;

import java.net.URI;
import java.util.Optional;

/**
 * The type of parse errors.
 */

@ImmutablesStyleType
@Value.Immutable
public interface Dx7XMLParseErrorType extends LexicalType<URI>
{
  @Override
  @Value.Parameter
  LexicalPosition<URI> lexical();

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
   * @return A humanly-readable form of the error
   */

  default String show()
  {
    final StringBuilder sb = new StringBuilder(128);
    this.lexical().file().ifPresent(uri -> {
      sb.append(uri);
      sb.append(":");
    });
    sb.append(this.lexical().line());
    sb.append(":");
    sb.append(this.lexical().column());
    sb.append(": ");
    sb.append(this.message());
    this.exception().ifPresent(ex -> {
      sb.append(" (");
      sb.append(ex.getClass().getCanonicalName());
      sb.append(")");
    });
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
