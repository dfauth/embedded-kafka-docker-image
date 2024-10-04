package io.github.dfauth.embedded.kafka.controllers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@Slf4j
@RestController
public class SchemaRegistryController {

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
            @RequestBody MySchema schema) {
        log.info("WOOZ search: {}",schema);
        return new ResponseEntity(new VersionSearchResults(0, List.of()), OK);
    }

    @Data
    public static class MySchema {
        private String type;
        private String name;
        private String namespace;
        private List<Map<String,Object>> fields;
    }

    @AllArgsConstructor
    @Getter
    public static class VersionSearchResults {
        private int count;
        private List<SearchedVersion> versions;
    }

    public static class SearchedVersion {
    }

}

/**

 request: <POST http://localhost:8090/search/versions?order=desc&orderby=globalId&groupId=default&artifactId=test-value&canonical=true&artifactType=AVRO,
 {type=record, name=User, namespace=io.github.dfauth.embedded.kafka.image.test, fields=[{name=id, type=long}, {name=userId, type={type=string, avro.java.string=String}}, {name=favoriteColor, type={type=int, logicalType=io.github.dfauth.embedded.kafka.image.FavouriteColour}}]},
 [user-agent:"Vert.x-WebClient/4.5.7", accept:"application/json", content-length:"315", host:"localhost:8090", Content-Type:"application/json;charset=UTF-8"]>

 request body: {type=record, name=User, namespace=io.github.dfauth.embedded.kafka.image.test,
 fields=[{name=id, type=long}, {name=userId, type={type=string, avro.java.string=String}}, {name=favoriteColor, type={type=int, logicalType=io.github.dfauth.embedded.kafka.image.FavouriteColour}}]}


 */
