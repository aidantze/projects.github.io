import request from "sync-request";
import config from "../config.json";

// If server sets up as localhost
const url = process.env.IP || "localhost";
const port = config.port;

describe("Success", () => {
	test("succesful create", () => {
		const res = request(
			"GET",
			`http://${url}:${port}` + "/healthcheck"
		);
		// Check 200 success just to be safe
		expect(res.statusCode).toEqual(200);
		// Check contents
		const status = {
			statusCode: 200
		};
		const data = JSON.parse(res.getBody() as string);
		expect(data).toStrictEqual(status);
	});
});
