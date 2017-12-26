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

package com.io7m.jdextrosa.cmdline;

import com.beust.jcommander.converters.BaseConverter;

import java.util.Objects;

/**
 * The supported DX7 voice formats.
 */

public enum Dx7Format
{
  /**
   * An XML format.
   */

  DX7_FORMAT_XML("xml"),

  /**
   * A GZ compressed XML format.
   */

  DX7_FORMAT_XML_GZ("xml.gz"),

  /**
   * A 32-voice binary SysEx format.
   */

  DX7_FORMAT_BINARY_SYSEX_32_VOICE("sysex-32");

  private final String type;

  /**
   * Conver this value to a string.
   */

  @Override
  public String toString()
  {
    return this.type;
  }

  Dx7Format(
    final String in_type)
  {
    this.type = Objects.requireNonNull(in_type, "Type");
  }

  /**
   * A format name converter.
   */

  public static final class Converter extends BaseConverter<Dx7Format>
  {
    /**
     * Construct a converter.
     *
     * @param name The parameter name
     */

    public Converter(
      final String name)
    {
      super(name);
    }

    @Override
    public Dx7Format convert(
      final String name)
    {
      switch (name) {
        case "xml.gz":
          return DX7_FORMAT_XML_GZ;
        case "xml":
          return DX7_FORMAT_XML;
        case "sysex-32":
          return DX7_FORMAT_BINARY_SYSEX_32_VOICE;
        default: {
          throw new IllegalArgumentException(
            "Unrecognized format name: " + name);
        }
      }
    }
  }
}
