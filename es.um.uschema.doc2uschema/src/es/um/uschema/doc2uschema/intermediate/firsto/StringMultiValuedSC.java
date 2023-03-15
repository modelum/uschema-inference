package es.um.uschema.doc2uschema.intermediate.firsto;

import java.util.HashSet;
import java.util.Set;

import es.um.uschema.doc2uschema.intermediate.raw.StringSC;

/**
 * @author dsevilla
 *
 */
public class StringMultiValuedSC extends StringSC implements MultiValued
{
  Set<String> values;

  public StringMultiValuedSC(String string)
  {
    super();
    values = new HashSet<String>();
  }

  @Override
  public Set<String> getValues()
  {
    return values;
  }
}
