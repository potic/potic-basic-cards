package me.potic.cards.domain

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode
@ToString(includeNames = true)
class PocketArticle {

    String item_id
    String resolved_id

    String given_url
    String resolved_url

    String given_title
    String resolved_title

    Long time_added

    String excerpt

    PocketImage image
    List<PocketImage> images
}
