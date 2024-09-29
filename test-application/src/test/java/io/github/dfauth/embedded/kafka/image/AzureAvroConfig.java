package io.github.dfauth.embedded.kafka.image;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.SchemaRegistryClientBuilder;
import com.azure.data.schemaregistry.apacheavro.SchemaRegistryApacheAvroSerializer;
import com.azure.data.schemaregistry.apacheavro.SchemaRegistryApacheAvroSerializerBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;
import reactor.netty.http.Http11SslContextSpec;
import reactor.netty.resources.ConnectionProvider;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

@Slf4j
@Configuration
public class AzureAvroConfig {

    @Value("${schema.registry.endpoint:127.0.0.1}")
    private String schemaRegistryEndpoint;
    @Value("${schema.group:schemaGroup}")
    private String schemaGroup;
//    private reactor.netty.http.client.HttpClient nettyHttpClient = reactor.netty.http.client.HttpClient.create().noSSL();
//
//    private HttpClient httpClient = new NettyAsyncHttpClientBuilder(nettyHttpClient).build();

    @Bean
    public SchemaRegistryApacheAvroSerializer avroDeserializer(SchemaRegistryAsyncClient schemaRegistryAsyncClient) {
        return new SchemaRegistryApacheAvroSerializerBuilder()
                .schemaRegistryClient(schemaRegistryAsyncClient)
                .schemaGroup(schemaGroup)
                .buildSerializer();
    }

    @Bean
    public HttpClient httpClient() {
        ConnectionProvider connectionProvider = ConnectionProvider.builder("fred")
                .build();
        reactor.netty.http.client.HttpClient nettyHttpClient = reactor.netty.http.client.HttpClient.create(connectionProvider);

        Http11SslContextSpec http11SslContextSpec =
                Http11SslContextSpec.forClient()
                        .configure(builder -> builder.trustManager(InsecureTrustManagerFactory.INSTANCE));
        nettyHttpClient.secure(spec -> {
            spec.sslContext(http11SslContextSpec);
        });
        return new NettyAsyncHttpClientBuilder(nettyHttpClient).build();
    }

    @Bean
    public SchemaRegistryAsyncClient schemaAregistryClient(HttpClient httpClient) {
        TokenCredential tokenCredential = ctx -> Mono.just(new AccessToken("", OffsetDateTime.of(LocalDateTime.now().plus(1l, ChronoUnit.MINUTES), ZoneOffset.UTC)));
        //new DefaultAzureCredentialBuilder().build();

        // {schema-registry-endpoint} is the fully qualified namespace of the Event Hubs instance. It is usually
        // of the form "{your-namespace}.servicebus.windows.net"
        SchemaRegistryAsyncClient asyncClient = new SchemaRegistryClientBuilder()
                .fullyQualifiedNamespace(schemaRegistryEndpoint)
                .credential(tokenCredential)
                .httpClient(httpClient)
                .buildAsyncClient();
        return asyncClient;
    }

    @Bean
    public Receiver<io.github.dfauth.embedded.kafka.image.test.User> receiverBean() {
        return new Receiver<>();
    }

}
