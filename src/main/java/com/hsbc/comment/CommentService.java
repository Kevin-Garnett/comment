package com.hsbc.comment;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@EnableBinding(Processor.class)
public class CommentService {

    private static Logger log = LoggerFactory.getLogger(CommentService.class);

    private CommentRepository repository;

    private final MeterRegistry meterRegistry;

    public CommentService(CommentRepository repository, MeterRegistry meterRegistry){
        this.repository = repository;
        this.meterRegistry = meterRegistry;
    }
    // Non-Cloud version:
/*
    @RabbitListener(bindings = @QueueBinding(
            value=@Queue,
            exchange=@Exchange(value="learning-spring-boot"), //Exchange
            key="comment.new" //Routing key
    ))
    public void save(Comment newComment){
        System.out.println("##################Check Here!!!");
        repository
                .save(newComment)
                .log("commentService-save")
                .subscribe(comment -> {
                    meterRegistry.counter("comment.consumed", "imageId", comment.getImageId())
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
*/
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

    @StreamListener
    @Output(Processor.OUTPUT)
    public Flux<Comment> save(@Input(Processor.INPUT) Flux<Comment> newComments){
        return repository.saveAll(newComments).map(
                comment -> {
                    log.info("Saving new comment " + comment);

                    meterRegistry.counter("comment.consumed","imageId", comment.getImageId())
                            .increment();
                    return comment;
                });
    }

}
