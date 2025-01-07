package io.github.dfauth.embedded.kafka.controllers;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@Slf4j
@RestController
public class SchemaRegistryController {

    private long schemaCnt = 1;
    private final Map<Long, MySchema> schemas = new HashMap<>();

    private static final ObjectMapper mapper = new ObjectMapper();

    @RequestMapping(value="**",method = {GET,HEAD,POST,PUT,PATCH,DELETE,OPTIONS,TRACE})
    public ResponseEntity<Void> all(RequestEntity<Object> request){
        log.info("request: {}",request);
        log.info("request body: {}",request.getBody());
        return new ResponseEntity(OK);
    }

//    @PostMapping(value = "search/versions", consumes = {"application/json"}, produces = {"application/json"})
//    public ResponseEntity<VersionSearchResults> search(
//            @RequestParam("order") String order,
//            @RequestParam("orderby") String orderby,
//            @RequestParam("groupId") String groupId,
//            @RequestParam("artifactId") String artifactId,
//            @RequestParam("canonical") Boolean canonical,
//            @RequestParam("artifactType") String artifactType,
//            @RequestBody String schema) {
//        log.info("WOOZ search: {}",schema);
//        List<SearchedVersion> result = search(schema).map(Map.Entry::getValue).map(SearchedVersion::new).map(List::of).orElse(List.of());
//        return new ResponseEntity(new VersionSearchResults(result.size(), result), OK);
//    }

    private Optional<Map.Entry<Long, MySchema>> search(
            @RequestBody String schema) {
        return schemas.entrySet().stream().filter(s -> s.getValue().equals(schema)).findFirst();
    }

    private Function<Object, Optional<Map.Entry<Long, MySchema>>> search() {
        return schema -> schemas.entrySet().stream().filter(s -> s.getValue().equals(schema)).findFirst();
    }

    // groups/default/artifacts?ifExists=FIND_OR_CREATE_VERSION&canonical=false
    // see https://raw.githubusercontent.com/Apicurio/apicurio-registry/2.6.x/common/src/main/resources/META-INF/openapi.json
    @PostMapping(value = "/apis/registry/v2/groups/{groupId}/artifacts", consumes = {"application/json"}, produces = {"application/json"})
    public ResponseEntity<ArtifactMetaData> register(
            @PathVariable("groupId") String groupId,
            @RequestParam("ifExists") Action ifExists,
            @RequestParam("canonical") Boolean canonical,
            @RequestBody MySchema schema) {
        log.info("WOOZ register: {}", schema);
        return Optional.ofNullable(schema).flatMap(search()).map(e -> {
            return new ResponseEntity<>(new ArtifactMetaData(e.getKey(),String.format("%s.%s-value",schema.namespace, schema.name), schema.name), OK);
        }).orElseGet(() -> {
            var cnt = schemaCnt++;
            schemas.put(cnt, schema);
            return new ResponseEntity<>(new ArtifactMetaData(cnt, String.format("%s.%s-value",schema.namespace, schema.name), schema.name), OK);
        });
    }

    // http://localhost:8080/apis/registry/v2/ids/globalIds/0
    @GetMapping(value = "/apis/registry/v2/ids/globalIds/{id}", consumes = {"application/json"}, produces = {"application/json"})
    public Optional<MySchema> ids(
            @PathVariable("id") Long id,
            @RequestParam("dereference") Boolean dereference) {
        log.info("WOOZ ids: {}", id);
        return Optional.ofNullable(schemas.get(id));
    }

    // http://localhost:8080/apis/registry/v2/ids/globalIds/0/references
    @GetMapping(value = "/apis/registry/v2/ids/globalIds/{id}/references", consumes = {"application/json"}, produces = {"application/json"})
    public List<MySchema> idsReferences(
            @PathVariable("id") Long id) {
        log.info("WOOZ ids/references: {}", id);
        return Optional.ofNullable(schemas.get(id)).map(s -> List.<MySchema>of()).orElse(List.of());
    }

    @Data
    @AllArgsConstructor
    public static class ArtifactMetaData {
        private Long contentId;
        private String createdBy = "";
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ssZ") // 2025-01-07T00:58:45+0000
        private Date createdOn = new Date();
        private Long globalId;
        private String id;
        private List<String> labels = new ArrayList();
        private String modifiedBy = "";
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ssZ") // 2025-01-07T00:58:45+0000
        private Date modifiedOn = new Date();
        private String name;
        private List<ArtifactReference> references = new ArrayList();
        private ArtifactState state = ArtifactState.ENABLED;
        private String type = "AVRO";
        private String version;

