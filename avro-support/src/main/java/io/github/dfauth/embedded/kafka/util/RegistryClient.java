package io.github.dfauth.embedded.kafka.util;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class RegistryClient {

    public static Stream<Class<?>> streamClasses(InputStream stream) {
        try {
            List<Class<?>> tmp = new ArrayList<>();
            JarInputStream jis = new JarInputStream(stream);
            JarEntry je;
            while((je = jis.getNextJarEntry()) != null) {
                if(je.getName().endsWith(".class")) {
                    Class<?> clazz = classLoader(jis.readAllBytes()).loadClass(Stream.of(je.getName().split("\\.")[0].split("/")).collect(Collectors.joining(".")));
                    tmp.add(clazz);
                }
            }
            return tmp.stream();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private static ClassLoader classLoader(byte[] bytes) {
        return new ClassLoader(){
            @Override
            public InputStream getResourceAsStream(String name) {
                return new ByteArrayInputStream(bytes);
            }
        };
    }
}
