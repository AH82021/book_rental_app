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





/*{
    "title": "Harry Potter",
    "author": "J.K. Rowling",
    "isbn": "9780439708180",
    "description": "A young wizard's journey",
    "price": 19.99,
    "rentalPrice": 4.99,
    "publicationDate": "1997-06-26",
    "publisher": "Bloomsbury",
    "pages": 223,
    "language": "English",
    "coverImageUrl": "https://example.com/hp.jpg",
    "status": "AVAILABLE",
    "totalCopies": 10,
    "availableCopies": 10
}*/