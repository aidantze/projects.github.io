import request from "sync-request";
import config from "../config.json";

// If server sets up as localhost
const url = process.env.IP || "localhost";
const port = config.port;

describe("Root url /", () => {
	test("should connect sucessfully", () => {
		const res = request(
			"GET",
			`http://${url}:${port}/`,
			{ qs: {} }
		);
		const data = JSON.parse(res.getBody() as string);
		expect(data).toStrictEqual({ message: expect.any(String) });
		expect(res.statusCode).toStrictEqual(200);
	});
});