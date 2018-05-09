package me.potic.cards.domain

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.builder.Builder

@Builder
@EqualsAndHashCode
@ToString(includeNames = true)
class Article {

    String id

    PocketArticle fromPocket

    Card card
}
