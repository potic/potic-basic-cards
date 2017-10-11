package me.potic.cards.basic.service

import com.codahale.metrics.annotation.Timed
import groovy.util.logging.Slf4j
import me.potic.cards.basic.domain.Article
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
@Slf4j
class CollectBasicCardsService {

    @Value(value = '${articles.request.size}')
    int articlesRequestSize

    @Autowired
    ArticlesService articlesService

    @Scheduled(fixedDelay = 30_000L)
    @Timed(name = 'collectBasicCards')
    void collectBasicCards() {
        log.info("collecting basic cards...")

        Collection<Article> articlesToProcess = articlesService.findNonActualArticles('basicCard', articlesRequestSize)
        log.info("got ${articlesToProcess.size()} articles to collect basic cards...")

        articlesToProcess.collect({ article -> collectBasicCard(article) }).forEach(articlesService.&updateArticle)
    }

    @Timed(name = 'collectBasicCard')
    Article collectBasicCard(Article article) {
        if (article.basicCard == null) {
            article.basicCard = [:]
        }

        determinePocketId(article)
        determineUrl(article)
        determineTitle(article)
        determineSource(article)
        determineExcerpt(article)
        determineImage(article)

        article.basicCard.actual = true

        return article
    }

    static void determinePocketId(Article article) {
        String pocketId = (article.fromPocket.resolved_id ?: article.fromPocket.item_id)
        if (pocketId != null && pocketId != '0') {
            article.basicCard.pocketId = pocketId
        }
    }

    static void determineUrl(Article article) {
        article.basicCard.url = (article.fromPocket.resolved_url ?: article.fromPocket.given_url)
    }

    static void determineTitle(Article article) {
        article.basicCard.title = (article.fromPocket.resolved_title ?: article.fromPocket.given_title)
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

            article.basicCard.source = url.substring(startIndex, endIndex)
        }
    }

    static void determineExcerpt(Article article) {
        if (article.fromPocket.excerpt) {
            article.basicCard.excerpt = article.fromPocket.excerpt
        }
    }

    static void determineImage(Article article) {
        if (article.fromPocket.containsKey('image')) {
            article.basicCard.image = [ src: article.fromPocket.image['src'] ]
        }
        if (article.fromPocket.containsKey('images') && article.fromPocket.images.containsKey('1') ) {
            article.basicCard.image = [ src: article.fromPocket.images['1']['src'] ]
        }
    }
}
