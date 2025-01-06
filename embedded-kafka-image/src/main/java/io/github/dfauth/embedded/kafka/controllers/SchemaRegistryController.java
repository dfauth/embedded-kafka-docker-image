package io.github.dfauth.embedded.kafka.controllers;

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

    private long schemaCnt = 0;
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
            return new ResponseEntity<>(new ArtifactMetaData(Long.valueOf(e.getKey()),schema.toString()), OK);
        }).orElseGet(() -> {
            var cnt = schemaCnt++;
            schemas.put(cnt, schema);
            return new ResponseEntity<>(new ArtifactMetaData(cnt, schema.toString()), OK);
        });
    }

    // http://localhost:8080/apis/registry/v2/ids/globalIds/0
    @GetMapping(value = "/apis/registry/v2/ids/globalIds/{id}", consumes = {"application/json"}, produces = {"application/json"})
    public ResponseEntity<MySchema> ids(
            @PathVariable("id") Long id,
            @RequestParam("dereference") Boolean dereference) {
        log.info("WOOZ ids: {}", id);
        return Optional.ofNullable(schemas.get(id)).map(s -> new ResponseEntity<>(s,OK)).orElse(new ResponseEntity<>(OK));
    }

    // http://localhost:8080/apis/registry/v2/ids/globalIds/0/references
    @GetMapping(value = "/apis/registry/v2/ids/globalIds/{id}/references", consumes = {"application/json"}, produces = {"application/json"})
    public ResponseEntity<MySchema> idsReferences(
            @PathVariable("id") String id) {
        log.info("WOOZ ids/references: {}", id);
        return Optional.ofNullable(schemas.get(id)).map(s -> new ResponseEntity<>(s,OK)).orElse(new ResponseEntity<>(OK));
    }

    @Data
    @AllArgsConstructor
    public static class ArtifactMetaData {
        private Long contentId;
        private String createdBy;
        private Date createdOn;
        private String description;
        private Long globalId;
        private String groupId;
        private String id;
        private List<String> labels = new ArrayList();
        private String modifiedBy;
        private Date modifiedOn;
        private String name;
        private Map<String, String> properties;
        private List<ArtifactReference> references = new ArrayList();
        private ArtifactState state;
        private String type;
        private String version;

        public ArtifactMetaData(long globalId, String string) {
            this.globalId = globalId;
            this.contentId = globalId;
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