        // {"contentId":1,"createdBy":"","createdOn":"2025-01-07T00:58:45+0000","globalId":1,
        // "id":"io.github.dfauth.embedded.kafka.test.User-value",
        // "modifiedBy":"","modifiedOn":"2025-01-07T00:58:45+0000",
        // "name":"User",
        // "references":[],
        // "state":"ENABLED",
        // "type":"AVRO",
        // "version":"1"}
        public ArtifactMetaData(long globalId, String key, String name) {
            this.globalId = globalId;
            this.contentId = globalId;
            this.version = String.valueOf(globalId);
            this.id = key;
            this.name = name;
        }
    }

    @Data
    public static class ArtifactReference {
        private String groupId;
        private String artifactId;
        private String version;
        private Long globalId;
        private Long contentId;
        private String contentHash;
    }

    public enum ArtifactState {
        ENABLED,
        DISABLED,
        DEPRECATED
    }

    @Data
    @AllArgsConstructor
    public static class VersionMetaData {

        private final String version;
        private final String name;
        private final String description;
        private final String owner;
        private final OffsetDateTime createdOn;
        private final ArtifactType artifactType;
        private final Long globalId;
        private final VersionState state;
        private final String labels;
        private final String groupId;
        private final Long contentId;
        private final String artifactId;
        private final Map<String, Object> additionalData;

        public VersionMetaData(String id, String groupId) {
            this(
                    id,
                    "name",
                    "description",
                    "owner",
                    OffsetDateTime.now(),
                    ArtifactType.AVRO,
                    0l,
                    VersionState.ENABLED,
                    "labels",
                    groupId,
                    0l,
                    "artifactId",
                    Map.of()
                    );
        }
    }

    @Data
    public static class Version {
    }

    public enum VersionState {
        ENABLED,
        DISABLED,
        DEPRECATED
    }

    @Data
    public static class Labels {
    }

    @Data
    public static class ArtifactId {
    }

