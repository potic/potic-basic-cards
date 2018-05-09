package me.potic.cards.domain

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode
@ToString(includeNames = true)
class Card {

    String id

    long timestamp

    String pocketId

    String url

    String title

    String source

    String excerpt

    Long addedTimestamp

    CardImage image
}
