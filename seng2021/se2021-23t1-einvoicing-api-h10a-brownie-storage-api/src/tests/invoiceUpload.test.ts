import { clear } from "../helper";
import request from "sync-request";
import config from "../config.json";
import { example1 } from "./testdata";

// If server sets up as localhost
const url = process.env.IP || "localhost";
const port = config.port;

let token1: string;
// let token2: string;

beforeEach(async () => {
	const temp = await clear();
	request(
		"POST",
		`http://${url}:${port}` + "/auth/register",
		{ json: { email: "iltg@game.com", name: "AzureDiamond", password: "hunter2" } }
	);

	const r1 = request(
		"POST",
		`http://${url}:${port}` + "/auth/login",
		{ json: { email: "iltg@game.com", password: "hunter2" } }
	);

	const data1 = JSON.parse(r1.getBody() as string);
	token1 = data1.message;

	request(
		"POST",
		`http://${url}:${port}` + "/auth/register",
		{ json: { email: "iltg@thegame.com", name: "AzureDiamond", password: "hunter2" } }
	);

	/* const r2  = */ request(
		"POST",
		`http://${url}:${port}` + "/auth/login",
		{ json: { email: "iltg@thegame.com", password: "hunter2" } }
	);

	// const data2 = JSON.parse(r2.getBody() as string);
	// token2 = data2.message;

	return temp;
});

describe("Incorrectly using endpoint /invoice/upload ", () => {
	describe("empty inputs: ", () => {
		test(" all empty", () => {
			const res = request(
				"POST",
				`http://${url}:${port}` + "/invoice/upload",
				{ json: { invoicee: "", einvoice: "" }, headers: { token: "" } }
			);
			// unauthorised has higher priority than bad input
			expect(res.statusCode).toBe(401);
		});

		test(" token empty", () => {
			const res = request(
				"POST",
				`http://${url}:${port}` + "/invoice/upload",
				{ json: { invoicee: "iltg@thegame.com", einvoice: "aa" }, headers: { token: "" } }
			);
			expect(res.statusCode).toBe(401);
		});

		test(" invoicee empty", () => {
			const res = request(
				"POST",
				`http://${url}:${port}` + "/invoice/upload",
				{ json: { invoicee: "", einvoice: "aa" }, headers: { token: token1 } }
			);
			expect(res.statusCode).toBe(400);
		});

		test(" invoice empty", () => {
			const res = request(
				"POST",
				`http://${url}:${port}` + "/invoice/upload",
				{ json: { invoicee: "iltg@thegame.com", einvoice: "" }, headers: { token: token1 } }
			);
			expect(res.statusCode).toBe(400);
		});

		test(" malformed response", () => {
			const res = request(
				"POST",
				`http://${url}:${port}` + "/invoice/upload",
				{ json: {}, headers: { token: token1 } }
			);
			expect(res.statusCode).toBe(400);
		});
	});

	describe(" when inputs don't match constraints: ", () => {
		test(" when the user token is invalid", () => {
			const res = request(
				"POST",
				`http://${url}:${port}` + "/invoice/upload",
				{ json: { invoicee: "aa", einvoice: "aa" }, headers: { token: "aa" } }
			);
			expect(res.statusCode).toBe(401);
		});

		test(" when the invoicee email is invalid", () => {
			const res = request(
				"POST",
				`http://${url}:${port}` + "/invoice/upload",
				{ json: { invoicee: "aa", einvoice: "aa" }, headers: { token: token1 } }
			);
			expect(res.statusCode).toBe(401);
		});
	});
});

describe("Correctly using endpoint /auth", () => {
	test("succesful invoice creation", () => {
		const res = request(
			"POST",
			`http://${url}:${port}` + "/invoice/upload",
			{ json: { invoicee: "iltg@thegame.com", einvoice: example1 }, headers: { token: token1 } }
		);

		expect(res.statusCode).toBe(200);

		const data = JSON.parse(res.getBody() as string);
		expect(data).toStrictEqual({ message: expect.any(String) });
	});
});