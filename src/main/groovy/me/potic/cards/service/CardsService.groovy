package me.potic.cards.service

import groovy.util.logging.Slf4j
import me.potic.cards.domain.Article
import me.potic.cards.domain.Card
import me.potic.cards.domain.CardImage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
@Slf4j
class CardsService {

    @Value(value = '${articles.request.size}')
    int articlesRequestSize

    @Autowired
    ArticlesService articlesService

    @Scheduled(fixedDelay = 10_000L)
    void prepareOutdatedCards() {
        log.info("preparing outdated cards...")

        Collection<Article> articlesToPrepareCards = articlesService.findArticlesWithOldestCard(articlesRequestSize)
        log.debug("got ${articlesToPrepareCards.size()} articles to preparing cards...")

        articlesToPrepareCards.collect({ article -> prepareCard(article) }).forEach({ article ->
            articlesService.updateArticleCard(article.id, article.card)
        })
    }

    static Article prepareCard(Article article) {
        article.card = new Card()
        article.card.id = article.id

        determinePocketId(article)

        determineUrl(article)

        determineTitle(article)

        determineSource(article)

        determineAddedTimestamp(article)

        determineExcerpt(article)

        determineImage(article)

        return article
    }

    static void determinePocketId(Article article) {
        String pocketId = (article.fromPocket.resolved_id ?: article.fromPocket.item_id)
        if (pocketId != null && pocketId != '0') {
            article.card.pocketId = pocketId
        }
    }

    static void determineUrl(Article article) {
        article.card.url = (article.fromPocket.resolved_url ?: article.fromPocket.given_url)
    }

    static void determineTitle(Article article) {
        article.card.title = (article.fromPocket.resolved_title ?: article.fromPocket.given_title)
    }

    static void determineSource(Article article) {
        String url = (article.fromPocket.resolved_url ?: article.fromPocket.given_url)
        if (url != null) {
            int startIndex = 0
            int endIndex

            if (url.startsWith('http://')) {
                startIndex = 'http://'.length()
            }

            if (url.startsWith('https://')) {
                startIndex = 'https://'.length()
            }

            if (url.substring(startIndex).startsWith('www.')) {
                startIndex += 'www.'.length()
            }

            if (url.substring(startIndex).startsWith('m.')) {
                startIndex += 'm.'.length()
            }

            if (url.substring(startIndex).startsWith('l.')) {
                startIndex += 'l.'.length()
            }

            endIndex = url.indexOf('/', startIndex)
            if (endIndex < 0) {
                endIndex = url.length()
            }

            article.card.source = url.substring(startIndex, endIndex)
        }
    }

    static void determineAddedTimestamp(Article article) {
        if (article.fromPocket.time_added) {
            article.card.addedTimestamp = article.fromPocket.time_added
        }
    }

    static void determineExcerpt(Article article) {
        if (article.fromPocket.excerpt) {
            article.card.excerpt = article.fromPocket.excerpt
        }
    }

    static void determineImage(Article article) {
        if (article.fromPocket.image != null) {
            article.card.image = new CardImage(src: article.fromPocket.image.src)
        }
        if (article.fromPocket.images != null && article.fromPocket.images.size() > 0 ) {
            article.card.image = new CardImage(src: article.fromPocket.images.first().src)
        }
    }
}
