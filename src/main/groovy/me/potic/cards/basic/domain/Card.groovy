package me.potic.cards.basic.domain

class Card {

    String id

    String pocketId

    boolean actual

    String url

    String title

    String source

    String excerpt

    Image image

    static class Image {
        String src
    }
}
