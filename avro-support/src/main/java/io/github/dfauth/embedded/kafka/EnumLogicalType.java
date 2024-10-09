package io.github.dfauth.embedded.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.LogicalType;
import org.apache.avro.Schema;

@Slf4j
public class EnumLogicalType extends LogicalType {

    public EnumLogicalType(String name) {
        super(name);
    }

    @Override
    public void validate(Schema schema) {
        super.validate(schema);
        if(schema.getType() != Schema.Type.INT) {
            throw new IllegalArgumentException("Logical type "+ getName()+" must be implemented by an int");
        }
    }
}
