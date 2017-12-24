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

package com.io7m.jdextrosa.io.xml.v1;

import com.io7m.jdextrosa.core.Dx7Operator;
import com.io7m.jdextrosa.core.Dx7OperatorType;
import com.io7m.jdextrosa.core.Dx7Voice;
import com.io7m.jdextrosa.core.Dx7VoiceMetadata;
import com.io7m.jdextrosa.core.Dx7VoiceNamed;
import com.io7m.jdextrosa.core.Dx7VoiceType;
import com.io7m.jdextrosa.io.xml.spi.Dx7XMLWriterType;
import com.io7m.junreachable.UnreachableCodeException;
import io.vavr.collection.Vector;
import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Objects;

import static com.io7m.jdextrosa.io.xml.v1.Dx7XMLv1FormatProvider.SCHEMA_NAMESPACE;

public final class Dx7v1Writer implements Dx7XMLWriterType
{
  private final URI file;
  private final OutputStream stream;
  private final Configuration config;
  private final XMLStreamWriter writer;

  Dx7v1Writer(
    final Configuration in_config,
    final URI in_file,
    final OutputStream in_stream)
    throws SaxonApiException
  {
    this.config = Objects.requireNonNull(in_config, "Configuration");
    this.file = Objects.requireNonNull(in_file, "File");
    this.stream = Objects.requireNonNull(in_stream, "Stream");

    final Processor processor = new Processor(this.config);
    final Serializer serializer = processor.newSerializer();
    serializer.setOutputProperty(Serializer.Property.METHOD, "xml");
    serializer.setOutputProperty(Serializer.Property.INDENT, "yes");
    serializer.setOutputStream(this.stream);
    this.writer = serializer.getXMLStreamWriter();
  }

  private static String waveformToString(
    final Dx7VoiceType.LFOWave wave)
  {
    switch (wave) {
      case LFO_TRIANGLE:
        return "triangle";
      case LFO_SAW_DOWN:
        return "sawDown";
      case LFO_SAW_UP:
        return "sawUp";
      case LFO_SQUARE:
        return "square";
      case LFO_SINE:
        return "sine";
      case LFO_SAMPLE_HOLD:
        return "sampleAndHold";
    }

    throw new UnreachableCodeException();
  }

  private static String curveToString(
    final Dx7OperatorType.LevelScalingCurve c)
  {
    switch (c) {
      case LINEAR_NEGATIVE:
        return "linearNegative";
      case EXPONENTIAL_NEGATIVE:
        return "exponentialNegative";
      case EXPONENTIAL_POSITIVE:
        return "exponentialPositive";
      case LINEAR_POSITIVE:
        return "linearPositive";
    }

    throw new UnreachableCodeException();
  }

  private static String modeToString(
    final Dx7OperatorType.OscillatorMode mode)
  {
    switch (mode) {
      case RATIO:
        return "ratio";
      case FIXED:
        return "fixed";
    }
    throw new UnreachableCodeException();
  }

  @Override
  public void start()
    throws IOException
  {
    try {
      this.writer.setPrefix("dx", SCHEMA_NAMESPACE);
      this.writer.writeStartDocument("UTF-8", "1.0");
      this.writer.writeStartElement("dx", "dx7-voices", SCHEMA_NAMESPACE);
    } catch (final XMLStreamException e) {
      throw new IOException(e);
    }
  }

  @Override
  public void write(
    final Vector<Dx7VoiceNamed> voices)
    throws IOException
  {
    try {
      for (final Dx7VoiceNamed voice : voices) {
        this.writeVoice(voice);
      }
    } catch (final XMLStreamException e) {
      throw new IOException(e);
    }
  }

  private void writeVoice(
    final Dx7VoiceNamed voice_named)
    throws XMLStreamException
  {
    final Dx7Voice voice = voice_named.voice();

    this.writer.writeStartElement(
      "dx", "dx7-voice", SCHEMA_NAMESPACE);

    this.writer.writeAttribute(
      "name",
      voice_named.name());

    this.writer.writeAttribute(
      "algorithm",
      Integer.toUnsignedString(voice.algorithm().id()));
    this.writer.writeAttribute(
      "feedback",
      Integer.toUnsignedString(voice.feedback()));
    this.writer.writeAttribute(
      "transpose",
      Integer.toUnsignedString(voice.transpose()));

    this.writer.writeAttribute(
      "pitchEnvelopeR1Level",
      Integer.toUnsignedString(voice.pitchEnvelopeR1Level()));
    this.writer.writeAttribute(
      "pitchEnvelopeR2Level",
      Integer.toUnsignedString(voice.pitchEnvelopeR2Level()));
    this.writer.writeAttribute(
      "pitchEnvelopeR3Level",
      Integer.toUnsignedString(voice.pitchEnvelopeR3Level()));
    this.writer.writeAttribute(
      "pitchEnvelopeR4Level",
      Integer.toUnsignedString(voice.pitchEnvelopeR4Level()));

    this.writer.writeAttribute(
      "pitchEnvelopeR1Rate",
      Integer.toUnsignedString(voice.pitchEnvelopeR1Rate()));
    this.writer.writeAttribute(
      "pitchEnvelopeR2Rate",
      Integer.toUnsignedString(voice.pitchEnvelopeR2Rate()));
    this.writer.writeAttribute(
      "pitchEnvelopeR3Rate",
      Integer.toUnsignedString(voice.pitchEnvelopeR3Rate()));
    this.writer.writeAttribute(
      "pitchEnvelopeR4Rate",
      Integer.toUnsignedString(voice.pitchEnvelopeR4Rate()));

    if (voice_named.metadata().isPresent()) {
      this.writeMetadata(voice_named.metadata().get());
    }

    this.writeOperator(voice.operator1());
    this.writeOperator(voice.operator2());
    this.writeOperator(voice.operator3());
    this.writeOperator(voice.operator4());
    this.writeOperator(voice.operator5());
    this.writeOperator(voice.operator6());
    this.writeLFO(voice);

    this.writer.writeEndElement();
  }

