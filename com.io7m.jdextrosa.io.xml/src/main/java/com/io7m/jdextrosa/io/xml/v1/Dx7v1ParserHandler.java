package com.io7m.jdextrosa.io.xml.v1;

import com.io7m.jdextrosa.core.Dx7AlgorithmID;
import com.io7m.jdextrosa.core.Dx7Operator;
import com.io7m.jdextrosa.core.Dx7OperatorID;
import com.io7m.jdextrosa.core.Dx7Voice;
import com.io7m.jdextrosa.core.Dx7VoiceMetadata;
import com.io7m.jdextrosa.core.Dx7VoiceNamed;
import com.io7m.jdextrosa.io.xml.spi.Dx7XMLContentHandlerType;
import com.io7m.jdextrosa.io.xml.spi.Dx7XMLErrorLog;
import com.io7m.jdextrosa.io.xml.spi.Dx7XMLParserRequest;
import com.io7m.junreachable.UnreachableCodeException;
import io.vavr.collection.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.ext.Locator2;

import java.net.URI;
import java.util.Objects;

import static com.io7m.jdextrosa.core.Dx7OperatorType.LevelScalingCurve;
import static com.io7m.jdextrosa.core.Dx7OperatorType.OscillatorMode;
import static com.io7m.jdextrosa.core.Dx7VoiceType.LFOWave;
import static java.lang.Integer.parseInt;
import static java.lang.Integer.parseUnsignedInt;

