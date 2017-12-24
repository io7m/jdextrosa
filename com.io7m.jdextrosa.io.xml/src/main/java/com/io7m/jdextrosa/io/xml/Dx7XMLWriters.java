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

package com.io7m.jdextrosa.io.xml;

import com.io7m.jdextrosa.io.xml.spi.Dx7XMLFormatProviderType;
import com.io7m.jdextrosa.io.xml.spi.Dx7XMLWriterRequest;
import com.io7m.jdextrosa.io.xml.spi.Dx7XMLWriterType;
import io.vavr.collection.Stream;
import io.vavr.collection.Vector;

import java.util.Objects;
import java.util.ServiceLoader;

public final class Dx7XMLWriters
{
  private final Vector<Dx7XMLFormatProviderType> formats;

  public Dx7XMLWriters()
  {
    this.formats =
      Stream.ofAll(ServiceLoader.load(Dx7XMLFormatProviderType.class).stream())
        .map(ServiceLoader.Provider::get)
        .toVector();
  }

  public Dx7XMLWriterType createWriter(
    final Dx7XMLWriterRequest request)
    throws Dx7WriterConfigurationException
  {
    Objects.requireNonNull(request, "Request");

    try {
      for (final Dx7XMLFormatProviderType format : this.formats) {
        if (Objects.equals(format.schema().namespace(), request.schema())) {
          return format.createWriter(request.file(), request.stream());
        }
      }
    } catch (final Exception e) {
      throw new Dx7WriterConfigurationException(e.getMessage(), e);
    }

    throw new Dx7WriterConfigurationException(
      "No writer available for schema: " + request.schema());
  }

  private static boolean namespaceMatches(
    final Dx7XMLWriterRequest request,
    final Dx7XMLFormatProviderType p)
  {
    return Objects.equals(p.schema().namespace(), request.schema());
  }
}
