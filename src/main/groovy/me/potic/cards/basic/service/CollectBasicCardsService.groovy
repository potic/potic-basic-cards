package me.potic.cards.basic.service

import com.codahale.metrics.annotation.Timed
import groovy.util.logging.Slf4j
import me.potic.cards.basic.domain.Article
import me.potic.cards.basic.domain.Card
import me.potic.cards.basic.domain.CardImage
import org.apache.commons.lang3.StringUtils
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

        Collection<Article> articlesToProcess = articlesService.findNonActualArticles(articlesRequestSize)
        log.debug("got ${articlesToProcess.size()} articles to collect basic cards...")

        articlesToProcess.collect({ article -> collectBasicCard(article) }).forEach({ article ->
            articlesService.updateArticleCard(article.id, article.card)
        })
    }

    @Timed(name = 'collectBasicCard')
    Article collectBasicCard(Article article) {
        article.card = new Card()
        article.card.id = article.id
        article.card.actual = true
        article.card.actual &= determinePocketId(article)
        article.card.actual &= determineUrl(article)
        article.card.actual &= determineTitle(article)
        article.card.actual &= determineSource(article)
        article.card.actual &= determineExcerpt(article)
        article.card.actual &= determineImage(article)

        return article
    }

    static boolean determinePocketId(Article article) {
        String pocketId = (article.fromPocket.resolved_id ?: article.fromPocket.item_id)
        if (pocketId != null && pocketId != '0') {
            article.card.pocketId = pocketId
        }
        return true
    }

    static boolean determineUrl(Article article) {
        article.card.url = (article.fromPocket.resolved_url ?: article.fromPocket.given_url)
        return StringUtils.isNoneBlank(article.card.url)
    }

    static boolean determineTitle(Article article) {
        article.card.title = (article.fromPocket.resolved_title ?: article.fromPocket.given_title)
        return StringUtils.isNoneBlank(article.card.title)
    }

    static boolean determineSource(Article article) {
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
        return true
    }

    static boolean determineExcerpt(Article article) {
        if (article.fromPocket.excerpt) {
            article.card.excerpt = article.fromPocket.excerpt
        }
        return true
    }

    static boolean determineImage(Article article) {
        if (article.fromPocket.image != null) {
            article.card.image = new CardImage(src: article.fromPocket.image.src)
        }
        if (article.fromPocket.images != null && article.fromPocket.images.size() > 0 ) {
            article.card.image = new CardImage(src: article.fromPocket.images.first().src)
        }
        return true
    }
}