  private void writeLFO(
    final Dx7Voice voice)
    throws XMLStreamException
  {
    this.writer.writeStartElement("dx", "dx7-lfo", SCHEMA_NAMESPACE);

    this.writer.writeAttribute(
      "pitchModulationDepth",
      Integer.toUnsignedString(voice.lfoPitchModulationDepth()));
    this.writer.writeAttribute(
      "pitchModulationSensitivity",
      Integer.toUnsignedString(voice.lfoPitchModulationSensitivity()));
    this.writer.writeAttribute(
      "amplitudeModulationDepth",
      Integer.toUnsignedString(voice.lfoAmplitudeModulationDepth()));
    this.writer.writeAttribute(
      "rate",
      Integer.toUnsignedString(voice.lfoSpeed()));
    this.writer.writeAttribute(
      "delay",
      Integer.toUnsignedString(voice.lfoDelay()));
    this.writer.writeAttribute(
      "keySynchronize",
      Boolean.toString(voice.lfoKeySync()));
    this.writer.writeAttribute(
      "waveform",
      waveformToString(voice.lfoWave()));

    this.writer.writeEndElement();
  }

  private void writeOperator(
    final Dx7Operator op)
    throws XMLStreamException
  {
    this.writer.writeStartElement(
      "dx", "dx7-operator", SCHEMA_NAMESPACE);

    this.writer.writeAttribute(
      "id",
      Integer.toUnsignedString(op.id().id()));

    this.writer.writeAttribute(
      "enabled",
      Boolean.toString(op.isEnabled()));

    this.writer.writeAttribute(
      "frequencyCoarse",
      Integer.toString(op.oscillatorFrequencyCoarse()));
    this.writer.writeAttribute(
      "frequencyFine",
      Integer.toString(op.oscillatorFrequencyFine()));
    this.writer.writeAttribute(
      "frequencyDetune",
      Integer.toString(op.oscillatorFrequencyDetune()));

    this.writer.writeAttribute(
      "mode",
      modeToString(op.oscillatorMode()));
    this.writer.writeAttribute(
      "output",
      Integer.toUnsignedString(op.outputLevel()));

    this.writer.writeAttribute(
      "envelopeR1Level",
      Integer.toUnsignedString(op.envelopeR1Level()));
    this.writer.writeAttribute(
      "envelopeR2Level",
      Integer.toUnsignedString(op.envelopeR2Level()));
    this.writer.writeAttribute(
      "envelopeR3Level",
      Integer.toUnsignedString(op.envelopeR3Level()));
    this.writer.writeAttribute(
      "envelopeR4Level",
      Integer.toUnsignedString(op.envelopeR4Level()));

    this.writer.writeAttribute(
      "envelopeR1Rate",
      Integer.toUnsignedString(op.envelopeR1Rate()));
    this.writer.writeAttribute(
      "envelopeR2Rate",
      Integer.toUnsignedString(op.envelopeR2Rate()));
    this.writer.writeAttribute(
      "envelopeR3Rate",
      Integer.toUnsignedString(op.envelopeR3Rate()));
    this.writer.writeAttribute(
      "envelopeR4Rate",
      Integer.toUnsignedString(op.envelopeR4Rate()));

    this.writer.writeAttribute(
      "levelScalingBreakpoint",
      Integer.toUnsignedString(op.levelScalingBreakpoint()));
    this.writer.writeAttribute(
      "levelScalingLeftDepth",
      Integer.toUnsignedString(op.levelScalingLeftDepth()));
    this.writer.writeAttribute(
      "levelScalingLeftCurve",
      curveToString(op.levelScalingLeftCurve()));
    this.writer.writeAttribute(
      "levelScalingRightDepth",
      Integer.toUnsignedString(op.levelScalingRightDepth()));
    this.writer.writeAttribute(
      "levelScalingRightCurve",
      curveToString(op.levelScalingRightCurve()));

    this.writer.writeAttribute(
      "velocitySensitivity",
      Integer.toUnsignedString(op.velocitySensitivity()));
    this.writer.writeAttribute(
      "lfoAmplitudeModulationSensitivity",
      Integer.toUnsignedString(op.lfoAmplitudeModulationSensitivity()));

    this.writer.writeEndElement();
  }

  private void writeMetadata(
    final Dx7VoiceMetadata meta)
    throws XMLStreamException
  {
    this.writer.writeStartElement(
      "dx", "dx7-voice-metadata", SCHEMA_NAMESPACE);
    this.writer.writeAttribute("id", meta.id().toString());
    this.writer.writeAttribute("source", meta.source().toString());
    this.writer.writeEndElement();
  }

  @Override
  public void finish()
    throws IOException
  {
    try {
      this.writer.writeEndElement();
      this.writer.writeEndDocument();
      this.writer.flush();
    } catch (final XMLStreamException e) {
      throw new IOException(e);
    }
  }

  @Override
  public void close()
    throws IOException
  {
    try {
      this.writer.close();
    } catch (final XMLStreamException e) {
      throw new IOException(e);
    }
  }
}
