package me.potic.cards.basic.controller

import com.codahale.metrics.annotation.Timed
import groovy.util.logging.Slf4j
import me.potic.cards.basic.domain.Card
import me.potic.cards.basic.service.ArticlesService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

import java.security.Principal

@RestController
@Slf4j
class GetCardsController {

    @Autowired
    ArticlesService articlesService

    @Timed(name = 'user.me.cards.basic')
    @CrossOrigin
    @GetMapping(path = '/user/me/cards/basic')
    @ResponseBody Collection<Card> getUserCards(
            @RequestParam(value = 'cursorId', required = false) String cursorId,
            @RequestParam(value = 'count') Integer count,
            @RequestParam(value = 'minLength', required = false) Integer minLength,
            @RequestParam(value = 'maxLength', required = false) Integer maxLength,
            final Principal principal
    ) {
        log.info 'receive GET request for /user/me/cards/basic'

        try {
            return articlesService.getUserUnreadArticles(principal.token, cursorId, count, minLength, maxLength).collect({ article -> article.basicCard })
        } catch (e) {
            log.error "GET request for /user/me/cards/basic failed: $e.message", e
            throw new RuntimeException("GET request for /user/me/cards/basic failed: $e.message", e)
        }
    }
}
