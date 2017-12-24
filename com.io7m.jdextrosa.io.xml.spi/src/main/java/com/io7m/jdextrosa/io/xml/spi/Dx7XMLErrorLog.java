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

import com.io7m.jdextrosa.io.xml.spi.Dx7XMLParseError;
import com.io7m.jdextrosa.io.xml.spi.Dx7XMLParseErrorType;
import com.io7m.jlexing.core.LexicalPosition;
import io.vavr.collection.Vector;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.net.URI;
import java.util.Objects;

/**
 * An error log.
 */

public final class Dx7XMLErrorLog
{
  private Vector<Dx7XMLParseError> errors;

  /**
   * Construct an empty log.
   */

  public Dx7XMLErrorLog()
  {
    this.errors = Vector.empty();
  }

  private void logError(
    final Dx7XMLParseError e)
  {
    this.errors = this.errors.append(
      Objects.requireNonNull(e, "Error"));
  }

  /**
   * Add the given parse error.
   *
   * @param e The parse error
   */

  public void addError(
    final Dx7XMLParseError e)
  {
    this.logError(Objects.requireNonNull(e, "Error"));
  }

  /**
   * @return The current (immutable) vector of errors
   */

  public Vector<Dx7XMLParseError> errors()
  {
    return this.errors;
  }

  /**
   * Add a warning based on the given exception
   *
   * @param e The exception
   */

  public void warning(
    final SAXParseException e)
  {
    this.logError(
      Dx7XMLParseError.builder()
        .setException(e)
        .setLexical(
          LexicalPosition.<URI>builder()
            .setFile(URI.create(e.getSystemId()))
            .setColumn(e.getColumnNumber())
            .setLine(e.getLineNumber())
            .build())
        .setSeverity(Dx7XMLParseErrorType.Severity.WARNING)
        .setMessage(e.getMessage())
        .build());
  }

  /**
   * Create a parse error from the given parse exception.
   *
   * @param e The exception
   *
   * @return A parse error
   */

  public static Dx7XMLParseError createErrorFromParseException(
    final SAXParseException e)
  {
    Objects.requireNonNull(e, "Exception");

    return Dx7XMLParseError.builder()
      .setException(e)
      .setLexical(
        LexicalPosition.<URI>builder()
          .setFile(URI.create(e.getSystemId()))
          .setColumn(e.getColumnNumber())
          .setLine(e.getLineNumber())
          .build())
      .setSeverity(Dx7XMLParseErrorType.Severity.ERROR)
      .setMessage(e.getMessage())
      .build();
  }

  /**
   * Create a parse error from the given exception.
   *
   * @param uri The URI of the source file
   * @param e   The exception
   *
   * @return A parse error
   */

  public static Dx7XMLParseError createErrorFromException(
    final URI uri,
    final SAXException e)
  {
    Objects.requireNonNull(uri, "URI");
    Objects.requireNonNull(e, "Exception");

    return Dx7XMLParseError.builder()
      .setException(e)
      .setLexical(
        LexicalPosition.<URI>builder()
          .setFile(uri)
          .setColumn(-1)
          .setLine(-1)
          .build())
      .setSeverity(Dx7XMLParseErrorType.Severity.ERROR)
      .setMessage(e.getMessage())
      .build();
  }

  /**
   * Add an error based on the given exception
   *
   * @param e The exception
   */

  public void error(
    final SAXParseException e)
  {
    this.errors = this.errors.append(createErrorFromParseException(e));
  }

  /**
   * Add a fatal error based on the given exception
   *
   * @param e The exception
   */

  public void fatalError(
    final SAXParseException e)
  {
    this.errors = this.errors.append(createErrorFromParseException(e));
  }
}
