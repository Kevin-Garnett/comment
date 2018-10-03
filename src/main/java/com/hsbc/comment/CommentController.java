package com.hsbc.comment;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
//@EnableBinding(Source.class)
public class CommentController {

    //Non-Cloud version

    //private final RabbitTemplate rabbitTemplate;

    private final MeterRegistry meterRegistry;

    private final CommentRepository commentRepository;

    public CommentController(MeterRegistry meterRegistry, CommentRepository commentRepository){
        //this.rabbitTemplate = rabbitTemplate;
        this.meterRegistry = meterRegistry;
        this.commentRepository = commentRepository;
    }

    // Use the Image service to send, no need to use this one to send when separating service
    /*
    @PostMapping("/comments")
    public Mono<String> addComment(Mono<Comment> newComment){
        return newComment.flatMap(
                comment -> Mono.fromRunnable(
                        () -> rabbitTemplate.convertAndSend(
                                "learning-spring-boot",
                                "comment.new",
                                comment
                        )
                ).then(Mono.just(comment))
        ).log("commentService-publish").flatMap(comment -> {
                    meterRegistry.counter("comment.produced", "imageId", comment.getImageId())
                            .increment();
                    return Mono.just("redirect:/");
                }
        );
    }
    */

    @GetMapping("/comments/{imageId}")
    @ResponseBody
    public List<Comment> comments(@PathVariable String imageId){
        return commentRepository.findByImageId(imageId).collectList().block();
    }


    //Cloud version:
    //private final CounterService counterService;
    /*
    private FluxSink<Message<Comment>> commentSink;
    private Flux<Message<Comment>> flux;

    public CommentController(){//CounterService counterService){
        //this.counterService = counterService;
        this.flux = Flux.<Message<Comment>>create(
                emitter -> this.commentSink = emitter,
                FluxSink.OverflowStrategy.IGNORE
        ).publish().autoConnect();
    }

    @PostMapping("/comment")
    public Mono<String> addComment(Mono<Comment> newComment){
        if (commentSink != null){
            return newComment.map(comment ->
                commentSink.next(MessageBuilder.withPayload(comment).build())
            ).then(Mono.just("redirect:/"));
        } else {
            return Mono.just("redirect:/");
        }
    }

    @StreamEmitter
    public void emit(@Output(Source.OUTPUT) FluxSender output){
        output.send(this.flux);
    }
    */
}
