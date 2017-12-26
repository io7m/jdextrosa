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

package com.io7m.jdextrosa.io;

import com.io7m.jaffirm.core.Invariants;
import com.io7m.jaffirm.core.Preconditions;
import com.io7m.jdextrosa.core.Dx7AlgorithmID;
import com.io7m.jdextrosa.core.Dx7Operator;
import com.io7m.jdextrosa.core.Dx7OperatorID;
import com.io7m.jdextrosa.core.Dx7OperatorType;
import com.io7m.jdextrosa.core.Dx7Voice;
import com.io7m.jdextrosa.core.Dx7VoiceNamed;
import com.io7m.junreachable.UnreachableCodeException;
import io.vavr.collection.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

import static com.io7m.jdextrosa.core.Dx7VoiceType.LFOWave;

final class Dx7Reader implements Dx7SysExReaderType
{
  private static final Logger LOG = LoggerFactory.getLogger(Dx7Reader.class);

  private final URI uri;
  private final Dx7InputStream stream;
  private final Dx7TrackingErrorListener errors;

  Dx7Reader(
    final Dx7ParseErrorListenerType in_errors,
    final URI in_uri,
    final Dx7InputStream in_stream)
  {
    this.uri = Objects.requireNonNull(in_uri, "URI");
    this.stream = Objects.requireNonNull(in_stream, "Stream");
    this.errors = new Dx7TrackingErrorListener(in_errors);
  }

  private void readSpecificByte(
    final int value)
    throws IOException
  {
    final int v_rec = this.stream.readByte();
    final int v_exp = value & 0xff;

    if (v_rec != v_exp) {
      this.errors.receiveError(this.parseError(
        new StringBuilder(128)
          .append("Unexpected byte value.")
          .append(System.lineSeparator())
          .append("  Expected: 0x")
          .append(Integer.toUnsignedString(v_exp, 16))
          .append(System.lineSeparator())
          .append("  Received: 0x")
          .append(Integer.toUnsignedString(v_rec, 16))
          .append(System.lineSeparator())
          .toString()));
    }
  }

  private Dx7ParseError parseError(
    final String message)
  {
    return Dx7ParseError.of(
      this.uri,
      this.stream.getByteCount(),
      Dx7ParseErrorType.Severity.ERROR,
      message,
      Optional.empty());
  }

  private Dx7ParseError parseWarning(
    final String message)
  {
    return Dx7ParseError.of(
      this.uri,
      this.stream.getByteCount(),
      Dx7ParseErrorType.Severity.WARNING,
      message,
      Optional.empty());
  }

  private Dx7ParseError parseException(
    final Exception e)
  {
    return Dx7ParseError.of(
      this.uri,
      this.stream.getByteCount(),
      Dx7ParseErrorType.Severity.ERROR,
      e.getMessage(),
      Optional.of(e));
  }

  @Override
  public Vector<Dx7VoiceNamed> parseAtMost(
    final int limit)
  {
    if (limit < 0) {
      throw new IllegalArgumentException("Limit must be non-negative");
    }

    try {
      this.errors.reset();
      this.readSpecificByte(0xf0);
      this.readSpecificByte(0x43);
      this.readSpecificByte(0x0);

      if (this.errors.errorsEncountered()) {
        return Vector.empty();
      }

      final int format = this.stream.readByte();
      return this.parseVoices(format, limit);
    } catch (final IOException e) {
      this.errors.receiveError(this.parseException(e));
      return Vector.empty();
    }
  }

  private Vector<Dx7VoiceNamed> parseVoices(
    final int format,
    final int limit)
    throws IOException
  {
    switch (format) {
      case 0x0: {
        return this.parseOneVoice();
      }
      case 0x9: {
        return this.parse32Voices(limit);
      }
      default: {
        this.errors.receiveError(this.parseError(
          new StringBuilder(128)
            .append("Unexpected voice format.")
            .append(System.lineSeparator())
            .append("  Expected: 0x0 or 0x9")
            .append(System.lineSeparator())
            .append("  Received: 0x")
            .append(Integer.toUnsignedString(format, 16))
            .append(System.lineSeparator())
            .toString()));
        return Vector.empty();
      }
    }
  }

  private Vector<Dx7VoiceNamed> parse32Voices(
    final int limit)
    throws IOException
  {
    return new Voice32Parser(
      this.uri,
      this.errors,
      this.stream,
      limit)
      .parse();
  }

  private Vector<Dx7VoiceNamed> parseOneVoice()
    throws IOException
  {
    LOG.debug("parsing 1 voice");
    return Vector.empty();
  }

