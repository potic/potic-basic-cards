package me.potic.cards.basic.service

import com.codahale.metrics.annotation.Timed
import groovy.util.logging.Slf4j
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
@Slf4j
class CollectBasicCardsService {

    @Scheduled(fixedDelay = 30_000L)
    @Timed(name = 'collectBasicCards')
    void collectBasicCards() {
        log.info("collecting basic cards...")
    }
}
