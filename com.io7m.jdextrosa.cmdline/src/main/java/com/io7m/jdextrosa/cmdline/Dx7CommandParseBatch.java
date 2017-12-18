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
import com.io7m.jdextrosa.io.Dx7ParseError;
import com.io7m.jdextrosa.io.Dx7SysExIO;
import com.io7m.jdextrosa.io.Dx7SysExReaderType;
import com.io7m.mutable.numbers.core.MutableInteger;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import io.vavr.control.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Parameters(
  commandNames = "parse-batch",
  commandDescription = "Parse each SysEx file specified in the given batch file")
final class Dx7CommandParseBatch extends Dx7CommandRoot
{
  private static final Logger LOG =
    LoggerFactory.getLogger(Dx7CommandParseBatch.class);

  @Parameter(
    names = "-file",
    required = true,
    description = "The batch file")
  private Path path;

  Dx7CommandParseBatch()
  {

  }

  @Override
  public Status execute()
    throws Exception
  {
    if (super.execute() == Status.FAILURE) {
      return Status.FAILURE;
    }

    final List<Path> files =
      Files.lines(this.path)
        .map(Paths::get)
        .collect(Collectors.toList());

    final MutableInteger warnings = MutableInteger.create();
    final MutableInteger errors = MutableInteger.create();
    final MutableInteger patches = MutableInteger.create();

    for (final Path patch_file : files) {
      try (InputStream stream = Files.newInputStream(patch_file)) {
        LOG.info("parse: {}", patch_file);

        final Dx7SysExReaderType reader =
          Dx7SysExIO.createReader(e -> {
            switch (e.severity()) {
              case WARNING: {
                LOG.warn("{}", e.show());
                warnings.setValue(warnings.value() + 1);
                break;
              }
              case ERROR: {
                LOG.error("{}", e.show());
                errors.setValue(errors.value() + 1);
                break;
              }
            }
          }, patch_file.toUri(), stream);

        final Vector<Dx7VoiceNamed> voices = reader.parse();
        System.out.println(patch_file + ": Parsed " + voices.size() + " voices");
        patches.setValue(patches.value() + voices.size());
      } catch (final IOException e) {
        LOG.error("Could not open {}: ", patch_file, e);
      }
    }

    System.out.println("Parsed " + patches.value() + " patches");
    System.out.println("Errors: " + errors.value());
    System.out.println("Warnings: " + warnings.value());
    return Status.SUCCESS;
  }
}