  private static final class Voice128ByteOperatorParser
  {
    private final Dx7TrackingErrorListener errors;
    private final int op_index;
    private final int voice_index;
    private final Dx7InputStream stream;
    private final URI uri;
    private long offset_start;

    Voice128ByteOperatorParser(
      final URI in_uri,
      final Dx7TrackingErrorListener in_errors,
      final Dx7InputStream in_stream,
      final int in_voice_index,
      final int in_op_index)
    {
      this.uri =
        Objects.requireNonNull(in_uri, "URI");
      this.errors =
        Objects.requireNonNull(in_errors, "Errors");
      this.stream =
        Objects.requireNonNull(in_stream, "Stream");

      this.voice_index = in_voice_index;
      this.op_index = in_op_index;
    }

    private int checkConstrainValueRange(
      final int param_value,
      final String param_name,
      final int param_min,
      final int param_max)
    {
      if (LOG.isTraceEnabled()) {
        LOG.trace(
          "check: [{}] [voice {}] [op {}] {} {}",
          Long.valueOf(this.stream.getByteCount()),
          Integer.valueOf(this.voice_index),
          Integer.valueOf(this.op_index),
          param_name,
          Integer.valueOf(param_value));
      }

      if (param_value >= param_min && param_value <= param_max) {
        return param_value;
      }

      this.errors.receiveError(Dx7ParseError.of(
        this.uri,
        this.stream.getByteCount(),
        Dx7ParseErrorType.Severity.ERROR,
        new StringBuilder(128)
          .append("Value out of range.")
          .append(System.lineSeparator())
          .append("  Byte offset: ")
          .append(this.stream.getByteCount())
          .append(System.lineSeparator())
          .append("  Voice:       ")
          .append(this.voice_index)
          .append(System.lineSeparator())
          .append("  Operator:    ")
          .append(this.op_index)
          .append(System.lineSeparator())
          .append("  Parameter:   ")
          .append(param_name)
          .append(System.lineSeparator())
          .append("  Valid range: [")
          .append(param_min)
          .append(", ")
          .append(param_max)
          .append("]")
          .append(System.lineSeparator())
          .append("  Received:    ")
          .append(param_value)
          .append(System.lineSeparator())
          .toString(),
        Optional.empty()));

      return Math.max(Math.min(param_max, param_value), param_min);
    }

