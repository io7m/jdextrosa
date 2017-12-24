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
import com.io7m.jdextrosa.core.Dx7Voice;
import com.io7m.jdextrosa.core.Dx7VoiceNamed;
import com.io7m.junreachable.UnreachableCodeException;
import io.vavr.collection.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

import static com.io7m.jdextrosa.core.Dx7OperatorType.LevelScalingCurve;
import static com.io7m.jdextrosa.core.Dx7OperatorType.OscillatorMode;

public final class Dx7SysExIO
{
  private static final Logger LOG = LoggerFactory.getLogger(Dx7SysExIO.class);

  private Dx7SysExIO()
  {
    throw new UnreachableCodeException();
  }

  public static Dx7SysExReaderType createReader(
    final Dx7ParseErrorListenerType errors,
    final URI uri,
    final InputStream stream)
  {
    Objects.requireNonNull(errors, "Errors");
    Objects.requireNonNull(uri, "URI");
    Objects.requireNonNull(stream, "Stream");
    return new Reader(errors, uri, stream);
  }

  private static final class TrackingErrorListener
    implements Dx7ParseErrorListenerType
  {
    private final Dx7ParseErrorListenerType delegate;
    private boolean warning;
    private boolean error;

    TrackingErrorListener(
      final Dx7ParseErrorListenerType in_delegate)
    {
      this.delegate = Objects.requireNonNull(in_delegate, "Delegate");
    }

    void reset()
    {
      this.warning = false;
      this.error = false;
    }

    @Override
    public void receiveError(
      final Dx7ParseError in_error)
    {
      switch (in_error.severity()) {
        case WARNING: {
          this.warning = true;
          break;
        }
        case ERROR: {
          this.error = true;
          break;
        }
      }

      this.delegate.receiveError(in_error);
    }

    public boolean errorsEncountered()
    {
      return this.error;
    }
  }

  private static final class PositionContext
  {
    private final URI uri;
    private Optional<PositionContext> parent;
    private long local_offset;

    PositionContext(
      final Optional<PositionContext> in_parent,
      final URI in_uri)
    {
      this.parent = Objects.requireNonNull(in_parent, "Parent");
      this.uri = Objects.requireNonNull(in_uri, "URI");
    }

    public static PositionContext withParent(
      final PositionContext parent)
    {
      return new PositionContext(Optional.of(parent), parent.uri);
    }

    long offset()
    {
      return this.parent.map(p -> Long.valueOf(p.offset()))
        .orElse(Long.valueOf(0L)).longValue()
        + this.local_offset;
    }

    void increase(
      final long octets)
    {
      this.local_offset += octets;
    }

    public int localOffset()
    {
      return (int) this.local_offset;
    }
  }

  private static final class Reader implements Dx7SysExReaderType
  {
    private final URI uri;
    private final InputStream stream;
    private final TrackingErrorListener errors;
    private PositionContext offset;

    Reader(
      final Dx7ParseErrorListenerType in_errors,
      final URI in_uri,
      final InputStream in_stream)
    {
      this.uri = Objects.requireNonNull(in_uri, "URI");
      this.stream = Objects.requireNonNull(in_stream, "Stream");
      this.offset = new PositionContext(Optional.empty(), in_uri);
      this.errors = new TrackingErrorListener(in_errors);
    }

    private int readStreamByte()
      throws IOException
    {
      this.offset.increase(1L);
      final int x = this.stream.read();
      if (x == -1) {
        throw new IOException("Unexpected EOF");
      }
      return x & 0xff;
    }

    private void readSpecificByte(
      final int value)
      throws IOException
    {
      final int v_rec = this.readStreamByte();
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
        this.offset.offset(),
        Dx7ParseErrorType.Severity.ERROR,
        message,
        Optional.empty());
    }

    private Dx7ParseError parseWarning(
      final String message)
    {
      return Dx7ParseError.of(
        this.uri,
        this.offset.offset(),
        Dx7ParseErrorType.Severity.WARNING,
        message,
        Optional.empty());
    }

