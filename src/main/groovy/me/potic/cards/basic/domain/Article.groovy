package me.potic.cards.basic.domain

import groovy.transform.builder.Builder

@Builder
class Article {

    String id

    String userId

    Map<String, Object> fromPocket

    Map<String, Object> basicCard
}
