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
import org.immutables.value.Value;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;

/**
 * A request to parse a file.
 */

@ImmutablesStyleType
@Value.Immutable
public interface Dx7XMLParserRequestType
{
  /**
   * Specify the base directory for parsing. Parsers are not allowed to access
   * files or directories in any ancestor of this directory.
   *
   * @return The base directory against which other relative filenames will be
   * resolved
   */

  @Value.Parameter
  Optional<Path> baseDirectory();

  /**
   * @return The URI of the file, for diagnostic purposes
   */

  @Value.Parameter
  URI file();

  /**
   * @return An open input stream of the file to be parsed
   */

  @Value.Parameter
  InputStream stream();
}
