package me.potic.cards.basic.domain

import groovy.transform.ToString

@ToString(includes = [ 'id' ])
class User {

    String id

    Collection<String> socialIds

    String pocketAccessToken
}
