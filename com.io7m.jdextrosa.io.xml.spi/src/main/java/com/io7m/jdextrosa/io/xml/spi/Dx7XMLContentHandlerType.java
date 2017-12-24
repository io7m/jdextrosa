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

import com.io7m.jdextrosa.core.Dx7VoiceNamed;
import io.vavr.collection.Vector;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * The type of content handlers that yield AST content.
 */

public interface Dx7XMLContentHandlerType extends ContentHandler, ErrorHandler
{
  /**
   * @return The error log used by the error handler
   */

  Dx7XMLErrorLog errorLog();

  @Override
  default void warning(
    final SAXParseException e)
    throws SAXException
  {
    this.errorLog().warning(e);
  }

  @Override
  default void error(
    final SAXParseException e)
    throws SAXException
  {
    this.errorLog().error(e);
  }

  @Override
  default void fatalError(
    final SAXParseException e)
    throws SAXException
  {
    this.errorLog().fatalError(e);
    throw e;
  }

  /**
   * @return The completed AST content
   *
   * @throws SAXParseException If any errors occurred
   */

  Vector<Dx7VoiceNamed> content()
    throws SAXParseException;


}
