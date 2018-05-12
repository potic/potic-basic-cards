package me.potic.cards.service

import groovy.util.logging.Slf4j
import groovyx.net.http.HttpBuilder
import me.potic.cards.domain.Article
import me.potic.cards.domain.Card
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

    Collection<Article> findArticlesWithOldestCard(int count) {
        log.debug "finding articles with oldest cards..."

        try {
            def response = articlesServiceRest.post {
                request.uri.path = '/graphql'
                request.contentType = 'application/json'
                request.body = [ query: """
                    {
                      withOldestCard(count: ${count}) {
                        id
                        fromPocket {
                            item_id
                            resolved_id
                            given_url
                            resolved_url
                            given_title
                            resolved_title
                            excerpt
                            time_added
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

            return response.data.withOldestCard.collect({ new Article(it) })
        } catch (e) {
            log.error "finding articles with oldest cards failed: $e.message", e
            throw new RuntimeException("finding articles with oldest cards failed", e)
        }
    }

    void updateArticleCard(String articleId, Card card) {
        log.debug "updating article #${articleId} with card ${card}..."

        try {
            articlesServiceRest.post {
                request.uri.path = "/article/${articleId}/card"
                request.contentType = 'application/json'
                request.body = card
            }
        } catch (e) {
            log.error "updating article #${articleId} with card ${card} failed: $e.message", e
            throw new RuntimeException("updating article #${articleId} with card ${card} failed", e)
        }
    }
}
