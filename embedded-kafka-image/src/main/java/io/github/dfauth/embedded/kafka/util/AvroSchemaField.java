package io.github.dfauth.embedded.kafka.util;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class AvroSchemaField {
    private String name;
    private Either<String,AvroSchemaFieldType> type;
}
