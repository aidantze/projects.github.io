import { randomUUID } from "crypto";

type tokenStore = { [token: string]: number };
const globalTokenStore: tokenStore = {};

/**
 * Creates a token and returns it
 *
 * @param userId - The user Id the token is generated for
 * @returns - A new token
 */
function addToken(userId: number): string {
	const newToken = randomUUID();

	globalTokenStore[newToken] = userId;
	return newToken;
}

/**
 * Revokes a user token
 *
 * @param token - The token to be revoked
 */
function revokeToken(token: string) {
	delete globalTokenStore[token];
}

/**
 * Retrieves the user Id from a given function
 *
 * @param token - A token to get the user Id from
 * @returns - A user Id asscoiated with the given token
 */
function uidFromToken(token: string) {
	return globalTokenStore[token];
}

/**
 * Checks if the token is valid.
 *
 * @param token - User's authentication method
 * @returns true/false
 */
function validateToken(token: string): boolean {
	return token !== undefined && globalTokenStore[token] !== undefined;
}

export { addToken, revokeToken, uidFromToken, validateToken };