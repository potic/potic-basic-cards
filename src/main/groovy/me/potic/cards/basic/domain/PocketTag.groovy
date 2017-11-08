package me.potic.cards.basic.domain

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode
@ToString(includeNames = true)
class PocketTag {

    String item_id
    String tag
}