    @Data
    public static class GroupId {
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateArtifact {
        private String artifactId;
        private ArtifactType artifactType;
        private Content firstVersion;
        private String contentType;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Content {
        private AndMoreContent content;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AndMoreContent {
        private String content;
    }

    @AllArgsConstructor
    @Getter
    public static class VersionSearchResults {
        private int count;
        private List<SearchedVersion> versions;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SearchedVersion {
        private String schema;
    }

    enum Action {
        RETURN_OR_UPDATE
    }

    enum ArtifactType {
        AVRO
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MySchema {
        private String type;
        private String name;
        private String namespace;
        private Collection<Map<String, Object>> fields;

        public String toString() {
            try {
                return mapper.writeValueAsString(this);
            } catch (JsonProcessingException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
    }
}

/**

 C:\Users\dfaut\workspace\tools\java-17-openjdk-17.0.11.0.9-1.win.x86_64\bin\java.exe -javaagent:C:\Users\dfaut\workspace\tools\intellij-2021.3.3\lib\idea_rt.jar=57601:C:\Users\dfaut\workspace\tools\intellij-2021.3.3\bin -Dfile.encoding=UTF-8 -classpath C:\Users\dfaut\workspace\projects\embedded-kafka-docker-image\apicurio-serde\target\test-classes;C:\Users\dfaut\workspace\projects\embedded-kafka-docker-image\test-application\target\classes;C:\Users\dfaut\workspace\projects\embedded-kafka-docker-image\test-ref\target\classes;C:\Users\dfaut\workspace\projects\embedded-kafka-docker-image\avro-support\target\classes;C:\Users\dfaut\.m2\repository\org\springframework\kafka\spring-kafka\3.2.2\spring-kafka-3.2.2.jar;C:\Users\dfaut\.m2\repository\org\springframework\spring-context\6.1.11\spring-context-6.1.11.jar;C:\Users\dfaut\.m2\repository\org\springframework\spring-aop\6.1.11\spring-aop-6.1.11.jar;C:\Users\dfaut\.m2\repository\org\springframework\spring-beans\6.1.11\spring-beans-6.1.11.jar;C:\Users\dfaut\.m2\repository\org\springframework\spring-core\6.1.11\spring-core-6.1.11.jar;C:\Users\dfaut\.m2\repository\org\springframework\spring-jcl\6.1.11\spring-jcl-6.1.11.jar;C:\Users\dfaut\.m2\repository\org\springframework\spring-expression\6.1.11\spring-expression-6.1.11.jar;C:\Users\dfaut\.m2\repository\org\springframework\spring-messaging\6.1.11\spring-messaging-6.1.11.jar;C:\Users\dfaut\.m2\repository\org\springframework\spring-tx\6.1.11\spring-tx-6.1.11.jar;C:\Users\dfaut\.m2\repository\org\springframework\retry\spring-retry\2.0.7\spring-retry-2.0.7.jar;C:\Users\dfaut\.m2\repository\org\apache\kafka\kafka-clients\3.7.1\kafka-clients-3.7.1.jar;C:\Users\dfaut\.m2\repository\com\github\luben\zstd-jni\1.5.6-3\zstd-jni-1.5.6-3.jar;C:\Users\dfaut\.m2\repository\org\lz4\lz4-java\1.8.0\lz4-java-1.8.0.jar;C:\Users\dfaut\.m2\repository\org\xerial\snappy\snappy-java\1.1.10.5\snappy-java-1.1.10.5.jar;C:\Users\dfaut\.m2\repository\io\micrometer\micrometer-observation\1.13.2\micrometer-observation-1.13.2.jar;C:\Users\dfaut\.m2\repository\io\micrometer\micrometer-commons\1.13.2\micrometer-commons-1.13.2.jar;C:\Users\dfaut\.m2\repository\org\junit\jupiter\junit-jupiter\5.10.3\junit-jupiter-5.10.3.jar;C:\Users\dfaut\.m2\repository\org\junit\jupiter\junit-jupiter-api\5.10.3\junit-jupiter-api-5.10.3.jar;C:\Users\dfaut\.m2\repository\org\opentest4j\opentest4j\1.3.0\opentest4j-1.3.0.jar;C:\Users\dfaut\.m2\repository\org\junit\platform\junit-platform-commons\1.10.3\junit-platform-commons-1.10.3.jar;C:\Users\dfaut\.m2\repository\org\apiguardian\apiguardian-api\1.1.2\apiguardian-api-1.1.2.jar;C:\Users\dfaut\.m2\repository\org\junit\jupiter\junit-jupiter-params\5.10.3\junit-jupiter-params-5.10.3.jar;C:\Users\dfaut\.m2\repository\org\junit\jupiter\junit-jupiter-engine\5.10.3\junit-jupiter-engine-5.10.3.jar;C:\Users\dfaut\.m2\repository\org\junit\platform\junit-platform-engine\1.10.3\junit-platform-engine-1.10.3.jar;C:\Users\dfaut\.m2\repository\org\apache\avro\avro\1.12.0\avro-1.12.0.jar;C:\Users\dfaut\.m2\repository\com\fasterxml\jackson\core\jackson-core\2.17.2\jackson-core-2.17.2.jar;C:\Users\dfaut\.m2\repository\com\fasterxml\jackson\core\jackson-databind\2.17.2\jackson-databind-2.17.2.jar;C:\Users\dfaut\.m2\repository\com\fasterxml\jackson\core\jackson-annotations\2.17.2\jackson-annotations-2.17.2.jar;C:\Users\dfaut\.m2\repository\org\apache\commons\commons-compress\1.26.2\commons-compress-1.26.2.jar;C:\Users\dfaut\.m2\repository\commons-codec\commons-codec\1.16.1\commons-codec-1.16.1.jar;C:\Users\dfaut\.m2\repository\commons-io\commons-io\2.16.1\commons-io-2.16.1.jar;C:\Users\dfaut\.m2\repository\org\apache\commons\commons-lang3\3.14.0\commons-lang3-3.14.0.jar;C:\Users\dfaut\.m2\repository\org\apache\avro\avro-compiler\1.12.0\avro-compiler-1.12.0.jar;C:\Users\dfaut\.m2\repository\org\apache\commons\commons-text\1.12.0\commons-text-1.12.0.jar;C:\Users\dfaut\.m2\repository\org\apache\velocity\velocity-engine-core\2.3\velocity-engine-core-2.3.jar;C:\Users\dfaut\.m2\repository\org\apache\avro\avro-maven-plugin\1.12.0\avro-maven-plugin-1.12.0.jar;C:\Users\dfaut\.m2\repository\org\apache\maven\shared\file-management\3.1.0\file-management-3.1.0.jar;C:\Users\dfaut\.m2\repository\org\codehaus\plexus\plexus-utils\3.4.2\plexus-utils-3.4.2.jar;C:\Users\dfaut\.m2\repository\org\apache\avro\avro-idl\1.12.0\avro-idl-1.12.0.jar;C:\Users\dfaut\.m2\repository\org\antlr\antlr4-runtime\4.13.1\antlr4-runtime-4.13.1.jar;C:\Users\dfaut\.m2\repository\io\projectreactor\reactor-core\3.6.8\reactor-core-3.6.8.jar;C:\Users\dfaut\.m2\repository\org\reactivestreams\reactive-streams\1.0.4\reactive-streams-1.0.4.jar;C:\Users\dfaut\.m2\repository\org\projectlombok\lombok\1.18.34\lombok-1.18.34.jar;C:\Users\dfaut\.m2\repository\io\apicurio\apicurio-registry-serdes-avro-serde\2.6.6.Final\apicurio-registry-serdes-avro-serde-2.6.6.Final.jar;C:\Users\dfaut\.m2\repository\io\apicurio\apicurio-registry-serde-common\2.6.6.Final\apicurio-registry-serde-common-2.6.6.Final.jar;C:\Users\dfaut\.m2\repository\io\apicurio\apicurio-registry-schema-resolver\2.6.6.Final\apicurio-registry-schema-resolver-2.6.6.Final.jar;C:\Users\dfaut\.m2\repository\io\apicurio\apicurio-registry-client\2.6.6.Final\apicurio-registry-client-2.6.6.Final.jar;C:\Users\dfaut\.m2\repository\io\apicurio\apicurio-registry-common\2.6.6.Final\apicurio-registry-common-2.6.6.Final.jar;C:\Users\dfaut\.m2\repository\io\apicurio\apicurio-common-rest-client-jdk\0.1.18.Final\apicurio-common-rest-client-jdk-0.1.18.Final.jar;C:\Users\dfaut\.m2\repository\io\apicurio\apicurio-common-rest-client-common\0.1.18.Final\apicurio-common-rest-client-common-0.1.18.Final.jar;C:\Users\dfaut\.m2\repository\junit\junit\4.13.2\junit-4.13.2.jar;C:\Users\dfaut\.m2\repository\org\hamcrest\hamcrest-core\2.2\hamcrest-core-2.2.jar;C:\Users\dfaut\.m2\repository\org\hamcrest\hamcrest\2.2\hamcrest-2.2.jar;C:\Users\dfaut\.m2\repository\org\slf4j\slf4j-api\2.0.13\slf4j-api-2.0.13.jar;C:\Users\dfaut\.m2\repository\ch\qos\logback\logback-classic\1.5.6\logback-classic-1.5.6.jar;C:\Users\dfaut\.m2\repository\ch\qos\logback\logback-core\1.5.6\logback-core-1.5.6.jar io.github.dfauth.apicurio.serde.ThreadedProxy
 2025-01-07 08:58:45,621 INFO i.g.d.a.s.ThreadedProxy [ForkJoinPool.commonPool-worker-1] incoming: POST /apis/registry/v2/groups/default/artifacts?ifExists=RETURN_OR_UPDATE&canonical=false HTTP/1.1
 Content-Length: 308
 Host: localhost:8090
 User-Agent: Java-http-client/17.0.11
 Accept: application/json
 Content-Type: application/json
 X-Registry-ArtifactId: io.github.dfauth.embedded.kafka.test.User-value
 X-Registry-ArtifactType: AVRO


 2025-01-07 08:58:45,625 INFO i.g.d.a.s.ThreadedProxy [ForkJoinPool.commonPool-worker-1] incoming: {"type":"record","name":"User","namespace":"io.github.dfauth.embedded.kafka.test","fields":[{"name":"id","type":"long"},{"name":"userId","type":{"type":"string","avro.java.string":"String"}},{"name":"favoriteColor","type":{"type":"int","logicalType":"io.github.dfauth.embedded.kafka.test.FavouriteColour"}}]}
 2025-01-07 08:58:46,012 INFO i.g.d.a.s.ThreadedProxy [ForkJoinPool.commonPool-worker-2] outgoing: HTTP/1.1 200 OK
 Date: Tue, 07 Jan 2025 00:58:45 GMT
 Expires: Mon, 06 Jan 2025 00:58:45 GMT
 Pragma: no-cache
 Cache-control: no-cache, no-store, must-revalidate
 Strict-Transport-Security: max-age=31536000; includeSubdomains
 Content-Type: application/json
 Content-Length: 269

 {"contentId":1,"createdBy":"","createdOn":"2025-01-07T00:58:45+0000","globalId":1,"id":"io.github.dfauth.embedded.kafka.test.User-value","modifiedBy":"","modifiedOn":"2025-01-07T00:58:45+0000","name":"User","references":[],"state":"ENABLED","type":"AVRO","version":"1"}
 2025-01-07 08:58:46,123 INFO i.g.d.a.s.ThreadedProxy [ForkJoinPool.commonPool-worker-3] incoming: GET /apis/registry/v2/ids/globalIds/1?dereference=false HTTP/1.1
 Content-Length: 0
 Host: localhost:8090
 User-Agent: Java-http-client/17.0.11
 Accept: application/json
 Content-Type: application/json


 2025-01-07 08:58:46,140 INFO i.g.d.a.s.ThreadedProxy [ForkJoinPool.commonPool-worker-4] outgoing: HTTP/1.1 200 OK
 Date: Tue, 07 Jan 2025 00:58:46 GMT
 Expires: Mon, 06 Jan 2025 00:58:46 GMT
 Pragma: no-cache
 Cache-control: no-cache, no-store, must-revalidate
 Strict-Transport-Security: max-age=31536000; includeSubdomains
 Content-Type: application/json
 Content-Length: 308

 {"type":"record","name":"User","namespace":"io.github.dfauth.embedded.kafka.test","fields":[{"name":"id","type":"long"},{"name":"userId","type":{"type":"string","avro.java.string":"String"}},{"name":"favoriteColor","type":{"type":"int","logicalType":"io.github.dfauth.embedded.kafka.test.FavouriteColour"}}]}
 2025-01-07 08:58:46,142 INFO i.g.d.a.s.ThreadedProxy [ForkJoinPool.commonPool-worker-3] incoming: GET /apis/registry/v2/ids/globalIds/1/references HTTP/1.1
 Content-Length: 0
 Host: localhost:8090
 User-Agent: Java-http-client/17.0.11
 Accept: application/json
 Content-Type: application/json


 2025-01-07 08:58:46,151 INFO i.g.d.a.s.ThreadedProxy [ForkJoinPool.commonPool-worker-4] outgoing: HTTP/1.1 200 OK
 Date: Tue, 07 Jan 2025 00:58:46 GMT
 Expires: Mon, 06 Jan 2025 00:58:46 GMT
 Pragma: no-cache
 Cache-control: no-cache, no-store, must-revalidate
 Strict-Transport-Security: max-age=31536000; includeSubdomains
 Content-Type: application/json
 Content-Length: 2

 []
 2025-01-07 08:58:46,671 ERROR i.g.d.a.s.ThreadedProxy [ForkJoinPool.commonPool-worker-3] Connection reset
 java.net.SocketException: Connection reset
 at java.base/sun.nio.ch.NioSocketImpl.implRead(NioSocketImpl.java:328)
 at java.base/sun.nio.ch.NioSocketImpl.read(NioSocketImpl.java:355)
 at java.base/sun.nio.ch.NioSocketImpl$1.read(NioSocketImpl.java:808)
 at java.base/java.net.Socket$SocketInputStream.read(Socket.java:966)
 at io.github.dfauth.apicurio.serde.ThreadedProxy.lambda$runnable$0(ThreadedProxy.java:42)
 at java.base/java.util.concurrent.ForkJoinTask$RunnableExecuteAction.exec(ForkJoinTask.java:1395)
 at java.base/java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:373)
 at java.base/java.util.concurrent.ForkJoinPool$WorkQueue.topLevelExec(ForkJoinPool.java:1182)
 at java.base/java.util.concurrent.ForkJoinPool.scan(ForkJoinPool.java:1655)
 at java.base/java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:1622)
 at java.base/java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:165)
 2025-01-07 08:58:46,671 ERROR i.g.d.a.s.ThreadedProxy [ForkJoinPool.commonPool-worker-1] Connection reset
 java.net.SocketException: Connection reset
 at java.base/sun.nio.ch.NioSocketImpl.implRead(NioSocketImpl.java:328)
 at java.base/sun.nio.ch.NioSocketImpl.read(NioSocketImpl.java:355)
 at java.base/sun.nio.ch.NioSocketImpl$1.read(NioSocketImpl.java:808)
 at java.base/java.net.Socket$SocketInputStream.read(Socket.java:966)
 at io.github.dfauth.apicurio.serde.ThreadedProxy.lambda$runnable$0(ThreadedProxy.java:42)
 at java.base/java.util.concurrent.ForkJoinTask$RunnableExecuteAction.exec(ForkJoinTask.java:1395)
 at java.base/java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:373)
 at java.base/java.util.concurrent.ForkJoinPool$WorkQueue.topLevelExec(ForkJoinPool.java:1182)
 at java.base/java.util.concurrent.ForkJoinPool.scan(ForkJoinPool.java:1655)
 at java.base/java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:1622)
 at java.base/java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:165)


 */