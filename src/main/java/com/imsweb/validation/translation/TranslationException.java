/*
 * Copyright (C) 2021 Information Management Services, Inc.
 */
package com.imsweb.validation.translation;

public class TranslationException extends Exception {

    public TranslationException(String message) {
        super(message);
    }

    public TranslationException(String message, Throwable cause) {
        super(message, cause);
    }
}
