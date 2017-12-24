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

import org.xml.sax.ext.Locator2;

import java.io.OutputStream;
import java.net.URI;

/**
 * The type of XML format providers.
 */

public interface Dx7XMLFormatProviderType
{
  /**
   * @return The XML schema supported by this format provider
   */

  Dx7XMLSchemaDefinition schema();

  /**
   * @param in_errors  The error log
   * @param in_locator The file locator
   * @param in_request The parse request
   *
   * @return A content handler capable of parsing this format
   */

  Dx7XMLContentHandlerType createParserContentHandler(
    Dx7XMLParserRequest in_request,
    Dx7XMLErrorLog in_errors,
    Locator2 in_locator);

  Dx7XMLWriterType createWriter(
    URI file,
    OutputStream stream)
    throws Exception;
}
