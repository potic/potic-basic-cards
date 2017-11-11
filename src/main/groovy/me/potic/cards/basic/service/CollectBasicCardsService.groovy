package me.potic.cards.basic.service

import com.codahale.metrics.annotation.Timed
import groovy.util.logging.Slf4j
import me.potic.cards.basic.domain.Article
import me.potic.cards.basic.domain.BasicCard
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
        log.info("got ${articlesToProcess.size()} articles to collect basic cards...")

        articlesToProcess.collect({ article -> collectBasicCard(article) }).forEach({ article ->
            article.fromPocket = null
            articlesService.updateArticle(article)
        })
    }

    @Timed(name = 'collectBasicCard')
    Article collectBasicCard(Article article) {
        article.basicCard = new BasicCard()
        article.basicCard.id = article.id
        article.basicCard.actual = true
        article.basicCard.actual &= determinePocketId(article)
        article.basicCard.actual &= determineUrl(article)
        article.basicCard.actual &= determineTitle(article)
        article.basicCard.actual &= determineSource(article)
        article.basicCard.actual &= determineExcerpt(article)
        article.basicCard.actual &= determineImage(article)

        return article
    }

    static boolean determinePocketId(Article article) {
        String pocketId = (article.fromPocket.resolved_id ?: article.fromPocket.item_id)
        if (pocketId != null && pocketId != '0') {
            article.basicCard.pocketId = pocketId
        }
        return true
    }

    static boolean determineUrl(Article article) {
        article.basicCard.url = (article.fromPocket.resolved_url ?: article.fromPocket.given_url)
        return StringUtils.isNoneBlank(article.basicCard.url)
    }

    static boolean determineTitle(Article article) {
        article.basicCard.title = (article.fromPocket.resolved_title ?: article.fromPocket.given_title)
        return StringUtils.isNoneBlank(article.basicCard.title)
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

            article.basicCard.source = url.substring(startIndex, endIndex)
        }
        return true
    }

    static boolean determineExcerpt(Article article) {
        if (article.fromPocket.excerpt) {
            article.basicCard.excerpt = article.fromPocket.excerpt
        }
        return true
    }

    static boolean determineImage(Article article) {
        if (article.fromPocket.image != null) {
            article.basicCard.image = new CardImage(src: article.fromPocket.image.src)
        }
        if (article.fromPocket.images != null && article.fromPocket.images.size() > 0 ) {
            article.basicCard.image = new CardImage(src: article.fromPocket.images.first().src)
        }
        return true
    }
}
