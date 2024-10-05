package io.github.dfauth.embedded.kafka.controllers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@Slf4j
@RestController
public class SchemaRegistryController {

    private int schemaCnt = 0;
    private final Map<Integer, String> schemas = new HashMap<>();

    @RequestMapping(value="**",method = {GET,HEAD,POST,PUT,PATCH,DELETE,OPTIONS,TRACE})
    public ResponseEntity<Void> all(RequestEntity<Object> request){
        log.info("request: {}",request);
        log.info("request body: {}",request.getBody());
        return new ResponseEntity(OK);
    }

    @PostMapping(value = "search/versions", consumes = {"application/json"}, produces = {"application/json"})
    public ResponseEntity<VersionSearchResults> search(
            @RequestParam("order") String order,
            @RequestParam("orderby") String orderby,
            @RequestParam("groupId") String groupId,
            @RequestParam("artifactId") String artifactId,
            @RequestParam("canonical") Boolean canonical,
            @RequestParam("artifactType") String artifactType,
            @RequestBody String schema) {
        log.info("WOOZ search: {}",schema);
        List<SearchedVersion> result = schemas.values().stream().filter(s -> s.equals(schema)).findFirst().map(SearchedVersion::new).map(List::of).orElse(List.of());
        return new ResponseEntity(new VersionSearchResults(result.size(), result), OK);
    }

    // groups/default/artifacts?ifExists=FIND_OR_CREATE_VERSION&canonical=false
    @PostMapping(value = "groups/{groupId}/artifacts", consumes = {"application/json"}, produces = {"application/json"})
    public ResponseEntity<VersionMetadata> register(
            @PathVariable("groupId") String groupId,
            @RequestParam("ifExists") Action ifExists,
            @RequestParam("canonical") Boolean canonical,
            @RequestBody Thingy thingy) {
        log.info("WOOZ register: {}",thingy);
        int cnt = schemaCnt++;
        schemas.put(cnt, thingy.getFirstVersion().getContent().getContent());
        return new ResponseEntity<>(new VersionMetadata(cnt, groupId), OK);
    }

    // {artifactId=test-value,
    //  artifactType=AVRO,
    //  firstVersion={
    //   content={
    //   content={
    //     "type":"record",
    //     "name":"User",
    //     "namespace":"io.github.dfauth.embedded.kafka.image.test",
    //     "fields":[{"name":"id","type":"long"},{"name":"userId","type":{"type":"string","avro.java.string":"String"}},{"name":"favoriteColor","type":{"type":"int","logicalType":"io.github.dfauth.embedded.kafka.image.FavouriteColour"}}]
    //   },
    //   contentType=application/json}
    //  }
    // }
    @Data
    @AllArgsConstructor
    public static class VersionMetadata {

        private final String version;
        private final String name;
        private final String description;
        private final String owner;
        private final LocalDateTime createdOn;
        private final ArtifactType artifactType;
        private final Long globalId;
        private final VersionState state;
        private final String labels;
        private final String groupId;
        private final Long contentId;
        private final String artifactId;

        public VersionMetadata(int i, String groupId) {
            this(
                    String.valueOf(i),
                    "name",
                    "description",
                    "owner",
                    LocalDateTime.now(),
                    ArtifactType.AVRO,
                    0l,
                    VersionState.ENABLED,
                    "labels",
                    groupId,
                    0l,
                    "artifactId"
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
    public static class Thingy {
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

//    @Data
//    @AllArgsConstructor
//    @NoArgsConstructor
//    public static class MySchema {
//        private String text;
//    }
//
//    @JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.WRAPPER_OBJECT, property="type")
//    @JsonSubTypes({
//            @JsonSubTypes.Type(value = SimpleField.class, name="simple"),
//            @JsonSubTypes.Type(value = ComplexField.class, name="complex")
//    })
//    interface Field<T> {
//        String getName();
//        T getType();
//    }
//    @Data
//    @AllArgsConstructor
//    @NoArgsConstructor
//    public static class SimpleField implements Field<String> {
//        private String name;
//        @JsonProperty("type")
//        private String type;
//    }
//
//    @Data
//    @AllArgsConstructor
//    @NoArgsConstructor
//    public static class ComplexField implements Field<ComplexType> {
//        private String name;
//        @JsonProperty("type")
//        private ComplexType type;
//    }
//
//    @Data
//    @AllArgsConstructor
//    @NoArgsConstructor
//    public static class ComplexType {
//        private String type;
//        @JsonProperty("avro.java.string")
//        private String avroJavaString;
//        private String logicalType;
//    }

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
        FIND_OR_CREATE_VERSION
    }

    enum ArtifactType {
        AVRO
    }

}

/**

 request: <POST http://localhost:8090/search/versions?order=desc&orderby=globalId&groupId=default&artifactId=test-value&canonical=true&artifactType=AVRO,
 {type=record, name=User, namespace=io.github.dfauth.embedded.kafka.image.test, fields=[{name=id, type=long}, {name=userId, type={type=string, avro.java.string=String}}, {name=favoriteColor, type={type=int, logicalType=io.github.dfauth.embedded.kafka.image.FavouriteColour}}]},
 [user-agent:"Vert.x-WebClient/4.5.7", accept:"application/json", content-length:"315", host:"localhost:8090", Content-Type:"application/json;charset=UTF-8"]>

 request: <POST http://localhost:8090/groups/default/artifacts?ifExists=FIND_OR_CREATE_VERSION&canonical=false,{artifactId=test-value, artifactType=AVRO, firstVersion={content={content={"type":"record","name":"User","namespace":"io.github.dfauth.embedded.kafka.image.test","fields":[{"name":"id","type":"long"},{"name":"userId","type":{"type":"string","avro.java.string":"String"}},{"name":"favoriteColor","type":{"type":"int","logicalType":"io.github.dfauth.embedded.kafka.image.FavouriteColour"}}]}, contentType=application/json}}},[user-agent:"Vert.x-WebClient/4.5.7", accept:"application/json", content-length:"489", host:"localhost:8090", Content-Type:"application/json;charset=UTF-8"]>
 request body: {artifactId=test-value,
                artifactType=AVRO,
                firstVersion={
                  content={
                     content={
                        "type":"record",
                        "name":"User",
                        "namespace":"io.github.dfauth.embedded.kafka.image.test",
                        "fields":[{
                               "name":"id",
                               "type":"long"
                             },
                             { "name":"userId",
                               "type":{"type":"string","avro.java.string":"String"}},{"name":"favoriteColor","type":{"type":"int","logicalType":"io.github.dfauth.embedded.kafka.image.FavouriteColour"}}]}, contentType=application/json}}}


 */