    public Optional<Dx7Operator> parse()
      throws IOException
    {
      this.offset_start = this.stream.getByteCount();

      this.errors.reset();

      final Dx7Operator.Builder op_b = Dx7Operator.builder();
      op_b.setId(Dx7OperatorID.of(this.op_index));

      final int env_r1_rate =
        this.checkConstrainValueRange(
          this.stream.readByte(),
          "R1 Rate",
          0,
          99);

      final int env_r2_rate =
        this.checkConstrainValueRange(
          this.stream.readByte(),
          "R2 Rate",
          0,
          99);

      final int env_r3_rate =
        this.checkConstrainValueRange(
          this.stream.readByte(),
          "R3 Rate",
          0,
          99);

      final int env_r4_rate =
        this.checkConstrainValueRange(
          this.stream.readByte(),
          "R4 Rate",
          0,
          99);

      final int env_r1_level =
        this.checkConstrainValueRange(
          this.stream.readByte(),
          "R1 Level",
          0,
          99);

      final int env_r2_level =
        this.checkConstrainValueRange(
          this.stream.readByte(),
          "R2 Level",
          0,
          99);

      final int env_r3_level =
        this.checkConstrainValueRange(
          this.stream.readByte(),
          "R3 Level",
          0,
          99);

      final int env_r4_level =
        this.checkConstrainValueRange(
          this.stream.readByte(),
          "R4 Level",
          0,
          99);

      final int level_scaling_breakpoint =
        this.checkConstrainValueRange(
          this.stream.readByte(),
          "Level scaling breakpoint",
          0,
          99);

      final int level_scaling_left_depth =
        this.checkConstrainValueRange(
          this.stream.readByte(),
          "Level scaling left depth",
          0,
          99);

      final int level_scaling_right_depth =
        this.checkConstrainValueRange(
          this.stream.readByte(),
          "Level scaling right depth",
          0,
          99);

      final int scaling_curves_packed = this.stream.readByte();

      final int left_curve_value =
        this.checkConstrainValueRange(
          (scaling_curves_packed >> 2) & 0b11,
          "Level scaling left curve",
          0,
          3);
      final int right_curve_value =
        this.checkConstrainValueRange(
          scaling_curves_packed & 0b11,
          "Level scaling right curve",
          0,
          3);

      final Dx7OperatorType.LevelScalingCurve left_curve =
        Dx7OperatorType.LevelScalingCurve.ofInteger(left_curve_value);
      final Dx7OperatorType.LevelScalingCurve right_curve =
        Dx7OperatorType.LevelScalingCurve.ofInteger(right_curve_value);

      final int detune_packed = this.stream.readByte();
      final int detune_raw =
        (detune_packed & 0b01111000) >> 3;
      final int detune =
        this.checkConstrainValueRange(
          detune_raw - 7,
          "Oscillator detune",
          -7,
          7);
      final int rate_scaling =
        this.checkConstrainValueRange(
          detune_packed & 0b111,
          "Rate scaling",
          0,
          7);

      final int sensitivity_packed = this.stream.readByte();

      final int vel_sensitivity =
        this.checkConstrainValueRange(
          sensitivity_packed >> 2,
          "Velocity sensitivity",
          0,
          7);

      final int amp_mod_sensitivity =
        this.checkConstrainValueRange(
          sensitivity_packed & 0b11,
          "Amplitude mod sensitivity",
          0,
          3);

      Preconditions.checkPreconditionL(
        this.stream.getByteCount(),
        this.stream.getByteCount() - this.offset_start == 14L,
        n -> "Must have consumed 14 octets");

      final int output_level =
        this.checkConstrainValueRange(
          this.stream.readByte(),
          "Output Level",
          0,
          99);

      final int osc_pack =
        this.stream.readByte();

      final int freq_coarse =
        this.checkConstrainValueRange(
          osc_pack >> 1,
          "Oscillator frequency coarse",
          0,
          31);

      final int osc_mode =
        this.checkConstrainValueRange(
          osc_pack & 0x1,
          "Oscillator mode",
          0,
          1);

      final int osc_frequency_fine =
        this.checkConstrainValueRange(
          this.stream.readByte(),
          "Oscillator frequency fine",
          0,
          99);

      op_b.setEnvelopeR1Rate(env_r1_rate);
      op_b.setEnvelopeR2Rate(env_r2_rate);
      op_b.setEnvelopeR3Rate(env_r3_rate);
      op_b.setEnvelopeR4Rate(env_r4_rate);
      op_b.setEnvelopeR1Level(env_r1_level);
      op_b.setEnvelopeR2Level(env_r2_level);
      op_b.setEnvelopeR3Level(env_r3_level);
      op_b.setEnvelopeR4Level(env_r4_level);
      op_b.setLevelScalingBreakpoint(level_scaling_breakpoint);
      op_b.setLevelScalingLeftDepth(level_scaling_left_depth);
      op_b.setLevelScalingRightDepth(level_scaling_right_depth);
      op_b.setLevelScalingLeftCurve(left_curve);
      op_b.setLevelScalingRightCurve(right_curve);
      op_b.setRateScaling(rate_scaling);
      op_b.setOscillatorFrequencyDetune(detune);
      op_b.setVelocitySensitivity(vel_sensitivity);
      op_b.setLfoAmplitudeModulationSensitivity(amp_mod_sensitivity);
      op_b.setOutputLevel(output_level);
      op_b.setOscillatorFrequencyCoarse(freq_coarse);
      op_b.setOscillatorMode(Dx7OperatorType.OscillatorMode.ofInteger(osc_mode));
      op_b.setOscillatorFrequencyFine(osc_frequency_fine);

      Preconditions.checkPreconditionL(
        this.stream.getByteCount() - this.offset_start,
        this.stream.getByteCount() - this.offset_start == 17L,
        n -> "Must have consumed 17 octets");

      return Optional.of(op_b.build());
    }
  }

  private static final class Voice128ByteParser
  {
    private final Dx7TrackingErrorListener errors;
    private final int voice_index;
    private final URI uri;
    private final Dx7InputStream stream;
    private long offset_start;

    Voice128ByteParser(
      final URI in_uri,
      final Dx7InputStream in_stream,
      final Dx7TrackingErrorListener in_errors,
      final int in_voice_index)
    {
      this.uri = Objects.requireNonNull(in_uri, "URI");
      this.errors = Objects.requireNonNull(in_errors, "Errors");
      this.stream = Objects.requireNonNull(in_stream, "Stream");
      this.voice_index = in_voice_index;
    }

