import { pool } from "./dbPool";

/**
 * Runs a query with the database
 *
 * @param {string} query - The Query that is run on the database
 *
 * @returns {Promise<object | undefined>} - What is returned by the database or undefined if there is an error
*/
async function runQuery(query: string): Promise<object | undefined> {
	try {
		const res = await pool.query(query);
		return res;
	} catch (err) {
		console.error(err);
		return undefined;
	}
}

/**
 * Runs a query with the database
 *
 * @param {string} query - The Query that is run on the database
 *
 * @returns {Promise<object>} - What is returned by the database or undefined if there is an error
*/
async function runQueryV2(query: string): Promise<object | undefined> {
	try {
		const res = await pool.query(query);
		return res;
	} catch (err) {
		if (err !== null && typeof (err) === "object" && "code" in err) {
			return Promise.reject(err.code);
		} else {
			console.error("Very bad things be happening");
		}
	}
}

export { runQuery, runQueryV2 };