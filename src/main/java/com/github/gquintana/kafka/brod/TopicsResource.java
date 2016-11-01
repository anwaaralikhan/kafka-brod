package com.github.gquintana.kafka.brod;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/topics")
@Produces(MediaType.APPLICATION_JSON)
public class TopicsResource {
    private final Resources resources;
    private final TopicService topicService;

    public TopicsResource(Resources resources, TopicService topicService) {
        this.resources = resources;
        this.topicService = topicService;
    }

    /**
     * Get topic list
     */
    @GET
    public List<String> getTopics() {
        return topicService.getTopics();
    }

    /**
     * Get topic detailed info
     */
    @Path("{name}")
    public TopicResource getTopic(@PathParam("name") String name) {
        return resources.topicResource(name);
    }
}