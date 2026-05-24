package com.rpg.rpghxh.rooms.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisInviteServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisInviteService redisInviteService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        redisInviteService = new RedisInviteService(redisTemplate);
    }

    @Test
    void getOrCreateInvite_WhenNoExistingInvite_ShouldCreateNewAndSaveToRedis() {
        UUID roomId = UUID.randomUUID();
        Long masterId = 1L;

        when(hashOperations.get("invite:" + roomId, "inviteHash")).thenReturn(null);

        String result = redisInviteService.getOrCreateInvite(roomId, masterId);

        assertNotNull(result);
        assertDoesNotThrow(() -> UUID.fromString(result));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(hashOperations).putAll(eq("invite:" + roomId), mapCaptor.capture());

        Map<String, String> savedData = mapCaptor.getValue();
        assertEquals(result, savedData.get("inviteHash"));
        assertEquals(masterId.toString(), savedData.get("masterId"));
        assertNotNull(savedData.get("createdAt"));

        verify(redisTemplate).expire("invite:" + roomId, Duration.ofHours(8));
        verify(valueOperations).set(eq("invite:hash:" + result), eq(roomId.toString()), eq(Duration.ofHours(8)));
    }

    @Test
    void getOrCreateInvite_WhenExistingInvite_ShouldReturnExistingHash() {
        UUID roomId = UUID.randomUUID();
        Long masterId = 1L;
        String existingHash = UUID.randomUUID().toString();

        when(hashOperations.get("invite:" + roomId, "inviteHash")).thenReturn(existingHash);

        String result = redisInviteService.getOrCreateInvite(roomId, masterId);

        assertEquals(existingHash, result);
        verify(hashOperations, never()).putAll(any(), any());
        verify(redisTemplate, never()).expire(any(), any(Duration.class));
    }

    @Test
    void getOrCreateInvite_ShouldGenerateUniqueHashPerCall() {
        UUID roomId1 = UUID.randomUUID();
        UUID roomId2 = UUID.randomUUID();
        Long masterId = 1L;

        when(hashOperations.get("invite:" + roomId1, "inviteHash")).thenReturn(null);
        when(hashOperations.get("invite:" + roomId2, "inviteHash")).thenReturn(null);

        String hash1 = redisInviteService.getOrCreateInvite(roomId1, masterId);
        String hash2 = redisInviteService.getOrCreateInvite(roomId2, masterId);

        assertNotEquals(hash1, hash2);
    }

    @Test
    void removeInvite_ShouldDeleteKeyFromRedis() {
        UUID roomId = UUID.randomUUID();

        when(hashOperations.get("invite:" + roomId, "inviteHash")).thenReturn(null);

        redisInviteService.removeInvite(roomId);

        verify(redisTemplate).delete("invite:" + roomId);
        verify(redisTemplate, times(1)).delete(anyString());
    }

    @Test
    void removeInvite_ShouldAlsoDeleteReverseKey_WhenHashExists() {
        UUID roomId = UUID.randomUUID();
        String hash = UUID.randomUUID().toString();

        when(hashOperations.get("invite:" + roomId, "inviteHash")).thenReturn(hash);

        redisInviteService.removeInvite(roomId);

        verify(redisTemplate).delete("invite:hash:" + hash);
        verify(redisTemplate).delete("invite:" + roomId);
    }

    @Test
    void getRoomIdByHash_WhenHashExists_ShouldReturnRoomId() {
        UUID roomId = UUID.randomUUID();
        String hash = UUID.randomUUID().toString();

        when(valueOperations.get("invite:hash:" + hash)).thenReturn(roomId.toString());

        Optional<UUID> result = redisInviteService.getRoomIdByHash(hash);

        assertTrue(result.isPresent());
        assertEquals(roomId, result.get());
    }

    @Test
    void getRoomIdByHash_WhenHashNotFound_ShouldReturnEmpty() {
        String hash = UUID.randomUUID().toString();

        when(valueOperations.get("invite:hash:" + hash)).thenReturn(null);

        Optional<UUID> result = redisInviteService.getRoomIdByHash(hash);

        assertTrue(result.isEmpty());
    }

    @Test
    void getOrCreateInvite_ShouldSetTTLOf8Hours() {
        UUID roomId = UUID.randomUUID();
        Long masterId = 1L;

        when(hashOperations.get("invite:" + roomId, "inviteHash")).thenReturn(null);

        redisInviteService.getOrCreateInvite(roomId, masterId);

        verify(redisTemplate).expire("invite:" + roomId, Duration.ofHours(8));
    }

    @Test
    void getOrCreateInvite_ShouldStoreMasterIdInRedis() {
        UUID roomId = UUID.randomUUID();
        Long masterId = 42L;

        when(hashOperations.get("invite:" + roomId, "inviteHash")).thenReturn(null);

        redisInviteService.getOrCreateInvite(roomId, masterId);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(hashOperations).putAll(eq("invite:" + roomId), mapCaptor.capture());

        assertEquals("42", mapCaptor.getValue().get("masterId"));
    }
}
