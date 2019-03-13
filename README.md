# Spring Data Rest - JSON Patch Test

A repo demonstrating an issue found when using [JSON Patch](https://tools.ietf.org/html/rfc6902) media type in 
[Spring Data REST](https://spring.io/projects/spring-data-rest) PATCH requests for updating associated resources.


## Entities

```java
@Entity
public class Header {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
}
```

```java
@Entity
public class Detail {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    @ManyToOne
    @JoinColumn(name = "headerId")
    private Header header;
}
```

## Test Setup
### Start Application
No configuration required.

Download repo and start the Spring Boot application from your IDE or mvn build and start. It will listen in port 8080 by default.

### Create Two Headers

`curl -X POST http://localhost:8080/headers -H 'Content-Type: application/json' -d '{"name":"header-1"}'`

Result:
```json
{
  "name" : "header-1",
  "_links" : {
    "self" : {
      "href" : "http://localhost:8080/headers/1"
    },
    "header" : {
      "href" : "http://localhost:8080/headers/1"
    }
  }
}
```

`curl -X POST http://localhost:8080/headers -H 'Content-Type: application/json' -d '{"name":"header-2"}'`

Result:
```json
{
  "name" : "header-2",
  "_links" : {
    "self" : {
      "href" : "http://localhost:8080/headers/2"
    },
    "header" : {
      "href" : "http://localhost:8080/headers/2"
    }
  }
}
```

### Create Detail Associated to First Header
`curl -X POST http://localhost:8080/details -H 'Content-Type: application/json' -d '{"name":"detail-1","header":"http://localhost:8080/headers/1"}'`

Result:
```json
{
  "name" : "detail-1",
  "_links" : {
    "self" : {
      "href" : "http://localhost:8080/details/3"
    },
    "detail" : {
      "href" : "http://localhost:8080/details/3"
    },
    "header" : {
      "href" : "http://localhost:8080/details/3/header"
    }
  }
}
```

### Inspect Associated Header
`curl -X GET http://localhost:8080/details/3/header`

Result:
```json
{
  "name" : "header-1",
  "_links" : {
    "self" : {
      "href" : "http://localhost:8080/headers/1"
    },
    "header" : {
      "href" : "http://localhost:8080/headers/1"
    }
  }
}
```

## Tests
### Update Header using PATCH (Success)
When `Content-Type: application/json` is used, the header association is updated successfully:

`curl -X PATCH http://localhost:8080/details/3 -H 'Content-Type: application/json' -d '{"header":"http://localhost:8080/headers/2"}'`

`curl -X GET http://localhost:8080/details/3/header`

Result:
```json
{
  "name" : "header-2",
  "_links" : {
    "self" : {
      "href" : "http://localhost:8080/headers/2"
    },
    "header" : {
      "href" : "http://localhost:8080/headers/2"
    }
  }
}
```

### Update Header using PUT (Success)
When PUT and `Content-Type: text/uri-list` is used, the header association is updated successfully:
 
`curl -X PUT http://localhost:8080/details/3/header -H 'Content-Type: text/uri-list' -d 'http://localhost:8080/headers/1'`

`curl -X GET http://localhost:8080/details/3/header`

Result:
```json
{
  "name" : "header-1",
  "_links" : {
    "self" : {
      "href" : "http://localhost:8080/headers/1"
    },
    "header" : {
      "href" : "http://localhost:8080/headers/1"
    }
  }
}
```

### Update Header using PATCH/json-patch (Failure)
Using JSON Patch with a replace operation fails:

`curl -X PATCH http://localhost:8080/details/3 -H 'Content-Type: application/json-patch+json' -d '[{"op":"replace","path":"/header","value":"http://localhost:8080/headers/2"}]'`

Result:
```json
{
    "cause": {
        "cause": {
            "cause": {
                "cause": {
                    "cause" : null,
                    "message" : "No converter found capable of converting from type [java.lang.String] to type [@javax.persistence.ManyToOne @javax.persistence.JoinColumn com.test.sdr.jsonpatchtest.Header]"
                },
                "message" : "EL1001E: Type conversion problem, cannot convert from java.lang.String to @javax.persistence.ManyToOne @javax.persistence.JoinColumn com.test.sdr.jsonpatchtest.Header"
            },
            "message" : "Type conversion failure"
        },
        "message" : "EL1034E: A problem occurred whilst attempting to set the property 'header': Type conversion failure"
    },
    "message" : "Could not read an object of type class com.test.sdr.jsonpatchtest.Detail from the request!; nested exception is org.springframework.expression.spel.SpelEvaluationException: EL1034E: A problem occurred whilst attempting to set the property 'header': Type conversion failure"
}
```
