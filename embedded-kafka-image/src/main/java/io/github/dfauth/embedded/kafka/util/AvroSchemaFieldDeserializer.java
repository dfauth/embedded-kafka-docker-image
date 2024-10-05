package io.github.dfauth.embedded.kafka.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class AvroSchemaFieldDeserializer extends JsonDeserializer<List<AvroSchemaField>> {

    @Override
    public List<AvroSchemaField> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        try {
            AvroSchemaField avroSchemaField = new AvroSchemaField();
            // name:String , type: Either<String,AvroSchemaFielType>, logicalType: String
//            while(jsonParser.hasCurrentToken()) {
//                String fieldName = jsonParser.nextFieldName();
//                if
//            }
            return List.of(avroSchemaField);
//        } catch (IOException e) {
//            log.error(e.getMessage(), e);
//            throw new RuntimeException(e);
        } finally {
        }
    }
}
