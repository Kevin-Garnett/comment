package com.hsbc.comments;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
//@EnableBinding(CustomProcessor.class)
public class CommentService {


    private CommentWriterRepository repository;

    private final MeterRegistry meterRegistry;

    public CommentService(CommentWriterRepository repository, MeterRegistry meterRegistry){
        this.repository = repository;
        this.meterRegistry = meterRegistry;
    }
    // Non-Cloud version:

    @RabbitListener(bindings = @QueueBinding(
            value=@Queue,
            exchange=@Exchange(value="learning-spring-boot"), //Exchange
            key="comments.new" //Routing key
    ))
    public void save(Comment newComment){
        System.out.println("##################Check Here!!!");
        repository
                .save(newComment)
                .log("commentService-save")
                .subscribe(comment -> {
                    meterRegistry.counter("comments.consumed", "imageId", comment.getImageId())
                            .increment();
                });
    }

    @Bean
    Jackson2JsonMessageConverter jackson2JsonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    CommandLineRunner setupComment(MongoOperations mongoOperations){
        return args -> {
            mongoOperations.dropCollection(Comment.class);
        };
    }

    /**
     * TBC - ?
     * Noticed that here need to define a Queue object, otherwise if we use the anonymous Queue,
     * it will be created until we used it.
     * If we defined first, then the queue will be created when spring boot up.
     * @return
     */
    //@Bean
    //org.springframework.amqp.core.Queue commentQueue() {
    //    return new org.springframework.amqp.core.Queue("learning-spring-boot-queue");
    //}

    // Cloud version:
    /*
    @StreamListener
    @Output(CustomProcessor.OUTPUT)
    public Flux<Void> save(@Input(CustomProcessor.INPUT) Flux<Comment> newComments){
        return repository.saveAll(newComments).flatMap(
                comment -> {
                    meterRegistry.counter("comments.consumed","imageId", comment.getImageId())
                            .increment();
                    return Mono.empty();
                });
    }
    */
}
