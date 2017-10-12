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
                request.uri.path = '/article/search/nonActual'
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

    @Timed(name = 'getUserUnreadArticles')
    Collection<Article> getUserUnreadArticles(String accessToken, String cursorId, Integer count, Integer minLength, Integer maxLength) {
        log.info "requesting $count articles longer than $minLength and shorter than $maxLength from cursor $cursorId"

        try {
            def query = [:]
            if (cursorId != null) {
                query['cursorId'] = cursorId
            }
            if (count != null) {
                query['count'] = count
            }
            if (minLength != null) {
                query['minLength'] = minLength
            }
            if (maxLength != null) {
                query['maxLength'] = maxLength
            }

            return articlesServiceRest.get {
                request.headers['Authorization'] = "Bearer $accessToken".toString()

                request.uri.path = '/user/me/article/unread'
                request.uri.query = query
            }
        } catch (e) {
            log.error "requesting $count articles longer than $minLength and shorter than $maxLength from cursor $cursorId failed: $e.message", e
            throw new RuntimeException("requesting $count articles longer than $minLength and shorter than $maxLength from cursor $cursorId failed: $e.message", e)
        }
    }
}
