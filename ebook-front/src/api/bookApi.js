import axios from 'axios';

const API_BASE_URL = '/api/books';

/**
 * Fetches all books from the server.
 */
export const getAllBooks = async () => {
    try {
        const response = await axios.get(API_BASE_URL);
        return response.data;
    } catch (error) {
        console.error("Error fetching all books:", error);
        throw error;
    }
};

/**
 * Searches for books by their name (title).
 */
export const searchBooksByName = async (name) => {
    if (!name.trim()) {
        return getAllBooks();
    }
    try {
        const response = await axios.get(`${API_BASE_URL}/name/${name}`);
        return response.data;
    } catch (error) {
        console.error(`Error searching for book with name "${name}":`, error);
        return [];
    }
};