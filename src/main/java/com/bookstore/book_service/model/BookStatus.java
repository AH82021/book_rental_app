package com.bookstore.book_service.model;


public enum BookStatus {
    AVAILABLE("Available for purchase and rental"),
    OUT_OF_STOCK("Temporarily out of stock"),
    DISCONTINUED("No longer available");

    private final String description;

    BookStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}





