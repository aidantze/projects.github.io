import validator from "validator";
import { runQuery, runQueryV2 } from "../dbInteract";
import { addToken, revokeToken } from "../tokenManager";

/**
 * Checks if the provided details are correct
 *
 * @param {string} email - e.g. iltg@unsw.edu.au
 * @param {string} name - e.g. John Smith
 * @param {string} password - e.g. hunter2
 *
 * @returns {bool} - whether input for registering user is valid or not
*/
function isValidInput(email: string, name: string, password: string): boolean {
	return validator.isEmail(email) &&
		password.length >= 6 &&
		name.length >= 1 && name.length <= 200;
}

/**
 * Registers a first time user given a provided username, password, firstName and lastName
 *  - generates a handle based on the firstName and lastName
 *  - checks if the user exists prior to registration
 *
 * @param {string} email - e.g. iltg@unsw.edu.au
 * @param {string} password - e.g. hunter2
 * @param {string} name - e.g. John Smith
 *
 * @returns {{code: number, message: string}} - generated authUserId
 */
async function authRegister(email: string, name: string, password: string) {
	if (isValidInput(email, name, password)) {
		const query = `INSERT INTO users(email, name, password) VALUES('${email}', '${name}', '${password}') RETURNING id`;
		try {
			const dbresponse = await runQueryV2(query);
			if (dbresponse === undefined) {
				return { code: 500, message: "Error inserting into database" };
			} else {
				if ("rowCount" in dbresponse && "rows" in dbresponse && dbresponse.rows instanceof Array) {
					const token: string = addToken(dbresponse.rows[0].id);
					return { code: 200, message: token };
				} else {
					return { code: 500, message: "Unknown issues" };
				}
			}
		} catch (err) {
			const code = Number.parseInt(err as string);
			if (code === 23505) {
				return { code: 403, message: "Email address already in use" };
			} else {
				return { code: 500, message: `Unhandled error from the database (${code})` };
			}
		}
	}
	return { code: 400, message: "Invalid input" };
}

/**
 * Login a user, checking that that the email exists and password is correct
 *
 * @param {string} email - e.g. iltg@unsw.edu.au
 * @param {string} password - e.g. hunter2
 *
 * @returns {code: number, message: string} - found authUserId
*/
async function authLogin(email: string, password: string) {
	if (email === "" || password === "") {
		return { code: 400, message: "Empty input" };
	}

	const query = `SELECT * FROM users WHERE email = '${email}'`;
	const dbresponse = await runQuery(query);
	if (dbresponse === undefined) {
		return { code: 500, message: "Error handling database" };
	} else {
		if ("rowCount" in dbresponse && "rows" in dbresponse && dbresponse.rows instanceof Array) {
			if (dbresponse.rowCount === 0) {
				return { code: 403, message: "Email does not exist" };
			}
			if ("password" in dbresponse.rows[0] && "id" in dbresponse.rows[0]) {
				if (dbresponse.rows[0].password === password) {
					const token: string = addToken(dbresponse.rows[0].id);
					return { code: 200, message: token };
				} else {
					return { code: 403, message: "Wrong password" };
				}
			}
		}
		return { code: 500, message: "Unknown internal issues" };
	}
}

/**
 * Logs a user out
 *
 * @param {string} token - The token of the user to logout
 */
function authLogout(token: string) {
	revokeToken(token);
}

export { authLogin, authRegister, authLogout };
