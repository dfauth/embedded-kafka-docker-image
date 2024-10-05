package io.github.dfauth.embedded.kafka.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;

@Slf4j
//@Configuration
public class CustomDeserializerConfig {

    @Bean
    public Module avroSchemaFieldDeserializer() {
        SimpleModule module = new SimpleModule();
//        module.addDeserializer(List.class, new AvroSchemaFieldDeserializer());
        return module;
    }
}
