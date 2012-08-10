
package semtex.archery.entities.data.reports;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;


public class KeyValueSerializer extends JsonSerializer<Map<Integer, Integer>> {

  @Override
  public void serialize(final Map<Integer, Integer> value, final JsonGenerator jgen, final SerializerProvider provider)
      throws IOException, JsonProcessingException {
    jgen.writeStartArray();
    final Set<Integer> keySet = value.keySet();

    final Iterator<Integer> iterator = keySet.iterator();
    while (iterator.hasNext()) {
      final Integer keyValue = iterator.next();
      jgen.writeStartArray();
      jgen.writeNumber(keyValue);
      jgen.writeNumber(value.get(keyValue));
      jgen.writeEndArray();
    }

    jgen.writeEndArray();
  }

}
