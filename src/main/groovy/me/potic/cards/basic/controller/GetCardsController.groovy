package me.potic.cards.basic.controller

import com.codahale.metrics.annotation.Timed
import groovy.util.logging.Slf4j
import me.potic.cards.basic.domain.BasicCard
import me.potic.cards.basic.domain.User
import me.potic.cards.basic.service.ArticlesService
import me.potic.cards.basic.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

import java.security.Principal

@RestController
@Slf4j
class GetCardsController {

    @Autowired
    ArticlesService articlesService

    @Autowired
    UserService userService

    @Timed(name = 'user.me.cards.basic')
    @CrossOrigin
    @GetMapping(path = '/user/me/cards/basic')
    @ResponseBody Collection<BasicCard> getUserCards(
            @RequestParam(value = 'cursorId', required = false) String cursorId,
            @RequestParam(value = 'count') Integer count,
            @RequestParam(value = 'minLength', required = false) Integer minLength,
            @RequestParam(value = 'maxLength', required = false) Integer maxLength,
            final Principal principal
    ) {
        log.info 'receive GET request for /user/me/cards/basic'

        try {
            User user = userService.findUserByAuth0Token(principal.token)
            return articlesService.getUserUnreadArticles(user, cursorId, count, minLength, maxLength).collect({ article -> article.basicCard })
        } catch (e) {
            log.error "GET request for /user/me/cards/basic failed: $e.message", e
            throw new RuntimeException("GET request for /user/me/cards/basic failed: $e.message", e)
        }
    }
}
