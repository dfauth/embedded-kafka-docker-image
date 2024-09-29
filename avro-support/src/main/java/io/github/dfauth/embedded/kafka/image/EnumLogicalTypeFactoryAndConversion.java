package io.github.dfauth.embedded.kafka.image;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Conversion;
import org.apache.avro.LogicalType;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;

import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;

@Slf4j
public class EnumLogicalTypeFactoryAndConversion<E extends Enum<E>> extends Conversion<E> implements LogicalTypes.LogicalTypeFactory {

    private Class<E> classOfE;

    public EnumLogicalTypeFactoryAndConversion(Class<E> classOfE) {
        this.classOfE = classOfE;
    }

    @Override
    public LogicalType fromSchema(Schema schema) {
        return new EnumLogicalType(classOfE.getName());
    }

    @Override
    public String getTypeName() {
        return classOfE.getName();
    }

    @Override
    public Class<E> getConvertedType() {
        return classOfE;
    }

    @Override
    public String getLogicalTypeName() {
        return classOfE.getName();
    }

    @Override
    public E fromInt(Integer value, Schema schema, LogicalType type) {
        return valueStream()
                .filter(e -> e.ordinal() == value)
                .findFirst()
                .orElseGet(() -> valueStream()
                        .filter(e -> "UNKNOWN".equalsIgnoreCase(e.name()))
                        .findFirst()
                        .orElseThrow());
    }

    private Stream<E> valueStream() {
        try {
            return Stream.of((E[])classOfE.getMethod("values").invoke(classOfE));
        } catch (IllegalAccessException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Integer toInt(E value, Schema schema, LogicalType type) {
        return value.ordinal();
    }
}
