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

import com.io7m.jdextrosa.core.Dx7VoiceNamed;
import com.io7m.jdextrosa.io.xml.spi.Dx7XMLContentHandlerType;
import com.io7m.jdextrosa.io.xml.spi.Dx7XMLErrorLog;
import com.io7m.jdextrosa.io.xml.spi.Dx7XMLFormatProviderType;
import com.io7m.jdextrosa.io.xml.spi.Dx7XMLParseError;
import com.io7m.jdextrosa.io.xml.spi.Dx7XMLParserRequest;
import com.io7m.jdextrosa.io.xml.spi.Dx7XMLSchemaDefinition;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import io.vavr.collection.Vector;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.ext.Locator2;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * The main XML parser provider.
 */

public final class Dx7XMLParsers
{
  private static final Logger LOG =
    LoggerFactory.getLogger(Dx7XMLParsers.class);

  private static final URI XML_NAMESPACE =
    URI.create("http://www.w3.org/XML/1998/namespace");

  private static final Dx7XMLSchemaDefinition XML_SCHEMA =
    Dx7XMLSchemaDefinition.builder()
      .setNamespace(XML_NAMESPACE)
      .setFileIdentifier("file::xml.xsd")
      .setLocation(Dx7XMLParsers.class.getResource(
        "/com/io7m/jdextrosa/io/xml/xml.xsd"))
      .build();

  private final Dx7XMLHardenedSAXParsers parsers;
  private final Vector<Dx7XMLFormatProviderType> formats;

  /**
   * Instantiate a parser provider.
   */

  public Dx7XMLParsers()
  {
    this.parsers = new Dx7XMLHardenedSAXParsers();
    this.formats =
      Stream.ofAll(ServiceLoader.load(Dx7XMLFormatProviderType.class).stream())
        .map(ServiceLoader.Provider::get)
        .toVector();
  }

  public Dx7XMLParserType create(
    final Dx7XMLParserRequest r)
    throws IOException, Dx7ParserConfigurationException
  {
    Objects.requireNonNull(r, "Request");

    try {
      final XMLReader reader =
        this.parsers.createXMLReader(r.baseDirectory(), true, this.formats);
      final HandlerInitial handler =
        new HandlerInitial(r, this.formats, reader);
      reader.setContentHandler(handler);
      return new Parser(r, reader, handler);
    } catch (final ParserConfigurationException | SAXException e) {
      throw new Dx7ParserConfigurationException(e.getMessage(), e);
    }
  }

  private static final class StopSAXParsing extends SAXException
  {
    StopSAXParsing()
    {

    }
  }

  private static final class Parser implements Dx7XMLParserType
  {
    private final XMLReader reader;
    private final Dx7XMLParserRequest request;
    private final HandlerInitial handler;

    Parser(
      final Dx7XMLParserRequest in_request,
      final XMLReader in_reader,
      final HandlerInitial in_handler)
    {
      this.request = Objects.requireNonNull(in_request, "Request");
      this.reader = Objects.requireNonNull(in_reader, "Reader");
      this.handler = Objects.requireNonNull(in_handler, "Handler");
    }

    @Override
    public Validation<Seq<Dx7XMLParseError>, Vector<Dx7VoiceNamed>> parse()
      throws IOException
    {
      try {
        final InputSource source = new InputSource(this.request.stream());
        source.setSystemId(this.request.file().toString());
        this.reader.parse(source);

        if (this.handler.errors.errors().isEmpty()) {
          return Validation.valid(
            Objects.requireNonNull(this.handler, "Handler")
              .content());
        }

      } catch (final SAXParseException e) {
        this.handler.errors.addError(
          Dx7XMLErrorLog.createErrorFromParseException(e));
      } catch (final SAXException e) {
        this.handler.errors.addError(
          Dx7XMLErrorLog.createErrorFromException(this.request.file(), e));
      }

      return Validation.invalid(this.handler.errors.errors());
    }
  }

  private static final class HandlerInitial
    extends DefaultHandler2 implements Dx7XMLContentHandlerType
  {
    private final Dx7XMLParserRequest request;
    private final XMLReader reader;
    private final Dx7XMLErrorLog errors;
    private final Vector<Dx7XMLFormatProviderType> formats;
    private Dx7XMLContentHandlerType sub_handler;
    private Locator2 locator;

    HandlerInitial(
      final Dx7XMLParserRequest in_request,
      final Vector<Dx7XMLFormatProviderType> in_formats,
      final XMLReader in_reader)
    {
      this.request = Objects.requireNonNull(in_request, "Request");
      this.formats = Objects.requireNonNull(in_formats, "Formats");
      this.reader = Objects.requireNonNull(in_reader, "Reader");
      this.reader.setErrorHandler(this);
      this.reader.setContentHandler(this);
      this.errors = new Dx7XMLErrorLog();
    }

    @Override
    public void setDocumentLocator(
      final Locator in_locator)
    {
      this.locator =
        Objects.requireNonNull((Locator2) in_locator, "Locator");
    }

    @Override
    public Dx7XMLErrorLog errorLog()
    {
      return this.errors;
    }

    @Override
    public void warning(
      final SAXParseException e)
      throws SAXException
    {
      this.errors.warning(e);
    }

    @Override
    public void error(
      final SAXParseException e)
      throws SAXException
    {
      this.errors.error(e);
    }

    @Override
    public void fatalError(
      final SAXParseException e)
      throws SAXException
    {
      this.errors.fatalError(e);
      throw e;
    }

    @Override
    public void startDocument()
      throws SAXException
    {
      LOG.debug("startDocument");
    }

    @Override
    public void endDocument()
      throws SAXException
    {
      LOG.debug("endDocument");
    }

    @Override
    public void startPrefixMapping(
      final String prefix,
      final String uri)
      throws SAXException
    {
      LOG.debug("startPrefixMapping: {} {}", prefix, uri);

      final URI uri_x = URI.create(uri);

      final Option<Dx7XMLFormatProviderType> format_opt =
        this.formats.find(f -> Objects.equals(f.schema().namespace(), uri_x));

      if (format_opt.isDefined()) {
        final Dx7XMLFormatProviderType format = format_opt.get();
        LOG.debug("instantiating format provider content handler: {}", format);
        this.sub_handler =
          format.createParserContentHandler(
            this.request,
            this.errors,
            this.locator);
        this.reader.setContentHandler(this.sub_handler);
        this.reader.setErrorHandler(this.sub_handler);
        return;
      }

      throw new SAXParseException(
        "Unrecognized schema namespace URI: " + uri,
        null,
        this.request.file().toString(), 1, 0);
    }

    @Override
    public Vector<Dx7VoiceNamed> content()
      throws SAXParseException
    {
      return this.sub_handler.content();
    }
  }
}
