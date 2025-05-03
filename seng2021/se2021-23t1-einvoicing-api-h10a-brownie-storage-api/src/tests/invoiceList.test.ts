import { clear } from "../helper";
import request from "sync-request";
import config from "../config.json";

// If server sets up as localhost
const url = process.env.IP || "localhost";
const port = config.port;

let token1: string;
let token2: string;

beforeEach(() => {
	const temp = clear();

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

	const r2 = request(
		"POST",
		`http://${url}:${port}` + "/auth/login",
		{ json: { email: "iltg@thegame.com", password: "hunter2" } }
	);

	const data2 = JSON.parse(r2.getBody() as string);
	token2 = data2.message;

	request(
		"POST",
		`http://${url}:${port}` + "/invoice/upload",
		{ json: { token: token1, invoicee: "iltg@thegame.com", einvoice: "aa" } }
	);

	request(
		"POST",
		`http://${url}:${port}` + "/invoice/upload",
		{ json: { token: token1, invoicee: "iltg@thegame.com", einvoice: "aaa" } }
	);

	request(
		"POST",
		`http://${url}:${port}` + "/invoice/upload",
		{ json: { token: token1, invoicee: "iltg@game.com", einvoice: "ab" } }
	);

	request(
		"POST",
		`http://${url}:${port}` + "/invoice/upload",
		{ json: { token: token1, invoicee: "iltg@game.com", einvoice: "aa" } }
	);

	return temp;
});

describe("Incorrectly using endpoint /invoice/list ", () => {
	test("returns error on all empty input", () => {
		const res = request(
			"GET",
			`http://${url}:${port}` + "/invoice/list",
			{ qs: { filter: "" }, headers: { token: "" } }
		);
		expect(res.statusCode).toBe(401);
	});

	test("returns error when the user token is invalid", () => {
		const res = request(
			"GET",
			`http://${url}:${port}` + "/invoice/list",
			{ qs: { filter: "" }, headers: { token: "badtoken" } }
		);
		expect(res.statusCode).toBe(401);
	});

	test("returns database error on bad filter", () => {
		const res = request(
			"GET",
			`http://${url}:${port}` + "/invoice/list",
			{ qs: { filter: "chicken" }, headers: { token: token1 } }
		);
		expect(res.statusCode).toBe(500);
	});
});

describe("Correctly using endpoint /invoice/list", () => {
	test("returns ids without a filter", () => {
		const res = request(
			"GET",
			`http://${url}:${port}` + "/invoice/list",
			{ headers: { token: token1 } }
		);

		expect(res.statusCode).toBe(200);

		const data = JSON.parse(res.getBody() as string);
		expect(data.message).toStrictEqual(expect.any(String));
	});

	test("returns ids with a filter", () => {
		const res = request(
			"GET",
			`http://${url}:${port}` + "/invoice/list",
			{ qs: { filter: "invoice='aa'" }, headers: { token: token2 } }
		);

		expect(res.statusCode).toBe(200);

		const data = JSON.parse(res.getBody() as string);
		expect(data.message).toStrictEqual(expect.any(String));
	});
});