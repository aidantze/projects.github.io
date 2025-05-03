import { clear } from "../helper";
import request from "sync-request";
import config from "../config.json";

// If server sets up as localhost
const url = process.env.IP || "localhost";
const port = config.port;

let token1: string;
let token2: string;
let id1: string;

beforeEach(async () => {
	const temp = await clear();
	request(
		"POST",
		`http://${url}:${port}` + "/auth/register",
		{ json: { email: "NNIck@gmail.com", name: "Nickk", password: "ynwhhh2426" } }
	);

	const response1 = request(
		"POST",
		`http://${url}:${port}` + "/auth/login",
		{ json: { email: "NNIck@gmail.com", password: "ynwhhh2426" } }
	);

	const data1 = JSON.parse(response1.getBody() as string);
	token1 = data1.message;

	request(
		"POST",
		`http://${url}:${port}` + "/auth/register",
		{ json: { email: "uuuyg@gmail.com", name: "NVyi", password: "biabiabia6" } }
	);

	const response2 = request(
		"POST",
		`http://${url}:${port}` + "/auth/login",
		{ json: { email: "uuuyg@gmail.com", password: "biabiabia6" } }
	);

	const data2 = JSON.parse(response2.getBody() as string);
	token2 = data2.message;

	const response3 = request(
		"POST",
		`http://${url}:${port}` + "/invoice/upload",
		{ json: { invoicee: "uuuyg@gmail.com", einvoice: "ubl not required" }, headers: { token: token1 } }
	);

	const data3 = JSON.parse(response3.getBody() as string);
	id1 = data3.message;

	return temp;
});

describe("Errors with /invoice/delete ", () => {
	test("error for empty input", () => {
		const res = request(
			"DELETE",
			`http://${url}:${port}` + "/invoice/delete",
			{ qs: { invoiceId: "" }, headers: { token: "" } }
		);
		expect(res.statusCode).toBe(400);
	});

	test("error for inalid token", () => {
		const res = request(
			"DELETE",
			`http://${url}:${port}` + "/invoice/delete",
			{ qs: { invoiceId: id1 }, headers: { token: "something" } }
		);
		expect(res.statusCode).toBe(401);
	});

	test("error for invaild invoice id - wrong format", () => {
		const res = request(
			"DELETE",
			`http://${url}:${port}` + "/invoice/delete",
			{ qs: { invoiceId: "ruok" }, headers: { token: token1 } }
		);
		expect(res.statusCode).toBe(400);
	});

	test("error for invaild invoice id - does not exist", () => {
		const res = request(
			"DELETE",
			`http://${url}:${port}` + "/invoice/delete",
			{ qs: { invoiceId: "12335" }, headers: { token: token1 } }
		);
		expect(res.statusCode).toBe(500);
	});

	test("error for requester without permission", () => {
		const res = request(
			"DELETE",
			`http://${url}:${port}` + "/invoice/delete",
			{ qs: { invoiceId: id1 }, headers: { token: token2 } }
		);
		expect(res.statusCode).toBe(403);
	});
});

describe("Correctly using endpoint /invoice/delete", () => {
	test("succesful invoice deletion", () => {
		const res = request(
			"DELETE",
			`http://${url}:${port}` + "/invoice/delete",
			{ qs: { invoiceId: id1 }, headers: { token: token1 } }
		);
		console.log(JSON.parse(res.getBody() as string));
		expect(res.statusCode).toBe(200);
	});
});
