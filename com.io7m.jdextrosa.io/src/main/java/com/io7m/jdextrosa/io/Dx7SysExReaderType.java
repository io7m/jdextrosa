package com.io7m.jdextrosa.io;

import com.io7m.jdextrosa.core.Dx7VoiceNamed;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import io.vavr.control.Validation;

public interface Dx7SysExReaderType
{
  Vector<Dx7VoiceNamed> parse();
}
