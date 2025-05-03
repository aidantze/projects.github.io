import { runQuery } from "./dbInteract";

/**
 * Clear: Reset the database
 * This is an internal function, used for testing. No route name
 */
async function clear(): Promise<1 | 0> {
	let query = "DELETE FROM einvoices";
	let dbresponse = await runQuery(query);
	if (dbresponse === undefined) {
		return 1;
	}

	query = "DELETE FROM users";
	dbresponse = await runQuery(query);
	if (dbresponse === undefined) {
		return 1;
	} else {
		return 0;
	}
}

export { clear };