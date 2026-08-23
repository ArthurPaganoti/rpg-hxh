package com.rpg.rpghxh.rooms.dto;

import java.io.InputStream;

public record RoomCoverDownload(InputStream stream, String contentType, long sizeBytes) {
}
