package com.rpg.rpghxh.sheets.dto;

import java.io.InputStream;

public record SheetDownload(InputStream stream, String fileName, String contentType, long sizeBytes) {
}