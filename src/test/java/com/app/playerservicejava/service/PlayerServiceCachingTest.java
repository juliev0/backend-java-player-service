package com.app.playerservicejava.service;

import com.app.playerservicejava.model.Player;
import com.app.playerservicejava.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(PlayerServiceCachingTest.CacheTestConfiguration.class)
class PlayerServiceCachingTest {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private AtomicInteger repositoryLookupCount;

    /**
     * Clears shared test state before each cache behavior test.
     */
    @BeforeEach
    void setUp() {
        repositoryLookupCount.set(0);
        cacheManager.getCache("playersById").clear();
    }

    /**
     * Verifies that repeated requests for one player perform one delayed lookup.
     */
    @Test
    void cachesSuccessfulPlayerLookupById() {
        Optional<Player> initialResult = playerService.getPlayerById("player-1");
        Optional<Player> cachedResult = playerService.getPlayerById("player-1");

        assertTrue(initialResult.isPresent());
        assertTrue(cachedResult.isPresent());
        assertEquals("player-1", cachedResult.get().getPlayerId());
        assertEquals(1, repositoryLookupCount.get());
    }

    /**
     * Provides the service, cache manager, and deterministic repository used by
     * the caching test.
     */
    @Configuration
    @EnableCaching
    static class CacheTestConfiguration {

        @Bean
        PlayerService playerService() {
            return new PlayerService();
        }

        @Bean
        CacheManager cacheManager() {
            return new CaffeineCacheManager("playersById");
        }

        @Bean
        AtomicInteger repositoryLookupCount() {
            return new AtomicInteger();
        }

        /**
         * Creates a repository proxy that counts player lookups.
         */
        @Bean
        PlayerRepository playerRepository(AtomicInteger repositoryLookupCount) {
            return (PlayerRepository) Proxy.newProxyInstance(
                    PlayerRepository.class.getClassLoader(),
                    new Class<?>[]{PlayerRepository.class},
                    (proxy, method, arguments) -> {
                        // Support identity methods used while Spring manages the bean.
                        if (method.getName().equals("hashCode")) {
                            return System.identityHashCode(proxy);
                        }
                        if (method.getName().equals("equals")) {
                            return proxy == arguments[0];
                        }
                        if (method.getName().equals("toString")) {
                            return "CountingPlayerRepository";
                        }
                        if (!method.getName().equals("findById")) {
                            throw new UnsupportedOperationException(
                                    "Unexpected repository method: " + method.getName());
                        }

                        repositoryLookupCount.incrementAndGet();
                        Player player = new Player();
                        player.setPlayerId((String) arguments[0]);
                        return Optional.of(player);
                    });
        }
    }
}
