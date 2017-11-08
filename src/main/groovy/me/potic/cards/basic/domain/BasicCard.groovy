package me.potic.cards.basic.domain

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode
@ToString
class BasicCard {

    String id

    String pocketId

    boolean actual

    String url

    String title

    String source

    String excerpt

    CardImage image
}
