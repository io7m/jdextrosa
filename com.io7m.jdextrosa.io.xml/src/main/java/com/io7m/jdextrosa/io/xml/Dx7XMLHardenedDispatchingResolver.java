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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

final class Dx7XMLHardenedDispatchingResolver implements EntityResolver2
{
  private static final Logger LOG =
    LoggerFactory.getLogger(Dx7XMLHardenedDispatchingResolver.class);

  private final Path base_directory;
  private final Vector<Dx7XMLFormatProviderType> schemas;

  Dx7XMLHardenedDispatchingResolver(
    final Path in_base_directory,
    final Vector<Dx7XMLFormatProviderType> in_schemas)
  {
    this.base_directory =
      Objects.requireNonNull(in_base_directory, "Base directory")
        .toAbsolutePath()
        .normalize();
    this.schemas =
      Objects.requireNonNull(in_schemas, "Schemas");
  }

  @Override
  public InputSource getExternalSubset(
    final String name,
    final String base_uri)
    throws SAXException, IOException
  {
    throw new UnsupportedOperationException(
      "External subset not supported");
  }

  @Override
  public InputSource resolveEntity(
    final String name,
    final String public_id,
    final String base_uri,
    final String system_id)
    throws SAXException, IOException
  {
    LOG.debug(
      "resolveEntity: {} {} {} {}", name, public_id, base_uri, system_id);

    final Optional<Dx7XMLFormatProviderType> format_opt =
      this.schemas
        .find(f -> Objects.equals(f.schema().fileIdentifier(), system_id))
        .toJavaOptional();

    if (format_opt.isPresent()) {
      final Dx7XMLFormatProviderType format = format_opt.get();
      LOG.debug("resolving {} from internal resources", system_id);
      return new InputSource(format.schema().location().openStream());
    }

    LOG.debug("resolving {} from filesystem", system_id);

    final Path resolved =
      this.base_directory.resolve(system_id)
        .toAbsolutePath()
        .normalize();

    if (resolved.startsWith(this.base_directory)) {
      if (Files.isRegularFile(resolved, LinkOption.NOFOLLOW_LINKS)) {
        return new InputSource(Files.newInputStream(resolved));
      }
      throw new NoSuchFileException(
        resolved.toString(),
        null,
        "File does not exist or is not a regular file");
    }

    throw new SAXException(
      new StringBuilder(128)
        .append("Refusing to allow access to files above the base directory.")
        .append(System.lineSeparator())
        .append("  Base: ")
        .append(this.base_directory)
        .append(System.lineSeparator())
        .append("  Path: ")
        .append(resolved)
        .append(System.lineSeparator())
        .toString());
  }

  @Override
  public InputSource resolveEntity(
    final String public_id,
    final String system_id)
    throws SAXException, IOException
  {
    throw new UnsupportedOperationException(
      "Simple entity resolution not supported");
  }
}
