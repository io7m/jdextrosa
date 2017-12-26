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
import com.io7m.jdextrosa.io.Dx7SysExIO;
import com.io7m.jdextrosa.io.Dx7SysExReaderType;
import io.vavr.collection.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Parameters(
  commandNames = "parse",
  commandDescription = "Parse the given SysEx file(s)")
final class Dx7CommandParse extends Dx7CommandRoot
{
  private static final Logger LOG =
    LoggerFactory.getLogger(Dx7CommandParse.class);

  @Parameter(
    names = "-file",
    required = true,
    description = "The sysex file")
  private List<Path> path;

  @Parameter(
    names = "-limit",
    required = false,
    description = "The limit on the number of voices to parse")
  private int limit = Integer.MAX_VALUE;

  Dx7CommandParse()
  {

  }

  @Override
  public Status execute()
    throws Exception
  {
    if (super.execute() == Status.FAILURE) {
      return Status.FAILURE;
    }

    for (final Path file : this.path) {
      try (InputStream stream =
             new BufferedInputStream(Files.newInputStream(file), 4096)) {
        final Dx7SysExReaderType reader =
          Dx7SysExIO.createReader(e -> {
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
          }, file.toUri(), stream);

        final Vector<Dx7VoiceNamed> voices = reader.parseAtMost(this.limit);
        System.out.println(file + ": Parsed " + voices.size() + " voices");
      }
    }

    return Status.SUCCESS;
  }
}
