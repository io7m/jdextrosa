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
import com.io7m.jdextrosa.core.Dx7Operator;
import com.io7m.jdextrosa.core.Dx7OperatorID;
import com.io7m.jdextrosa.core.Dx7Voice;
import com.io7m.jdextrosa.core.Dx7VoiceMetadata;
import com.io7m.jdextrosa.core.Dx7VoiceNamed;
import com.io7m.jdextrosa.io.Dx7ParseErrorListenerType;
import com.io7m.jdextrosa.io.Dx7SysExIO;
import com.io7m.jdextrosa.io.Dx7SysExReaderType;
import com.io7m.jdextrosa.io.Dx7SysExWriterType;
import com.io7m.jdextrosa.io.xml.Dx7ParserConfigurationException;
import com.io7m.jdextrosa.io.xml.Dx7WriterConfigurationException;
import com.io7m.jdextrosa.io.xml.Dx7XMLParserType;
import com.io7m.jdextrosa.io.xml.Dx7XMLParsers;
import com.io7m.jdextrosa.io.xml.Dx7XMLWriters;
import com.io7m.jdextrosa.io.xml.spi.Dx7XMLParseError;
import com.io7m.jdextrosa.io.xml.spi.Dx7XMLParserRequest;
import com.io7m.jdextrosa.io.xml.spi.Dx7XMLWriterRequest;
import com.io7m.jdextrosa.io.xml.spi.Dx7XMLWriterType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.mutable.numbers.core.MutableInteger;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import io.vavr.control.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Parameters(
  commandNames = "parse-batch",
  commandDescription = "Parse each SysEx file specified in the given batch file")
final class Dx7CommandConvertBatch extends Dx7CommandRoot
{
  private static final Logger LOG =
    LoggerFactory.getLogger(Dx7CommandConvertBatch.class);

  private final Dx7XMLParsers xml_parsers;
  private final Dx7XMLWriters xml_writers;

  @Parameter(
    names = "-file-batch",
    required = true,
    description = "The batch file")
  private Path path;

  @Parameter(
    names = "-format-output",
    required = false,
    description = "The output file format",
    converter = Dx7Format.Converter.class)
  private Dx7Format format_output;

  @Parameter(
    names = "-file-output",
    required = false,
    description = "The output file")
  private Path file_output;

  @Parameter(
    names = "-pick-random-32",
    required = false,
    description = "Make a random selection of 32 non-default voices")
  private boolean pick_random_32;
  public static final Dx7Voice DEFAULT_VOICE = Dx7Voice.builder()
    .setOperator1(Dx7Operator.of(Dx7OperatorID.of(1)))
    .setOperator2(Dx7Operator.of(Dx7OperatorID.of(2)))
    .setOperator3(Dx7Operator.of(Dx7OperatorID.of(3)))
    .setOperator4(Dx7Operator.of(Dx7OperatorID.of(4)))
    .setOperator5(Dx7Operator.of(Dx7OperatorID.of(5)))
    .setOperator6(Dx7Operator.of(Dx7OperatorID.of(6)))
    .build();

  Dx7CommandConvertBatch()
  {
    this.xml_parsers = new Dx7XMLParsers();
    this.xml_writers = new Dx7XMLWriters();
  }

  private static Dx7Format inferFileFormat(
    final Path path,
    final Dx7Format format)
  {
    if (format != null) {
      return format;
    }

    final String path_upper = path.toString().toUpperCase();
    if (path_upper.endsWith(".SYSX")) {
      return Dx7Format.DX7_FORMAT_BINARY_SYSEX_32_VOICE;
    }
    if (path_upper.endsWith(".SYX")) {
      return Dx7Format.DX7_FORMAT_BINARY_SYSEX_32_VOICE;
    }
    if (path_upper.endsWith(".DX7")) {
      return Dx7Format.DX7_FORMAT_BINARY_SYSEX_32_VOICE;
    }
    if (path_upper.endsWith(".XML")) {
      return Dx7Format.DX7_FORMAT_XML;
    }
    if (path_upper.endsWith(".XML.GZ")) {
      return Dx7Format.DX7_FORMAT_XML_GZ;
    }

    throw new IllegalArgumentException(
      "Could not infer file format from file name: " + path);
  }