    private int checkConstrainValueRange(
      final int param_value,
      final String param_name,
      final int param_min,
      final int param_max)
    {
      if (LOG.isTraceEnabled()) {
        LOG.trace(
          "check: [{}] [voice {}] {} {}",
          Long.valueOf(this.stream.getByteCount()),
          Integer.valueOf(this.voice_index),
          param_name,
          Integer.valueOf(param_value));
      }

      if (param_value >= param_min && param_value <= param_max) {
        return param_value;
      }

      this.errors.receiveError(Dx7ParseError.of(
        this.uri,
        this.stream.getByteCount(),
        Dx7ParseErrorType.Severity.ERROR,
        new StringBuilder(128)
          .append("Value out of range.")
          .append(System.lineSeparator())
          .append("  Byte offset: ")
          .append(this.stream.getByteCount())
          .append(System.lineSeparator())
          .append("  Voice:       ")
          .append(this.voice_index)
          .append(System.lineSeparator())
          .append("  Parameter:   ")
          .append(param_name)
          .append(System.lineSeparator())
          .append("  Valid range: [")
          .append(param_min)
          .append(", ")
          .append(param_max)
          .append("]")
          .append(System.lineSeparator())
          .append("  Received:    ")
          .append(param_value)
          .append(System.lineSeparator())
          .toString(),
        Optional.empty()));
      return Math.max(Math.min(param_max, param_value), param_min);
    }

    public Optional<Dx7VoiceNamed> parse()
      throws IOException
    {
      final Dx7Voice.Builder vb = Dx7Voice.builder();

      this.offset_start = this.stream.getByteCount();
      this.errors.reset();

      int valid_ops_processed = 0;
      for (int op_index = 6; op_index >= 1; --op_index) {
        final Optional<Dx7Operator> op_opt =
          new Voice128ByteOperatorParser(
            this.uri,
            this.errors,
            this.stream,
            this.voice_index,
            op_index)
            .parse();

        if (op_opt.isPresent()) {
          valid_ops_processed += 1;
          final Dx7Operator op = op_opt.get();
          switch (op_index) {
            case 6: {
              vb.setOperator6(op);
              break;
            }
            case 5: {
              vb.setOperator5(op);
              break;
            }
            case 4: {
              vb.setOperator4(op);
              break;
            }
            case 3: {
              vb.setOperator3(op);
              break;
            }
            case 2: {
              vb.setOperator2(op);
              break;
            }
            case 1: {
              vb.setOperator1(op);
              break;
            }
            default:
              throw new UnreachableCodeException();
          }
        }
      }

      Invariants.checkInvariantL(
        this.stream.getByteCount(),
        this.stream.getByteCount() - this.offset_start == 102L,
        x -> "Must have consumed 102 octets");

      vb.setPitchEnvelopeR1Rate(
        this.checkConstrainValueRange(
          this.stream.readByte(),
          "Pitch Envelope R1 Rate",
          0,
          99));

      vb.setPitchEnvelopeR2Rate(
        this.checkConstrainValueRange(
          this.stream.readByte(),
          "Pitch Envelope R2 Rate",
          0,
          99));

      vb.setPitchEnvelopeR3Rate(
        this.checkConstrainValueRange(
          this.stream.readByte(),
          "Pitch Envelope R3 Rate",
          0,
          99));

      vb.setPitchEnvelopeR4Rate(
        this.checkConstrainValueRange(
          this.stream.readByte(),
          "Pitch Envelope R4 Rate",
          0,
          99));

      vb.setPitchEnvelopeR1Level(
        this.checkConstrainValueRange(
          this.stream.readByte(),
          "Pitch Envelope R1 Level",
          0,
          99));

      vb.setPitchEnvelopeR2Level(
        this.checkConstrainValueRange(
          this.stream.readByte(),
          "Pitch Envelope R2 Level",
          0,
          99));

      vb.setPitchEnvelopeR3Level(
        this.checkConstrainValueRange(
          this.stream.readByte(),
          "Pitch Envelope R3 Level",
          0,
          99));

      vb.setPitchEnvelopeR4Level(
        this.checkConstrainValueRange(
          this.stream.readByte(),
          "Pitch Envelope R4 Level",
          0,
          99));

      Invariants.checkInvariantL(
        this.stream.getByteCount(),
        this.stream.getByteCount() - this.offset_start == 110L,
        x -> "Must have consumed 110 octets");

      final int algo_raw =
        this.checkConstrainValueRange(
          this.stream.readByte() & 0b11111,
          "Algorithm",
          0,
          31);

      vb.setAlgorithm(Dx7AlgorithmID.of(algo_raw + 1));

      {
        final int pack = this.stream.readByte();
        final int feed = (pack & 0b0000_0111);
        final int sync = (pack & 0b0000_1000) >>> 3;

        vb.setFeedback(
          this.checkConstrainValueRange(
            feed,
            "Feedback",
            0,
            31));

        vb.setOscillatorKeySync(sync == 1);
      }

      vb.setLfoSpeed(
        this.checkConstrainValueRange(
          this.stream.readByte(),
          "LFO Rate",
          0,
          99));

      vb.setLfoDelay(
        this.checkConstrainValueRange(
          this.stream.readByte(),
          "LFO Delay",
          0,
          99));

      vb.setLfoPitchModulationDepth(
        this.checkConstrainValueRange(
          this.stream.readByte(),
          "LFO Pitch Modulation Depth",
          0,
          99));

      vb.setLfoAmplitudeModulationDepth(
        this.checkConstrainValueRange(
          this.stream.readByte(),
          "LFO Amplitude Modulation Depth",
          0,
          99));

      Invariants.checkInvariantL(
        this.stream.getByteCount(),
        this.stream.getByteCount() - this.offset_start == 116L,
        x -> "Must have consumed 116 octets");

      {
        final int lfo_pack = this.stream.readByte();
        final int lfpms = (lfo_pack & 0b0111_0000) >>> 4;
        final int lwave = (lfo_pack & 0b0000_1110) >>> 1;
        final int lsync = (lfo_pack & 0b0000_0001);

        vb.setLfoPitchModulationSensitivity(lfpms);
        vb.setLfoWave(LFOWave.ofInteger(lwave));
        vb.setLfoKeySync(lsync == 1);
      }

      {
        final int transpose_raw = this.stream.readByte();
        this.checkConstrainValueRange(
          transpose_raw,
          "Transpose value",
          0,
          48);

        final int transpose = transpose_raw - 24;
        vb.setTranspose(transpose);
      }

      final byte[] name = new byte[10];
      for (int index = 0; index < 10; ++index) {
        name[index] = (byte) this.stream.readByte();
      }
      final String voice_name = new String(name, StandardCharsets.US_ASCII);

      Invariants.checkInvariantL(
        this.stream.getByteCount() - this.offset_start,
        this.stream.getByteCount() - this.offset_start == 128L,
        x -> "Must have consumed 128 octets");

      if (this.errors.errorsEncountered() || valid_ops_processed != 6) {
        return Optional.empty();
      }

      return Optional.of(Dx7VoiceNamed.of(
        voice_name,
        vb.build(),
        Optional.empty()));
    }
  }

