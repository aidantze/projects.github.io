import { clear } from "../helper";
import request from "sync-request";
import config from "../config.json";

// If server sets up as localhost
const url = process.env.IP || "localhost";
const port = config.port;

beforeEach(async () => {
	return await clear();
});

describe("Incorrectly using endpoint /auth/login ", () => {
	describe("malformed response", () => {
		const res = request(
			"POST",
			`http://${url}:${port}` + "/auth/login",
			{ json: {} }
		);
		expect(res.statusCode).toBe(400);
	});

	describe("empty inputs: ", () => {
		test(" all empty", () => {
			const res = request(
				"POST",
				`http://${url}:${port}` + "/auth/login",
				{ json: { email: "", password: "" } }
			);
			expect(res.statusCode).toBe(400);
		});

		test(" email empty", () => {
			const res = request(
				"POST",
				`http://${url}:${port}` + "/auth/login",
				{ json: { email: "", password: "hunter2" } }
			);
			expect(res.statusCode).toBe(400);
		});

		test(" password empty", () => {
			const res = request(
				"POST",
				`http://${url}:${port}` + "/auth/login",
				{ json: { email: "iltg@game.com", password: "" } }
			);
			expect(res.statusCode).toBe(400);
		});
	});

	test(" when a user has not registered", () => {
		const res = request(
			"POST",
			`http://${url}:${port}` + "/auth/login",
			{ json: { email: "cthon@game.com", password: "hunter2" } }
		);
		expect(res.statusCode).toBe(403);
	});

	test(" when a user has the wrong password", () => {
		request(
			"POST",
			`http://${url}:${port}` + "/auth/register",
			{ json: { email: "iltg@game.com", name: "AzureDiamond", password: "hunter2" } }
		);
		const res = request(
			"POST",
			`http://${url}:${port}` + "/auth/login",
			{ json: { email: "iltg@game.com", password: "hunter3" } }
		);
		expect(res.statusCode).toBe(403);
	});
});

describe("Correctly using endpoint /auth/login", () => {
	test(" succesfully loging in", () => {
		request(
			"POST",
			`http://${url}:${port}` + "/auth/register",
			{ json: { email: "iltg@game.com", name: "AzureDiamond", password: "hunter2" } }
		);

		const res = request(
			"POST",
			`http://${url}:${port}` + "/auth/login",
			{ json: { email: "iltg@game.com", password: "hunter2" } }
		);

		const data = JSON.parse(res.getBody() as string);
		expect(res.statusCode).toStrictEqual(200);
		expect(data.message).toMatch(/^[0-9a-f]{8}-[0-9a-f]{4}-[0-5][0-9a-f]{3}-[089ab][0-9a-f]{3}-[0-9a-f]{12}$/);
		// Regex taken from user 'Gambol' on stack overflow
	});
});