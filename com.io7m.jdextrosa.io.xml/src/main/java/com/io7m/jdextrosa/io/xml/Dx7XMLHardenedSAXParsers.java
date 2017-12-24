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
import io.vavr.collection.Vector;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.nio.file.Path;
import java.util.Objects;

final class Dx7XMLHardenedSAXParsers
{
  private final SAXParserFactory parsers;

  Dx7XMLHardenedSAXParsers()
  {
    this.parsers = SAXParserFactory.newInstance();
  }

  XMLReader createXMLReaderNonValidating(
    final boolean xinclude,
    final Path base_directory)
    throws ParserConfigurationException, SAXException
  {
    Objects.requireNonNull(base_directory, "Base directory");

    final SAXParser parser = this.parsers.newSAXParser();
    final XMLReader reader = parser.getXMLReader();

    /*
     * Turn on "secure processing". Sets various resource limits to prevent
     * various denial of service attacks.
     */

    reader.setFeature(
      XMLConstants.FEATURE_SECURE_PROCESSING,
      true);

    /*
     * Don't allow access to schemas or DTD files.
     */

    reader.setProperty(
      XMLConstants.ACCESS_EXTERNAL_SCHEMA,
      "");
    reader.setProperty(
      XMLConstants.ACCESS_EXTERNAL_DTD,
      "");

    /*
     * Don't load DTDs at all.
     */

    reader.setFeature(
      "http://apache.org/xml/features/nonvalidating/load-external-dtd",
      false);

    /*
     * Enable XInclude.
     */

    reader.setFeature(
      "http://apache.org/xml/features/xinclude",
      xinclude);

    /*
     * Ensure namespace processing is enabled.
     */

    reader.setFeature(
      "http://xml.org/sax/features/namespaces",
      true);

    /*
     * Disable validation.
     */

    reader.setFeature(
      "http://xml.org/sax/features/validation",
      false);
    reader.setFeature(
      "http://apache.org/xml/features/validation/schema",
      false);

     /*
      * Tell the parser to use the full EntityResolver2 interface (by default,
      * the extra EntityResolver2 methods will not be called - only those of
      * the original EntityResolver interface would be called).
      */

    reader.setFeature(
      "http://xml.org/sax/features/use-entity-resolver2",
      true);

    reader.setEntityResolver(
      new Dx7XMLHardenedDispatchingResolver(base_directory, Vector.empty()));
    return reader;
  }

  XMLReader createXMLReader(
    final Path base_directory,
    final boolean xinclude,
    final Vector<Dx7XMLFormatProviderType> in_schemas)
    throws ParserConfigurationException, SAXException
  {
    Objects.requireNonNull(base_directory, "Base directory");
    Objects.requireNonNull(in_schemas, "Schemas");

    final SAXParser parser = this.parsers.newSAXParser();
    final XMLReader reader = parser.getXMLReader();

    /*
     * Turn on "secure processing". Sets various resource limits to prevent
     * various denial of service attacks.
     */

    reader.setFeature(
      XMLConstants.FEATURE_SECURE_PROCESSING,
      true);

    /*
     * Only allow access to schema files using a "file" URL scheme. This is
     * restricted even further via a custom entity resolver.
     */

    reader.setProperty(
      XMLConstants.ACCESS_EXTERNAL_SCHEMA,
      "file");

    /*
     * Deny access to external DTD files.
     */

    reader.setProperty(
      XMLConstants.ACCESS_EXTERNAL_DTD,
      "");

    /*
     * Don't load DTDs at all.
     */

    reader.setFeature(
      "http://apache.org/xml/features/nonvalidating/load-external-dtd",
      false);

    /*
     * Enable XInclude.
     */

    reader.setFeature(
      "http://apache.org/xml/features/xinclude",
      xinclude);

    /*
     * Ensure namespace processing is enabled.
     */

    reader.setFeature(
      "http://xml.org/sax/features/namespaces",
      true);

    /*
     * Enable validation and, more to the point, enable XSD schema validation.
     */

    reader.setFeature(
      "http://xml.org/sax/features/validation",
      true);
    reader.setFeature(
      "http://apache.org/xml/features/validation/schema",
      true);

    /*
     * Create a space separated list of mappings from namespace URIs to
     * schema system IDs. This will indicate to the parser that when it encounters
     * a given namespace, it should ask the _entity resolver_ to resolve the
     * corresponding system ID specified here.
     */

    final StringBuilder locations = new StringBuilder(128);
    in_schemas.forEach(format -> {
      locations.append(format.schema().namespace());
      locations.append(" ");
      locations.append(format.schema().fileIdentifier());
      locations.append(" ");
    });

    reader.setProperty(
      "http://apache.org/xml/properties/schema/external-schemaLocation",
      locations.toString());

     /*
      * Tell the parser to use the full EntityResolver2 interface (by default,
      * the extra EntityResolver2 methods will not be called - only those of
      * the original EntityResolver interface would be called).
      */

    reader.setFeature(
      "http://xml.org/sax/features/use-entity-resolver2",
      true);

    reader.setEntityResolver(
      new Dx7XMLHardenedDispatchingResolver(base_directory, in_schemas));
    return reader;
  }
}