  @Override
  public Status execute()
    throws Exception
  {
    if (super.execute() == Status.FAILURE) {
      return Status.FAILURE;
    }

    final Optional<Path> output =
      this.determineOutputPath();

    final List<Path> files =
      Files.lines(this.path)
        .map(Paths::get)
        .collect(Collectors.toList());

    final MutableInteger errors = MutableInteger.create();
    final MutableInteger warnings = MutableInteger.create();

    final Dx7ParseErrorListenerType parse_errors = e -> {
      switch (e.severity()) {
        case WARNING: {
          warnings.setValue(warnings.value() + 1);
          LOG.warn("{}", e.show());
          break;
        }
        case ERROR: {
          errors.setValue(errors.value() + 1);
          LOG.error("{}", e.show());
          break;
        }
      }
    };

    final Consumer<? super Dx7XMLParseError> xml_errors = e -> {
      switch (e.severity()) {
        case WARNING: {
          warnings.setValue(warnings.value() + 1);
          LOG.warn("{}", e.show());
          break;
        }
        case ERROR: {
          errors.setValue(errors.value() + 1);
          LOG.error("{}", e.show());
          break;
        }
      }
    };

    Vector<Dx7VoiceNamed> voices_all = Vector.empty();
    for (final Path patch_file : files) {
      try {
        LOG.info("parse: {}", patch_file);
        final Dx7Format format =
          inferFileFormat(patch_file, null);
        voices_all =
          voices_all.appendAll(updateMetadata(
            patch_file,
            this.parse(patch_file, format, parse_errors, xml_errors)));
      } catch (final IllegalArgumentException e) {
        LOG.error("parse: {}: illegal argument: ", patch_file, e);
      } catch (final Dx7ParserConfigurationException e) {
        LOG.error("parse: {}: parser configuration: ", patch_file, e);
      }
    }

    LOG.debug(
      "parsed {} voices ({} warnings, {} errors)",
      Integer.valueOf(voices_all.size()),
      Integer.valueOf(warnings.value()),
      Integer.valueOf(errors.value()));

    this.writeOutput(output, voices_all);
    return Status.SUCCESS;
  }

  private Optional<Path> determineOutputPath()
  {
    final Optional<Path> output;
    if (this.file_output != null) {
      final Path file_abs_output =
        this.file_output.toAbsolutePath();
      this.format_output =
        inferFileFormat(file_abs_output, this.format_output);
      output = Optional.of(file_abs_output);
    } else {
      output = Optional.empty();
    }
    return output;
  }

  private void writeOutput(
    final Optional<Path> output,
    final Vector<Dx7VoiceNamed> voices_all)
    throws IOException, Dx7WriterConfigurationException
  {
    final Vector<Dx7VoiceNamed> voices;
    if (output.isPresent()) {
      final Path out = output.get();

      if (this.pick_random_32) {
        LOG.debug("picking 32 random patches");

        final Vector<Dx7VoiceNamed> voices_shuffled =
          voices_all.filter(Dx7CommandConvertBatch::shouldBeIncluded)
            .shuffle();
        voices = voices_shuffled.take(Math.min(32, voices_shuffled.size()));
      } else {
        voices = voices_all;
      }

      LOG.debug("write: {} ({})", out, this.format_output);
      this.write(voices, out, this.format_output);
    }
  }

  private static boolean shouldBeIncluded(
    final Dx7VoiceNamed v)
  {
    if (Objects.equals(v.name(), "INIT VOICE")) {
      return false;
    }
    if (Objects.equals(v.voice(), DEFAULT_VOICE)) {
      return false;
    }
    return true;
  }

  private Vector<Dx7VoiceNamed> parse(
    final Path file,
    final Dx7Format format,
    final Dx7ParseErrorListenerType parse_errors,
    final Consumer<? super Dx7XMLParseError> xml_errors)
    throws IOException, Dx7ParserConfigurationException
  {
    switch (format) {
      case DX7_FORMAT_XML: {
        return this.parseXML(file, xml_errors);
      }
      case DX7_FORMAT_XML_GZ: {
        return this.parseXMLGZ(file, xml_errors);
      }
      case DX7_FORMAT_BINARY_SYSEX_32_VOICE: {
        return parseSysEx32(file, parse_errors);
      }
    }
    throw new UnreachableCodeException();
  }

  private Vector<Dx7VoiceNamed> parseXMLGZ(
    final Path file,
    final Consumer<? super Dx7XMLParseError> xml_errors)
    throws IOException, Dx7ParserConfigurationException
  {
    try (InputStream stream =
           new GZIPInputStream(
             new BufferedInputStream(Files.newInputStream(file)))) {
      return this.parseXMLStream(file, xml_errors, stream);
    }
  }

