package me.potic.cards.basic.service

import com.codahale.metrics.annotation.Timed
import groovy.util.logging.Slf4j
import groovyx.net.http.HttpBuilder
import me.potic.cards.basic.domain.Article
import me.potic.cards.basic.domain.User
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
    Collection<Article> findNonActualArticles(int count) {
        log.debug "finding non-actual articles..."

        try {
            def response = articlesServiceRest.post {
                request.uri.path = '/graphql'
                request.contentType = 'application/json'
                request.body = [ query: """
                    {
                      withNonActualCard(count: ${count}) {
                        id
                        fromPocket {
                            item_id
                            resolved_id
                            given_url
                            resolved_url
                            given_title
                            resolved_title
                            excerpt
                            image {
                                src
                            }
                            images {
                                src
                            }
                        }
                      }
                    }
                """ ]
            }

            List errors = response.errors
            if (errors != null && !errors.empty) {
                throw new RuntimeException("Request failed: $errors")
            }

            return response.data.withNonActualBasicCard.collect({ new Article(it) })
        } catch (e) {
            log.error "finding non-actual articles failed: $e.message", e
            throw new RuntimeException("finding non-actual articles failed", e)
        }
    }

    @Timed(name = 'updateArticle')
    void updateArticle(Article article) {
        log.debug "updating article ${article}..."

        try {
            articlesServiceRest.put {
                request.uri.path = '/article'
                request.contentType = 'application/json'
                request.body = article
            }
        } catch (e) {
            log.error "updating article ${article} failed: $e.message", e
            throw new RuntimeException("updating article ${article} failed", e)
        }
    }

    @Timed(name = 'getUserUnreadArticles')
    Collection<Article> getUserUnreadArticles(User user, String cursorId, Integer count, Integer minLength, Integer maxLength) {
        log.debug "requesting $count articles for user ${user.id} longer than $minLength and shorter than $maxLength from cursor $cursorId"

        try {
            String params = "userId: \"${user.id}\""
            if (cursorId != null) {
                params += ", cursorId: \"${cursorId}\""
            }
            if (count != null) {
                params += ", count: ${count}"
            }
            if (minLength != null) {
                params += ", minLength: ${minLength}"
            }
            if (maxLength != null) {
                params += ", maxLength: ${maxLength}"
            }

            def response = articlesServiceRest.post {
                request.uri.path = '/graphql'
                request.contentType = 'application/json'
                request.body = [ query: """
                    {
                      unread(${params}) {
                        card {
                            id
                            pocketId
                            actual
                            url
                            title
                            source
                            excerpt
                            image {
                                src
                            }
                        }
                      }
                    }
                """ ]
            }

            List errors = response.errors
            if (errors != null && !errors.empty) {
                throw new RuntimeException("Request failed: $errors")
            }

            return response.data.unread.collect({ new Article(it) })
        } catch (e) {
            log.error "requesting $count articles for user ${user.id} longer than $minLength and shorter than $maxLength from cursor $cursorId failed: $e.message", e
            throw new RuntimeException("requesting $count articles for user ${user.id} longer than $minLength and shorter than $maxLength from cursor $cursorId failed: $e.message", e)
        }
    }
}
