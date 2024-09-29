package io.github.dfauth.embedded.kafka.image;

import org.apache.avro.LogicalType;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;

public class EnumLogicalTypeFactory implements LogicalTypes.LogicalTypeFactory {

    private String name;

    public <E extends Enum<E>> EnumLogicalTypeFactory(Class<E> classOfE) {
        this.name = classOfE.getName();
    }

    @Override
    public LogicalType fromSchema(Schema schema) {
        return new EnumLogicalType(name);
    }

    @Override
    public String getTypeName() {
        return name;
    }
}
