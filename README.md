# Ramsey Worker
Worker component for the Ramsey Project

## Analysis Types

### Naive

With the Naive approach we use the Apache Math library to generate all combinations for a given graph and subgraph size.

Each subgraph is then independently checked for completeness. While the simplest and most thorough options this is **unreasonable for checking large graphs** such as (288,8) as a total of `1,064,204,174,039,292` subgraphs would need to be analyzed. 

### Comprehensive

Comprehensive analysis will crawl the graph vertex by vertex to try to identify sets of connected vertices. 

This is done on a color by color basis. 

For example if starting with Vertex 0 it will first see if it's connected to Vertex 1, if it is it will see if both of them are connected to Vertex 2.
```
> 0
> 0,1
> 0,1,2
```
In the event Vertex 2 is not connected to both Vertex 0 and 1 it will not attempt to add another vertex `0,1,2,3` but rather it will remove Vertex 2 and add Vertex 3 `0,1,3` and continue on until all options have been assessed.

### Targeted

The targeted analysis approach builds on the comprehensive analysis above. The key difference is we begin with a graph which we know the complete sub-graphs (cliques) for already as well as a target mutation to produce a derived graph to analyze.

The mutation takes the form of a list of edges to flip the color for, usually this would be a list of 2 edges of differing colors.

For example, say we wanted to flip the edges `[100,110]` and `[200,210]`. With the target approach we would:

1. Flip the two edges.
2. Remove all cliques containing either of those two edges from the list of known cliques of the base graph.
3. Run the comprehensive analysis above only for vertex sets that start with `100,110` or `200,210`.
4. Combine the original cliques from the base graph that are still valid with the newly identified cliques for a new total count of the derived graph.

## Image Build and Deploy

Build the image using SpringBoot defaults
```bash
mvn spring-boot:build-image -Dspring-boot.build-image.imageName=benferenchak/ramsey-worker:dev
````

Publish the image to Dockerhub
```bash
docker push benferenchak/ramsey-worker:dev
```

Start a container using the image
```bash
docker run --restart=always \
  --name=ramsey-worker \
  -e SPRING_PROFILES_ACTIVE=dev \
  --cpus=8 \
  benferenchak/ramsey-worker:dev
```