  private static final class Voice32Parser
  {
    private final Dx7TrackingErrorListener errors;
    private final Dx7InputStream stream;
    private final int limit;
    private final URI uri;
    private int checksum;

    Voice32Parser(
      final URI in_uri,
      final Dx7TrackingErrorListener in_errors,
      final Dx7InputStream in_stream,
      final int in_limit)
    {
      this.uri = Objects.requireNonNull(in_uri, "URI");
      this.errors = Objects.requireNonNull(in_errors, "Errors");
      this.stream = Objects.requireNonNull(in_stream, "Stream");
      this.checksum = 0;
      this.limit = in_limit;
    }

    private int readSize()
      throws IOException
    {
      final int r0 = this.stream.readByte();
      final int r1 = this.stream.readByte();
      final int ms = r0 << 7;
      final int cmb = ms | r1;
      return cmb;
    }

    public Vector<Dx7VoiceNamed> parse()
      throws IOException
    {
      final int size = this.readSize();
      final int expected_voices = size / 128;
      final int limited = Math.min(this.limit, expected_voices);

      LOG.debug(
        "expected: {} octets ({} voices [limited to {}])",
        Integer.valueOf(size),
        Integer.valueOf(expected_voices),
        Integer.valueOf(limited));

      Vector<Dx7VoiceNamed> voices = Vector.empty();
      final byte[] buffer = new byte[128];
      for (int voice_index = 0; voice_index < limited; ++voice_index) {
        final Optional<Dx7VoiceNamed> voice_opt =
          new Voice128ByteParser(
            this.uri,
            this.stream,
            this.errors,
            voice_index)
            .parse();

        if (voice_opt.isPresent()) {
          voices = voices.append(voice_opt.get());
        }

        this.checksum = Dx7Checksum.checksumAdd(this.checksum, buffer);
      }

      if (limited != expected_voices) {
        final int checksum_expect = this.stream.readByte();
        final int checksum_received = Dx7Checksum.checksumFinish(this.checksum);
        LOG.trace(
          "expected checksum: {}",
          Integer.toUnsignedString(checksum_expect, 16));
        LOG.trace(
          "received checksum: {}",
          Integer.toUnsignedString(checksum_received, 16));
      }

      return voices;
    }
  }
}
