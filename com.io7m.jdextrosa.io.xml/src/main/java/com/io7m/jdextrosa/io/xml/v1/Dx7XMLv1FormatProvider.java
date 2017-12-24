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

package com.io7m.jdextrosa.io.xml.v1;

import com.io7m.jdextrosa.io.xml.spi.Dx7XMLContentHandlerType;
import com.io7m.jdextrosa.io.xml.spi.Dx7XMLErrorLog;
import com.io7m.jdextrosa.io.xml.spi.Dx7XMLFormatProviderType;
import com.io7m.jdextrosa.io.xml.spi.Dx7XMLParserRequest;
import com.io7m.jdextrosa.io.xml.spi.Dx7XMLSchemaDefinition;
import com.io7m.jdextrosa.io.xml.spi.Dx7XMLWriterType;
import net.sf.saxon.Configuration;
import org.xml.sax.ext.Locator2;

import java.io.OutputStream;
import java.net.URI;

/**
 * A 1.0 format provider.
 */

public final class Dx7XMLv1FormatProvider implements Dx7XMLFormatProviderType
{
  static final String SCHEMA_NAMESPACE = "schema:com.io7m.jdextrosa:1.0";

  static final URI SCHEMA_NAMESPACE_URI = URI.create(SCHEMA_NAMESPACE);

  private static final Dx7XMLSchemaDefinition SCHEMA =
    Dx7XMLSchemaDefinition.builder()
      .setFileIdentifier("file::jdextrosa-1.0.xsd")
      .setNamespace(SCHEMA_NAMESPACE_URI)
      .setLocation(Dx7XMLv1FormatProvider.class.getResource(
        "/com/io7m/jdextrosa/io/xml/jdextrosa-1.0.xsd"))
      .build();

  private final Configuration config;

  /**
   * Instantiate a format provider.
   */

  public Dx7XMLv1FormatProvider()
  {
    this.config = new Configuration();
  }

  @Override
  public String toString()
  {
    final StringBuilder sb = new StringBuilder(128);
    sb.append("[");
    sb.append(Dx7XMLv1FormatProvider.class.getCanonicalName());
    sb.append("]");
    return sb.toString();
  }

  @Override
  public Dx7XMLSchemaDefinition schema()
  {
    return SCHEMA;
  }

  @Override
  public Dx7XMLContentHandlerType createParserContentHandler(
    final Dx7XMLParserRequest in_request,
    final Dx7XMLErrorLog in_errors,
    final Locator2 in_locator)
  {
    return new Dx7v1ParserHandler(in_request, in_errors, in_locator);
  }

  @Override
  public Dx7XMLWriterType createWriter(
    final URI file,
    final OutputStream stream)
    throws Exception
  {
    return new Dx7v1Writer(this.config, file, stream);
  }
}
