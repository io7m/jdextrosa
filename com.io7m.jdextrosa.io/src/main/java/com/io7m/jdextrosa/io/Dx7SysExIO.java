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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Objects;

/**
 * Functions that provider readers and writers for binary SysEx messages.
 */

public final class Dx7SysExIO
{
  private Dx7SysExIO()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Create a reader for the given stream.
   *
   * @param errors An error receiver
   * @param uri    The URI of the stream for diagnostic purposes
   * @param stream An input stream
   *
   * @return A reader
   */

  public static Dx7SysExReaderType createReader(
    final Dx7ParseErrorListenerType errors,
    final URI uri,
    final InputStream stream)
  {
    Objects.requireNonNull(errors, "Errors");
    Objects.requireNonNull(uri, "URI");
    Objects.requireNonNull(stream, "Stream");
    return new Dx7Reader(errors, uri, new Dx7InputStream(stream));
  }

  /**
   * Create a writer for the given stream.
   *
   * @param uri    The URI of the stream for diagnostic purposes
   * @param stream An output stream
   *
   * @return A writer
   */

  public static Dx7SysExWriterType createWriter(
    final URI uri,
    final OutputStream stream)
  {
    Objects.requireNonNull(uri, "URI");
    Objects.requireNonNull(stream, "Stream");
    return new Dx7Writer(uri, stream);
  }
}
