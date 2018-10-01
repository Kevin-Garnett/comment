package com.hsbc.comment;

import org.springframework.data.repository.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CommentRepository extends Repository<Comment,String> {


    Mono<Comment> save(Comment newComment);

    Flux<Comment> findByImageId(String imageId);

    // Needed to support save()
    Mono<Comment> findById(String id);

    Flux<Comment> saveAll(Flux<Comment> newComment);

    Mono<Void> deleteAll();
}