final class Dx7v1ParserHandler
  extends DefaultHandler2 implements Dx7XMLContentHandlerType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(Dx7v1ParserHandler.class);

  private final Dx7XMLParserRequest request;
  private final Dx7XMLErrorLog errors;
  private Dx7Voice.Builder voice_builder;
  private Locator2 locator;
  private Vector<Dx7VoiceNamed> voices;
  private Dx7VoiceNamed.Builder voice_named_builder;
  private Dx7Operator.Builder op_builder;
  private Dx7VoiceMetadata.Builder voice_meta_builder;

  /**
   * Construct a handler.
   *
   * @param in_request The original request
   * @param in_errors  The error log
   * @param in_locator The current locator
   */

  Dx7v1ParserHandler(
    final Dx7XMLParserRequest in_request,
    final Dx7XMLErrorLog in_errors,
    final Locator2 in_locator)
  {
    this.request = Objects.requireNonNull(in_request, "Request");
    this.errors = Objects.requireNonNull(in_errors, "Errors");
    this.locator = Objects.requireNonNull(in_locator, "Locator");
    this.voices = Vector.empty();
  }

  private static LFOWave waveform(
    final String value)
  {
    switch (value) {
      case "triangle":
        return LFOWave.LFO_TRIANGLE;
      case "sawDown":
        return LFOWave.LFO_SAW_DOWN;
      case "sawUp":
        return LFOWave.LFO_SAW_UP;
      case "square":
        return LFOWave.LFO_SQUARE;
      case "sine":
        return LFOWave.LFO_SINE;
      case "sampleAndHold":
        return LFOWave.LFO_SAMPLE_HOLD;
      default:
        throw new UnreachableCodeException();
    }
  }

  private static LevelScalingCurve curve(
    final String value)
  {
    switch (value) {
      case "linearNegative":
        return LevelScalingCurve.LINEAR_NEGATIVE;
      case "linearPositive":
        return LevelScalingCurve.LINEAR_POSITIVE;
      case "exponentialNegative":
        return LevelScalingCurve.EXPONENTIAL_NEGATIVE;
      case "exponentialPositive":
        return LevelScalingCurve.EXPONENTIAL_POSITIVE;
      default:
        throw new UnreachableCodeException();
    }
  }

  @Override
  public void startElement(
    final String uri,
    final String local_name,
    final String qual_name,
    final Attributes attributes)
    throws SAXException
  {
    LOG.trace(
      "startElement: {} {} {} {}",
      uri,
      local_name,
      qual_name,
      attributes);

    if (!this.errors.errors().isEmpty()) {
      return;
    }

    if (!Objects.equals(uri, Dx7XMLv1FormatProvider.SCHEMA_NAMESPACE)) {
      return;
    }

    switch (local_name) {
      case "dx7-voices": {
        break;
      }

      case "dx7-voice": {
        this.startVoice(attributes);
        break;
      }

      case "dx7-voice-metadata": {
        this.startVoiceMetadata(attributes);
        break;
      }

      case "dx7-lfo": {
        this.startVoiceLFO(attributes);
        break;
      }

      case "dx7-operator": {
        this.startOperator(attributes);
        break;
      }

      default: {
        break;
      }
    }
  }

  private void startVoiceLFO(
    final Attributes attributes)
  {
    for (int index = 0; index < attributes.getLength(); ++index) {
      final String value = attributes.getValue(index);
      final String name = attributes.getLocalName(index);
      switch (name) {
        case "pitchModulationDepth": {
          this.voice_builder.setLfoPitchModulationDepth(
            parseUnsignedInt(value));
          break;
        }
        case "pitchModulationSensitivity": {
          this.voice_builder.setLfoPitchModulationSensitivity(
            parseUnsignedInt(value));
          break;
        }
        case "amplitudeModulationDepth": {
          this.voice_builder.setLfoAmplitudeModulationDepth(
            parseUnsignedInt(value));
          break;
        }
        case "rate": {
          this.voice_builder.setLfoSpeed(parseUnsignedInt(value));
          break;
        }
        case "delay": {
          this.voice_builder.setLfoDelay(parseUnsignedInt(value));
          break;
        }
        case "keySynchronize": {
          this.voice_builder.setLfoKeySync(Boolean.parseBoolean(value));
          break;
        }
        case "waveform": {
          this.voice_builder.setLfoWave(waveform(value));
          break;
        }

        default: {
          throw new UnreachableCodeException(
            new IllegalArgumentException(name));
        }
      }
    }
  }

  private void startVoiceMetadata(
    final Attributes attributes)
  {
    this.voice_meta_builder = Dx7VoiceMetadata.builder();

    for (int index = 0; index < attributes.getLength(); ++index) {
      final String value = attributes.getValue(index);
      final String name = attributes.getLocalName(index);
      switch (name) {
        case "id": {
          this.voice_meta_builder.setId(URI.create(value));
          break;
        }
        case "source": {
          this.voice_meta_builder.setSource(URI.create(value));
          break;
        }

        default: {
          throw new UnreachableCodeException(
            new IllegalArgumentException(name));
        }
      }
    }
  }

  private void startOperator(
    final Attributes attributes)
  {
    this.op_builder = Dx7Operator.builder();

    for (int index = 0; index < attributes.getLength(); ++index) {
      final String value = attributes.getValue(index);
      final String name = attributes.getLocalName(index);
      switch (name) {

        case "id": {
          this.op_builder.setId(Dx7OperatorID.of(parseUnsignedInt(value)));
          break;
        }
        case "enabled": {
          this.op_builder.setEnabled(Boolean.parseBoolean(value));
          break;
        }

        case "frequencyCoarse": {
          this.op_builder.setOscillatorFrequencyCoarse(parseUnsignedInt(value));
          break;
        }
        case "frequencyFine": {
          this.op_builder.setOscillatorFrequencyFine(parseUnsignedInt(value));
          break;
        }
        case "frequencyDetune": {
          this.op_builder.setOscillatorFrequencyDetune(parseInt(value));
          break;
        }

        case "mode": {
          this.op_builder.setOscillatorMode(OscillatorMode.valueOf(value.toUpperCase()));
          break;
        }
        case "output": {
          this.op_builder.setOutputLevel(parseUnsignedInt(value));
          break;
        }

        case "velocitySensitivity": {
          this.op_builder.setVelocitySensitivity(parseUnsignedInt(value));
          break;
        }
        case "lfoAmplitudeModulationSensitivity": {
          this.op_builder.setLfoAmplitudeModulationSensitivity(parseUnsignedInt(
            value));
          break;
        }
        case "levelScalingBreakpoint": {
          this.op_builder.setLevelScalingBreakpoint(parseUnsignedInt(value));
          break;
        }
        case "levelScalingLeftDepth": {
          this.op_builder.setLevelScalingLeftDepth(parseUnsignedInt(value));
          break;
        }
        case "levelScalingRightDepth": {
          this.op_builder.setLevelScalingRightDepth(parseUnsignedInt(value));
          break;
        }
        case "levelScalingLeftCurve": {
          this.op_builder.setLevelScalingLeftCurve(curve(value));
          break;
        }
        case "levelScalingRightCurve": {
          this.op_builder.setLevelScalingRightCurve(curve(value));
          break;
        }

        case "envelopeR1Level": {
          this.op_builder.setEnvelopeR1Level(parseUnsignedInt(value));
          break;
        }
        case "envelopeR2Level": {
          this.op_builder.setEnvelopeR2Level(parseUnsignedInt(value));
          break;
        }
        case "envelopeR3Level": {
          this.op_builder.setEnvelopeR3Level(parseUnsignedInt(value));
          break;
        }
        case "envelopeR4Level": {
          this.op_builder.setEnvelopeR4Level(parseUnsignedInt(value));
          break;
        }

        case "envelopeR1Rate": {
          this.op_builder.setEnvelopeR1Rate(parseUnsignedInt(value));
          break;
        }
        case "envelopeR2Rate": {
          this.op_builder.setEnvelopeR2Rate(parseUnsignedInt(value));
          break;
        }
        case "envelopeR3Rate": {
          this.op_builder.setEnvelopeR3Rate(parseUnsignedInt(value));
          break;
        }
        case "envelopeR4Rate": {
          this.op_builder.setEnvelopeR4Rate(parseUnsignedInt(value));
          break;
        }

        default: {
          throw new UnreachableCodeException(
            new IllegalArgumentException(name));
        }
      }
    }
  }

  private void startVoice(
    final Attributes attributes)
  {
    this.voice_builder = Dx7Voice.builder();
    this.voice_named_builder = Dx7VoiceNamed.builder();

    for (int index = 0; index < attributes.getLength(); ++index) {
      final String value = attributes.getValue(index);
      switch (attributes.getLocalName(index)) {

        case "name": {
          this.voice_named_builder.setName(value);
          break;
        }

        case "algorithm": {
          this.voice_builder.setAlgorithm(
            Dx7AlgorithmID.of(parseUnsignedInt(value)));
          break;
        }

        case "feedback": {
          this.voice_builder.setFeedback(parseUnsignedInt(value));
          break;
        }

        case "transpose": {
          this.voice_builder.setTranspose(parseInt(value));
          break;
        }

        case "pitchEnvelopeR1Level": {
          this.voice_builder.setPitchEnvelopeR1Level(parseUnsignedInt(value));
          break;
        }
        case "pitchEnvelopeR2Level": {
          this.voice_builder.setPitchEnvelopeR2Level(parseUnsignedInt(value));
          break;
        }
        case "pitchEnvelopeR3Level": {
          this.voice_builder.setPitchEnvelopeR3Level(parseUnsignedInt(value));
          break;
        }
        case "pitchEnvelopeR4Level": {
          this.voice_builder.setPitchEnvelopeR4Level(parseUnsignedInt(value));
          break;
        }

        case "pitchEnvelopeR1Rate": {
          this.voice_builder.setPitchEnvelopeR1Rate(parseUnsignedInt(value));
          break;
        }
        case "pitchEnvelopeR2Rate": {
          this.voice_builder.setPitchEnvelopeR2Rate(parseUnsignedInt(value));
          break;
        }
        case "pitchEnvelopeR3Rate": {
          this.voice_builder.setPitchEnvelopeR3Rate(parseUnsignedInt(value));
          break;
        }
        case "pitchEnvelopeR4Rate": {
          this.voice_builder.setPitchEnvelopeR4Rate(parseUnsignedInt(value));
          break;
        }

        default: {
          throw new UnreachableCodeException();
        }
      }
    }
  }

  @Override
  public void endElement(
    final String uri,
    final String local_name,
    final String qual_name)
    throws SAXException
  {
    LOG.trace("endElement: {} {} {}", uri, local_name, qual_name);

    if (!this.errors.errors().isEmpty()) {
      return;
    }

    if (!Objects.equals(uri, Dx7XMLv1FormatProvider.SCHEMA_NAMESPACE)) {
      return;
    }

    switch (local_name) {
      case "dx7-voices": {
        break;
      }

      case "dx7-voice": {
        this.finishVoice();
        break;
      }

      case "dx7-voice-metadata": {
        this.finishVoiceMetadata();
        break;
      }

      case "dx7-lfo": {
        break;
      }

      case "dx7-operator": {
        this.finishOperator();
        break;
      }

      default: {
        break;
      }
    }
  }

  private void finishVoiceMetadata()
  {
    this.voice_named_builder.setMetadata(this.voice_meta_builder.build());
  }

  private void finishOperator()
  {
    final Dx7Operator op = this.op_builder.build();
    switch (op.id().id()) {
      case 1:
        this.voice_builder.setOperator1(op);
        break;
      case 2:
        this.voice_builder.setOperator2(op);
        break;
      case 3:
        this.voice_builder.setOperator3(op);
        break;
      case 4:
        this.voice_builder.setOperator4(op);
        break;
      case 5:
        this.voice_builder.setOperator5(op);
        break;
      case 6:
        this.voice_builder.setOperator6(op);
        break;
      default:
        throw new UnreachableCodeException();
    }
  }

  private void finishVoice()
  {
    this.voice_named_builder.setVoice(this.voice_builder.build());
    this.voices = this.voices.append(this.voice_named_builder.build());
  }

  @Override
  public Dx7XMLErrorLog errorLog()
  {
    return this.errors;
  }

  @Override
  public Vector<Dx7VoiceNamed> content()
    throws SAXParseException
  {
    return this.voices;
  }
}
