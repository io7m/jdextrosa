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

package com.io7m.jdextrosa.transforms;

import com.io7m.jdextrosa.core.Dx7AlgorithmOperators;
import com.io7m.jdextrosa.core.Dx7Operator;
import com.io7m.jdextrosa.core.Dx7OperatorID;
import com.io7m.jdextrosa.core.Dx7Voice;
import com.io7m.junreachable.UnreachableCodeException;
import io.vavr.collection.SortedSet;

import java.util.Objects;

/**
 * Attempt to make a voice staccato by increasing the attack and release rates
 * of the carriers.
 */

public final class Dx7Staccato
{
  private final Dx7Voice voice;
  private final Dx7StaccatoParameters parameters;
  private final SortedSet<Dx7OperatorID> carriers;
  private final SortedSet<Dx7OperatorID> modulators;

  private Dx7Staccato(
    final Dx7Voice in_voice,
    final Dx7StaccatoParameters in_parameters)
  {
    this.voice =
      Objects.requireNonNull(in_voice, "Voice");
    this.parameters =
      Objects.requireNonNull(in_parameters, "Parameters");
    this.carriers =
      Dx7AlgorithmOperators.carriers(in_voice.algorithm());
    this.modulators =
      Dx7AlgorithmOperators.modulators(in_voice.algorithm());
  }

  /**
   * Apply the staccato function.
   *
   * @return A modified voice
   */

  public Dx7Voice apply()
  {
    final Dx7Operator op1 =
      this.staccatoOp(this.voice.operator1());
    final Dx7Operator op2 =
      this.staccatoOp(this.voice.operator2());
    final Dx7Operator op3 =
      this.staccatoOp(this.voice.operator3());
    final Dx7Operator op4 =
      this.staccatoOp(this.voice.operator4());
    final Dx7Operator op5 =
      this.staccatoOp(this.voice.operator5());
    final Dx7Operator op6 =
      this.staccatoOp(this.voice.operator6());

    return Dx7Voice.builder()
      .from(this.voice)
      .setOperator1(op1)
      .setOperator2(op2)
      .setOperator3(op3)
      .setOperator4(op4)
      .setOperator5(op5)
      .setOperator6(op6)
      .build();
  }

  private Dx7Operator staccatoOp(
    final Dx7Operator op)
  {
    switch (this.parameters.affect()) {
      case AFFECT_CARRIERS: {
        if (this.isCarrier(op)) {
          return this.staccatoOpActual(op);
        }
        return op;
      }
      case AFFECT_MODULATORS: {
        if (this.isModulator(op)) {
          return this.staccatoOpActual(op);
        }
        return op;
      }
      case AFFECT_ALL: {
        return this.staccatoOpActual(op);
      }
    }
    throw new UnreachableCodeException();
  }

  private boolean isModulator(
    final Dx7Operator op)
  {
    return this.modulators.contains(op.id());
  }

  private Dx7Operator staccatoOpActual(
    final Dx7Operator op)
  {
    return Dx7Operator.builder()
      .from(op)
      .setEnvelopeR1Rate(this.parameters.modifyAttack() ? 99 : op.envelopeR1Rate())
      .setEnvelopeR4Rate(this.parameters.modifyRelease() ? 99 : op.envelopeR4Rate())
      .build();
  }

  private boolean isCarrier(
    final Dx7Operator op)
  {
    return this.carriers.contains(op.id());
  }
}
