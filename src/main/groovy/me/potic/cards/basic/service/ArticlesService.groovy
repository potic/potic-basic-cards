package me.potic.cards.basic.service

import com.codahale.metrics.annotation.Timed
import groovy.util.logging.Slf4j
import groovyx.net.http.HttpBuilder
import me.potic.cards.basic.domain.Article
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
@Slf4j
class ArticlesService {

    HttpBuilder articlesServiceRest

    @Autowired
    HttpBuilder articlesServiceRest(@Value('${services.articles.url}') String articlesServiceUrl) {
        articlesServiceRest = HttpBuilder.configure {
            request.uri = articlesServiceUrl
        }
    }

    @Timed(name = 'findNonActualArticles')
    Collection<Article> findNonActualArticles(String groupName, int count) {
        log.info "finding non-actual articles for group ${groupName}..."

        try {
            Collection response = articlesServiceRest.get(Collection) {
                request.uri.path = '/article/search/newOrExpired'
                request.uri.query = [ group: groupName, count: count ]
            }

            return response.collect({ new Article(it) })
        } catch (e) {
            log.error "finding non-actual articles for group ${groupName} failed: $e.message", e
            throw new RuntimeException("finding non-actual articles for group ${groupName} failed", e)
        }
    }

    @Timed(name = 'updateArticle')
    void updateArticle(Article article) {
        log.info "updating article ${article}..."

        try {
            articlesServiceRest.put {
                request.uri.path = '/article'
                request.body = article
                request.contentType = 'application/json'
            }
        } catch (e) {
            log.error "updating article ${article} failed: $e.message", e
            throw new RuntimeException("updating article ${article} failed", e)
        }
    }
}
