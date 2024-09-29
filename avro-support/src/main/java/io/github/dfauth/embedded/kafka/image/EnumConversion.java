package io.github.dfauth.embedded.kafka.image;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Conversion;
import org.apache.avro.LogicalType;
import org.apache.avro.Schema;

import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;

@Slf4j
public class EnumConversion<E extends Enum<E>> extends Conversion<E> {

    private final Class<E> classOfE;
    private final String name;

    public EnumConversion(Class<E> classOfE) {
        this.classOfE = classOfE;
        this.name = classOfE.getName();
    }

    @Override
    public Class<E> getConvertedType() {
        return classOfE;
    }

    @Override
    public String getLogicalTypeName() {
        return name;
    }

    @Override
    public E fromInt(Integer value, Schema schema, LogicalType type) {
        return valueStream().findFirst().orElseThrow();
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
