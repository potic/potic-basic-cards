package me.potic.cards.basic.domain

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.builder.Builder

@Builder
@EqualsAndHashCode
@ToString
class Article {

    String id

    PocketArticle fromPocket

    BasicCard basicCard
}
