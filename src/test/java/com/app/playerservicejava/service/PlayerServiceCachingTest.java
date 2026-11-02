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
import java.util.concurrent.atomic.AtomicReference;

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

    @Autowired
    private AtomicReference<Player> storedPlayer;

    /**
     * Clears shared test state before each cache behavior test.
     */
    @BeforeEach
    void setUp() {
        repositoryLookupCount.set(0);
        cacheManager.getCache("playersById").clear();

        Player player = new Player();
        player.setPlayerId("player-1");
        player.setFirstName("Original");
        storedPlayer.set(player);
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
     * Verifies that an update evicts the cached player before the next read.
     */
    @Test
    void evictsCachedPlayerAfterUpdate() {
        playerService.getPlayerById("player-1");

        Player updatedPlayer = new Player();
        updatedPlayer.setPlayerId("player-1");
        updatedPlayer.setFirstName("Updated");
        playerService.updatePlayer(updatedPlayer);

        Optional<Player> refreshedResult = playerService.getPlayerById("player-1");

        assertTrue(refreshedResult.isPresent());
        assertEquals("Updated", refreshedResult.get().getFirstName());
        assertEquals(2, repositoryLookupCount.get());
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

        @Bean
        AtomicReference<Player> storedPlayer() {
            return new AtomicReference<>();
        }

        /**
         * Creates a repository proxy that counts player lookups.
         */
        @Bean
        PlayerRepository playerRepository(
                AtomicInteger repositoryLookupCount,
                AtomicReference<Player> storedPlayer) {
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
                        if (method.getName().equals("findById")) {
                            repositoryLookupCount.incrementAndGet();
                            return Optional.ofNullable(storedPlayer.get());
                        }
                        if (method.getName().equals("save")) {
                            Player updatedPlayer = (Player) arguments[0];
                            storedPlayer.set(updatedPlayer);
                            return updatedPlayer;
                        }

                        throw new UnsupportedOperationException(
                                "Unexpected repository method: " + method.getName());

                    });
        }
    }
}
