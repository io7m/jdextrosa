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

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.io7m.jdextrosa.core.Dx7VoiceNamed;
import com.io7m.jdextrosa.io.xml.Dx7XMLParserType;
import com.io7m.jdextrosa.io.xml.Dx7XMLParsers;
import com.io7m.jdextrosa.io.xml.spi.Dx7XMLParseError;
import com.io7m.jdextrosa.io.xml.spi.Dx7XMLParserRequest;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import io.vavr.control.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Parameters(
  commandNames = "parse",
  commandDescription = "Parse/validate the given XML voices file(s)")
final class Dx7CommandParseXML extends Dx7CommandRoot
{
  private static final Logger LOG =
    LoggerFactory.getLogger(Dx7CommandParseXML.class);

  @Parameter(
    names = "-file",
    required = true,
    description = "The XML voices file")
  private Path path;

  Dx7CommandParseXML()
  {

  }

  @Override
  public Status execute()
    throws Exception
  {
    if (super.execute() == Status.FAILURE) {
      return Status.FAILURE;
    }

    final Dx7XMLParsers parsers = new Dx7XMLParsers();

    try (InputStream stream = Files.newInputStream(this.path)) {
      final Dx7XMLParserType parser =
        parsers.create(Dx7XMLParserRequest.builder()
                         .setBaseDirectory(this.path.getParent())
                         .setFile(this.path.toUri())
                         .setStream(stream)
                         .build());

      final Validation<Seq<Dx7XMLParseError>, Vector<Dx7VoiceNamed>> result =
        parser.parse();

      if (result.isValid()) {
        final Vector<Dx7VoiceNamed> voices = result.get();
        System.out.println(this.path + ": Parsed " + voices.size() + " voices");
      } else {
        result.getError().forEach(e -> {
          switch (e.severity()) {
            case WARNING: {
              LOG.warn("{}", e.show());
              break;
            }
            case ERROR: {
              LOG.error("{}", e.show());
              break;
            }
          }
        });
      }
    }

    return Status.SUCCESS;
  }
}
