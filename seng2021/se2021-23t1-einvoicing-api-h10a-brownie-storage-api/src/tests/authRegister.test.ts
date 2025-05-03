import { clear } from "../helper";
import request from "sync-request";
import config from "../config.json";

// If server sets up as localhost
const url = process.env.IP || "localhost";
const port = config.port;

beforeEach(async () => {
	return await clear();
});

function generateStringOfLength(n: number): string {
	if (n <= 0) {
		return "";
	} else if (n === 1) {
		return "a";
	} else {
		return "a" + generateStringOfLength(n - 1);
	}
}

describe("Incorrectly using endpoint /auth/register ", () => {
	describe("malformed response", () => {
		const res = request(
			"POST",
			`http://${url}:${port}` + "/auth/register",
			{ json: {} }
		);
		expect(res.statusCode).toBe(400);
	});

	describe("empty inputs: ", () => {
		test(" all empty", () => {
			const res = request(
				"POST",
				`http://${url}:${port}` + "/auth/register",
				{ json: { email: "", name: "", password: "" } }
			);
			expect(res.statusCode).toBe(400);
		});

		test(" email empty", () => {
			const res = request(
				"POST",
				`http://${url}:${port}` + "/auth/register",
				{ json: { email: "", name: "AzureDiamond", password: "hunter2" } }
			);
			expect(res.statusCode).toBe(400);
		});

		test(" name empty", () => {
			const res = request(
				"POST",
				`http://${url}:${port}` + "/auth/register",
				{ json: { email: "iltg@game.com", name: "", password: "hunter2" } }
			);
			expect(res.statusCode).toBe(400);
		});

		test(" password empty", () => {
			const res = request(
				"POST",
				`http://${url}:${port}` + "/auth/register",
				{ json: { email: "iltg@game.com", name: "AzureDiamond", password: "" } }
			);
			expect(res.statusCode).toBe(400);
		});
	});

	describe(" when inputs don't match constraints: ", () => {
		test(" invalid email address", () => {
			const res = request(
				"POST",
				`http://${url}:${port}` + "/auth/register",
				{ json: { email: "iltg", name: "AzureDiamond", password: "hunter2" } }
			);
			expect(res.statusCode).toBe(400);
		});
		test(" invalid name address", () => {
			const res = request(
				"POST",
				`http://${url}:${port}` + "/auth/register",
				{ json: { email: "iltg", name: `${generateStringOfLength(202)}`, password: "hunter2" } }
			);
			expect(res.statusCode).toBe(400);
		});
		test(" invalid password", () => {
			const res = request(
				"POST",
				`http://${url}:${port}` + "/auth/register",
				{ json: { email: "iltg", name: `${generateStringOfLength(202)}`, password: "a" } }
			);
			expect(res.statusCode).toBe(400);
		});
	});

	test(" when a user with an email that already exists is entereted into the database", () => {
		request(
			"POST",
			`http://${url}:${port}` + "/auth/register",
			{ json: { email: "iltg@game.com", name: "AzureDiamond", password: "hunter2" } }
		);

		const res = request(
			"POST",
			`http://${url}:${port}` + "/auth/register",
			{ json: { email: "iltg@game.com", name: "Cthon98", password: "hunter2" } }
		);
		expect(res.statusCode).toBe(403);
	});
});

describe("Correctly using endpoint /auth", () => {
	test("/register should register an account", () => {
		const res = request(
			"POST",
			`http://${url}:${port}` + "/auth/register",
			{ json: { email: "iltg@game.com", name: "AzureDiamond", password: "hunter2" } }
		);
		const data = JSON.parse(res.getBody() as string);
		expect(res.statusCode).toStrictEqual(200);
		expect(data.message).toMatch(/^[0-9a-f]{8}-[0-9a-f]{4}-[0-5][0-9a-f]{3}-[089ab][0-9a-f]{3}-[0-9a-f]{12}$/);
		// Regex taken from user 'Gambol' on stack overflow
	});
});