package me.potic.cards.basic.domain

import groovy.transform.ToString
import groovy.transform.builder.Builder

@Builder
@ToString
class Article {

    String id

    String userId

    Map<String, Object> fromPocket

    Card basicCard
}