    private Dx7ParseError parseException(
      final Exception e)
    {
      return Dx7ParseError.of(
        this.uri,
        this.offset.offset(),
        Dx7ParseErrorType.Severity.ERROR,
        e.getMessage(),
        Optional.of(e));
    }

    @Override
    public Vector<Dx7VoiceNamed> parse()
    {
      try {
        this.errors.reset();
        this.readSpecificByte(0xf0);
        this.readSpecificByte(0x43);
        this.readSpecificByte(0x0);

        if (this.errors.errorsEncountered()) {
          return Vector.empty();
        }

        final int format = this.readStreamByte();
        return this.parseVoices(format);
      } catch (final IOException e) {
        this.errors.receiveError(this.parseException(e));
        return Vector.empty();
      }
    }

    private Vector<Dx7VoiceNamed> parseVoices(
      final int format)
      throws IOException
    {
      switch (format) {
        case 0x0: {
          return this.parseOneVoice();
        }
        case 0x9: {
          return this.parse32Voices();
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

    private Vector<Dx7VoiceNamed> parse32Voices()
      throws IOException
    {
      LOG.debug("parsing 32 voices");

      return new Voice32Parser(
        this.errors,
        this.stream,
        PositionContext.withParent(this.offset)).parse();
    }

    private Vector<Dx7VoiceNamed> parseOneVoice()
      throws IOException
    {
      LOG.debug("parsing 1 voice");
      return Vector.empty();
    }

    private static final class Voice128ByteOperatorParser
    {
      private final TrackingErrorListener errors;
      private final byte[] buffer;
      private final String voice_name;
      private final int op_index;
      private final PositionContext position;
      private final int voice_index;

      Voice128ByteOperatorParser(
        final TrackingErrorListener in_errors,
        final byte[] in_buffer,
        final int in_voice_index,
        final PositionContext in_position,
        final int in_op_index,
        final String in_name)
      {
        this.errors =
          Objects.requireNonNull(in_errors, "Errors");
        this.buffer =
          Objects.requireNonNull(in_buffer, "Buffer");
        this.position =
          Objects.requireNonNull(in_position, "Position");
        this.voice_name =
          Objects.requireNonNull(in_name, "Name");

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
            "check: [{}] [voice {} ({})] [op {}] {} {}",
            Long.valueOf(this.position.offset()),
            Integer.valueOf(this.voice_index),
            this.voice_name,
            Integer.valueOf(this.op_index),
            param_name,
            Integer.valueOf(param_value));
        }

        if (param_value >= param_min && param_value <= param_max) {
          return param_value;
        }

        this.errors.receiveError(Dx7ParseError.of(
          this.position.uri,
          this.position.offset(),
          Dx7ParseErrorType.Severity.ERROR,
          new StringBuilder(128)
            .append("Value out of range.")
            .append(System.lineSeparator())
            .append("  Byte offset: ")
            .append(this.position.offset())
            .append(System.lineSeparator())
            .append("  Voice:       ")
            .append(this.voice_index)
            .append(" (")
            .append(this.voice_name)
            .append(")")
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
      {
        Preconditions.checkPreconditionI(
          this.position.localOffset(),
          this.position.localOffset() == 0,
          n -> "Local offset should be zero");

        this.errors.reset();

        final Dx7Operator.Builder op_b = Dx7Operator.builder();
        op_b.setId(Dx7OperatorID.of(this.op_index));

        final int env_r1_rate =
          this.checkConstrainValueRange(
            this.buffer[this.position.localOffset()],
            "R1 Rate",
            0,
            99);
        this.position.increase(1L);

        final int env_r2_rate =
          this.checkConstrainValueRange(
            this.buffer[this.position.localOffset()],
            "R2 Rate",
            0,
            99);
        this.position.increase(1L);

        final int env_r3_rate =
          this.checkConstrainValueRange(
            this.buffer[this.position.localOffset()],
            "R3 Rate",
            0,
            99);
        this.position.increase(1L);

        final int env_r4_rate =
          this.checkConstrainValueRange(
            this.buffer[this.position.localOffset()],
            "R4 Rate",
            0,
            99);
        this.position.increase(1L);

        final int env_r1_level =
          this.checkConstrainValueRange(
            this.buffer[this.position.localOffset()],
            "R1 Level",
            0,
            99);
        this.position.increase(1L);

        final int env_r2_level =
          this.checkConstrainValueRange(
            this.buffer[this.position.localOffset()],
            "R2 Level",
            0,
            99);
        this.position.increase(1L);

        final int env_r3_level =
          this.checkConstrainValueRange(
            this.buffer[this.position.localOffset()],
            "R3 Level",
            0,
            99);
        this.position.increase(1L);

        final int env_r4_level =
          this.checkConstrainValueRange(
            this.buffer[this.position.localOffset()],
            "R4 Level",
            0,
            99);
        this.position.increase(1L);

        final int level_scaling_breakpoint =
          this.checkConstrainValueRange(
            this.buffer[this.position.localOffset()],
            "Level scaling breakpoint",
            0,
            99);
        this.position.increase(1L);

        final int level_scaling_left_depth =
          this.checkConstrainValueRange(
            this.buffer[this.position.localOffset()],
            "Level scaling left depth",
            0,
            99);
        this.position.increase(1L);

        final int level_scaling_right_depth =
          this.checkConstrainValueRange(
            this.buffer[this.position.localOffset()],
            "Level scaling right depth",
            0,
            99);
        this.position.increase(1L);

        final int scaling_curves_packed =
          this.buffer[this.position.localOffset()];
        this.position.increase(1L);

        final int left_curve_value =
          this.checkConstrainValueRange(
            scaling_curves_packed & 0b11,
            "Level scaling left curve",
            0,
            3);
        final int right_curve_value =
          this.checkConstrainValueRange(
            (scaling_curves_packed >> 2) & 0b11,
            "Level scaling right curve",
            0,
            3);

        final LevelScalingCurve left_curve =
          LevelScalingCurve.ofInteger(left_curve_value);
        final LevelScalingCurve right_curve =
          LevelScalingCurve.ofInteger(right_curve_value);

        final int detune_packed =
          this.buffer[this.position.localOffset()];
        final int detune_raw =
          (detune_packed & 0b01111000) >> 3;
        final int detune =
          this.checkConstrainValueRange(
            detune_raw - 7,
            "Oscillator detune",
            -7,
            7);
        this.position.increase(1L);

        final int sensitivity_packed =
          this.buffer[this.position.localOffset()];
        this.position.increase(1L);

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

        final int output_level =
          this.checkConstrainValueRange(
            this.buffer[this.position.localOffset()],
            "Output Level",
            0,
            99);
        this.position.increase(1L);

        final int osc_pack =
          this.buffer[this.position.localOffset()];
        this.position.increase(1L);

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
            this.buffer[this.position.localOffset()],
            "Oscillator frequency fine",
            0,
            99);
        this.position.increase(1L);

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
        op_b.setOscillatorFrequencyDetune(detune);
        op_b.setVelocitySensitivity(vel_sensitivity);
        op_b.setLfoAmplitudeModulationSensitivity(amp_mod_sensitivity);
        op_b.setOutputLevel(output_level);
        op_b.setOscillatorFrequencyCoarse(freq_coarse);
        op_b.setOscillatorMode(OscillatorMode.ofInteger(osc_mode));
        op_b.setOscillatorFrequencyFine(osc_frequency_fine);
        return Optional.of(op_b.build());
      }
    }

    private static final class Voice128ByteParser
    {
      private final TrackingErrorListener errors;
      private final byte[] buffer;
      private final int voice_index;
      private final PositionContext position;
      private String voice_name;

      Voice128ByteParser(
        final TrackingErrorListener in_errors,
        final byte[] in_buffer,
        final PositionContext in_position,
        final int in_voice_index)
      {
        this.errors = Objects.requireNonNull(in_errors, "Errors");
        this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
        this.position = Objects.requireNonNull(in_position, "Position");
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
            "check: [{}] [voice {} ({})] {} {}",
            Long.valueOf(this.position.offset()),
            Integer.valueOf(this.voice_index),
            this.voice_name,
            param_name,
            Integer.valueOf(param_value));
        }

        if (param_value >= param_min && param_value <= param_max) {
          return param_value;
        }

        this.errors.receiveError(Dx7ParseError.of(
          this.position.uri,
          this.position.offset(),
          Dx7ParseErrorType.Severity.ERROR,
          new StringBuilder(128)
            .append("Value out of range.")
            .append(System.lineSeparator())
            .append("  Byte offset: ")
            .append(this.position.offset())
            .append(System.lineSeparator())
            .append("  Voice:       ")
            .append(this.voice_index)
            .append(" (")
            .append(this.voice_name)
            .append(")")
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
      {
        final Dx7Voice.Builder vb = Dx7Voice.builder();

        this.errors.reset();

        final byte[] name = new byte[10];
        for (int index = 0; index < 10; ++index) {
          name[index] = this.buffer[118 + index];
        }
        this.voice_name = new String(name, StandardCharsets.US_ASCII);

        int valid_ops_processed = 0;
        for (int op_index = 6; op_index >= 1; --op_index) {
          final Optional<Dx7Operator> op_opt =
            new Voice128ByteOperatorParser(
              this.errors,
              this.buffer,
              this.voice_index,
              PositionContext.withParent(this.position),
              op_index,
              this.voice_name).parse();

          this.position.increase(17L);

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

        Invariants.checkInvariantI(
          this.position.localOffset(),
          this.position.localOffset() == 102,
          x -> "Must have consumed 102 octets");

        vb.setPitchEnvelopeR1Rate(
          this.checkConstrainValueRange(
            this.buffer[this.position.localOffset()],
            "Pitch Envelope R1 Rate",
            0,
            99));
        this.position.increase(1L);

        vb.setPitchEnvelopeR2Rate(
          this.checkConstrainValueRange(
            this.buffer[this.position.localOffset()],
            "Pitch Envelope R2 Rate",
            0,
            99));

        this.position.increase(1L);

        vb.setPitchEnvelopeR3Rate(
          this.checkConstrainValueRange(
            this.buffer[this.position.localOffset()],
            "Pitch Envelope R3 Rate",
            0,
            99));
        this.position.increase(1L);

        vb.setPitchEnvelopeR4Rate(
          this.checkConstrainValueRange(
            this.buffer[this.position.localOffset()],
            "Pitch Envelope R4 Rate",
            0,
            99));
        this.position.increase(1L);

        vb.setPitchEnvelopeR1Level(
          this.checkConstrainValueRange(
            this.buffer[this.position.localOffset()],
            "Pitch Envelope R1 Level",
            0,
            99));
        this.position.increase(1L);

        vb.setPitchEnvelopeR2Level(
          this.checkConstrainValueRange(
            this.buffer[this.position.localOffset()],
            "Pitch Envelope R2 Level",
            0,
            99));
        this.position.increase(1L);

        vb.setPitchEnvelopeR3Level(
          this.checkConstrainValueRange(
            this.buffer[this.position.localOffset()],
            "Pitch Envelope R3 Level",
            0,
            99));
        this.position.increase(1L);

        vb.setPitchEnvelopeR4Level(
          this.checkConstrainValueRange(
            this.buffer[this.position.localOffset()],
            "Pitch Envelope R4 Level",
            0,
            99));
        this.position.increase(1L);

        Invariants.checkInvariantI(
          this.position.localOffset(),
          this.position.localOffset() == 110,
          x -> "Must have consumed 110 octets");

        final int algo_raw =
          this.checkConstrainValueRange(
            this.buffer[this.position.localOffset()] & 0b11111,
            "Algorithm",
            0,
            31);

        vb.setAlgorithm(Dx7AlgorithmID.of(algo_raw + 1));

        //        vb.setFeedback(this.buffer[this.voice_offset + 111] & 0b111);
        //        vb.setLfoSpeed(this.buffer[this.voice_offset + 112]);
        //        vb.setLfoDelay(this.buffer[this.voice_offset + 113]);
        //        vb.setLfoPitchModulationDepth(this.buffer[this.voice_offset + 114]);
        //        vb.setLfoAmplitudeModulationDepth(this.buffer[this.voice_offset + 115]);
        //
        //        final int lfo_pack = this.buffer[this.voice_offset + 116];
        //        final int lpms = (lfo_pack & 0b0111_0000) >> 4;
        //        final int lwave = (lfo_pack & 0b0000_1110) >> 1;
        //        final int lsync = lfo_pack & 1;
        //
        //        vb.setLfoPitchModulationSensitivity(lpms);
        //        vb.setLfoWave(Dx7VoiceType.LFOWave.ofInteger(lwave));
        //        vb.setLfoKeySync(lsync == 1);
        //
        //        final int transpose_raw = this.buffer[this.voice_offset + 117];
        //        final int transpose = transpose_raw - 24;
        //        vb.setTranspose(transpose);

        if (this.errors.errorsEncountered() || valid_ops_processed != 6) {
          return Optional.empty();
        }

        return Optional.of(Dx7VoiceNamed.of(
          this.voice_name,
          vb.build(),
          Optional.empty()));
      }
    }

    private static final class Voice32Parser
    {
      private final TrackingErrorListener errors;
      private final InputStream stream;
      private final PositionContext offset;
      private int checksum;

      Voice32Parser(
        final TrackingErrorListener in_errors,
        final InputStream in_stream,
        final PositionContext in_offset)
      {
        this.errors = Objects.requireNonNull(in_errors, "Errors");
        this.stream = Objects.requireNonNull(in_stream, "Stream");
        this.offset = Objects.requireNonNull(in_offset, "Offset");
        this.checksum = 0;
      }

      private int readStreamByte()
        throws IOException
      {
        this.offset.increase(1L);
        final int x = this.stream.read();
        if (x == -1) {
          throw new IOException("Unexpected EOF");
        }
        return x & 0xff;
      }

      private void checksumAdd(
        final byte[] data)
      {
        for (int index = 0; index < data.length; ++index) {
          this.checksum = (this.checksum - data[index]) & 0xff;
        }
      }

      private int checksumFinish()
      {
        return this.checksum & 0x7f;
      }

      private int readSize()
        throws IOException
      {
        final int r0 = this.readStreamByte();
        final int r1 = this.readStreamByte();
        final int ms = r0 << 7;
        final int cmb = ms | r1;
        return cmb;
      }

      public Vector<Dx7VoiceNamed> parse()
        throws IOException
      {
        final int size = this.readSize();
        LOG.trace("expected size: {} octets", Integer.valueOf(size));
        final int expected_voices = size / 128;
        LOG.trace("expecting {} voices", Integer.valueOf(expected_voices));

        Vector<Dx7VoiceNamed> voices = Vector.empty();
        final byte[] buffer = new byte[128];
        for (int voice_index = 0; voice_index < expected_voices; ++voice_index) {
          final int received = this.stream.readNBytes(buffer, 0, 128);
          LOG.trace("read: {} octets", Integer.valueOf(received));
          if (received < 128) {
            this.errors.receiveError(Dx7ParseError.of(
              this.offset.uri,
              this.offset.offset(),
              Dx7ParseErrorType.Severity.ERROR,
              new StringBuilder(128)
                .append("Unexpected EOF")
                .append(System.lineSeparator())
                .append("  Expected: ")
                .append(128)
                .append(" bytes starting at offset ")
                .append(this.offset.offset())
                .append(System.lineSeparator())
                .append("  Received: ")
                .append(received)
                .append(System.lineSeparator())
                .toString(),
              Optional.empty()));
          }

          final Optional<Dx7VoiceNamed> voice_opt =
            new Voice128ByteParser(
              this.errors,
              buffer,
              PositionContext.withParent(this.offset),
              voice_index)
              .parse();

          if (voice_opt.isPresent()) {
            voices = voices.append(voice_opt.get());
          }

          this.checksumAdd(buffer);
          this.offset.increase(Integer.toUnsignedLong(received));
        }

        final int checksum_expect = this.readStreamByte();
        final int checksum_received = this.checksumFinish();
        LOG.trace(
          "expected checksum: {}",
          Integer.toUnsignedString(checksum_expect, 16));
        LOG.trace(
          "received checksum: {}",
          Integer.toUnsignedString(checksum_received, 16));

        return voices;
      }
    }
  }
}