  private static Vector<Dx7VoiceNamed> parseSysEx32(
    final Path file,
    final Dx7ParseErrorListenerType error_listener)
    throws IOException
  {
    try (InputStream stream =
           new BufferedInputStream(Files.newInputStream(file))) {
      final Dx7SysExReaderType reader =
        Dx7SysExIO.createReader(error_listener, file.toUri(), stream);

      return reader.parse();
    }
  }

  private Vector<Dx7VoiceNamed> parseXML(
    final Path file,
    final Consumer<? super Dx7XMLParseError> xml_errors)
    throws IOException, Dx7ParserConfigurationException
  {
    try (InputStream stream =
           new BufferedInputStream(Files.newInputStream(file))) {
      return this.parseXMLStream(file, xml_errors, stream);
    }
  }

  private Vector<Dx7VoiceNamed> parseXMLStream(
    final Path file,
    final Consumer<? super Dx7XMLParseError> xml_errors,
    final InputStream stream)
    throws IOException, Dx7ParserConfigurationException
  {
    final Path parent = file.getParent();

    final Dx7XMLParserType parser =
      this.xml_parsers.create(Dx7XMLParserRequest.builder()
                                .setBaseDirectory(parent)
                                .setFile(file.toUri())
                                .setStream(stream)
                                .build());

    final Validation<Seq<Dx7XMLParseError>, Vector<Dx7VoiceNamed>> result =
      parser.parse();

    if (result.isValid()) {
      return result.get();
    }

    result.getError().forEach(xml_errors);
    throw new IOException("At least one parse error occurred.");
  }

  private void write(
    final Vector<Dx7VoiceNamed> voices,
    final Path file,
    final Dx7Format format)
    throws IOException, Dx7WriterConfigurationException
  {
    switch (format) {
      case DX7_FORMAT_XML:
        this.writeXML(voices, file);
        break;
      case DX7_FORMAT_XML_GZ:
        this.writeXMLGZ(voices, file);
        break;
      case DX7_FORMAT_BINARY_SYSEX_32_VOICE:
        writeSysEx32(voices, file);
        break;
    }
  }

  private static void writeSysEx32(
    final Vector<Dx7VoiceNamed> voices,
    final Path file)
    throws IOException
  {
    try (OutputStream stream = Files.newOutputStream(file)) {
      try (Dx7SysExWriterType writer = Dx7SysExIO.createWriter(
        file.toUri(),
        stream)) {
        writer.write(voices);
      }
    }
  }

  private void writeXMLGZ(
    final Vector<Dx7VoiceNamed> voices,
    final Path file)
    throws IOException, Dx7WriterConfigurationException
  {
    try (OutputStream stream =
           new GZIPOutputStream(
             new BufferedOutputStream(Files.newOutputStream(file)))) {
      this.writeXMLStream(voices, file, stream);
      stream.flush();
    }
  }

  private void writeXML(
    final Vector<Dx7VoiceNamed> voices,
    final Path file)
    throws IOException, Dx7WriterConfigurationException
  {
    try (OutputStream stream =
           new BufferedOutputStream(Files.newOutputStream(file))) {
      this.writeXMLStream(voices, file, stream);
      stream.flush();
    }
  }

  private void writeXMLStream(
    final Vector<Dx7VoiceNamed> voices,
    final Path file,
    final OutputStream stream)
    throws IOException, Dx7WriterConfigurationException
  {
    final Dx7XMLWriterRequest request =
      Dx7XMLWriterRequest.builder()
        .setFile(file.toUri())
        .setSchema(URI.create("schema:com.io7m.jdextrosa:1.0"))
        .setStream(stream)
        .build();

    try (Dx7XMLWriterType writer = this.xml_writers.createWriter(request)) {
      writer.start();
      writer.write(voices);
      writer.finish();
    }
  }

  private static Vector<Dx7VoiceNamed> updateMetadata(
    final Path file,
    final Vector<Dx7VoiceNamed> voices)
  {
    return voices.map(
      voice -> Dx7VoiceNamed.of(
        voice.name(),
        voice.voice(),
        Optional.of(
          Dx7VoiceMetadata.builder()
            .setSource(file.toUri())
            .setId(URI.create(file.toUri().toString() + "/" + makeNameSafe(voice)))
            .build())));
  }

  private static String makeNameSafe(
    final Dx7VoiceNamed voice)
  {
    try {
      return URLEncoder.encode(voice.name(), "UTF-8");
    } catch (final UnsupportedEncodingException e) {
      throw new UnreachableCodeException(e);
    }
  }
}
